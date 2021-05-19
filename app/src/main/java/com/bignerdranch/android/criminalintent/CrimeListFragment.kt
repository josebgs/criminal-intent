package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "CrimeListFragment"
private const val REQUIRES_POLICE = 1
private const val DOESNT_REQUIRE_POLICE = 0
class CrimeListFragment: Fragment() {

    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var adapter: CrimeAdapter

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this@CrimeListFragment).get(CrimeListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Total crimes: ${crimeListViewModel.crimes.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView =
            view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        updateUI()
        return view

    }

    private fun updateUI(){
        val crimes = crimeListViewModel.crimes
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    companion object{
        fun newInstance(): CrimeListFragment{
            return CrimeListFragment()
        }
    }

    private inner class CrimeHolder(view: View)
        :RecyclerView.ViewHolder(view), View.OnClickListener{
        private lateinit var crime: Crime
        private val titleTextView: TextView? = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView? = itemView.findViewById(R.id.crime_date)
        private val contactPoliceButton: Button? = itemView.findViewById(R.id.police_button)

        init{
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime){
            this.crime = crime
            titleTextView?.text = this.crime.title
            dateTextView?.text = this.crime.date.toString()
        }

        override fun onClick(v: View){
            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>)
        : RecyclerView.Adapter<CrimeHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {

            return if (viewType == REQUIRES_POLICE){
                CrimeHolder(layoutInflater.inflate(R.layout.list_item_serious_crime, parent, false))
            }else{
                CrimeHolder(layoutInflater.inflate(R.layout.list_item_crime, parent, false))
            }

        }

        override fun getItemViewType(position: Int): Int {
            return when(crimes[position].requiresPolice){ //returns proper code for when police are required
                true -> REQUIRES_POLICE
                false -> DOESNT_REQUIRE_POLICE
            }
        }
        override fun getItemCount() = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            if(getItemViewType(position) != REQUIRES_POLICE) holder.bind(crime)
        }
    }
}