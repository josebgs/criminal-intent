package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val REQUEST_DATE = "DialogDate"
private const val REQUEST_TIME = "DialogTime"

class CrimeFragment: Fragment(), FragmentResultListener {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var timeButton: Button
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy{
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime,
            container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        timeButton = view.findViewById(R.id.crime_time) as Button

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.setFragmentResultListener(REQUEST_DATE, viewLifecycleOwner, this)
        childFragmentManager.setFragmentResultListener(REQUEST_TIME, viewLifecycleOwner, this)

        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer{ crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            }
        )
    }

    private fun updateUI() {
        titleField.setText(crime.title)

        dateButton.text = DateFormat.format("EEEE, MMM dd, yyyy", crime.date)
        timeButton.text = DateFormat.format("h:mm aa", crime.date)


        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?,
                                           start: Int,
                                           count: Int,
                                           after: Int) {
                //left purposely blank
            }

            override fun onTextChanged(s: CharSequence?,
                                       start: Int,
                                       before: Int,
                                       count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                //left purposely blank
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener{_, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener{
            DatePickerFragment
                .newInstance(crime.date, REQUEST_DATE)
                .show(childFragmentManager, REQUEST_DATE)
        }

        timeButton.setOnClickListener{
            TimePickerFragment
                .newInstance(crime.date, REQUEST_TIME)
                .show(childFragmentManager, REQUEST_TIME)
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    companion object{
        fun newInstance(crimeId: UUID): CrimeFragment{
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey){
            REQUEST_DATE ->{
                Log.d(TAG, "received result for $requestKey")
                val newDate = Calendar.getInstance()
                val oldDate = Calendar.getInstance()
                val temp = Calendar.getInstance()

                newDate.time = DatePickerFragment.getSelectedDate(result)
                oldDate.time = crime.date

                temp.set(newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH),newDate.get(Calendar.DATE),
                oldDate.get(Calendar.HOUR_OF_DAY), oldDate.get(Calendar.MINUTE))
                crime.date = temp.time

                updateUI()
            }

            REQUEST_TIME->{
                val newDate = Calendar.getInstance()
                val oldDate = Calendar.getInstance()
                val temp = Calendar.getInstance()

                newDate.time = TimePickerFragment.getSelectedDate(result)
                oldDate.time = crime.date

                temp.set(oldDate.get(Calendar.YEAR), oldDate.get(Calendar.MONTH),oldDate.get(Calendar.DATE),
                    newDate.get(Calendar.HOUR_OF_DAY), newDate.get(Calendar.MINUTE))
                crime.date = temp.time

                updateUI()
            }
        }
    }

}