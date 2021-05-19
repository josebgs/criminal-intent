package com.bignerdranch.android.criminalintent

import java.util.Date
import java.util.UUID

data class Crime(val id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var requiresPolice: Boolean = false,
                 var isSolved: Boolean = false) {
}