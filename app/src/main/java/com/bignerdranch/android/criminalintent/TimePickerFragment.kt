package com.bignerdranch.android.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.*

private const val ARG_TIME = "time"
private const val ARG_REQUEST_CODE = "requestTimeCode"
private const val RESULT_DATE_KEY = "com.bignerdranch.android.criminalintent.TimePickerFragment.result_date_key"
class TimePickerFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val timeListener = TimePickerDialog.OnTimeSetListener{ _, hour, minute ->
            val calendar = Calendar.getInstance()
            calendar.set(0,0,0,hour,minute)
            val resultDate: Date =  calendar.time

            //create our result Bundle
            val result = Bundle().apply{
                putSerializable(RESULT_DATE_KEY, resultDate)
            }

            val resultRequestCode = requireArguments().getString(ARG_REQUEST_CODE, "")
            setFragmentResult(resultRequestCode, result)
        }
        val time = Calendar.getInstance()

        return TimePickerDialog(
            requireContext(),
            timeListener,
            time.get(Calendar.HOUR_OF_DAY),
            time.get(Calendar.MINUTE),
            false
        )
    }

    companion object{

        fun newInstance(time:Date, requestCode: String): TimePickerFragment{
            val args = Bundle().apply{
                putSerializable(ARG_TIME, time)
                putString(ARG_REQUEST_CODE, requestCode)
            }

            return TimePickerFragment().apply {
                arguments = args
            }
        }

        fun getSelectedDate(result: Bundle) = result.getSerializable(RESULT_DATE_KEY) as Date

    }
}