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
  addedBy: { type: String, default: 'व्यवस्थापक' }
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
  donorName: { type: String, required: true }, // देणगीदाराचे नाव
  donorPhone: { type: String, default: '' },    // मोबाईल नंबर
  donationType: { type: String, enum: ['CASH', 'ITEM', 'ONLINE'], default: 'CASH' }, // CASH (रोख रक्कम), ITEM (वस्तू देणगी), ONLINE (ऑनलाइन/UPI)
  amount: { type: Number, default: 0 },          // रोख/ऑनलाइन रक्कम (उदा. ₹ ५००१)
  itemDetails: { type: String, default: '' },   // वस्तू देणगीचे नाव व तपशील (उदा. २१ चांदीचे मोदक, ५० किलो धान्य, साऊंड सिस्टीम, चांदीचा मुकुट)
  date: { type: String, required: true },       // तारीख
  address: { type: String, default: '' },       // गाव / पत्ता
  receiptNo: { type: String, default: '' }      // पावती क्रमांक
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
