# श्री गणेश मित्र मंडळ माने/ देरे वस्ती - ॲप आणि बॅकएंड सिस्टीम

हा संपूर्ण प्रोजेक्ट **Android Studio (Java + XML)** मोबाईल ॲप आणि **Vercel (Node.js + MongoDB Atlas)** बॅकएंड API चा आहे.

---

## १. प्रोजेक्ट स्ट्रक्चर (Project Structure)
- `backend/` : Node.js Express API जे **Vercel** वर deploy करण्यासाठी तयार आहे आणि **MongoDB Atlas** शी कनेक्ट होते.
- `frontend/` : Android Studio चा Java आणि XML मधील मोबाईल ॲप प्रोजेक्ट.

---

## २. बॅकएंड API सुरू करणे आणि Vercel वर Deploy करणे (Backend API & MongoDB)

### लोकल टेस्टिंग (Local Testing):
1. टर्मिनलमध्ये `cd backend` डिरेक्टरीमध्ये जा.
2. `npm install` रन करा.
3. `.env.example` कॉपी करून `.env` फाईल बनवा आणि त्यात तुमचा MongoDB Connection String टाका:
   ```env
   MONGODB_URI="mongodb+srv://username:password@cluster0.mongodb.net/ganesh_mandal?retryWrites=true&w=majority"
   PORT=5000
   ```
4. `npm run dev` रन करा. सर्व्हर `http://localhost:5000` वर सुरू होईल.

### Vercel वर Deploy करणे (1-Click Deploy to Vercel):
1. हा प्रोजेक्ट तुमच्या GitHub वर पुश करा.
2. **Vercel.com** वर जा -> **Add New Project** -> तुमचा GitHub प्रोजेक्ट निवडा.
3. Root Directory म्हणून `backend` निवडा.
4. **Environment Variables** मध्ये `MONGODB_URI` आणि त्याची व्हॅल्यू (तुमचा MongoDB Atlas connection string) टाका.
5. **Deploy** वर क्लिक करा! तुम्हाला `https://your-project.vercel.app` अशी लिंक मिळेल.

---

## ३. Android Studio ॲप (Java + XML) सुरू करणे
1. **Android Studio** उघडा आणि `frontend` फोल्डर ओपन करा.
2. `app/src/main/java/com/ganeshmandal/app/api/ApiClient.java` फाईल उघडा.
3. `BASE_URL` मध्ये जर तुम्ही लोकल एम्युलेटर वापरत असाल तर `http://10.0.2.2:5000/` राहू द्या, किंवा जर Vercel वर deploy केले असेल तर तुमची Vercel लिंक टाका (उदा. `https://your-project.vercel.app/`).
4. **Run** बटण दाबून ॲप रन करा!

---

## ४. डीफॉल्ट लॉगिन (Default Login Credentials)
पहिले टेस्टिंग करण्यासाठी खालील डीफॉल्ट मोबाईल नंबर आणि पिन वापरा:

- **व्यवस्थापक (Admin Login):**
  - मोबाईल नंबर: `9999999999`
  - PIN: `1234`
  - *(Admin ला जमा करा आणि खर्च करा बटणे दिसतील आणि डाव्या बाजूच्या मेनूमध्ये सदस्य/कार्यक्रम ॲड करण्याचे पर्याय दिसतील)*

- **सामान्य सदस्य (Normal User Login):**
  - मोबाईल नंबर: `8888888888`
  - PIN: `1234`
  - *(Normal User ला फक्त व्यवहारांची यादी दिसेल, जमा/खर्च करण्याची बटणे दिसणार नाहीत)*
