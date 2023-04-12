package com.webiki.bucketlist.fragments.home

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.orm.SugarRecord
import com.webiki.bucketlist.Goal
import com.webiki.bucketlist.ProjectSharedPreferencesHelper
import com.webiki.bucketlist.R
import com.webiki.bucketlist.activities.GoalWizard
import com.webiki.bucketlist.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var storageHelper: ProjectSharedPreferencesHelper
    private lateinit var fab: AppCompatButton

    private var goalsList: MutableList<Goal> = mutableListOf()
    private val CREATE_GOAL_REQUEST_CODE = 1
    private val SEE_GOAL_REQUEST_CODE = 2
    private var goalSetKey = ""
    private var isNeedToLoadGoals = true

    private lateinit var goalsLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fab = root.findViewById(R.id.homeFabButton)
        fab.setOnClickListener { handleFabClick(it) }
        goalSetKey = getString(R.string.userGoalsSet)
        storageHelper = ProjectSharedPreferencesHelper(this.requireContext())
        goalsLayout = binding.goalsLayout

        goalsList = SugarRecord.listAll(Goal::class.java)
        goalsList.sort()

        return root
    }

    override fun onResume() {
        super.onResume()

//        if (isNeedToLoadGoals) {
            initializeGoalLayout(goalsLayout, goalsList)

            if (goalsList.isEmpty()) Snackbar.make(
                goalsLayout,
                getString(R.string.hasNotGoals),
                Snackbar.LENGTH_LONG
            ).show()
//        }
    }

    override fun onPause() {
        super.onPause()

        SugarRecord.deleteAll(Goal::class.java)
        goalsList.forEach { it.save() }
    }

    /**
     * Инициализирует макет целей
     *
     * @param layout Макет для списка целей
     * @param goals Список целей
     */
    private fun initializeGoalLayout(
        layout: LinearLayout,
        goals: MutableList<Goal>
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
    private fun addGoalsToLayout(
        layout: LinearLayout,
        goals: MutableList<Goal>
    ) {
        for (goal in goals) addGoalToLayout(layout, goal)
    }

    /**
     * Добавляет цель в макет (LinearLayout)
     *
     * @param layout Макет для списка целей
     * @param goal Цель
     */
    private fun addGoalToLayout(
        layout: LinearLayout,
        goal: Goal
    ) {
        val isCurrentThemeDay = requireContext().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO

        fun customCheckboxHandleClick(checkbox: CheckBox) {
            checkbox.setTextColor(
                requireContext().getColor(
                    if (checkbox.isChecked) R.color.gray
                    else if (isCurrentThemeDay) R.color.black_dark
                    else R.color.white_light
                )
            )
        }

        val view = layoutInflater.inflate(R.layout.simple_goal_view, layout, false)
        val viewCheckBox = view.findViewById<CheckBox>(R.id.goalCheckBox)
        val layoutParameters = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 6; topMargin = 6 }

        viewCheckBox.let {
            it.layoutParams = layoutParameters
            it.text = goal.getLabel()
            it.tag = goal.getLabel()
            it.isChecked = goal.getCompleted()

        }

        viewCheckBox.setOnClickListener {
            customCheckboxHandleClick(viewCheckBox)
            goalsList
                .find { goal -> goal.getLabel() == viewCheckBox.text.toString() }!!
                .setCompleted(viewCheckBox.isChecked)
        }

        viewCheckBox.setOnLongClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.popup_window_view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val dialogText = dialog.window?.findViewById<TextView>(R.id.popupText)!!
            val dialogSubmitButton = dialog.window?.findViewById<AppCompatButton>(R.id.popupButtonSubmit)!!
            val dialogCancelButton = dialog.window?.findViewById<AppCompatButton>(R.id.popupButtonCancel)!!

            dialogText.text = "${getString(R.string.doYouWantDeleteGoal)}\n\n(${viewCheckBox.text})"
            dialogSubmitButton.text = getString(R.string.submitDeleteGoal)
            dialogCancelButton.text = getString(R.string.cancelDeleteGoal)

            dialogSubmitButton.setOnClickListener { btn ->
                btn as AppCompatButton
                val index = goalsList.indexOf(Goal(viewCheckBox.text.toString(), viewCheckBox.isChecked))

                SugarRecord.delete(goalsList[index])
                goalsList.removeAt(index)
                initializeGoalLayout(goalsLayout, goalsList)
                dialog.dismiss()
            }

            dialogCancelButton.setOnClickListener { btn ->
                btn as AppCompatButton
                dialog.dismiss()
            }

            dialog.create()
            dialog.show()

            return@setOnLongClickListener true
        }

        customCheckboxHandleClick(viewCheckBox)

        layout.addView(view, if (viewCheckBox.isChecked) layout.childCount else 0)
    }

    private fun addGoalToStorage(name: String) {
        val newGoalVariation = mutableListOf(Goal(name, false), Goal(name, true))

        if (newGoalVariation.all { !goalsList.contains(it) }) {
            goalsList.add(newGoalVariation.first())
            addGoalToLayout(goalsLayout, newGoalVariation.first())
        } else
            Toast.makeText(
                context,
                getString(R.string.existingTarget),
                Toast.LENGTH_SHORT
            ).show()
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

        if (requestCode == CREATE_GOAL_REQUEST_CODE) {
            //region add/remove code
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
            //endregion
            if (resultCode == Activity.RESULT_OK) {
                val goalInfo = data?.getStringExtra(getString(R.string.newGoalKey))!!
                addGoalToStorage(goalInfo)
            }
        }
    }
}