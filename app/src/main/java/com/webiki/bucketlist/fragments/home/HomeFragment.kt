package com.webiki.bucketlist.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.webiki.bucketlist.R
import com.webiki.bucketlist.activities.MainActivity
import com.webiki.bucketlist.databinding.FragmentHomeBinding
import java.util.ArrayList

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val goalsList = arrayListOf("goal 1", "goal 2")

    private lateinit var goalsLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        goalsLayout = root.findViewById(R.id.goalsLayout)

        binding.fab.setOnClickListener { view -> /*TODO: create onClickListener*/ }
        return root
    }

    override fun onStart() {
        super.onStart()

        fillGoalsLayoutFromList(goalsLayout, goalsList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun <T> fillGoalsLayoutFromList(layout: LinearLayout, items: ArrayList<T>) {
        val inflater = layoutInflater
        layout.removeAllViews()
        for (item in items) {
            val view = inflater.inflate(R.layout.simple_goal_view, layout, false)
            val viewCheckBox = view.findViewById<CheckBox>(R.id.goalCheckBox)
            val layoutParameters = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            layoutParameters.setMargins(0, 0, 0, 12)
            view.layoutParams = layoutParameters
            viewCheckBox.text = item.toString()

            layout.addView(view)
        }
    }
}