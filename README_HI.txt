MY JARVIS AI — Android प्रोजेक्ट (v1.0)

इस ZIP में पूरा Android Studio प्रोजेक्ट सोर्स है। यह अभी debug APK बनाने के लिए तैयार है।

इस संस्करण में:
- Native Android Hindi speech recognition (फोन की speech recognition सेवा पर निर्भर)
- हिंदी Text-to-Speech
- समय और तारीख के जवाब
- Google और YouTube खोलना
- माइक्रोफोन अनुमति

अभी शामिल नहीं:
- ChatGPT/क्लाउड AI दिमाग (इसके लिए सुरक्षित backend/API key चाहिए)
- हमेशा बैकग्राउंड में wake word सुनना
- टॉर्च/कॉल/SMS/अन्य सिस्टम कंट्रोल
- रिलीज़ signing key

APK बनाने के दो तरीके:

A) Android Studio:
1. ZIP extract करें।
2. Android Studio में extracted MyJarvisAI folder खोलें।
3. Gradle sync पूरा होने दें (JDK 17 और Android SDK 35 चाहिए)।
4. Build > Build Bundle(s) / APK(s) > Build APK(s) चुनें।
5. APK यहाँ मिलेगा: app/build/outputs/apk/debug/app-debug.apk

B) केवल Android मोबाइल से GitHub Actions:
1. GitHub पर नया खाली repository बनाएँ।
2. ZIP को extract करके अंदर की सभी फाइलें repository में upload/commit करें।
3. Repository में Actions tab खोलें।
4. "Build MY JARVIS AI APK" workflow चुनकर Run workflow दबाएँ।
5. Build पूरा होने पर workflow run के Artifacts में MY-JARVIS-AI-debug-apk डाउनलोड करें।
6. ZIP artifact खोलें; उसमें app-debug.apk मिलेगा। Android में install करते समय अनुमति दें।

महत्वपूर्ण:
- यह debug APK होगा, Play Store release APK नहीं।
- Speech recognition के लिए फोन में Google/अन्य speech service और इंटरनेट की जरूरत हो सकती है।
- पहली बार ऐप माइक्रोफोन permission माँगेगा।
- AI chat जोड़ने के लिए API key को ऐप में hardcode न करें; secure backend चाहिए।
