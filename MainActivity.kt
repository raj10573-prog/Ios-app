package com.myjarvis.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var webView: WebView
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private val requestAudio = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(JarvisBridge(), "Jarvis")
        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("hi", "IN"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun sendToPage(functionCall: String) {
        runOnUiThread { webView.evaluateJavascript(functionCall, null) }
    }

    private fun beginListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), requestAudio)
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            sendToPage("onJarvisError('इस फोन में वॉइस पहचान सेवा उपलब्ध नहीं है।')")
            return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { sendToPage("onJarvisStatus('सुन रहा हूँ सर...')") }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { sendToPage("onJarvisStatus('आवाज़ समझ रहा हूँ...')") }
            override fun onError(error: Int) { sendToPage("onJarvisError('आवाज़ नहीं समझ आई। फिर कोशिश करें।')") }
            override fun onResults(results: Bundle?) {
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (spoken != null) sendToPage("onJarvisResult(${org.json.JSONObject.quote(spoken)})")
                else sendToPage("onJarvisError('कोई आवाज़ नहीं मिली।')")
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun handleCommand(raw: String) {
        val c = raw.lowercase(Locale.ROOT)
        val answer = when {
            "समय" in c -> "सर, अभी समय है " + SimpleDateFormat("hh:mm a", Locale("hi", "IN")).format(Date())
            "तारीख" in c || "दिनांक" in c -> "आज " + SimpleDateFormat("EEEE, d MMMM yyyy", Locale("hi", "IN")).format(Date()) + " है।"
            "गूगल" in c -> { openWeb("https://www.google.com"); "सर, गूगल खोल रहा हूँ।" }
            "यूट्यूब" in c -> { openWeb("https://www.youtube.com"); "सर, यूट्यूब खोल रहा हूँ।" }
            "नमस्ते" in c || "हेलो" in c || "जार्विस" in c -> "नमस्ते सर! मैं आपका जार्विस हूँ। बताइए, क्या करना है?"
            "तुम कौन" in c || "नाम" in c -> "मेरा नाम माय जार्विस एआई है। मैं आपका हिंदी वॉइस असिस्टेंट हूँ।"
            "धन्यवाद" in c || "शुक्रिया" in c -> "आपका स्वागत है सर।"
            else -> "माफ कीजिए सर, यह कमांड अभी उपलब्ध नहीं है।"
        }
        speak(answer)
    }

    private fun openWeb(url: String) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {}
    }

    private fun speak(text: String) {
        sendToPage("showJarvisAnswer(${org.json.JSONObject.quote(text)})")
        if (ttsReady) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply")
    }

    inner class JarvisBridge {
        @JavascriptInterface fun startListening() { runOnUiThread { beginListening() } }
        @JavascriptInterface fun speak(text: String) { runOnUiThread { this@MainActivity.speak(text) } }
        @JavascriptInterface fun command(text: String) { runOnUiThread { handleCommand(text) } }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestAudio && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) beginListening()
        else sendToPage("onJarvisError('माइक्रोफोन अनुमति नहीं मिली।')")
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        webView.destroy()
        super.onDestroy()
    }
}
