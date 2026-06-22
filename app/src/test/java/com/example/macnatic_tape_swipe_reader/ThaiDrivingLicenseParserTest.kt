package com.example.macnatic_tape_swipe_reader

import com.example.macnatic_tape_swipe_reader.features.msr.parsers.ThaiDrivingLicenseParser
import org.junit.Assert.*
import org.junit.Test

class ThaiDrivingLicenseParserTest {

    @Test
    fun testParseStandardThaiDrivingLicense() {
        val track1 = "%TH998274163^DEEDEE^SOMCHAI^19900824^20300824^ท.2?"
        val track2 = ";1300900123456=300824?"
        val track3 = ""

        val license = ThaiDrivingLicenseParser.parse(track1, track2, track3)

        assertTrue(license.isParsedSuccessfully)
        assertNull(license.parsingErrorMessage)
        assertEquals("1300900123456", license.citizenId)
        assertEquals("998274163", license.licenseNumber)
        assertEquals("SOMCHAI DEEDEE", license.fullNameEn)
        assertEquals("SOMCHAI", license.firstNameEn)
        assertEquals("DEEDEE", license.lastNameEn)
        assertEquals("24/08/1990", license.birthDate)
        assertEquals("2030-08-24", license.expiryDate)
        assertEquals("ท.2", license.licenseType)
    }

    @Test
    fun testParseTrack2Only() {
        val track1 = ""
        val track2 = ";1234567890123=2806?"
        val track3 = ""

        val license = ThaiDrivingLicenseParser.parse(track1, track2, track3)

        assertTrue(license.isParsedSuccessfully)
        assertEquals("1234567890123", license.citizenId)
        assertEquals("2028-06-30", license.expiryDate)
        assertEquals("", license.fullNameEn)
    }

    @Test
    fun testParseTrack1Only() {
        val track1 = "%TH123456789^SMITH^ALICE^19850212?"
        val track2 = ""
        val track3 = ""

        val license = ThaiDrivingLicenseParser.parse(track1, track2, track3)

        assertTrue(license.isParsedSuccessfully)
        assertEquals("", license.citizenId)
        assertEquals("123456789", license.licenseNumber)
        assertEquals("ALICE SMITH", license.fullNameEn)
        assertEquals("ALICE", license.firstNameEn)
        assertEquals("SMITH", license.lastNameEn)
        assertEquals("12/02/1985", license.birthDate)
    }

    @Test
    fun testParseEmptyTracks() {
        val license = ThaiDrivingLicenseParser.parse(null, null, null)

        assertFalse(license.isParsedSuccessfully)
        assertNotNull(license.parsingErrorMessage)
    }
}
