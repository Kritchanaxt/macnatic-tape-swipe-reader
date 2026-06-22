package com.example.macnatic_tape_swipe_reader.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macnatic_tape_swipe_reader.view.components.MsrToolbar
import com.example.macnatic_tape_swipe_reader.view.components.TechnicalMetadataCard
import com.example.macnatic_tape_swipe_reader.view.components.UnderlinedTextField
import java.util.Calendar

@Composable
fun FormScreen(
    connectionStatus: String,
    citizenId: String,
    onCitizenIdChange: (String) -> Unit,
    licenseNumber: String,
    onLicenseNumberChange: (String) -> Unit,
    driverName: String,
    onDriverNameChange: (String) -> Unit,
    expiryDate: String,
    onExpiryDateChange: (String) -> Unit,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val showDatePicker = { currentVal: String, onDateSelected: (String) -> Unit ->
        var yearVal = calendar.get(Calendar.YEAR)
        var monthVal = calendar.get(Calendar.MONTH)
        var dayVal = calendar.get(Calendar.DAY_OF_MONTH)

        if (currentVal.isNotEmpty()) {
            val parts = currentVal.split("-")
            if (parts.size == 3) {
                parts[0].toIntOrNull()?.let { yearVal = it }
                parts[1].toIntOrNull()?.let { monthVal = it - 1 }
                parts[2].toIntOrNull()?.let { dayVal = it }
            }
        }

        val datePickerDialog = android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                onDateSelected(formattedDate)
            },
            yearVal,
            monthVal,
            dayVal
        )
        datePickerDialog.show()
    }

    Scaffold(
        topBar = {
            MsrToolbar(title = "e-Driver License Reader")
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Description Text matching passport-reader info box
                Text(
                    text = "Please fill the details below and swipe your driving license card on the MSR reader.\n\nFollowing information is required to read license data locally. We do not store, upload or share any of your data. The app is completely secure.",
                    color = Color(0xFF1E293B),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                // Underlined Input Fields (matching visual design)
                UnderlinedTextField(
                    label = "Citizen ID",
                    value = citizenId,
                    onValueChange = onCitizenIdChange,
                    readOnly = false
                )

                UnderlinedTextField(
                    label = "License number",
                    value = licenseNumber,
                    onValueChange = onLicenseNumberChange,
                    readOnly = false
                )

                UnderlinedTextField(
                    label = "Driver Name",
                    value = driverName,
                    onValueChange = onDriverNameChange,
                    readOnly = false
                )

                UnderlinedTextField(
                    label = "Expiration date",
                    value = expiryDate,
                    onClick = {
                        showDatePicker(expiryDate, onExpiryDateChange)
                    }
                )

                UnderlinedTextField(
                    label = "Date of birth",
                    value = birthDate,
                    onClick = {
                        showDatePicker(birthDate, onBirthDateChange)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Submit button for manual input
                Button(
                    onClick = onSubmit,
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
                        text = "ดูผลลัพธ์",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Connection Metadata Card
                TechnicalMetadataCard(connectionStatus = connectionStatus)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
fun FormScreenPreview() {
    MaterialTheme {
        FormScreen(
            connectionStatus = "USB Reader Connected (Ready)",
            citizenId = "1234567890123",
            onCitizenIdChange = {},
            licenseNumber = "12345678",
            onLicenseNumberChange = {},
            driverName = "MR. SATHEANPONG JEUNGUDOMPORN",
            onDriverNameChange = {},
            expiryDate = "2026-12-22",
            onExpiryDateChange = {},
            birthDate = "1990-12-22",
            onBirthDateChange = {},
            onSubmit = {}
        )
    }
}
