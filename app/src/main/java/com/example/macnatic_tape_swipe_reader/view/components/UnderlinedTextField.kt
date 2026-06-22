package com.example.macnatic_tape_swipe_reader.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UnderlinedTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = Color(0xFF475569),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        val textFieldModifier = if (onClick != null) {
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .clickable { onClick() }
        } else {
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly || (onClick != null),
            enabled = (onClick == null),
            textStyle = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily.Default,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Normal
            ),
            modifier = textFieldModifier,
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = if (onClick != null) "Select $label" else "Enter $label",
                                color = Color(0xFF94A3B8),
                                fontSize = 15.sp
                            )
                        }
                        if (onClick != null) {
                            Text(
                                text = value,
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp
                            )
                        } else {
                            innerTextField()
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF94A3B8))
                    )
                }
            }
        )
    }
}
