package com.awada.synapse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun UIAI(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        UIAIChat(modifier = Modifier.fillMaxSize())
        UIAIMain(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}
