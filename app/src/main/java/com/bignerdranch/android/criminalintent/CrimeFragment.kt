package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
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
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import java.util.*

private const val TAG = "CrimeFragment"
private const val LOG_TAG = "LOG_CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val REQUEST_DATE = "DialogDate"
private const val REQUEST_TIME = "DialogTime"
private const val DATE_FORMAT = "EEE, MMM, dd"
private const val REQUEST_CONTACT = 1

class CrimeFragment: Fragment(), FragmentResultListener {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var timeButton: Button
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var pickContactContract: ActivityResultContract<Uri, Uri?>
    private lateinit var pickContactCallback: ActivityResultCallback<Uri?>
    private lateinit var pickContactLauncher: ActivityResultLauncher<Uri>

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy{
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)

        pickContactContract = object : ActivityResultContract<Uri, Uri?>(){
            override fun createIntent(context: Context, input: Uri?): Intent {
                Log.d(TAG, "createIntent() called")
                return Intent(Intent.ACTION_PICK, input)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                Log.d(TAG, "parseResult() called")
                if(resultCode != Activity.RESULT_OK || intent == null)
                    return null
                return intent.data
            }
        }

        pickContactCallback = ActivityResultCallback<Uri?> { contactUri: Uri? ->
            val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
            // Perform your query - the contactUri is like a "where" clause here
            val cursor = contactUri?.let {
                requireActivity().contentResolver
                    .query(it, queryFields, null, null, null)
            }

            cursor?.use{
                //Verify cursor contains at least one result

                if (it.count > 0){
                    // Pull out the first column of the first row of data -
                    // that is your suspect's name

                    it.moveToFirst()

                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }
        }

        pickContactLauncher = registerForActivityResult(pickContactContract, pickContactCallback)
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
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
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

        if (crime.suspect.isNotEmpty()){
            suspectButton.text = crime.suspect
        }

    }

    private fun getCrimeReport():String{
        val solvedString =  if (crime.isSolved){
            getString(R.string.crime_report_solved)
        } else{
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        var suspect = if(crime.suspect.isBlank()){
            getString(R.string.crime_report_no_suspect)
        } else{
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)

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

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject))}
                .also{  intent ->
                    val chooserIntent = Intent.createChooser(intent,
                        getString(R.string.send_report))
                    startActivity(chooserIntent)
                }
        }

        suspectButton.apply{
            val pickContactIntent =
                pickContactContract.createIntent(requireContext(), ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                pickContactLauncher.launch(ContactsContract.Contacts.CONTENT_URI)
            }
/*
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if(resolvedActivity == null){
                isEnabled = false
            }
*/
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