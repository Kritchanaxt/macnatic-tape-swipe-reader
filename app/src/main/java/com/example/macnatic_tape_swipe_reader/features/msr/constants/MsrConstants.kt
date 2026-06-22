package com.example.macnatic_tape_swipe_reader.features.msr.constants

object MsrConstants {
    // Card Types from Sunmi AidlConstantsV2
    const val CARD_TYPE_MAGNETIC = 1

    // Track Error Codes
    const val TRACK_NO_ERROR = 0
    const val TRACK_EMPTY = -1
    const val TRACK_PARITY_ERROR = -2
    const val TRACK_LRC_ERROR = -3

    // Labels for Track Errors
    fun getTrackErrorLabel(code: Int): String {
        return when (code) {
            TRACK_NO_ERROR -> "OK"
            TRACK_EMPTY -> "No Data (Empty)"
            TRACK_PARITY_ERROR -> "Parity Error"
            TRACK_LRC_ERROR -> "LRC Error"
            else -> "Unknown Error ($code)"
        }
    }
}
