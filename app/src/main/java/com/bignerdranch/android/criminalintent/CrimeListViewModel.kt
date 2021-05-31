package com.bignerdranch.android.criminalintent

import androidx.lifecycle.ViewModel

class CrimeListViewModel: ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()
    var createVisibility: Int = 0

    fun addCrime(crime: Crime){
        crimeRepository.addCrime(crime)
    }
}