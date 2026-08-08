require('dotenv').config();
const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const { User, Transaction, Member, Event, Donation, Gallery } = require('../models');

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
      try {
        await User.findOneAndUpdate(
          { phone: '9999999999' },
          { name: 'मुख्य व्यवस्थापक (Admin)', phone: '9999999999', pin: '1234', role: 'ADMIN', roleInMandal: 'मुख्य व्यवस्थापक' },
          { upsert: true }
        );
        await User.findOneAndUpdate(
          { phone: '8888888888' },
          { name: 'गणेश विठ्ठल माने', phone: '8888888888', pin: '1234', role: 'USER', roleInMandal: 'उपाध्यक्ष' },
          { upsert: true }
        );
      } catch (seedErr) {
        console.error('Seed error:', seedErr);
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
    const user = await User.findOneAndUpdate(
      { phone },
      {
        name,
        phone,
        pin,
        role: role || 'USER',
        roleInMandal: roleInMandal || 'सामान्य सदस्य',
        photoUrl: photoUrl || ''
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
    const updatedUser = await User.findOneAndUpdate(
      { phone },
      { name, pin, role, roleInMandal, photoUrl },
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

// --- EVENTS (कार्यक्रम) ---
app.get('/api/events', async (req, res) => {
  try {
    const events = await Event.find().sort({ createdAt: -1 });
    res.json({ success: true, data: events });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.post('/api/events', async (req, res) => {
  try {
    const { title, date, description, expenseAmount } = req.body;
    const event = await Event.create({ title, date, description, expenseAmount });
    res.status(201).json({ success: true, data: event });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// --- DONATIONS (देणगी) ---
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
    const { donorName, amount, date, details } = req.body;
    const donation = await Donation.create({ donorName, amount, date, details });
    res.status(201).json({ success: true, data: donation });
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
