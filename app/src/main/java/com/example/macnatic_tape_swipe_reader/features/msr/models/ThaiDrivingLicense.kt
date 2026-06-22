package com.example.macnatic_tape_swipe_reader.features.msr.models

data class ThaiDrivingLicense(
    val citizenId: String = "",
    val licenseNumber: String = "",
    val firstNameEn: String = "",
    val lastNameEn: String = "",
    val fullNameEn: String = "",
    val birthDate: String = "",       // Format: DD/MM/YYYY or YYYY-MM-DD
    val expiryDate: String = "",      // Format: DD/MM/YYYY or YYYY-MM-DD
    val licenseType: String = "",     // e.g. "ท.2", "Personal Car", "Temporary"
    val rawTrack1: String = "",
    val rawTrack2: String = "",
    val rawTrack3: String = "",
    val isParsedSuccessfully: Boolean = false,
    val parsingErrorMessage: String? = null,
    val photo: android.graphics.Bitmap? = null
)
