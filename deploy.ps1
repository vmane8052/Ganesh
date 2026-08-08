Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "   श्री गणेश मित्र मंडळ - ऑटो डेप्लॉय स्क्रिप्ट   " -ForegroundColor Yellow
Write-Host "===============================================" -ForegroundColor Cyan

Write-Host "`n[१/२] GitHub वर कोड Push केला जात आहे..." -ForegroundColor Green
Write-Host "टीप: जर GitHub लॉगिन विंडो उघडली, तर 'Sign in with browser' वर क्लिक करा." -ForegroundColor Yellow
git push -u origin main

Write-Host "`n[२/२] Vercel वर लॉगिन आणि Backend API जोडली जात आहे..." -ForegroundColor Green
cd backend
Write-Host "Vercel वर लॉगिन केले जात आहे..." -ForegroundColor Yellow
npx.cmd vercel login
Write-Host "Backend API Deploy केली जात आहे..." -ForegroundColor Yellow
npx.cmd vercel --prod

Write-Host "`n===============================================" -ForegroundColor Cyan
Write-Host "   अभिनंदन! तुमचा कोड GitHub आणि Vercel वर जोडला गेला आहे!   " -ForegroundColor Yellow
Write-Host "===============================================" -ForegroundColor Cyan
