package com.webiki.bucketlist.fragments.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.webiki.bucketlist.Goal
import com.webiki.bucketlist.ProjectSharedPreferencesHelper
import com.webiki.bucketlist.R
import com.webiki.bucketlist.activities.GoalWizard
import com.webiki.bucketlist.databinding.FragmentHomeBinding
import java.time.LocalDate


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var storageHelper: ProjectSharedPreferencesHelper

    private var goalsList: MutableSet<String> = mutableSetOf()
    private val CREATE_GOAL_REQUEST_CODE = 1
    private val SEE_GOAL_REQUEST_CODE = 2
    private var goalSetKey = ""

    private lateinit var goalsLayout: LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        goalSetKey = getString(R.string.userGoalsSet)
        storageHelper = ProjectSharedPreferencesHelper(this.requireContext())
        goalsLayout = binding.goalsLayout
        goalsList = storageHelper.getStringSetFromStorage(goalSetKey)

        binding.fab.setOnClickListener { handleFabClick(it) }
        return root
    }

    override fun onStart() {
        super.onStart()
        initializeGoalLayout(goalsLayout, goalsList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleFabClick(view: View) {
        startActivityForResult(
            Intent(view.context, GoalWizard::class.java),
            CREATE_GOAL_REQUEST_CODE
        )
    }

    @Deprecated("Deprecated in Java (everywhere)")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CREATE_GOAL_REQUEST_CODE) handleGoalActivityReturn(resultCode, data)
    }

    /**
     * Добавляет или удаляет цель в список целей
     * TODO add possibility change the goal
     * @param resultCode Результат работы GoalWizard Activity
     * @param data Данные, отданные GoalWizard Activity
     */
    private fun handleGoalActivityReturn(resultCode: Int, data: Intent?) {
//        val action: (MutableSet<String>, String) -> Boolean =
//            when (resultCode) {
//                Activity.RESULT_OK -> { set, value -> set.add(value) }
//                Activity.RESULT_CANCELED -> { set, value -> set.remove(value) }
//                else -> throw InvalidPropertiesFormatException(getString(R.string.unexpectedResultCode))
//            }
//
//        storageHelper.refreshStringSetFromStorage(
//            goalSetKey,
//            data?.getStringExtra(getString(R.string.newGoalKey)),
//            action
//        )

        if (resultCode == Activity.RESULT_OK) {
            val goalInfo = data?.getStringArrayExtra(getString(R.string.newGoalKey))!!
            Goal(goalInfo[0], goalInfo[1], LocalDate.parse(goalInfo[2], Goal.DateFormat)).save()
            // TODO add posibility get goal object from db
        }
    }

    /**
     * Инициализирует макет целей
     *
     * @param layout Макет для списка целей
     * @param goals Список целей
     */
    private fun <T> initializeGoalLayout(
        layout: LinearLayout,
        goals: MutableSet<T>
    ) {
        layout.removeAllViews()
        addGoalsToLayout(layout, goals)
    }

    /**
     * Добавляет цели в макет (LinearLayout)
     *
     * @param layout Макет для списка целей
     * @param goals Список целей
     */
    private fun <T> addGoalsToLayout(
        layout: LinearLayout,
        goals: MutableSet<T>
    ) {
        if (goals.isEmpty()) {
            Snackbar.make(layout, getString(R.string.hasNotGoals), Snackbar.LENGTH_LONG).show()
            return
        }

        for (goal in goals) {
            if (!goalsList.contains(goal.toString())) goalsList.add(goal.toString())
            val view = layoutInflater.inflate(R.layout.simple_goal_view, layout, false)
            val viewCheckBox = view.findViewById<CheckBox>(R.id.goalCheckBox)
            val layoutParameters = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 6; topMargin = 6 }

            view.layoutParams = layoutParameters
            viewCheckBox.text = goal.toString()

            layout.addView(view)
        }
    }
}