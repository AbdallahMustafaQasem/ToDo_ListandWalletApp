package com.abdallah.taskvault.ui.common

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abdallah.taskvault.R
import java.util.Locale

@Composable
fun VoiceInputButton(
    onResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    isListening: Boolean = false
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            matches?.firstOrNull()?.let { onResult(it) }
        }
    }

    val pulse by rememberInfiniteTransition(label = "micPulse").animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "micScale"
    )

    IconButton(
        onClick = {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.voice_input_prompt))
            }
            launcher.launch(intent)
        },
        modifier = modifier.scale(if (isListening) pulse else 1f)
    ) {
        Icon(
            Icons.Default.Mic,
            contentDescription = stringResource(R.string.voice_input_cd),
            modifier = Modifier.size(24.dp),
            tint = if (isListening) MaterialTheme.colorScheme.error
                   else MaterialTheme.colorScheme.primary
        )
    }
}
