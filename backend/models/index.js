const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  name: { type: String, required: true },
  phone: { type: String, required: true, unique: true },
  pin: { type: String, required: true }, // Simple 4-digit PIN for login
  role: { type: String, enum: ['ADMIN', 'USER'], default: 'USER' }
}, { timestamps: true });

const transactionSchema = new mongoose.Schema({
  type: { type: String, enum: ['JAMA', 'KHARCH'], required: true },
  amount: { type: Number, required: true },
  details: { type: String, required: true },
  date: { type: String, required: true },
  category: { type: String, default: 'सामान्य' }, // e.g. देणगी, मंडप खर्च, साऊंड सिस्टम
  memberName: { type: String }, // optional link to member/donor
  addedBy: { type: String } // Admin name who added it
}, { timestamps: true });

const memberSchema = new mongoose.Schema({
  name: { type: String, required: true },
  roleInMandal: { type: String, default: 'सदस्य' }, // e.g. अध्यक्ष, उपाध्यक्ष, खजिनदार, सदस्य
  phone: { type: String },
  photoUrl: { type: String }
}, { timestamps: true });

const eventSchema = new mongoose.Schema({
  title: { type: String, required: true },
  date: { type: String, required: true },
  description: { type: String },
  expenseAmount: { type: Number, default: 0 }
}, { timestamps: true });

const donationSchema = new mongoose.Schema({
  donorName: { type: String, required: true },
  amount: { type: Number, required: true },
  date: { type: String, required: true },
  details: { type: String }
}, { timestamps: true });

const gallerySchema = new mongoose.Schema({
  title: { type: String },
  imageUrl: { type: String, required: true },
  uploadedBy: { type: String }
}, { timestamps: true });

module.exports = {
  User: mongoose.model('User', userSchema),
  Transaction: mongoose.model('Transaction', transactionSchema),
  Member: mongoose.model('Member', memberSchema),
  Event: mongoose.model('Event', eventSchema),
  Donation: mongoose.model('Donation', donationSchema),
  Gallery: mongoose.model('Gallery', gallerySchema)
};
