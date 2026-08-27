require('dotenv').config();
const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const cloudinary = require('cloudinary').v2;
const { Mandal, User, Transaction, Member, Event, Donation, Gallery } = require('../models');

// Configure Cloudinary
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME || 't19ganuk',
  api_key: process.env.CLOUDINARY_API_KEY || '936194428912286',
  api_secret: process.env.CLOUDINARY_API_SECRET || 'teZxIFJt4hit5pnT9NnxP0zfpjc'
});

const dns = require('dns');
try { dns.setServers(['8.8.8.8', '8.8.4.4']); } catch(e){}

const app = express();
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

// MongoDB connection with caching for serverless environments (Vercel)
let cachedPromise = null;
async function connectDB() {
  try {
    dns.setServers(['8.8.8.8', '8.8.4.4']);
  } catch (e) {}

  if (mongoose.connection.readyState === 1) {
    return;
  }

  if (!cachedPromise) {
    const mongoURI = process.env.MONGODB_URI || 'mongodb+srv://vmane8052_db_user:TkcXsv15P0Ry2GWk@cluster0.t9mz6dx.mongodb.net/ganesh_mandal?retryWrites=true&w=majority';
    cachedPromise = mongoose.connect(mongoURI, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
      family: 4,
      serverSelectionTimeoutMS: 8000,
    }).then(async (db) => {
      console.log('Connected to MongoDB Atlas successfully');
      
      // Auto-initialize default Mandal M001 if empty
      try {
        const count = await Mandal.countDocuments();
        if (count === 0) {
          await Mandal.create({
            mandalId: 'M001',
            mandalName: 'श्री गणेश मित्र मंडळ',
            address: 'माने/ढेरे वस्ती, बाळेवाडी',
            status: 'active'
          });
          console.log('Created default Mandal M001');
        }
      } catch (mErr) {
        console.error('Error auto-creating default mandal:', mErr);
      }

      return db;
    }).catch(err => {
      cachedPromise = null;
      console.error('MongoDB connection error:', err);
      throw err;
    });
  }
  
  await cachedPromise;
}

// Middleware to ensure DB connection before route handling
app.use(async (req, res, next) => {
  try {
    await connectDB();
    next();
  } catch (err) {
    res.status(500).json({ success: false, error: 'Database Connection Error: ' + err.message });
  }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', message: 'श्री गणेश मित्र मंडळ Multi-Mandal API is running smoothly!' });
});

// --- MANDALS MANAGEMENT (Multi-Tenant Endpoints) ---
app.get('/api/mandals', async (req, res) => {
  try {
    const mandals = await Mandal.find().sort({ createdAt: 1 }).lean();
    for (let m of mandals) {
      const adminUser = await User.findOne({ mandalId: m.mandalId, role: 'ADMIN' }).select('name phone pin roleInMandal').lean();
      m.adminName = adminUser ? adminUser.name : '';
      m.adminPhone = adminUser ? adminUser.phone : '';
      m.adminPin = adminUser ? adminUser.pin : '';
    }
    res.json({ success: true, data: mandals });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/mandals', async (req, res) => {
  try {
    const { mandalName, address, contactPhone, logoUrl } = req.body;
    if (!mandalName) {
      return res.status(400).json({ success: false, message: 'मंडळाचे नाव आवश्यक आहे' });
    }

    let finalLogoUrl = logoUrl || '';
    if (logoUrl && logoUrl.startsWith('data:image')) {
      try {
        const uploadRes = await cloudinary.uploader.upload(logoUrl, {
          folder: 'ganesh_mandal_logos'
        });
        finalLogoUrl = uploadRes.secure_url;
      } catch (uploadErr) {
        console.error('Cloudinary logo upload error:', uploadErr);
      }
    }

    const count = await Mandal.countDocuments();
    const nextIdNum = count + 1;
    const mandalId = 'M' + String(nextIdNum).padStart(3, '0');

    const mandal = await Mandal.create({
      mandalId,
      mandalName,
      address: address || '',
      contactPhone: contactPhone || '',
      logoUrl: finalLogoUrl,
      status: 'active'
    });

    res.status(201).json({ success: true, message: 'नवीन मंडळ यशस्वीरीत्या तयार केले!', data: mandal });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.put('/api/mandals/:mandalId', async (req, res) => {
  try {
    const { mandalId } = req.params;
    const { mandalName, address, contactPhone, logoUrl, status } = req.body;

    let finalLogoUrl = logoUrl;
    if (logoUrl && logoUrl.startsWith('data:image')) {
      try {
        const uploadRes = await cloudinary.uploader.upload(logoUrl, {
          folder: 'ganesh_mandal_logos'
        });
        finalLogoUrl = uploadRes.secure_url;
      } catch (uploadErr) {
        console.error('Cloudinary logo upload error:', uploadErr);
      }
    }

    const updateFields = { mandalName, address, contactPhone, status };
    if (finalLogoUrl !== undefined) updateFields.logoUrl = finalLogoUrl;

    const updatedMandal = await Mandal.findOneAndUpdate(
      { mandalId },
      updateFields,
      { new: true }
    );

    if (!updatedMandal) {
      return res.status(404).json({ success: false, message: 'मंडळ सापडले नाही' });
    }

    res.json({ success: true, message: 'मंडळाची माहिती यशस्वीरीत्या अपडेट केली', data: updatedMandal });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- AUTH / LOGIN ---
app.post('/api/login', async (req, res) => {
  try {
    const { phone, pin, mandalId } = req.body || {};
    if (!phone || !pin) {
      return res.status(400).json({ success: false, message: 'कृपया मोबाईल नंबर आणि पिन टाका' });
    }
    const cleanPhone = String(phone).replace(/\D/g, '');
    const cleanPin = String(pin).trim();

    const queryFilter = {
      $or: [
        { phone: cleanPhone },
        { phone: String(phone).trim() },
        { phone: cleanPhone.length === 9 ? cleanPhone + cleanPhone.slice(-1) : cleanPhone },
        { phone: cleanPhone.length === 10 ? cleanPhone.slice(0, 9) : cleanPhone }
      ],
      pin: cleanPin
    };
    if (mandalId) {
      queryFilter.mandalId = mandalId;
    }

    const user = await User.findOne(queryFilter);
    if (!user) {
      return res.status(401).json({ success: false, message: 'चुकीचा मोबाईल नंबर किंवा पिन' });
    }

    const userMandalId = user.mandalId || 'M001';
    const mandalInfo = await Mandal.findOne({ mandalId: userMandalId });

    res.json({
      success: true,
      user: {
        id: user._id,
        name: user.name,
        phone: user.phone,
        role: user.role,
        roleInMandal: user.roleInMandal || (user.role === 'ADMIN' ? 'मुख्य व्यवस्थापक' : 'सामान्य सदस्य'),
        photoUrl: user.photoUrl || '',
        mandalId: userMandalId,
        mandalName: mandalInfo ? mandalInfo.mandalName : 'श्री गणेश मित्र मंडळ',
        mandalAddress: mandalInfo ? mandalInfo.address : 'माने/ढेरे वस्ती, बाळेवाडी',
        mandalLogoUrl: mandalInfo ? (mandalInfo.logoUrl || '') : ''
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- USERS / MEMBERS ---
app.get('/api/users', async (req, res) => {
  try {
    const targetMandalId = req.query.mandalId || 'M001';
    const users = await User.find({ mandalId: targetMandalId }).sort({ createdAt: -1 });
    res.json({ success: true, data: users });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/users', async (req, res) => {
  try {
    const { name, phone, pin, role, roleInMandal, photoUrl, mandalId } = req.body;
    if (!name || !phone || !pin) {
      return res.status(400).json({ success: false, message: 'नाव, मोबाईल नंबर आणि पासवर्ड आवश्यक आहेत' });
    }
    const targetMandalId = mandalId || 'M001';

    let finalPhotoUrl = photoUrl || '';
    if (photoUrl && photoUrl.startsWith('data:image')) {
      try {
        const uploadRes = await cloudinary.uploader.upload(photoUrl, {
          folder: 'ganesh_mandal_profiles',
          transformation: [{ width: 500, height: 500, crop: 'fill', gravity: 'face' }]
        });
        finalPhotoUrl = uploadRes.secure_url;
      } catch (uploadErr) {
        console.error('Cloudinary upload error:', uploadErr);
      }
    }

    const user = await User.findOneAndUpdate(
      { phone, mandalId: targetMandalId },
      {
        name,
        phone,
        pin,
        role: role || 'USER',
        roleInMandal: roleInMandal || 'सामान्य सदस्य',
        photoUrl: finalPhotoUrl,
        mandalId: targetMandalId
      },
      { upsert: true, new: true, setDefaultsOnInsert: true }
    );

    res.status(201).json({
      success: true,
      user: {
        id: user._id,
        name: user.name,
        phone: user.phone,
        pin: user.pin,
        role: user.role,
        roleInMandal: user.roleInMandal,
        photoUrl: user.photoUrl,
        mandalId: user.mandalId
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/users/change-password', async (req, res) => {
  try {
    const { phone, currentPin, newPin, mandalId } = req.body;
    if (!phone || !newPin) {
      return res.status(400).json({ success: false, message: 'मोबाईल नंबर आणि नवीन पासवर्ड आवश्यक आहे' });
    }
    const targetMandalId = mandalId || 'M001';

    const user = await User.findOne({ phone, mandalId: targetMandalId });
    if (!user) {
      return res.status(404).json({ success: false, message: 'सदस्य सापडला नाही' });
    }
    if (currentPin && user.pin !== currentPin) {
      return res.status(400).json({ success: false, message: 'जुना पासवर्ड चुकीचा आहे' });
    }

    user.pin = newPin;
    await user.save();

    res.json({ success: true, message: 'पासवर्ड यशस्वीरीत्या बदलला!' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.put('/api/users/phone/:phone', async (req, res) => {
  try {
    const { phone } = req.params;
    const { name, pin, role, roleInMandal, photoUrl, mandalId } = req.body;
    const targetMandalId = mandalId || req.query.mandalId || 'M001';

    let finalPhotoUrl = photoUrl || '';
    if (photoUrl && photoUrl.startsWith('data:image')) {
      try {
        const uploadRes = await cloudinary.uploader.upload(photoUrl, {
          folder: 'ganesh_mandal_profiles',
          transformation: [{ width: 500, height: 500, crop: 'fill', gravity: 'face' }]
        });
        finalPhotoUrl = uploadRes.secure_url;
      } catch (uploadErr) {
        console.error('Cloudinary upload error:', uploadErr);
      }
    }

    const updatedUser = await User.findOneAndUpdate(
      { phone, mandalId: targetMandalId },
      { name, pin, role, roleInMandal, photoUrl: finalPhotoUrl },
      { new: true }
    );
    if (!updatedUser) {
      return res.status(404).json({ success: false, message: 'सदस्य सापडला नाही' });
    }
    res.json({ success: true, user: updatedUser });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.delete('/api/users/phone/:phone', async (req, res) => {
  try {
    const { phone } = req.params;
    const targetMandalId = req.query.mandalId || 'M001';
    const deletedUser = await User.findOneAndDelete({ phone, mandalId: targetMandalId });
    if (!deletedUser) {
      return res.status(404).json({ success: false, message: 'सदस्य सापडला नाही' });
    }
    res.json({ success: true, message: 'सदस्य यशस्वीरित्या हटवला' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- TRANSACTIONS (जमा-खर्च) ---
app.get('/api/transactions', async (req, res) => {
  try {
    const { type, mandalId } = req.query;
    const targetMandalId = mandalId || 'M001';
    const query = { mandalId: targetMandalId };
    if (type) query.type = type;

    const transactions = await Transaction.find(query).sort({ createdAt: -1 });
    res.json({ success: true, data: transactions });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/transactions', async (req, res) => {
  try {
    const { type, amount, details, date, category, memberName, memberPhone, addedBy, receiptNo, mandalId } = req.body;
    if (!type || !amount || !details || !date) {
      return res.status(400).json({ success: false, message: 'सर्व आवश्यक माहिती भरा' });
    }
    const targetMandalId = mandalId || 'M001';
    const finalReceiptNo = receiptNo || (`REC-${new Date().getFullYear()}-${Math.floor(1000 + Math.random() * 9000)}`);
    
    const newTx = await Transaction.create({
      type, amount, details, date, category, memberName, memberPhone: memberPhone || '', addedBy, receiptNo: finalReceiptNo, mandalId: targetMandalId
    });
    res.status(201).json({ success: true, data: newTx });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.put('/api/transactions/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const updatedTx = await Transaction.findByIdAndUpdate(id, req.body, { new: true });
    res.json({ success: true, data: updatedTx });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.delete('/api/transactions/:id', async (req, res) => {
  try {
    const { id } = req.params;
    await Transaction.findByIdAndDelete(id);
    res.json({ success: true, message: 'व्यवहार यशस्वीरीत्या हटवला' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- SUMMARY (एकूण जमा, खर्च आणि शिल्लक) ---
app.get('/api/summary', async (req, res) => {
  try {
    const targetMandalId = req.query.mandalId || 'M001';
    const all = await Transaction.find({ mandalId: targetMandalId });
    let totalJama = 0;
    let totalKharch = 0;
    all.forEach(tx => {
      if (tx.type === 'JAMA') totalJama += Number(tx.amount);
      if (tx.type === 'KHARCH') totalKharch += Number(tx.amount);
    });
    res.json({
      success: true,
      summary: {
        totalJama,
        totalKharch,
        balance: totalJama - totalKharch
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- MEMBERS (सदस्य) ---
app.get('/api/members', async (req, res) => {
  try {
    const targetMandalId = req.query.mandalId || 'M001';
    const members = await Member.find({ mandalId: targetMandalId }).sort({ createdAt: -1 });
    res.json({ success: true, data: members });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/members', async (req, res) => {
  try {
    const { name, roleInMandal, phone, photoUrl, mandalId } = req.body;
    const targetMandalId = mandalId || 'M001';
    const member = await Member.create({ name, roleInMandal, phone, photoUrl, mandalId: targetMandalId });
    res.status(201).json({ success: true, data: member });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- EVENTS (कार्यक्रम व्यवस्थापन) ---
app.get('/api/events', async (req, res) => {
  try {
    const targetMandalId = req.query.mandalId || 'M001';
    const events = await Event.find({ mandalId: targetMandalId }).lean();

    events.sort((a, b) => {
      const getDayNum = (title) => {
        if (!title) return 999;
        const clean = title.replace(/[१1]/g, '1').replace(/[२2]/g, '2').replace(/[३3]/g, '3')
                           .replace(/[४4]/g, '4').replace(/[५5]/g, '5').replace(/[६6]/g, '6')
                           .replace(/[७7]/g, '7').replace(/[८8]/g, '8').replace(/[९9]/g, '9')
                           .replace(/[०0]/g, '0');
        const match = clean.match(/\d+/);
        return match ? parseInt(match[0], 10) : 999;
      };

      const numA = getDayNum(a.dayTitle);
      const numB = getDayNum(b.dayTitle);
      if (numA !== numB) return numA - numB;
      return (a.date || '').localeCompare(b.date || '');
    });

    res.json({ success: true, data: events });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/events', async (req, res) => {
  try {
    const { dayTitle, date, morningAarti, eveningAarti, lunchHost, modakHost, culturalProgram, specialNotes, mandalId } = req.body;
    if (!dayTitle || !date) {
      return res.status(400).json({ success: false, message: 'दिवस आणि तारीख आवश्यक आहे' });
    }
    const targetMandalId = mandalId || 'M001';
    const event = await Event.create({
      dayTitle,
      date,
      morningAarti: morningAarti || '',
      eveningAarti: eveningAarti || '',
      lunchHost: lunchHost || '',
      modakHost: modakHost || '',
      culturalProgram: culturalProgram || '',
      specialNotes: specialNotes || '',
      mandalId: targetMandalId
    });
    res.status(201).json({ success: true, message: 'कार्यक्रम यशस्वीरीत्या जोडला गेला', data: event });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.put('/api/events/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const updated = await Event.findByIdAndUpdate(id, req.body, { new: true });
    if (!updated) {
      return res.status(404).json({ success: false, message: 'कार्यक्रम सापडला नाही' });
    }
    res.json({ success: true, message: 'कार्यक्रम यशस्वीरीत्या अपडेट केला', data: updated });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.delete('/api/events/:id', async (req, res) => {
  try {
    const { id } = req.params;
    await Event.findByIdAndDelete(id);
    res.json({ success: true, message: 'कार्यक्रम यशस्वीरीत्या हटवला गेला' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- DONATIONS (देणगी व्यवस्थापन) ---
app.get('/api/donations', async (req, res) => {
  try {
    const targetMandalId = req.query.mandalId || 'M001';
    const donations = await Donation.find({ mandalId: targetMandalId }).sort({ createdAt: -1 });
    res.json({ success: true, data: donations });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/donations', async (req, res) => {
  try {
    const { donorName, donorPhone, donationType, amount, itemDetails, date, address, receiptNo, mandalId } = req.body;
    if (!donorName || !date) {
      return res.status(400).json({ success: false, message: 'देणगीदाराचे नाव आणि तारीख आवश्यक आहे' });
    }
    const targetMandalId = mandalId || 'M001';
    const donation = await Donation.create({
      donorName,
      donorPhone: donorPhone || '',
      donationType: donationType || 'CASH',
      amount: amount || 0,
      itemDetails: itemDetails || '',
      date,
      address: address || '',
      receiptNo: receiptNo || '',
      mandalId: targetMandalId
    });
    res.status(201).json({ success: true, message: 'देणगी यशस्वीरीत्या नोंदवली गेली', data: donation });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- PHOTO UPLOAD ---
app.post('/api/upload', async (req, res) => {
  try {
    const { image, phone, mandalId } = req.body;
    if (!image) {
      return res.status(400).json({ success: false, message: 'फोटो डेटा आवश्यक आहे' });
    }
    const targetMandalId = mandalId || 'M001';
    let finalUrl = image;

    if (process.env.CLOUDINARY_CLOUD_NAME && process.env.CLOUDINARY_API_KEY && process.env.CLOUDINARY_API_SECRET) {
      const uploadRes = await cloudinary.uploader.upload(image, {
        folder: 'ganesh_mandal_profiles',
        transformation: [{ width: 500, height: 500, crop: 'fill', gravity: 'face' }]
      });
      finalUrl = uploadRes.secure_url;
    }

    if (phone) {
      const cleanPhone = String(phone).replace(/\D/g, '');
      const filter = {
        $or: [
          { phone: cleanPhone },
          { phone: String(phone).trim() }
        ]
      };
      if (mandalId) {
        filter.mandalId = mandalId;
      }
      
      let updatedUser = await User.findOneAndUpdate(filter, { photoUrl: finalUrl }, { new: true });
      if (!updatedUser) {
        // Fallback: update by phone alone so M002, M003, etc. always succeed
        await User.findOneAndUpdate(
          {
            $or: [
              { phone: cleanPhone },
              { phone: String(phone).trim() }
            ]
          },
          { photoUrl: finalUrl }
        );
      }
    }

    res.json({ success: true, message: 'फोटो यशस्वीरीत्या सेव्ह झाला', photoUrl: finalUrl });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- GALLERY (फोटो गॅलरी) ---
app.get('/api/gallery', async (req, res) => {
  try {
    const { year, mandalId } = req.query;
    const targetMandalId = mandalId || 'M001';
    const filter = { mandalId: targetMandalId };
    if (year && year !== 'ALL' && year !== 'सर्व' && year !== 'सर्व वर्षे') {
      filter.year = year;
    }
    const photos = await Gallery.find(filter).sort({ year: -1, createdAt: -1 });
    res.json({ success: true, data: photos });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/gallery', async (req, res) => {
  try {
    const { title, imageUrl, uploadedBy, year, mandalId } = req.body;
    if (!imageUrl) {
      return res.status(400).json({ success: false, message: 'फोटो आवश्यक आहे' });
    }
    const targetMandalId = mandalId || 'M001';

    let finalImageUrl = imageUrl;
    if (imageUrl.startsWith('data:image')) {
      try {
        const uploadRes = await cloudinary.uploader.upload(imageUrl, {
          folder: 'ganesh_mandal_gallery'
        });
        finalImageUrl = uploadRes.secure_url;
      } catch (uploadErr) {
        console.error('Cloudinary gallery upload error:', uploadErr);
      }
    }

    const photo = await Gallery.create({
      title: title || '',
      imageUrl: finalImageUrl,
      uploadedBy: uploadedBy || 'मंडळ सदस्य',
      year: year || '2026',
      mandalId: targetMandalId
    });
    res.status(201).json({ success: true, message: 'फोटो गॅलरीमध्ये जोडला गेला!', data: photo });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/gallery/batch', async (req, res) => {
  try {
    const { photos, year, mandalId } = req.body;
    if (!photos || !Array.isArray(photos) || photos.length === 0) {
      return res.status(400).json({ success: false, message: 'फोटो आवश्यक आहेत' });
    }

    const targetMandalId = mandalId || 'M001';
    const targetYear = year || '2026';
    const createdList = [];
    for (const item of photos) {
      let finalImageUrl = item.imageUrl;
      if (item.imageUrl && item.imageUrl.startsWith('data:image')) {
        try {
          const uploadRes = await cloudinary.uploader.upload(item.imageUrl, {
            folder: 'ganesh_mandal_gallery'
          });
          finalImageUrl = uploadRes.secure_url;
        } catch (uploadErr) {
          console.error('Cloudinary batch upload error:', uploadErr);
        }
      }

      const p = await Gallery.create({
        title: item.title || '',
        imageUrl: finalImageUrl,
        uploadedBy: item.uploadedBy || 'मंडळ सदस्य',
        year: item.year || targetYear,
        mandalId: targetMandalId
      });
      createdList.push(p);
    }

    res.status(201).json({
      success: true,
      message: `${createdList.length} फोटो (${targetYear}) गॅलरीमध्ये जोडले गेले!`,
      data: createdList
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.delete('/api/gallery/:id', async (req, res) => {
  try {
    const { id } = req.params;
    await Gallery.findByIdAndDelete(id);
    res.json({ success: true, message: 'फोटो गॅलरीमधून हटवला गेला' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

const PORT = process.env.PORT || 5000;
if (process.env.NODE_ENV !== 'production') {
  app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
  });
}

module.exports = app;
