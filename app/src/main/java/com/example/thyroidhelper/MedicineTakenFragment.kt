package com.example.thyroidhelper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.DateFormat

class MedicineTakenFragment : Fragment() {

    private lateinit var drugTakenMessage: String
    private lateinit var drugTakenMessageView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drugTakenMessage =  getString(R.string.drug_taken_message)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_medicine_taken, container, false)
        drugTakenMessageView = root.findViewById(R.id.drug_taken_message)
        return root
    }

    override fun onResume() {
        val timestamp = getDrugTakenTime(context!!)
        val timeStr = DateFormat.getTimeInstance(DateFormat.SHORT).format(timestamp)
        // Use non-breaking space to avoid a line-break between 6:00 and AM
        val nbspTimeStr = timeStr.replace(" ", "\u00A0" )
        drugTakenMessageView.text = String.format(drugTakenMessage, nbspTimeStr)

        super.onResume()
    }

}
