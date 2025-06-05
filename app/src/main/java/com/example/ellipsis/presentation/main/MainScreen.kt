package com.example.ellipsis.presentation.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // 수정: Hilt 사용

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) { // 수정: hiltViewModel() 사용
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var hashtags by remember { mutableStateOf(TextFieldValue()) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            viewModel.onImageSelected(it, context)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("봄날 음악 추천 (ML Kit)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("사진 선택")
        }

        state.bitmap?.let { bmp ->
            Spacer(modifier = Modifier.height(8.dp))
            Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.height(200.dp).fillMaxWidth())

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                imageUri?.let {
                    viewModel.recommendMusic(context, it, hashtags.text)
                }
            }) {
                Text("객체 인식 및 음악 추천")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("해시태그 입력")
        BasicTextField(
            value = hashtags,
            onValueChange = { hashtags = it },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("촬영 시간: ${state.photoDate}")
        Text("촬영 위치: ${state.locationName}")
        Text("인식된 객체: ${state.detectedObjects.joinToString()}", color = MaterialTheme.colorScheme.secondary)

        if (state.recommendedSongs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("추천 음악:", style = MaterialTheme.typography.titleMedium)
            state.recommendedSongs.forEach { song ->
                Text("🎵 $song")
            }
        }

        state.errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}