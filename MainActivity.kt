package com.example.jarvis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var status: TextView
    private lateinit var chat: TextView
    private lateinit var input: EditText
    private lateinit var tts: TextToSpeech
    private var recognizer: SpeechRecognizer? = null
    private val cyan = 0xFF39D9FF.toInt()
    private val dark = 0xFF061321.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = dark
        window.navigationBarColor = dark
        tts = TextToSpeech(this, this)
        buildInterface()
        requestAudioPermission()
    }

    private fun buildInterface() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(22, 24, 22, 18)
            setBackgroundColor(dark)
        }

        val title = TextView(this).apply {
            text = "J.A.R.V.I.S"
            textSize = 30f
            letterSpacing = 0.22f
            setTextColor(cyan)
            gravity = Gravity.CENTER
        }
        root.addView(title, match(WRAP_CONTENT))

        status = TextView(this).apply {
            text = "SYSTEM ONLINE  •  READY"
            textSize = 12f
            letterSpacing = 0.12f
            setTextColor(0xFF8BEAFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        }
        root.addView(status, match(WRAP_CONTENT))

        val orb = TextView(this).apply {
            text = "◉"
            textSize = 100f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 12)
        }
        root.addView(orb, match(WRAP_CONTENT))

        val heading = TextView(this).apply {
            text = "PERSONAL AI ASSISTANT"
            textSize = 13f
            letterSpacing = 0.14f
            setTextColor(0xFFB8EFFF.toInt())
            gravity = Gravity.CENTER
        }
        root.addView(heading, match(WRAP_CONTENT))

        chat = TextView(this).apply {
            text = "JARVIS: Good day. Systems are ready. How may I assist you?"
            textSize = 15f
            setTextColor(0xFFE5F8FF.toInt())
            setPadding(14, 16, 14, 16)
            setBackgroundColor(0xFF0D263A.toInt())
        }
        val scroll = ScrollView(this)
        scroll.addView(chat)
        val chatParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ).apply { setMargins(0, 18, 0, 12) }
        root.addView(scroll, chatParams)

        input = EditText(this).apply {
            hint = "Ask JARVIS something..."
            setHintTextColor(0xFF7795A8.toInt())
            setTextColor(-1)
            textSize = 16f
            setSingleLine(true)
            setPadding(14, 12, 14, 12)
            setBackgroundColor(0xFF102D42.toInt())
        }
        root.addView(input, match(WRAP_CONTENT))

        val send = makeButton("SEND COMMAND") { submitText() }
        root.addView(send, buttonParams())

        val speak = makeButton("🎙  TAP TO SPEAK") { startListening() }
        root.addView(speak, buttonParams())

        val voice = makeButton("VOICE RESPONSE: ON") {
            voiceEnabled = !voiceEnabled
            it.text = if (voiceEnabled) "VOICE RESPONSE: ON" else "VOICE RESPONSE: OFF"
        }
        root.addView(voice, buttonParams())

        setContentView(root)
    }

    private var voiceEnabled = true

    private fun makeButton(label: String, action: (Button) -> Unit): Button {
        return Button(this).apply {
            text = label
            setTextColor(cyan)
            setBackgroundColor(0xFF12334B.toInt())
            textSize = 13f
            setOnClickListener { action(this) }
        }
    }

    private fun match(height: Int) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, height
    )

    private fun buttonParams() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, WRAP_CONTENT
    ).apply { setMargins(0, 5, 0, 5) }

    private fun requestAudioPermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 10)
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            reply("Speech recognition is not available on this device.")
            return
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermission()
            reply("Please allow microphone permission first.")
            return
        }

        status.text = "LISTENING..."
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { status.text = "LISTENING..." }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { status.text = "PROCESSING..." }
            override fun onError(error: Int) {
                status.text = "READY"
                reply("I couldn't catch that. Please try again.")
            }
            override fun onResults(results: Bundle?) {
                val words = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val command = words?.firstOrNull().orEmpty()
                input.setText(command)
                status.text = "COMMAND RECEIVED"
                processCommand(command)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to JARVIS")
        }
        recognizer?.startListening(intent)
    }

    private fun submitText() {
        val command = input.text.toString().trim()
        if (command.isNotEmpty()) processCommand(command)
    }

    private fun processCommand(raw: String) {
        val command = raw.lowercase(Locale.getDefault())
        when {
            command.contains("hello") || command.contains("hi jarvis") ->
                reply("Hello. JARVIS is online and ready.")
            command.contains("time") ->
                reply("The current time is " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()))
            command.contains("date") ->
                reply("Today is " + SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date()))
            command.contains("open youtube") -> openUrl("https://www.youtube.com")
            command.contains("open google") -> openUrl("https://www.google.com")
            command.contains("open instagram") -> openUrl("https://www.instagram.com")
            command.startsWith("search ") -> {
                val query = raw.substringAfter("search ", "")
                openUrl("https://www.google.com/search?q=" + Uri.encode(query))
            }
            command.contains("who are you") ->
                reply("I am JARVIS, your Android personal assistant.")
            command.contains("help") ->
                reply("Try: what time is it, what is the date, open YouTube, open Google, open Instagram, or search followed by a topic.")
            else ->
                reply("I can handle basic phone commands right now. Connect an AI backend to enable advanced open-ended conversations.")
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            reply("Opening your request.")
        } catch (e: Exception) {
            reply("I couldn't open that link.")
        }
    }

    private fun reply(message: String) {
        chat.text = "JARVIS: $message"
        status.text = "SYSTEM ONLINE  •  READY"
        if (voiceEnabled && ::tts.isInitialized) tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply")
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
        }
    }

    override fun onDestroy() {
        recognizer?.destroy()
        if (::tts.isInitialized) tts.shutdown()
        super.onDestroy()
    }

    companion object {
        private const val WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT
    }
}
