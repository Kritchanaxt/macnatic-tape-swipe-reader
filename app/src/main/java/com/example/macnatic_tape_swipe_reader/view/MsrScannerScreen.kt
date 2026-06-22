package com.example.macnatic_tape_swipe_reader.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.macnatic_tape_swipe_reader.features.msr.models.ThaiDrivingLicense
import com.example.macnatic_tape_swipe_reader.view.components.LoadingScreen
import kotlinx.coroutines.delay

enum class ActiveScreen {
    Form,
    Result
}

@Composable
fun MsrScannerScreen(
    connectionStatus: String,
    licenseData: ThaiDrivingLicense?,
    onStartPolling: () -> Unit,
    onCancelPolling: () -> Unit,
    onReset: () -> Unit,
    onUpdateLicenseData: (ThaiDrivingLicense) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(ActiveScreen.Form) }
    var isLoading by remember { mutableStateOf(false) }

    // Local input states
    var citizenIdInput by remember { mutableStateOf("") }
    var licenseNumberInput by remember { mutableStateOf("") }
    var driverNameInput by remember { mutableStateOf("") }
    var expiryDateInput by remember { mutableStateOf("") }
    var birthDateInput by remember { mutableStateOf("") }

    // Launch loading animation and transition screens when a card is swiped or submitted
    LaunchedEffect(licenseData) {
        if (licenseData != null) {
            citizenIdInput = licenseData.citizenId
            licenseNumberInput = licenseData.licenseNumber
            driverNameInput = licenseData.fullNameEn
            expiryDateInput = licenseData.expiryDate
            birthDateInput = licenseData.birthDate

            isLoading = true
            delay(1200) // 1.2 seconds premium loading feel
            isLoading = false
            currentScreen = ActiveScreen.Result
        } else {
            citizenIdInput = ""
            licenseNumberInput = ""
            driverNameInput = ""
            expiryDateInput = ""
            birthDateInput = ""
            currentScreen = ActiveScreen.Form
        }
    }

    if (isLoading) {
        LoadingScreen()
    } else {
        when (currentScreen) {
            ActiveScreen.Form -> {
                FormScreen(
                    connectionStatus = connectionStatus,
                    citizenId = citizenIdInput,
                    onCitizenIdChange = { citizenIdInput = it },
                    licenseNumber = licenseNumberInput,
                    onLicenseNumberChange = { licenseNumberInput = it },
                    driverName = driverNameInput,
                    onDriverNameChange = { driverNameInput = it },
                    expiryDate = expiryDateInput,
                    onExpiryDateChange = { expiryDateInput = it },
                    birthDate = birthDateInput,
                    onBirthDateChange = { birthDateInput = it },
                    onSubmit = {
                        onUpdateLicenseData(
                            ThaiDrivingLicense(
                                citizenId = citizenIdInput,
                                licenseNumber = licenseNumberInput,
                                fullNameEn = driverNameInput,
                                expiryDate = expiryDateInput,
                                birthDate = birthDateInput,
                                isParsedSuccessfully = true
                            )
                        )
                    },
                    modifier = modifier
                )
            }
            ActiveScreen.Result -> {
                ResultScreen(
                    licenseData = licenseData,
                    onBackClick = onReset,
                    onRescan = onReset,
                    modifier = modifier
                )
            }
        }
    }
}
