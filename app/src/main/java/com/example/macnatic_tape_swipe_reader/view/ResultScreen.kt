package com.example.macnatic_tape_swipe_reader.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macnatic_tape_swipe_reader.features.msr.models.ThaiDrivingLicense
import com.example.macnatic_tape_swipe_reader.view.components.MsrToolbar
import com.example.macnatic_tape_swipe_reader.view.components.ProfileAvatarPlaceholder
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ResultScreen(
    licenseData: ThaiDrivingLicense?,
    onBackClick: () -> Unit,
    onRescan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isRawDataExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MsrToolbar(
                title = "e-Driver License Reader",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Symmetrical Faint Red Concentric Circle Watermark Stamp (matches passport layout)
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .border(width = 3.dp, color = Color(0xFFEF4444).copy(alpha = 0.05f), shape = CircleShape)
                    .padding(8.dp)
                    .border(width = 1.dp, color = Color(0xFFEF4444).copy(alpha = 0.05f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = Color(0xFFEF4444).copy(alpha = 0.04f),
                    modifier = Modifier.size(130.dp)
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Profile Avatar / Picture Placeholder (Centered)
                ProfileAvatarPlaceholder(licenseData?.photo)

                if (licenseData != null) {
                    // Symmetrical key-value list centered details (symmetrical columns matching image)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CenteredGridRow(
                            label = "Citizen ID",
                            value = licenseData.citizenId.ifEmpty { "Not Found" }
                        )
                        CenteredGridRow(
                            label = "First name",
                            value = licenseData.firstNameEn.ifEmpty { "Not Found" }
                        )
                        CenteredGridRow(
                            label = "Last name",
                            value = licenseData.lastNameEn.ifEmpty { "Not Found" }
                        )
                        CenteredGridRow(
                            label = "License Number",
                            value = licenseData.licenseNumber.ifEmpty { "Not Found" }
                        )
                        CenteredGridRow(
                            label = "Date of Birth",
                            value = licenseData.birthDate.ifEmpty { "Not Found" }
                        )
                        CenteredGridRow(
                            label = "Expiration Date",
                            value = licenseData.expiryDate.ifEmpty { "Not Found" }
                        )
                        CenteredGridRow(
                            label = "License Type",
                            value = licenseData.licenseType.ifEmpty { "Not Found" }
                        )
                    }

                    if (!licenseData.isParsedSuccessfully) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = licenseData.parsingErrorMessage ?: "ไม่สามารถตรวจสอบข้อมูลได้",
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Raw Tracks Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isRawDataExpanded = !isRawDataExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF009688),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "ข้อความแทร็กแถบแม่เหล็กดิบ",
                                        color = Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    imageVector = if (isRawDataExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = isRawDataExpanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val rawText = buildString {
                                        append("TRACK 1: ").append(licenseData.rawTrack1.ifEmpty { "[Empty]" }).append("\n\n")
                                        append("TRACK 2: ").append(licenseData.rawTrack2.ifEmpty { "[Empty]" }).append("\n\n")
                                        append("TRACK 3: ").append(licenseData.rawTrack3.ifEmpty { "[Empty]" })
                                    }
                                    Text(
                                        text = rawText,
                                        color = Color(0xFF334155),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                            .verticalScroll(rememberScrollState())
                                            .padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Premium Back button to go back to scanning screen
                Button(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009688), // Teal primary button
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "กลับไปหน้าเริ่ม",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CenteredGridRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label (Right-aligned column)
        Text(
            text = label,
            color = Color(0xFF475569),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )

        // Value (Left-aligned column)
        Text(
            text = value,
            color = Color(0xFF0F172A),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(1.2f)
                .padding(start = 16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
fun ResultScreenPreview() {
    MaterialTheme {
        ResultScreen(
            licenseData = ThaiDrivingLicense(
                citizenId = "1234567890123",
                licenseNumber = "12345678",
                firstNameEn = "SATHEANPONG",
                lastNameEn = "JEUNGUDOMPORN",
                fullNameEn = "MR. SATHEANPONG JEUNGUDOMPORN",
                birthDate = "1990-12-22",
                expiryDate = "2026-12-22",
                licenseType = "Personal Car",
                rawTrack1 = "%MR SATHEANPONG JEUNGUDOMPORN^1234567890123?",
                rawTrack2 = ";1234567890123=22122026?",
                isParsedSuccessfully = true
            ),
            onBackClick = {},
            onRescan = {}
        )
    }
}
