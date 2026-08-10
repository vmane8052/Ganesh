require('dotenv').config();
const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const cloudinary = require('cloudinary').v2;
const { User, Transaction, Member, Event, Donation, Gallery } = require('../models');

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
  res.json({ status: 'OK', message: 'श्री गणेश मित्र मंडळ API is running smoothly!' });
});

// --- AUTH / LOGIN ---
app.post('/api/login', async (req, res) => {
  try {
    const { phone, pin } = req.body || {};
    if (!phone || !pin) {
      return res.status(400).json({ success: false, message: 'कृपया मोबाईल नंबर आणि पिन टाका' });
    }
    const cleanPhone = String(phone).replace(/\D/g, '');
    const cleanPin = String(pin).trim();
    const user = await User.findOne({
      $or: [
        { phone: cleanPhone },
        { phone: String(phone).trim() },
        { phone: cleanPhone.length === 9 ? cleanPhone + cleanPhone.slice(-1) : cleanPhone },
        { phone: cleanPhone.length === 10 ? cleanPhone.slice(0, 9) : cleanPhone }
      ],
      pin: cleanPin
    });
    if (!user) {
      return res.status(401).json({ success: false, message: 'चुकीचा मोबाईल नंबर किंवा पिन' });
    }
    res.json({
      success: true,
      user: {
        id: user._id,
        name: user.name,
        phone: user.phone,
        role: user.role,
        roleInMandal: user.roleInMandal || (user.role === 'ADMIN' ? 'मुख्य व्यवस्थापक' : 'सामान्य सदस्य'),
        photoUrl: user.photoUrl || ''
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- USERS / MEMBERS (सदस्य ॲड करणे) ---
app.post('/api/users', async (req, res) => {
  try {
    const { name, phone, pin, role, roleInMandal, photoUrl } = req.body;
    if (!name || !phone || !pin) {
      return res.status(400).json({ success: false, message: 'नाव, मोबाईल नंबर आणि पासवर्ड आवश्यक आहेत' });
    }

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
      { phone },
      {
        name,
        phone,
        pin,
        role: role || 'USER',
        roleInMandal: roleInMandal || 'सामान्य सदस्य',
        photoUrl: finalPhotoUrl
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
        photoUrl: user.photoUrl
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.get('/api/users', async (req, res) => {
  try {
    const users = await User.find().sort({ createdAt: -1 });
    res.json({ success: true, data: users });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.put('/api/users/phone/:phone', async (req, res) => {
  try {
    const { phone } = req.params;
    const { name, pin, role, roleInMandal, photoUrl } = req.body;

    let finalPhotoUrl = photoUrl;
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
      { phone },
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
    const deletedUser = await User.findOneAndDelete({ phone });
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
    const { type } = req.query; // optional filter by JAMA or KHARCH
    const query = type ? { type } : {};
    const transactions = await Transaction.find(query).sort({ createdAt: -1 });
    res.json({ success: true, data: transactions });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/transactions', async (req, res) => {
  try {
    const { type, amount, details, date, category, memberName, addedBy } = req.body;
    if (!type || !amount || !details || !date) {
      return res.status(400).json({ success: false, message: 'सर्व आवश्यक माहिती भरा' });
    }
    const newTx = await Transaction.create({
      type, amount, details, date, category, memberName, addedBy
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
    const all = await Transaction.find();
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
    const members = await Member.find().sort({ createdAt: -1 });
    res.json({ success: true, data: members });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/members', async (req, res) => {
  try {
    const { name, roleInMandal, phone, photoUrl } = req.body;
    const member = await Member.create({ name, roleInMandal, phone, photoUrl });
    res.status(201).json({ success: true, data: member });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- EVENTS (कार्यक्रम व्यवस्थापन) ---
app.get('/api/events', async (req, res) => {
  try {
    const events = await Event.find().sort({ createdAt: 1 });
    res.json({ success: true, data: events });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/events', async (req, res) => {
  try {
    const { dayTitle, date, morningAarti, eveningAarti, lunchHost, modakHost, culturalProgram, specialNotes } = req.body;
    if (!dayTitle || !date) {
      return res.status(400).json({ success: false, message: 'दिवस आणि तारीख आवश्यक आहे' });
    }
    const event = await Event.create({
      dayTitle,
      date,
      morningAarti: morningAarti || '',
      eveningAarti: eveningAarti || '',
      lunchHost: lunchHost || '',
      modakHost: modakHost || '',
      culturalProgram: culturalProgram || '',
      specialNotes: specialNotes || ''
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

// --- DONATIONS (देणगी व्यवस्थापन - रोख रक्कम व वस्तू देणगी) ---
app.get('/api/donations', async (req, res) => {
  try {
    const donations = await Donation.find().sort({ createdAt: -1 });
    res.json({ success: true, data: donations });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/donations', async (req, res) => {
  try {
    const { donorName, donorPhone, donationType, amount, itemDetails, date, address, receiptNo } = req.body;
    if (!donorName || !date) {
      return res.status(400).json({ success: false, message: 'देणगीदाराचे नाव आणि तारीख आवश्यक आहे' });
    }
    const donation = await Donation.create({
      donorName,
      donorPhone: donorPhone || '',
      donationType: donationType || 'CASH',
      amount: amount || 0,
      itemDetails: itemDetails || '',
      date,
      address: address || '',
      receiptNo: receiptNo || ''
    });
    res.status(201).json({ success: true, message: 'देणगी यशस्वीरीत्या नोंदवली गेली', data: donation });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.put('/api/donations/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const updated = await Donation.findByIdAndUpdate(id, req.body, { new: true });
    if (!updated) {
      return res.status(404).json({ success: false, message: 'देणगी नोंद सापडली नाही' });
    }
    res.json({ success: true, message: 'देणगी यशस्वीरीत्या अपडेट केली', data: updated });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.delete('/api/donations/:id', async (req, res) => {
  try {
    const { id } = req.params;
    await Donation.findByIdAndDelete(id);
    res.json({ success: true, message: 'देणगी यशस्वीरीत्या हटवली गेली' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- PHOTO UPLOAD (Cloudinary & Base64 Fallback) ---
app.post('/api/upload', async (req, res) => {
  try {
    const { image, phone } = req.body;
    if (!image) {
      return res.status(400).json({ success: false, message: 'फोटो डेटा आवश्यक आहे' });
    }

    let finalUrl = image;

    // If Cloudinary environment variables are present, upload to Cloudinary
    if (process.env.CLOUDINARY_CLOUD_NAME && process.env.CLOUDINARY_API_KEY && process.env.CLOUDINARY_API_SECRET) {
      const uploadRes = await cloudinary.uploader.upload(image, {
        folder: 'ganesh_mandal_profiles',
        transformation: [{ width: 500, height: 500, crop: 'fill', gravity: 'face' }]
      });
      finalUrl = uploadRes.secure_url;
    }

    // If phone is supplied, automatically update the user's profile photo in MongoDB
    if (phone) {
      await User.findOneAndUpdate({ phone }, { photoUrl: finalUrl });
    }

    res.json({ success: true, message: 'फोटो यशस्वीरीत्या सेव्ह झाला', photoUrl: finalUrl });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- GALLERY (फोटो) ---
app.get('/api/gallery', async (req, res) => {
  try {
    const photos = await Gallery.find().sort({ createdAt: -1 });
    res.json({ success: true, data: photos });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/gallery', async (req, res) => {
  try {
    const { title, imageUrl, uploadedBy } = req.body;
    const photo = await Gallery.create({ title, imageUrl, uploadedBy });
    res.status(201).json({ success: true, data: photo });
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
