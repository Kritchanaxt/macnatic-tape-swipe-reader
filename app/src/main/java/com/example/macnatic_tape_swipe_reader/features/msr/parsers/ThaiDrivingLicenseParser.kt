package com.example.macnatic_tape_swipe_reader.features.msr.parsers

import com.example.macnatic_tape_swipe_reader.features.msr.models.ThaiDrivingLicense
import java.util.regex.Pattern

object ThaiDrivingLicenseParser {

    /**
     * Parses raw Track 1, Track 2, and Track 3 strings into a ThaiDrivingLicense object.
     * This parser is designed to be robust and handle various DLT / AAMVA formats.
     */
    fun parse(track1: String?, track2: String?, track3: String?): ThaiDrivingLicense {
        val t1Clean = cleanTrack(track1)
        val t2Clean = cleanTrack(track2)
        val t3Clean = cleanTrack(track3)

        var citizenId = ""
        var licenseNumber = ""
        var firstName = ""
        var lastName = ""
        var birthDate = ""
        var expiryDate = ""
        var licenseType = ""

        try {
            // 1. Parse Track 2 (Numeric only, typically contains Citizen ID and Expiry Date)
            // Standard Track 2 format: ;[CitizenID/PAN]=[Expiry][ServiceCode][DiscretionaryData]?
            if (t2Clean.isNotEmpty()) {
                val parts = t2Clean.split("=")
                if (parts.isNotEmpty()) {
                    val firstPart = parts[0]
                    // In Thai licenses, the main number in Track 2 is typically the 13-digit Citizen ID
                    if (firstPart.length >= 13) {
                        citizenId = firstPart.take(13)
                    }
                    
                    if (parts.size > 1) {
                        val secondPart = parts[1]
                        // Expiry is typically YYMM at the beginning of the discretionary data
                        // e.g. "2812" -> Dec 2028
                        if (secondPart.length >= 6) {
                            val yy = secondPart.substring(0, 2)
                            val mm = secondPart.substring(2, 4)
                            val dd = secondPart.substring(4, 6)
                            if (yy.toIntOrNull() != null && mm.toIntOrNull() != null && dd.toIntOrNull() != null) {
                                expiryDate = "20$yy-$mm-$dd"
                            }
                        } else if (secondPart.length >= 4) {
                            val yy = secondPart.substring(0, 2)
                            val mm = secondPart.substring(2, 4)
                            if (yy.toIntOrNull() != null && mm.toIntOrNull() != null) {
                                expiryDate = "20$yy-$mm-30" // Approximation of end of month
                            }
                        }
                    }
                }
            }

            // 2. Parse Track 1 (Alphanumeric, contains License Number, Names)
            // Standard Track 1 format: %[FormatCode][LicenseNo]^[LastName]^[FirstName]^[BirthDate/ExpiryDate/Other]?
            if (t1Clean.isNotEmpty()) {
                val fields = t1Clean.split("^")
                if (fields.isNotEmpty()) {
                    // Extract license number from the first field (usually after `%TH` or similar)
                    val firstField = fields[0]
                    // Remove non-digit characters to isolate license number or extract digits
                    val licenseDigits = firstField.replace(Regex("[^0-9]"), "")
                    if (licenseDigits.isNotEmpty()) {
                        licenseNumber = licenseDigits
                    }

                    // Extract Last Name (Field 2)
                    if (fields.size > 1) {
                        lastName = fields[1].trim()
                    }

                    // Extract First Name (Field 3)
                    if (fields.size > 2) {
                        firstName = fields[2].trim()
                    }

                    // Attempt to extract dates from later fields if available
                    if (fields.size > 3) {
                        val dateField = fields[3].trim()
                        // Often contains DOB or Expiry in YYYYMMDD or YYMMDD format
                        if (dateField.length >= 8 && dateField.take(8).all { it.isDigit() }) {
                            val yyyy = dateField.substring(0, 4)
                            val mm = dateField.substring(4, 6)
                            val dd = dateField.substring(6, 8)
                            birthDate = "$dd/$mm/$yyyy"
                        }
                    }

                    if (fields.size > 4) {
                        val expiryField = fields[4].trim()
                        if (expiryField.length >= 8 && expiryField.take(8).all { it.isDigit() }) {
                            val yyyy = expiryField.substring(0, 4)
                            val mm = expiryField.substring(4, 6)
                            val dd = expiryField.substring(6, 8)
                            expiryDate = "$yyyy-$mm-$dd"
                        }
                    }

                    if (fields.size > 5) {
                        val typeField = fields[5].trim()
                        if (typeField.isNotEmpty()) {
                            licenseType = typeField
                        }
                    }
                }
            }

            // Fallback for Name Parsing (if standard delimiter isn't matched but raw name is in Track 1)
            if (firstName.isEmpty() && lastName.isEmpty() && t1Clean.isNotEmpty()) {
                // Look for names like "SURNAME/NAME" or standard name formatting
                val namePattern = Pattern.compile("([A-Z\\s]+)/([A-Z\\s]+)")
                val matcher = namePattern.matcher(t1Clean)
                if (matcher.find()) {
                    lastName = matcher.group(1)?.trim() ?: ""
                    firstName = matcher.group(2)?.trim() ?: ""
                }
            }

            // Standardize License Type if empty
            if (licenseType.isEmpty()) {
                licenseType = when {
                    licenseNumber.startsWith("2") -> "Personal Car (ประเภท 2)"
                    licenseNumber.startsWith("1") -> "Personal Motorcycle (ประเภท 1)"
                    else -> "Thai Driving License"
                }
            }

            val fullName = listOf(firstName, lastName).filter { it.isNotEmpty() }.joinToString(" ")

            val isSuccess = citizenId.isNotEmpty() || licenseNumber.isNotEmpty() || fullName.isNotEmpty()

            return ThaiDrivingLicense(
                citizenId = citizenId,
                licenseNumber = licenseNumber,
                firstNameEn = firstName,
                lastNameEn = lastName,
                fullNameEn = fullName,
                birthDate = birthDate,
                expiryDate = expiryDate,
                licenseType = licenseType,
                rawTrack1 = track1 ?: "",
                rawTrack2 = track2 ?: "",
                rawTrack3 = track3 ?: "",
                isParsedSuccessfully = isSuccess,
                parsingErrorMessage = if (isSuccess) null else "Could not extract critical fields (Citizen ID, License Number, or Name)"
            )

        } catch (e: Exception) {
            return ThaiDrivingLicense(
                rawTrack1 = track1 ?: "",
                rawTrack2 = track2 ?: "",
                rawTrack3 = track3 ?: "",
                isParsedSuccessfully = false,
                parsingErrorMessage = "Exception occurred during parsing: ${e.localizedMessage}"
            )
        }
    }

    private fun cleanTrack(track: String?): String {
        if (track == null) return ""
        var cleaned = track.trim()
        // Strip start sentinels
        if (cleaned.startsWith("%") || cleaned.startsWith(";") || cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1)
        }
        // Strip end sentinels
        if (cleaned.endsWith("?")) {
            cleaned = cleaned.substring(0, cleaned.length - 1)
        }
        return cleaned
    }
}
