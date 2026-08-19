package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.engine.VoiceMathParser
import java.util.Locale

@Composable
fun VoiceInputDialog(
    onDismiss: () -> Unit,
    onMathResult: (String) -> Unit
) {
    val context = LocalContext.current
    var spokenText by remember { mutableStateOf("") }
    var parsedMath by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Tap the microphone and speak your calculation") }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.35f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Speech Recognizer reference
    var speechRecognizer: SpeechRecognizer? by remember { mutableStateOf(null) }

    val startListening = {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            isListening = true
                            statusMessage = "Listening... Speak naturally (e.g., 'twenty five plus seventy four')"
                        }
                        override fun onBeginningOfSpeech() {
                            statusMessage = "Processing your speech..."
                        }
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            isListening = false
                        }
                        override fun onError(error: Int) {
                            isListening = false
                            statusMessage = "Could not understand. Tap mic to try again."
                        }
                        override fun onResults(results: Bundle?) {
                            isListening = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val topMatch = matches[0]
                                spokenText = topMatch
                                parsedMath = VoiceMathParser.parseSpokenTextToMath(topMatch)
                                statusMessage = "Speech converted to expression successfully!"
                            }
                        }
                        override fun onPartialResults(partialResults: Bundle?) {
                            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!partial.isNullOrEmpty()) {
                                spokenText = partial[0]
                                parsedMath = VoiceMathParser.parseSpokenTextToMath(partial[0])
                            }
                        }
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })

                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a mathematical calculation...")
                    }
                    startListening(intent)
                }
            } catch (e: Exception) {
                isListening = false
                statusMessage = "Voice error: ${e.localizedMessage}"
            }
        } else {
            statusMessage = "Speech recognition is not available on this device."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            statusMessage = "Audio recording permission is required for voice calculation."
        }
    }

    val onMicClick = {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
        } else {
            val permissionCheck = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                startListening()
            } else {
                permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Voice Math Input",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Microphone Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    if (isListening) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(pulseScale)
                        ) {}
                    }

                    IconButton(
                        onClick = onMicClick,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            .testTag("voice_record_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Spoken Words Preview
                if (spokenText.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "You said:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "\"$spokenText\"",
                                fontSize = 14.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Converted Expression:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = parsedMath,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Example Spoken Phrases Chips
                Text(
                    text = "Try saying:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val examples = listOf(
                        "square root of 144 plus 25" to "sqrt(144)+25",
                        "sin of 90 degrees plus 50" to "sin(90)+50",
                        "twenty five percent of eight hundred" to "25% * 800"
                    )
                    examples.forEach { (phrase, math) ->
                        SuggestionChip(
                            onClick = {
                                spokenText = phrase
                                parsedMath = math
                            },
                            label = { Text(phrase, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedMath.isNotEmpty()) {
                        onMathResult(parsedMath)
                    } else if (spokenText.isNotEmpty()) {
                        onMathResult(VoiceMathParser.parseSpokenTextToMath(spokenText))
                    }
                    onDismiss()
                },
                enabled = parsedMath.isNotEmpty() || spokenText.isNotEmpty(),
                modifier = Modifier.testTag("insert_voice_math_button")
            ) {
                Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Insert into Calculator")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
