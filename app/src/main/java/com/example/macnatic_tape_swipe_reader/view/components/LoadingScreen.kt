package com.example.macnatic_tape_swipe_reader.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF009688), // Teal primary color
                strokeWidth = 4.dp,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "กำลังอ่านข้อมูล…",
                color = Color(0xFF475569),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
