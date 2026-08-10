package com.ganeshmandal.app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.adapters.AartiAdapter;
import com.ganeshmandal.app.models.AartiItem;
import java.util.ArrayList;
import java.util.List;

public class AartiActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnZoomIn, btnZoomOut;
    private TextView chipGanapati, chipShankar, chipDurga, chipVitthal, chipDatta, chipYeiHoVitthale, chipDnyanraja, chipTukaram, chipLotangan, chipMantra, chipPasaydan;
    private RecyclerView rvAartis;
    private LinearLayoutManager layoutManager;
    private AartiAdapter adapter;
    private final List<AartiItem> aartiList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aarti);

        btnBack = findViewById(R.id.btnBack);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        rvAartis = findViewById(R.id.rvAartis);

        chipGanapati = findViewById(R.id.chipGanapati);
        chipShankar = findViewById(R.id.chipShankar);
        chipDurga = findViewById(R.id.chipDurga);
        chipVitthal = findViewById(R.id.chipVitthal);
        chipDatta = findViewById(R.id.chipDatta);
        chipYeiHoVitthale = findViewById(R.id.chipYeiHoVitthale);
        chipDnyanraja = findViewById(R.id.chipDnyanraja);
        chipTukaram = findViewById(R.id.chipTukaram);
        chipLotangan = findViewById(R.id.chipLotangan);
        chipMantra = findViewById(R.id.chipMantra);
        chipPasaydan = findViewById(R.id.chipPasaydan);

        layoutManager = new LinearLayoutManager(this);
        rvAartis.setLayoutManager(layoutManager);
        adapter = new AartiAdapter();
        rvAartis.setAdapter(adapter);

        loadAllAartis();
        adapter.setAartiList(aartiList);

        btnBack.setOnClickListener(v -> finish());

        // Zoom Controls
        btnZoomIn.setOnClickListener(v -> {
            float cur = adapter.getTextSize();
            if (cur < 26f) {
                adapter.setTextSize(cur + 2f);
                Toast.makeText(this, "अक्षर मोठे केले", Toast.LENGTH_SHORT).show();
            }
        });

        btnZoomOut.setOnClickListener(v -> {
            float cur = adapter.getTextSize();
            if (cur > 13f) {
                adapter.setTextSize(cur - 2f);
                Toast.makeText(this, "अक्षर लहान केले", Toast.LENGTH_SHORT).show();
            }
        });

        // Quick Scroll Navigation Chips
        chipGanapati.setOnClickListener(v -> scrollToPosition(0));
        chipShankar.setOnClickListener(v -> scrollToPosition(1));
        chipDurga.setOnClickListener(v -> scrollToPosition(2));
        chipVitthal.setOnClickListener(v -> scrollToPosition(3));
        chipDatta.setOnClickListener(v -> scrollToPosition(4));
        chipYeiHoVitthale.setOnClickListener(v -> scrollToPosition(5));
        chipDnyanraja.setOnClickListener(v -> scrollToPosition(6));
        chipTukaram.setOnClickListener(v -> scrollToPosition(7));
        chipLotangan.setOnClickListener(v -> scrollToPosition(8));
        chipMantra.setOnClickListener(v -> scrollToPosition(9));
        chipPasaydan.setOnClickListener(v -> scrollToPosition(10));
    }

    private void scrollToPosition(int pos) {
        if (pos >= 0 && pos < aartiList.size()) {
            layoutManager.scrollToPositionWithOffset(pos, 0);
        }
    }

    private void loadAllAartis() {
        aartiList.clear();

        // 1. गणपतीची आरती
        aartiList.add(new AartiItem(
                "ganapati",
                "🐘",
                "श्री गणपतीची आरती",
                "सुखकर्ता दुखहर्ता वार्ता विघ्नाची ।\n" +
                "नुरवी पुरवी प्रेम कृपा जयाची ।\n" +
                "सर्वांगी सुंदर उटी शेंदुराची ।\n" +
                "कंठी झळके माळ मुक्ताफळांची ॥ १ ॥\n\n" +
                "जय देव जय देव जय मंगलमूर्ती ।\n" +
                "दर्शनमात्रे मनकामना पुरती ॥ धृ. ॥\n\n" +
                "रत्नखचित फरा तुज गौरीकुमरा ।\n" +
                "चंदनाची उटी कुंकुमकेशरा ।\n" +
                "हिरे जडित मुकुट शोभतो बरा ।\n" +
                "रुणझुणती नूपुरे चरणी घागरिया ॥ जय देव. ॥ २ ॥\n\n" +
                "लंबोदर पीतांबर फणी वरवंदना ।\n" +
                "सरळ सोंड वक्रतुंड त्रिनयना ।\n" +
                "दास रामाचा वाट पाहे सदना ।\n" +
                "संकटी पावावे निर्वाणी रक्षावे सुरवंदना ॥\n\n" +
                "जय देव जय देव जय मंगलमूर्ती ।\n" +
                "दर्शनमात्रे मनकामना पुरती ॥ दर्शन० ॥ ३ ॥"
        ));

        // 2. शंकराची आरती (लवथवती विक्राळा)
        aartiList.add(new AartiItem(
                "shankar",
                "🔱",
                "लवथवती विक्राळा (श्री शंकराची आरती)",
                "लवथवती विक्राळा ब्रह्मांडी माळा ।\n" +
                "विषें कंठी काळा त्रिनेत्रीं ज्वाळां ॥\n" +
                "लावण्यसुंदर मस्तकीं बाळा ।\n" +
                "तेथुनिया जळ निर्मळ वाहे झुळझुळां ॥ १ ॥\n\n" +
                "जय देव जय देव जय श्रीशंकरा ।\n" +
                "आरती ओवाळू तुज कर्पुरगौरा ॥ धृ. ॥\n\n" +
                "कर्पुरगौरा भोळा नयनीं विशाळा ।\n" +
                "अर्धांगीं पार्वती सुमनांच्या माळा ।\n" +
                "विभुतीचें उधळण शितिकंठ नीळा ।\n" +
                "ऐसा शंकर शोभे उमावेल्हाळा ॥ जय देव. ॥ २ ॥\n\n" +
                "देवी दैत्यीं सागरमंथन पैं केलें ।\n" +
                "त्यामाजीं जें अवचित हळाहळ उठिलें ।\n" +
                "तें त्वां असुरपणें प्राशन केलें ।\n" +
                "नीळकंठ नाम प्रसिद्ध झालें ॥ जय देव. ॥ ३ ॥\n\n" +
                "व्याघ्रांबर फणिवरधर सुंदर मदनारी ।\n" +
                "पंचानन मनमोहन मुनिजनसुखकारी ।\n" +
                "शतकोटीचें बीज वाचे उच्चारी ।\n" +
                "रघुकुलतिलक रामदासा अंतरी ॥\n\n" +
                "जय देव जय देव जय श्रीशंकरा ॥ ४ ॥"
        ));

        // 3. श्री दुर्गादेवीची आरती
        aartiList.add(new AartiItem(
                "durga",
                "🦁",
                "श्री दुर्गादेवीची आरती (दुर्गे दुर्घट भारी)",
                "दुर्गे दुर्घट भारी तुजविण संसारी ।\n" +
                "अनाथनाथे अंबे करुणा विस्तारी ॥\n" +
                "वारी वारीं जन्ममरणाते वारी ।\n" +
                "हारी पडलो आता संकट नीवारी ॥ १ ॥\n\n" +
                "जय देवी जय देवी जय महिषासुरमथनी ।\n" +
                "सुरवरईश्वरवरदे तारक संजीवनी ॥ धृ. ॥\n\n" +
                "त्रिभुवनी भुवनी पाहतां तुज से नाही ।\n" +
                "चारी श्रमले परंतु न बोलावे काहीँ ॥\n" +
                "साही विवाद करितां पडिले प्रवाही ।\n" +
                "ते तूं भक्तालागी पावसि लवलाही ॥ २ ॥\n\n" +
                "प्रसन्न वदने प्रसन्न होसी निजदासां ।\n" +
                "क्लेशापासूनी सोडी तोडी भवपाशा ॥\n" +
                "अंबे तुजवांचून कोण पुरविल आशा ।\n" +
                "नरहरि तल्लिन झाला पदपंकजलेशा ॥ ३ ॥"
        ));

        // 4. श्री विठ्ठल आरती
        aartiList.add(new AartiItem(
                "vitthal",
                "🚩",
                "श्री विठ्ठल आरती (युगे अठ्ठावीस)",
                "युगे अठ्ठावीस विटेवरी ऊभा ।\n" +
                "वामांगी रखुमाई दिसे दिव्य शोभा ।\n" +
                "पुंडलिकाचे भेटी परब्रह्म आलें गा ।\n" +
                "चरणी वाहे भीमा उद्धारी जगा ॥ १ ॥\n\n" +
                "जय देव जय देव जय पांडुरंगा ।\n" +
                "रखुमाईवल्लभा राईच्या वल्लभा पावे जिवलगा ॥ धृ. ॥\n\n" +
                "तुळसी माळा गळा कर ठेवूनी कटी ।\n" +
                "कांसे पीतांबर कस्तुरी लल्लाटी ।\n" +
                "देव सुरवर नित्य येती भेटी ।\n" +
                "गुरूड़ हनुमंत पुढे उभे राहती ॥ जय देव. ॥ २ ॥\n\n" +
                "धन्य वेणुनाद अनुक्षेत्रपाळा ।\n" +
                "सुवर्णाची कमळे वनमाळा गळा ।\n" +
                "राई रखुमाबाई राणीया सकळा ।\n" +
                "ओवळिती राजा विठोबा सावळा ॥ जय देव. ॥ ३ ॥\n\n" +
                "ओवाळू आरत्या कर्वड्या येती ।\n" +
                "चंद्रभागेमाजी सोडुनियां देती ।\n" +
                "दिंड्या पताका वैष्णव नाचती ।\n" +
                "पंढरीचा महिमा वर्णावा किती ॥ जय देव. ॥ ४ ॥\n\n" +
                "आषाढी कार्तिकी भक्तजन येती ।\n" +
                "चंद्रभागेमध्यें स्नाने जे करिती ॥\n" +
                "दर्शनहेळामात्रे तया होय मुक्ती ।\n" +
                "केशवासी नामदेव भावे ऑवळिती ॥\n\n" +
                "जय देव जय देव ॥ ५ ॥"
        ));

        // 5. श्री दत्ताची आरती
        aartiList.add(new AartiItem(
                "datta",
                "🪔",
                "श्री दत्ताची आरती (त्रिगुणात्मक त्रैमूर्ती)",
                "त्रिगुणात्मक त्रैमूर्ती दत्त हा जाणा ।\n" +
                "त्रिगुणी अवतार त्रैलोक्य राणा ।\n" +
                "नेती नेती शब्द न ये अनुमाना ॥\n" +
                "सुरवर मुनिजन योगी समाधी न ये ध्याना ॥ १ ॥\n\n" +
                "जय देव जय देव जय श्री गुरुदत्ता ।\n" +
                "आरती ओवाळिता हरली भवचिंता ॥ धृ ॥\n\n" +
                "सबाह्य अभ्यंतरी तू एक दत्त ।\n" +
                "अभाग्यासी कैची कळेल हि मात ॥\n" +
                "पराही परतली तेथे कैचा हेत ।\n" +
                "जन्ममरणाचाही पुरलासे अंत ॥ २ ॥\n\n" +
                "दत्त येऊनियां ऊभा ठाकला ।\n" +
                "भावे साष्टांगेसी प्रणिपात केला ॥\n" +
                "प्रसन्न होऊनि आशिर्वाद दिधला ।\n" +
                "जन्ममरणाचा फेरा चुकवीला ॥ ३ ॥\n\n" +
                "दत्त दत्त ऐसें लागले ध्यान ।\n" +
                "हरपले मन झाले उन्मन ॥\n" +
                "मी तू पणाची झाली बोळवण ।\n" +
                "एका जनार्दनी श्रीदत्तध्यान ॥ ४ ॥"
        ));

        // 6. येई हो विठ्ठले (दत्ताच्या आरतीनंतर १)
        aartiList.add(new AartiItem(
                "yei_ho_vitthale",
                "🌸",
                "येई हो विठ्ठले",
                "येई हो विठ्ठले माझे माऊली ये ॥\n" +
                "निढळावरी कर ठेऊनी वाट मी पाहे ॥ धृ. ॥\n\n" +
                "आलिया गेलीया हातीं धाडी निरोप ॥\n" +
                "पंढरपुरी आहे माझा मायबाप ॥\n" +
                "येई हो विठ्ठले माझे माऊली ये..\n\n" +
                "पिंवळा पीतांबर कैसा गगनी झळकला ॥\n" +
                "गरुडावरी बैसून माझा कैवारी आला ॥\n" +
                "येई हो विठ्ठले माझे माऊली ये ..\n\n" +
                "विठोबाचे राज आम्हां नित्य दिपवाळी ॥\n" +
                "विष्णुदास नामा जीवेंभावे ओंवाळी ॥\n" +
                "येई हो विठ्ठले माझे माऊली ये.."
        ));

        // 7. आरती ज्ञानराजा (दत्ताच्या आरतीनंतर २)
        aartiList.add(new AartiItem(
                "dnyanraja",
                "📖",
                "आरती ज्ञान राजा",
                "आरती ज्ञानराजा\n" +
                "महाकैवल्यतेजा\n" +
                "सेविती साधुसंत\n" +
                "मनु वेधला माझा\n" +
                "आरती ज्ञानराजा ॥ धृ०\n\n" +
                "लोपलें ज्ञान जगीं\n" +
                "हित नेणती कोणी\n" +
                "अवतार पांडुरंग\n" +
                "नाम ठेविलें ज्ञानी ॥ आरती ॥\n\n" +
                "कनकाचे ताट करीं\n" +
                "उभ्या गोपिका नारी\n" +
                "नारद तुंबरु हो\n" +
                "साम गायन करी ॥ आरती ॥\n\n" +
                "प्रगट गुह्य बोले\n" +
                "विश्व ब्रह्माचे केलें\n" +
                "नामा जनार्दनी\n" +
                "पायीं मस्तक ठेविलें ॥ आरती ॥"
        ));

        // 8. संत तुकाराम आरती (दत्ताच्या आरतीनंतर ३)
        aartiList.add(new AartiItem(
                "tukaram",
                "🚩",
                "संत तुकाराम आरती",
                "आरती तुकारामा\n" +
                "स्वामी सद्गुरुधामा\n" +
                "सच्चिदानंद मूर्ती\n" +
                "पाय दाखवी आम्हा\n" +
                "आरती तुकारामा ॥ धृ. ॥\n\n" +
                "राघवे सागरात\n" +
                "पाषाण तारीले\n" +
                "तैसे हे तुकोबाचे\n" +
                "अभंग उदकी रक्षियेले\n" +
                "आरती तुकारामा ...॥ १ ॥\n\n" +
                "तुकिता तुलनेशी\n" +
                "ब्रह्म तुकासी आले\n" +
                "म्हणोनि रामेश्वरे\n" +
                "चरणी मस्तक ठेविले\n" +
                "आरती तुकारामा ...॥ २ ॥\n\n" +
                "स्वामी सद्गुरुधामा\n" +
                "सच्चिदानंद मूर्ती\n" +
                "पाय दाखवी आम्हा\n" +
                "आरती तुकारामा ...॥"
        ));

        // 9. घालीन लोटांगण (दत्ताच्या आरतीनंतर ४)
        aartiList.add(new AartiItem(
                "lotangan",
                "🙏",
                "घालीन लोटांगण",
                "घालीन लोटांगण, वंदीन चरण ।\n" +
                "डोळ्यांनी पाहीन रुप तुझें ।\n" +
                "प्रेमें आलिंगन, आनंदे पूजिन ।\n" +
                "भावें ओवाळीन म्हणे नामा ॥ १ ॥\n\n" +
                "त्वमेव माता च पिता त्वमेव ।\n" +
                "त्वमेव बंधुश्च सखा त्वमेव ।\n" +
                "त्वमेव विद्या द्रविणं त्वमेव ।\n" +
                "त्वमेव सर्वं मम देवदेव ॥ २ ॥\n\n" +
                "कायेन वाचा मनसेंद्रीयेव्रा, बुद्ध्यात्मना वा प्रकृतिस्वभावात ।\n" +
                "करोमि यध्यत सकलं परस्मे, नारायणायेति समर्पयामि ॥ ३ ॥\n\n" +
                "अच्युतं केशवं रामनारायणं कृष्णदामोदरं वासुदेवं हरिम ।\n" +
                "श्रीधरं माधवं गोपिकावल्लभं, जानकीनायकं रामचंद्र भजे ॥ ४ ॥\n\n" +
                "हरे राम हर राम, राम राम हरे हरे ।\n" +
                "हरे कृष्ण हरे कृष्ण, कृष्ण कृष्ण हरे हरे ।"
        ));

        // 10. मंत्रपुष्पांजली
        aartiList.add(new AartiItem(
                "mantra",
                "🌿",
                "मंत्रपुष्पांजली",
                "ॐ यज्ञेन यज्ञमयजन्त देवास्तानि धर्माणि प्रथमान्यासन् ।\n" +
                "ते ह नाकं महिमानः सचन्त यत्र पूर्वे साध्याः सन्ति देवाः ॥\n\n" +
                "ॐ राजाधिराजाय प्रसह्यसाहिने ।\n" +
                "नमो वयं वैश्रवणाय कुर्महे ।\n" +
                "स मे कामान् कामकामाय मह्यम् ।\n" +
                "कामेश्वरो वैश्रवणो ददातु ।\n" +
                "कुबेराय वैश्रवणाय । महाराजाय नमः ॥\n\n" +
                "ॐ स्वस्ति । साम्राज्यं भौज्यं स्वाराज्यं वैराज्यं पारमेष्ठ्यं राज्यं महाराज्यमाधिपत्यमयं समन्तपर्यायी स्यात् सार्वभौमः सार्वायुष आन्तादापरार्धात् पृथिव्यै समुद्रपर्यन्ताया एकराळिति ॥\n\n" +
                "तदप्येष श्लोकोऽभिगीतो मरुतः परिवेष्टारो मरुत्तस्यावसन् गृहे ।\n" +
                "आविक्षितस्य कामप्रेर्विश्वेदेवाः सभासद इति ॥\n\n" +
                "एकदन्ताय विद्महे वक्रतुण्डाय धीमहि ।\n" +
                "तन्नो दन्तिः प्रचोदयात् ॥\n\n" +
                "ॐ शान्तिः शान्तिः शान्तिः ॥"
        ));

        // 11. पसायदान
        aartiList.add(new AartiItem(
                "pasaydan",
                "✨",
                "पसायदान (संत ज्ञानेश्वर महाराज)",
                "आता विश्वात्मकें देवें । येणे वाग्यज्ञें तोषावें ।\n" +
                "तोषोनि मज द्यावें । पसायदान हें ॥ १ ॥\n\n" +
                "जे खळांची व्यंकटी सांडो । तयां सत्कर्मीं रती वाढो ।\n" +
                "भूतां परस्परें पडो । मैत्र जीवाचें ॥ २ ॥\n\n" +
                "दुरितांचें तिमिर जावो । विश्व स्वधर्म सूर्यें पाहो ।\n" +
                "जो जे वांछील तो तें लाहो । प्राणिजात ॥ ३ ॥\n\n" +
                "वर्षत सकळ मंगळीं । ईश्वरनिष्ठांची मांदियाळी ।\n" +
                "अनवरत भूमंडळीं । भेटतु भूतां ॥ ४ ॥\n\n" +
                "हे जलद ज्ञानरसें भरले । सकळ लोक सुखावले ।\n" +
                "संतचरणीं लीन झाले । सर्व प्राणी ॥ ५ ॥\n\n" +
                "किंबहुना सर्व सुखी । पूर्ण होऊनि तिन्हीं लोकीं ।\n" +
                "भजिजो आदिपुरुखीं । अखंडित ॥ ६ ॥\n\n" +
                "आणि ग्रंथोपजीविये । विशेषीं लोकीं इयें ।\n" +
                "दृष्टादृष्ट विजयें । होआवे जी ॥ ७ ॥\n\n" +
                "येथ म्हणे श्रीविश्वेशरावो । हा होईल दानपसावो ।\n" +
                "येणें वरें ज्ञानदेवो । सुखिया जाहला ॥ ८ ॥"
        ));
    }
}
