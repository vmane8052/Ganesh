# श्री गणेश मित्र मंडळ - Backend API (Vercel + MongoDB)

हे बॅकएंड API **Vercel Serverless Functions** आणि **MongoDB Atlas** साठी डिझाइन केलेले आहे.

## १. लोकल मशिनवर चालवण्यासाठी (Local Testing):
1. `npm install` रन करा.
2. `.env.example` फाईल कॉपी करून `.env` फाईल बनवा आणि त्यात तुमचा MongoDB Connection String टाका:
   ```env
   MONGODB_URI="mongodb+srv://username:password@cluster0.mongodb.net/ganesh_mandal?retryWrites=true&w=majority"
   PORT=5000
   ```
3. `npm run dev` किंवा `npm start` रन करा.
4. API चा पत्ता: `http://localhost:5000/api/health`

## २. Vercel वर Deploy करण्यासाठी (1-Click Deployment):
1. हा `backend` फोल्डर तुमच्या GitHub रिपॉझिटरीवर पुश करा.
2. [Vercel](https://vercel.com) वर लॉग इन करा आणि **Add New Project** वर क्लिक करा.
3. तुमचा GitHub प्रोजेक्ट निवडा.
4. **Environment Variables** मध्ये खालील व्हेरिएबल टाका:
   - Name: `MONGODB_URI`
   - Value: तुमचा MongoDB Atlas Connection String (`mongodb+srv://...`)
5. **Deploy** वर क्लिक करा! काही सेकंदात तुमचे बॅकएंड API लाईव्ह होईल.

## ३. Default Login Credentials:
पहिले टेस्टिंग करण्यासाठी डीफॉल्ट अकाउंट्स स्वयंचलितरित्या तयार होतात:
- **Admin Login:** मोबाईल - `9999999999` | PIN - `1234`
- **Normal User Login:** मोबाईल - `8888888888` | PIN - `1234`
