const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  name: { type: String, required: true },
  phone: { type: String, required: true, unique: true },
  pin: { type: String, required: true },
  role: { type: String, enum: ['ADMIN', 'USER'], default: 'USER' },
  roleInMandal: { type: String, default: 'सामान्य सदस्य' },
  photoUrl: { type: String, default: '' }
}, { timestamps: true });

const transactionSchema = new mongoose.Schema({
  type: { type: String, enum: ['JAMA', 'KHARCH'], required: true },
  amount: { type: Number, required: true },
  details: { type: String, required: true },
  date: { type: String, required: true },
  category: { type: String, default: 'इतर' },
  memberName: { type: String, default: 'सदस्य' },
  memberPhone: { type: String, default: '' },
  addedBy: { type: String, default: 'व्यवस्थापक' },
  receiptNo: { type: String, default: '' }
}, { timestamps: true });

const memberSchema = new mongoose.Schema({
  name: { type: String, required: true },
  roleInMandal: { type: String, default: 'सामान्य सदस्य' },
  phone: { type: String },
  photoUrl: { type: String }
}, { timestamps: true });

const eventSchema = new mongoose.Schema({
  dayTitle: { type: String, required: true },
  date: { type: String, required: true },
  morningAarti: { type: String, default: '' },
  eveningAarti: { type: String, default: '' },
  lunchHost: { type: String, default: '' },
  modakHost: { type: String, default: '' },
  culturalProgram: { type: String, default: '' },
  specialNotes: { type: String, default: '' }
}, { timestamps: true });

const donationSchema = new mongoose.Schema({
  donorName: { type: String, required: true },
  donorPhone: { type: String, default: '' },
  donationType: { type: String, default: 'CASH' },
  amount: { type: Number, default: 0 },
  itemDetails: { type: String, default: '' },
  date: { type: String, required: true },
  address: { type: String, default: '' },
  receiptNo: { type: String, default: '' }
}, { timestamps: true });

const gallerySchema = new mongoose.Schema({
  title: { type: String },
  imageUrl: { type: String, required: true },
  uploadedBy: { type: String },
  year: { type: String, default: '2026' }
}, { timestamps: true });

module.exports = {
  User: mongoose.models.User || mongoose.model('User', userSchema),
  Transaction: mongoose.models.Transaction || mongoose.model('Transaction', transactionSchema),
  Member: mongoose.models.Member || mongoose.model('Member', memberSchema),
  Event: mongoose.models.Event || mongoose.model('Event', eventSchema),
  Donation: mongoose.models.Donation || mongoose.model('Donation', donationSchema),
  Gallery: mongoose.models.Gallery || mongoose.model('Gallery', gallerySchema)
};
