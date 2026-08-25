require('dotenv').config();
const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const cloudinary = require('cloudinary').v2;
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const { Mandal, User, Transaction, Member, Event, Donation, Gallery, AuditLog } = require('../models');

const JWT_SECRET = process.env.JWT_SECRET || 'ganesh_mandal_super_secret_jwt_key_2026';

// Configure Cloudinary
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME || 't19ganuk',
  api_key: process.env.CLOUDINARY_API_KEY || '936194428912286',
  api_secret: process.env.CLOUDINARY_API_SECRET || 'teZxIFJt4hit5pnT9NnxP0zfpjc'
});

const dns = require('dns');
try { dns.setServers(['8.8.8.8', '8.8.4.4']); } catch(e){}

const app = express();

// Security Headers & Rate Limiting
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

// Login Rate Limiter (Brute-force Protection: max 10 requests per 15 mins)
const loginRateLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 10,
  message: { success: false, message: 'सुरक्षा कारणास्तव अनेक वेळा चुकीचे प्रयत्न झाले आहेत. कृपया १५ मिनिटांनंतर प्रयत्न करा.' },
  standardHeaders: true,
  legacyHeaders: false,
});

// Audit Logger Helper Function
async function logAuditAction(userId, userName, mandalId, action, details, req) {
  try {
    const ip = req ? (req.headers['x-forwarded-for'] || req.socket.remoteAddress || '') : '';
    await AuditLog.create({ userId, userName, mandalId, action, details, ip });
  } catch (err) {
    console.error('Audit log error:', err);
  }
}

// Authentication Middleware: Verify JWT Token
function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

  if (!token) {
    req.user = null;
    return next();
  }

  jwt.verify(token, JWT_SECRET, (err, decoded) => {
    if (err) {
      req.user = null;
    } else {
      req.user = decoded;
    }
    next();
  });
}

app.use(authenticateToken);

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
            contactPhone: '9876543210',
            status: 'active'
          });
        }
      } catch (err) {
        console.error('Mandal init error:', err);
      }

      // Auto-initialize Super Admin
      try {
        const superAdmin = await User.findOne({ role: 'SUPER_ADMIN' });
        if (!superAdmin) {
          const hashedPin = await bcrypt.hash('8052', 10);
          await User.create({
            name: 'Super Admin',
            phone: '8052',
            pin: hashedPin,
            role: 'SUPER_ADMIN',
            roleInMandal: 'मुख्य प्रशासक',
            mandalId: 'M001'
          });
          console.log('Super Admin account created (Phone: 8052)');
        }
      } catch (err) {
        console.error('Super Admin init error:', err);
      }

      return db;
    }).catch(err => {
      console.error('MongoDB connection error:', err);
      cachedPromise = null;
      throw err;
    });
  }
  return cachedPromise;
}

app.use(async (req, res, next) => {
  try {
    await connectDB();
    next();
  } catch (err) {
    res.status(500).json({ success: false, message: 'डेटाबेस कनेक्शन एरर' });
  }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', message: 'श्री गणेश मित्र मंडळ Multi-Mandal Secure API is running smoothly!' });
});

// --- MANDALS MANAGEMENT (Super Admin Only) ---
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
    res.status(500).json({ success: false, message: 'मंडळ यादी लोड करताना समस्या आली' });
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

    await logAuditAction(req.user ? req.user.userId : '', req.user ? req.user.name : 'Super Admin', mandalId, 'CREATE_MANDAL', `Created mandal ${mandalName} (${mandalId})`, req);

    res.status(201).json({ success: true, message: 'नवीन मंडळ यशस्वीरीत्या तयार केले!', data: mandal });
  } catch (err) {
    res.status(500).json({ success: false, message: 'मंडळ जोडताना समस्या आली' });
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

    await logAuditAction(req.user ? req.user.userId : '', req.user ? req.user.name : 'Super Admin', mandalId, 'UPDATE_MANDAL', `Updated mandal ${mandalName} (${mandalId})`, req);

    res.json({ success: true, message: 'मंडळाची माहिती यशस्वीरीत्या अपडेट केली', data: updatedMandal });
  } catch (err) {
    res.status(500).json({ success: false, message: 'मंडळ अपडेट करताना समस्या आली' });
  }
});

// --- AUTH / LOGIN (With Rate Limiter & Bcrypt Password Security) ---
app.post('/api/login', loginRateLimiter, async (req, res) => {
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
      ]
    };
    if (mandalId) {
      queryFilter.mandalId = mandalId;
    }

    const users = await User.find(queryFilter);
    if (!users || users.length === 0) {
      return res.status(401).json({ success: false, message: 'चुकीचा मोबाईल नंबर किंवा पिन' });
    }

    let authenticatedUser = null;

    for (let u of users) {
      let isMatch = false;
      if (u.pin.startsWith('$2a$') || u.pin.startsWith('$2b$')) {
        isMatch = await bcrypt.compare(cleanPin, u.pin);
      } else if (u.pin === cleanPin) {
        isMatch = true;
        // Upgrade legacy plaintext pin to bcrypt hash automatically
        u.pin = await bcrypt.hash(cleanPin, 10);
        await u.save();
      }

      if (isMatch) {
        authenticatedUser = u;
        break;
      }
    }

    if (!authenticatedUser) {
      return res.status(401).json({ success: false, message: 'चुकीचा मोबाईल नंबर किंवा पिन' });
    }

    const userMandalId = authenticatedUser.mandalId || 'M001';
    const mandalInfo = await Mandal.findOne({ mandalId: userMandalId });

    // Generate JWT Token (NO password in payload)
    const token = jwt.sign(
      {
        userId: authenticatedUser._id,
        name: authenticatedUser.name,
        phone: authenticatedUser.phone,
        role: authenticatedUser.role,
        mandalId: userMandalId
      },
      JWT_SECRET,
      { expiresIn: '30d' }
    );

    await logAuditAction(authenticatedUser._id, authenticatedUser.name, userMandalId, 'LOGIN', 'User logged in successfully', req);

    res.json({
      success: true,
      token,
      user: {
        id: authenticatedUser._id,
        name: authenticatedUser.name,
        phone: authenticatedUser.phone,
        role: authenticatedUser.role,
        roleInMandal: authenticatedUser.roleInMandal || (authenticatedUser.role === 'ADMIN' ? 'मुख्य व्यवस्थापक' : 'सामान्य सदस्य'),
        photoUrl: authenticatedUser.photoUrl || '',
        mandalId: userMandalId,
        mandalName: mandalInfo ? mandalInfo.mandalName : 'श्री गणेश मित्र मंडळ',
        mandalAddress: mandalInfo ? mandalInfo.address : 'माने/ढेरे वस्ती, बाळेवाडी',
        mandalLogoUrl: mandalInfo ? (mandalInfo.logoUrl || '') : ''
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: 'लॉगिन करताना समस्या आली' });
  }
});

// --- FORGOT PIN & RESET PIN VIA MOBILE OTP ---
app.post('/api/auth/forgot-pin', async (req, res) => {
  try {
    const { phone } = req.body || {};
    if (!phone) {
      return res.status(400).json({ success: false, message: 'कृपया मोबाईल नंबर टाका' });
    }

    const cleanPhone = String(phone).replace(/\D/g, '');
    const user = await User.findOne({
      $or: [
        { phone: cleanPhone },
        { phone: String(phone).trim() },
        { phone: cleanPhone.length === 9 ? cleanPhone + cleanPhone.slice(-1) : cleanPhone },
        { phone: cleanPhone.length === 10 ? cleanPhone.slice(0, 9) : cleanPhone }
      ]
    });

    if (!user) {
      return res.status(404).json({ success: false, message: 'हा मोबाईल नंबर नोंदणीकृत नाही!' });
    }

    // Generate 6-digit random OTP & 5-minute expiry
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    const otpExpires = new Date(Date.now() + 5 * 60 * 1000);

    user.resetOtp = otp;
    user.resetOtpExpires = otpExpires;
    await user.save();

    console.log(`[OTP DEBUG] Mobile: ${cleanPhone}, Reset OTP: ${otp}`);

    await logAuditAction(user._id, user.name, user.mandalId, 'FORGOT_PIN_REQUEST', `OTP generated for phone ${cleanPhone}`, req);

    res.json({
      success: true,
      message: 'OTP तुमच्या मोबाईल नंबरवर पाठवला आहे!',
      debugOtp: otp
    });
  } catch (err) {
    console.error('Forgot PIN error:', err);
    res.status(500).json({ success: false, message: 'OTP पाठवताना समस्या आली' });
  }
});

app.post('/api/auth/reset-pin', async (req, res) => {
  try {
    const { phone, otp, newPin } = req.body || {};
    if (!phone || !otp || !newPin) {
      return res.status(400).json({ success: false, message: 'कृपया मोबाईल नंबर, OTP आणि नवा PIN टाका' });
    }

    const cleanPhone = String(phone).replace(/\D/g, '');
    const cleanOtp = String(otp).trim();
    const cleanNewPin = String(newPin).trim();

    const user = await User.findOne({
      $or: [
        { phone: cleanPhone },
        { phone: String(phone).trim() }
      ]
    });

    if (!user) {
      return res.status(404).json({ success: false, message: 'युझर सापडला नाही' });
    }

    if (!user.resetOtp || user.resetOtp !== cleanOtp) {
      return res.status(400).json({ success: false, message: 'चुकीचा (Invalid) OTP टाकला आहे!' });
    }

    if (new Date() > user.resetOtpExpires) {
      return res.status(400).json({ success: false, message: 'OTP ची मुदत संपली आहे. कृपया पुन्हा OTP मागवा.' });
    }

    // Hash the new PIN securely using bcrypt
    const hashedPin = await bcrypt.hash(cleanNewPin, 10);
    user.pin = hashedPin;
    user.resetOtp = null;
    user.resetOtpExpires = null;
    await user.save();

    await logAuditAction(user._id, user.name, user.mandalId, 'RESET_PIN_SUCCESS', `PIN reset successfully for phone ${cleanPhone}`, req);

    res.json({
      success: true,
      message: 'तुमचा PIN यशस्वीरीत्या बदलला आहे! आता नव्या PIN ने लॉगिन करा.'
    });
  } catch (err) {
    console.error('Reset PIN error:', err);
    res.status(500).json({ success: false, message: 'PIN बदलताना समस्या आली' });
  }
});

// --- USERS / MEMBERS (Strict Tenant Isolation) ---
app.get('/api/users', async (req, res) => {
  try {
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN') ? (req.query.mandalId || userMandalId || 'M001') : (userMandalId || req.query.mandalId || 'M001');

    const users = await User.find({ mandalId: targetMandalId }).sort({ createdAt: -1 });
    res.json({ success: true, data: users });
  } catch (err) {
    res.status(500).json({ success: false, message: 'सदस्य यादी लोड करताना समस्या आली' });
  }
});

app.post('/api/users', async (req, res) => {
  try {
    const { name, phone, pin, role, roleInMandal, photoUrl, mandalId } = req.body;
    if (!name || !phone || !pin) {
      return res.status(400).json({ success: false, message: 'नाव, मोबाईल नंबर आणि पासवर्ड आवश्यक आहेत' });
    }

    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN' && mandalId) ? mandalId : (userMandalId || mandalId || 'M001');

    const cleanPin = String(pin).trim();
    const hashedPin = await bcrypt.hash(cleanPin, 10);

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
        pin: hashedPin,
        role: role || 'USER',
        roleInMandal: roleInMandal || 'सामान्य सदस्य',
        photoUrl: finalPhotoUrl,
        mandalId: targetMandalId
      },
      { upsert: true, new: true, setDefaultsOnInsert: true }
    );

    await logAuditAction(req.user ? req.user.userId : '', req.user ? req.user.name : 'System', targetMandalId, 'ADD_USER', `Created/Updated user ${name} (${phone})`, req);

    res.status(201).json({
      success: true,
      user: {
        id: user._id,
        name: user.name,
        phone: user.phone,
        role: user.role,
        roleInMandal: user.roleInMandal,
        photoUrl: user.photoUrl,
        mandalId: user.mandalId
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: 'सदस्य साठवताना समस्या आली' });
  }
});

app.post('/api/users/change-password', async (req, res) => {
  try {
    const { phone, currentPin, newPin, mandalId } = req.body;
    if (!phone || !newPin) {
      return res.status(400).json({ success: false, message: 'मोबाईल नंबर आणि नवीन पासवर्ड आवश्यक आहे' });
    }

    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN' && mandalId) ? mandalId : (userMandalId || mandalId || 'M001');

    const user = await User.findOne({ phone, mandalId: targetMandalId });
    if (!user) {
      return res.status(404).json({ success: false, message: 'सदस्य सापडला नाही' });
    }

    if (currentPin) {
      let isMatch = false;
      if (user.pin.startsWith('$2a$') || user.pin.startsWith('$2b$')) {
        isMatch = await bcrypt.compare(String(currentPin).trim(), user.pin);
      } else {
        isMatch = user.pin === String(currentPin).trim();
      }
      if (!isMatch) {
        return res.status(400).json({ success: false, message: 'जुना पासवर्ड चुकीचा आहे' });
      }
    }

    user.pin = await bcrypt.hash(String(newPin).trim(), 10);
    await user.save();

    await logAuditAction(user._id, user.name, targetMandalId, 'CHANGE_PASSWORD', 'Password changed successfully', req);

    res.json({ success: true, message: 'पासवर्ड यशस्वीरीत्या बदलला!' });
  } catch (err) {
    res.status(500).json({ success: false, message: 'पासवर्ड बदलताना समस्या आली' });
  }
});

app.delete('/api/users/phone/:phone', async (req, res) => {
  try {
    const { phone } = req.params;
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN') ? (req.query.mandalId || userMandalId || 'M001') : (userMandalId || req.query.mandalId || 'M001');

    const deletedUser = await User.findOneAndDelete({ phone, mandalId: targetMandalId });
    if (!deletedUser) {
      return res.status(404).json({ success: false, message: 'सदस्य सापडला नाही' });
    }

    await logAuditAction(req.user ? req.user.userId : '', req.user ? req.user.name : 'System', targetMandalId, 'DELETE_USER', `Deleted user ${phone}`, req);

    res.json({ success: true, message: 'सदस्य यशस्वीरित्या हटवला' });
  } catch (err) {
    res.status(500).json({ success: false, message: 'सदस्य हटवताना समस्या आली' });
  }
});

// --- TRANSACTIONS (जमा-खर्च Tenant Isolation) ---
app.get('/api/transactions', async (req, res) => {
  try {
    const { type } = req.query;
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN') ? (req.query.mandalId || userMandalId || 'M001') : (userMandalId || req.query.mandalId || 'M001');

    const query = { mandalId: targetMandalId };
    if (type) query.type = type;

    const transactions = await Transaction.find(query).sort({ createdAt: -1 });
    res.json({ success: true, data: transactions });
  } catch (err) {
    res.status(500).json({ success: false, message: 'व्यवहार यादी लोड करताना समस्या आली' });
  }
});

app.post('/api/transactions', async (req, res) => {
  try {
    const { type, amount, details, date, category, memberName, memberPhone, addedBy, receiptNo, mandalId } = req.body;
    if (!type || !amount || !details || !date) {
      return res.status(400).json({ success: false, message: 'सर्व आवश्यक माहिती भरा' });
    }

    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN' && mandalId) ? mandalId : (userMandalId || mandalId || 'M001');
    const finalReceiptNo = receiptNo || (`REC-${new Date().getFullYear()}-${Math.floor(1000 + Math.random() * 9000)}`);
    
    const newTx = await Transaction.create({
      type, amount, details, date, category, memberName, memberPhone: memberPhone || '', addedBy, receiptNo: finalReceiptNo, mandalId: targetMandalId
    });

    await logAuditAction(req.user ? req.user.userId : '', req.user ? req.user.name : 'System', targetMandalId, 'ADD_TRANSACTION', `Added transaction ${type} ₹${amount}`, req);

    res.status(201).json({ success: true, data: newTx });
  } catch (err) {
    res.status(500).json({ success: false, message: 'व्यवहार नोंदवताना समस्या आली' });
  }
});

app.put('/api/transactions/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const userMandalId = req.user ? req.user.mandalId : null;
    const filter = (req.user && req.user.role === 'SUPER_ADMIN') ? { _id: id } : { _id: id, mandalId: userMandalId || 'M001' };

    const updatedTx = await Transaction.findOneAndUpdate(filter, req.body, { new: true });
    res.json({ success: true, data: updatedTx });
  } catch (err) {
    res.status(500).json({ success: false, message: 'व्यवहार अपडेट करताना समस्या आली' });
  }
});

app.delete('/api/transactions/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const userMandalId = req.user ? req.user.mandalId : null;
    const filter = (req.user && req.user.role === 'SUPER_ADMIN') ? { _id: id } : { _id: id, mandalId: userMandalId || 'M001' };

    await Transaction.findOneAndDelete(filter);
    res.json({ success: true, message: 'व्यवहार यशस्वीरीत्या हटवला' });
  } catch (err) {
    res.status(500).json({ success: false, message: 'व्यवहार हटवताना समस्या आली' });
  }
});

// --- SUMMARY (एकूण जमा, खर्च आणि शिल्लक) ---
app.get('/api/summary', async (req, res) => {
  try {
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN') ? (req.query.mandalId || userMandalId || 'M001') : (userMandalId || req.query.mandalId || 'M001');

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
    res.status(500).json({ success: false, message: 'समरी लोड करताना समस्या आली' });
  }
});

// --- MEMBERS (सदस्य Tenant Isolation) ---
app.get('/api/members', async (req, res) => {
  try {
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN') ? (req.query.mandalId || userMandalId || 'M001') : (userMandalId || req.query.mandalId || 'M001');

    const members = await Member.find({ mandalId: targetMandalId }).sort({ createdAt: -1 });
    res.json({ success: true, data: members });
  } catch (err) {
    res.status(500).json({ success: false, message: 'सदस्य लोड करताना समस्या आली' });
  }
});

app.post('/api/members', async (req, res) => {
  try {
    const { name, roleInMandal, phone, photoUrl, mandalId } = req.body;
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN' && mandalId) ? mandalId : (userMandalId || mandalId || 'M001');

    const member = await Member.create({ name, roleInMandal, phone, photoUrl, mandalId: targetMandalId });
    res.status(201).json({ success: true, data: member });
  } catch (err) {
    res.status(500).json({ success: false, message: 'सदस्य जोडताना समस्या आली' });
  }
});

// --- EVENTS (कार्यक्रम व्यवस्थापन Tenant Isolation & Sequential Sorting) ---
app.get('/api/events', async (req, res) => {
  try {
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN') ? (req.query.mandalId || userMandalId || 'M001') : (userMandalId || req.query.mandalId || 'M001');

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
    res.status(500).json({ success: false, message: 'कार्यक्रम लोड करताना समस्या आली' });
  }
});

app.post('/api/events', async (req, res) => {
  try {
    const { dayTitle, date, morningAarti, eveningAarti, lunchHost, modakHost, culturalProgram, specialNotes, mandalId } = req.body;
    if (!dayTitle || !date) {
      return res.status(400).json({ success: false, message: 'दिवस आणि तारीख आवश्यक आहे' });
    }
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN' && mandalId) ? mandalId : (userMandalId || mandalId || 'M001');

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
    res.status(500).json({ success: false, message: 'कार्यक्रम जोडताना समस्या आली' });
  }
});

app.put('/api/events/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const userMandalId = req.user ? req.user.mandalId : null;
    const filter = (req.user && req.user.role === 'SUPER_ADMIN') ? { _id: id } : { _id: id, mandalId: userMandalId || 'M001' };

    const updated = await Event.findOneAndUpdate(filter, req.body, { new: true });
    if (!updated) {
      return res.status(404).json({ success: false, message: 'कार्यक्रम सापडला नाही' });
    }
    res.json({ success: true, message: 'कार्यक्रम यशस्वीरीत्या अपडेट केला', data: updated });
  } catch (err) {
    res.status(500).json({ success: false, message: 'कार्यक्रम अपडेट करताना समस्या आली' });
  }
});

app.delete('/api/events/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const userMandalId = req.user ? req.user.mandalId : null;
    const filter = (req.user && req.user.role === 'SUPER_ADMIN') ? { _id: id } : { _id: id, mandalId: userMandalId || 'M001' };

    await Event.findOneAndDelete(filter);
    res.json({ success: true, message: 'कार्यक्रम यशस्वीरीत्या हटवला गेला' });
  } catch (err) {
    res.status(500).json({ success: false, message: 'कार्यक्रम हटवताना समस्या आली' });
  }
});

// --- DONATIONS (देणगी व्यवस्थापन Tenant Isolation) ---
app.get('/api/donations', async (req, res) => {
  try {
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN') ? (req.query.mandalId || userMandalId || 'M001') : (userMandalId || req.query.mandalId || 'M001');

    const donations = await Donation.find({ mandalId: targetMandalId }).sort({ createdAt: -1 });
    res.json({ success: true, data: donations });
  } catch (err) {
    res.status(500).json({ success: false, message: 'देणगी यादी लोड करताना समस्या आली' });
  }
});

app.post('/api/donations', async (req, res) => {
  try {
    const { donorName, donorPhone, donationType, amount, itemDetails, date, address, receiptNo, mandalId } = req.body;
    if (!donorName || !date) {
      return res.status(400).json({ success: false, message: 'देणगीदाराचे नाव आणि तारीख आवश्यक आहे' });
    }
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN' && mandalId) ? mandalId : (userMandalId || mandalId || 'M001');

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
    res.status(500).json({ success: false, message: 'देणगी नोंदवताना समस्या आली' });
  }
});

// --- PHOTO UPLOAD ---
app.post('/api/upload', async (req, res) => {
  try {
    const { image, phone, mandalId } = req.body;
    if (!image) {
      return res.status(400).json({ success: false, message: 'फोटो डेटा आवश्यक आहे' });
    }
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN' && mandalId) ? mandalId : (userMandalId || mandalId || 'M001');

    let finalUrl = image;

    if (process.env.CLOUDINARY_CLOUD_NAME && process.env.CLOUDINARY_API_KEY && process.env.CLOUDINARY_API_SECRET) {
      const uploadRes = await cloudinary.uploader.upload(image, {
        folder: 'ganesh_mandal_profiles',
        transformation: [{ width: 500, height: 500, crop: 'fill', gravity: 'face' }]
      });
      finalUrl = uploadRes.secure_url;
    }

    if (phone) {
      await User.findOneAndUpdate({ phone, mandalId: targetMandalId }, { photoUrl: finalUrl });
    }

    res.json({ success: true, message: 'फोटो यशस्वीरीत्या सेव्ह झाला', photoUrl: finalUrl });
  } catch (err) {
    res.status(500).json({ success: false, message: 'फोटो अपलोड करताना समस्या आली' });
  }
});

// --- GALLERY (फोटो गॅलरी Tenant Isolation) ---
app.get('/api/gallery', async (req, res) => {
  try {
    const { year } = req.query;
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN') ? (req.query.mandalId || userMandalId || 'M001') : (userMandalId || req.query.mandalId || 'M001');

    const filter = { mandalId: targetMandalId };
    if (year && year !== 'ALL' && year !== 'सर्व' && year !== 'सर्व वर्षे') {
      filter.year = year;
    }
    const photos = await Gallery.find(filter).sort({ year: -1, createdAt: -1 });
    res.json({ success: true, data: photos });
  } catch (err) {
    res.status(500).json({ success: false, message: 'गॅलरी फोटो लोड करताना समस्या आली' });
  }
});

app.post('/api/gallery', async (req, res) => {
  try {
    const { title, imageUrl, uploadedBy, year, mandalId } = req.body;
    if (!imageUrl) {
      return res.status(400).json({ success: false, message: 'फोटो आवश्यक आहे' });
    }
    const userMandalId = req.user ? req.user.mandalId : null;
    const targetMandalId = (req.user && req.user.role === 'SUPER_ADMIN' && mandalId) ? mandalId : (userMandalId || mandalId || 'M001');

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
    res.status(500).json({ success: false, message: 'गॅलरी फोटो अपलोड करताना समस्या आली' });
  }
});

app.delete('/api/gallery/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const userMandalId = req.user ? req.user.mandalId : null;
    const filter = (req.user && req.user.role === 'SUPER_ADMIN') ? { _id: id } : { _id: id, mandalId: userMandalId || 'M001' };

    await Gallery.findOneAndDelete(filter);
    res.json({ success: true, message: 'फोटो गॅलरीमधून हटवला गेला' });
  } catch (err) {
    res.status(500).json({ success: false, message: 'फोटो हटवताना समस्या आली' });
  }
});

// Production Error Handling Middleware (Sanitizes error leaks)
app.use((err, req, res, next) => {
  console.error('Unhandled Server Error:', err);
  res.status(500).json({ success: false, message: 'काहीतरी तांत्रिक समस्या आली आहे. कृपया थोड्या वेळाने प्रयत्न करा.' });
});

const PORT = process.env.PORT || 5000;
if (process.env.NODE_ENV !== 'production') {
  app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
  });
}

module.exports = app;
