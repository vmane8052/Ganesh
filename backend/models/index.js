const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  name: { type: String, required: true },
  phone: { type: String, required: true, unique: true },
  pin: { type: String, required: true },
  role: { type: String, enum: ['ADMIN', 'USER'], default: 'USER' },
  roleInMandal: { type: String, default: 'सामान्य सदस्य' }, // अध्यक्ष, उपाध्यक्ष, सचिव, खजिनदार, कार्यकर्ते, सामान्य सदस्य
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
  dayTitle: { type: String, required: true }, // दिवस १ (श्री गणेश प्रतिष्ठापना), दिवस २, दिवस ५ (गौरी आगमन)...
  date: { type: String, required: true },     // ०८ सप्टेंबर २०२६
  morningAarti: { type: String, default: '' }, // सकाळची आरती (नाव / यजमान)
  eveningAarti: { type: String, default: '' }, // संध्याकाळची आरती (नाव / यजमान)
  lunchHost: { type: String, default: '' },    // महाप्रसाद / जेवणाचा मान (नाव / यजमान)
  modakHost: { type: String, default: '' },    // मोदकाचा मान (नाव / यजमान)
  culturalProgram: { type: String, default: '' }, // सांस्कृतिक कार्यक्रम / भजन / पूजा
  specialNotes: { type: String, default: '' }  // विशेष सूचना / टीप
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
