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
    val clipboardManager = LocalClipboardManager.current

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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ตรวจสอบข้อมูลการสแกน",
                    color = Color(0xFF0F172A),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "กรุณาตรวจสอบรายละเอียดที่อ่านได้จากแถบแม่เหล็กของบัตร",
                    color = Color(0xFF475569),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Profile Avatar Placeholder
            ProfileAvatarPlaceholder(licenseData?.photo)

            if (licenseData != null) {
                // Parsed Details Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, if (licenseData.isParsedSuccessfully) Color(0xFFE2E8F0) else Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "รายละเอียดใบขับขี่",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FactCheck,
                                contentDescription = null,
                                tint = if (licenseData.isParsedSuccessfully) Color(0xFF059669) else Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        DataRow(
                            label = "Citizen ID",
                            value = licenseData.citizenId.ifEmpty { "Not Found" },
                            onCopy = { clipboardManager.setText(AnnotatedString(licenseData.citizenId)) }
                        )
                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        DataRow(
                            label = "License Number",
                            value = licenseData.licenseNumber.ifEmpty { "Not Found" },
                            onCopy = { clipboardManager.setText(AnnotatedString(licenseData.licenseNumber)) }
                        )
                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        DataRow(
                            label = "Driver Name (EN)",
                            value = licenseData.fullNameEn.ifEmpty { "Not Found" },
                            onCopy = { clipboardManager.setText(AnnotatedString(licenseData.fullNameEn)) }
                        )
                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        DataRow(
                            label = "Date of Birth",
                            value = licenseData.birthDate.ifEmpty { "Not Found" }
                        )
                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        DataRow(
                            label = "Expiration Date",
                            value = licenseData.expiryDate.ifEmpty { "Not Found" }
                        )
                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        DataRow(
                            label = "License Type",
                            value = licenseData.licenseType.ifEmpty { "Not Found" }
                        )

                        if (!licenseData.isParsedSuccessfully) {
                            Spacer(modifier = Modifier.height(4.dp))
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
                    }
                }

                // Raw Tracks Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF009688),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "ข้อความแทร็กแถบแม่เหล็กดิบ",
                                    color = Color(0xFF0F172A),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (isRawDataExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }

                        AnimatedVisibility(
                            visible = isRawDataExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val rawText = buildString {
                                    append("TRACK 1: ").append(licenseData.rawTrack1.ifEmpty { "[Empty]" }).append("\n\n")
                                    append("TRACK 2: ").append(licenseData.rawTrack2.ifEmpty { "[Empty]" }).append("\n\n")
                                    append("TRACK 3: ").append(licenseData.rawTrack3.ifEmpty { "[Empty]" })
                                }
                                Text(
                                    text = rawText,
                                    color = Color(0xFF334155),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                        .verticalScroll(rememberScrollState())
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRescan,
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "สแกนใหม่",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Button(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009688), // Teal primary button
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "ยืนยัน",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DataRow(
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color(0xFF475569),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            if (onCopy != null && value.isNotEmpty() && value != "Not Found") {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy details",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Text(
            text = value,
            color = Color(0xFF0F172A),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth()
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
