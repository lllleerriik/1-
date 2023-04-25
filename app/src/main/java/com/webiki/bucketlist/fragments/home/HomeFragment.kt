package com.webiki.bucketlist.fragments.home

import android.app.Dialog
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import com.webiki.bucketlist.activities.MainActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.orm.SugarRecord
import com.webiki.bucketlist.Goal
import com.webiki.bucketlist.enums.GoalPriority
import com.webiki.bucketlist.ProjectSharedPreferencesHelper
import com.webiki.bucketlist.R
import com.webiki.bucketlist.databinding.FragmentHomeBinding
import com.webiki.bucketlist.enums.GoalCategory
import java.util.Locale.Category


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var storageHelper: ProjectSharedPreferencesHelper
    private lateinit var fab: AppCompatButton

    private val CREATE_GOAL_REQUEST_CODE = 1
    private var goalsList: MutableList<Goal> = mutableListOf()
    private lateinit var topIndexesByPriority: MutableList<Int>
    private lateinit var goalSetKey: String

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
        goalsLayout = binding.goalsCategoriesLayout

        goalsList = SugarRecord
            .listAll(Goal::class.java)

        goalsList.sortDescending()
        
        val priorityGroups = goalsList
            .groupBy { g -> g.getPriority() }
            .toSortedMap(compareByDescending { it.value })

        goalsList = priorityGroups.flatMap { pair -> pair.value }.toMutableList()
        
        return root
    }

    override fun onResume() {
        super.onResume()

        initializeGoalLayout(goalsLayout, goalsList)

        if (goalsList.isEmpty()) Snackbar.make(
            goalsLayout,
            getString(R.string.hasNotGoals),
            Snackbar.LENGTH_LONG
        ).show()
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
        topIndexesByPriority = mutableListOf(0, 0, 0)
        layout.removeAllViews()
        addGoalsToLayout(layout, goals)
        for (i in goalsList.size - 1 downTo 0)
            topIndexesByPriority[goalsList[i].getPriority().value] = i
        // reversing list index and decrement 1 for get previous position
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
        for (goal in goals) createCheckboxWithPosition(layout, goal)
    }

    /**
     * Создаёт и возвращает чекбокс, созданный по цели
     *
     * @param layout Макет для списка целей
     * @param goal Цель
     * @param index Номер позиции в макете для вставки чекбокса
     * @return Пару (чекбокс, позиция в макете)
     */
    private fun createCheckboxWithPosition(
        layout: LinearLayout,
        goal: Goal,
        index: Int? = null
    ) : Pair<View, Int> {
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
            (activity as MainActivity).createModalWindow(
                requireContext(),
                "${getString(R.string.doYouWantToDeleteGoal)}\n\n(${viewCheckBox.text})",
                getString(R.string.submitDeleteGoal),
                getString(R.string.cancelButtonText),
                { val index = goalsList.indexOf(
                        Goal(
                            viewCheckBox.text.toString(),
                            viewCheckBox.isChecked
                        )
                    )

                    SugarRecord.delete(goalsList[index])
                    goalsList.removeAt(index)
                    initializeGoalLayout(goalsLayout, goalsList)
                })

            return@setOnLongClickListener true
        }

        customCheckboxHandleClick(viewCheckBox)

        return Pair(view, index ?: layout.childCount)
    }

    /**
     * Сохраняет созданную цель
     *
     * @param name Название цели
     * @param priority Приоритет цели (высокий\средний\низкий)
     */
    private fun addGoalToStorage(name: String, priority: GoalPriority, category: GoalCategory) {
        val newGoalVariation = mutableListOf(Goal(name, false, priority, category), Goal(name, true, priority, category))

        if (newGoalVariation.all { !goalsList.contains(it) }) {
            goalsList.add(topIndexesByPriority[priority.value], newGoalVariation.first())
            createCheckboxWithPosition(goalsLayout, newGoalVariation.first(),
                topIndexesByPriority[newGoalVariation.first().getPriority().value])
        } else
            Toast.makeText(
                context,
                getString(R.string.existingTarget),
                Toast.LENGTH_SHORT
            ).show()
    }

    /**
     * Вызывает модальное окно добавления цели по имени, приоритету (и группе)
     *
     * @param view Fab-кнопка
     */
    private fun handleFabClick(view: View) {

        val dialog = Dialog(view.context)

        dialog.setContentView(R.layout.popup_goal_wizard_view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dialogNameInput = dialog.window?.findViewById<EditText>(R.id.goalWizardName)!!
        val dialogButton = dialog.window?.findViewById<AppCompatButton>(R.id.goalWizardConfirmButton)!!
        val dialogPriorityList = dialog.window?.findViewById<Spinner>(R.id.goalWizardPriorityList)!!
        val dialogCategoryList = dialog.window?.findViewById<Spinner>(R.id.goalWizardCategoryList)!!

        dialogButton.setOnClickListener {
            if (dialogNameInput.text.trim().isNotEmpty()
                && (dialogPriorityList.selectedView as MaterialTextView).text.toString() != Resources.getSystem().getStringArray(R.array.goalPriorities)[0]
                && (dialogCategoryList.selectedView as MaterialTextView).text.toString() != Resources.getSystem().getStringArray(R.array.goalCategories)[0]) {

                val priority = when ((dialogPriorityList.selectedView as MaterialTextView).text.toString()){
                    view.context.resources.getStringArray(R.array.goalPriorities)[1] -> GoalPriority.Low
                    view.context.resources.getStringArray(R.array.goalPriorities)[2] -> GoalPriority.Middle
                    else -> GoalPriority.High
                }

                val category = when ((dialogCategoryList.selectedView as MaterialTextView).text.toString()){
                    view.context.resources.getStringArray(R.array.goalCategories)[0] -> GoalCategory.Health
                    view.context.resources.getStringArray(R.array.goalCategories)[1] -> GoalCategory.Career
                    view.context.resources.getStringArray(R.array.goalCategories)[2] -> GoalCategory.Finance
                    view.context.resources.getStringArray(R.array.goalCategories)[3] -> GoalCategory.Relationships
                    view.context.resources.getStringArray(R.array.goalCategories)[4] -> GoalCategory.FamilyAndFriends
                    view.context.resources.getStringArray(R.array.goalCategories)[5] -> GoalCategory.Materialist
                    view.context.resources.getStringArray(R.array.goalCategories)[6] -> GoalCategory.Selfbuilding
                    else -> GoalCategory.Other
                }

                addGoalToStorage(dialogNameInput.text.toString(), priority, category)
                dialog.dismiss()
                initializeGoalLayout(goalsLayout, goalsList)
            } else
                Toast.makeText(requireContext(), getString(R.string.incorrectGoal), Toast.LENGTH_LONG).show()
        }

        dialog.create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}