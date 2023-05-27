package com.webiki.bucketlist.fragments.home

import android.accessibilityservice.AccessibilityService.SoftKeyboardController
import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.orm.SugarRecord
import com.webiki.bucketlist.Goal
import com.webiki.bucketlist.ProjectSharedPreferencesHelper
import com.webiki.bucketlist.R
import com.webiki.bucketlist.activities.MainActivity
import com.webiki.bucketlist.databinding.FragmentHomeBinding
import com.webiki.bucketlist.enums.GoalCategory
import com.webiki.bucketlist.enums.GoalPriority
import com.webiki.bucketlist.enums.GoalProgress
import com.webiki.bucketlist.activities.AccountActivity
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import kotlin.system.exitProcess

class HomeFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var storageHelper: ProjectSharedPreferencesHelper
    private lateinit var fab: AppCompatButton
    private lateinit var goalsProgressSpinner: Spinner
    private lateinit var goalsLayout: LinearLayout

    private var goalsList: MutableList<Goal> = mutableListOf()
    private lateinit var topIndexesByPriority: MutableList<Int>
    private var collapsedGoalCategories: MutableSet<Int> = mutableSetOf()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

//        SugarRecord.deleteAll(Goal::class.java)
//        requireActivity().moveTaskToBack(true)
//        exitProcess(-1)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fab = root.findViewById(R.id.homeFabButton)
        fab.setOnClickListener { handleFabClick(it) }

        goalsProgressSpinner = root.findViewById(R.id.goalsProgressSpinner)
        goalsLayout = binding.goalsCategoriesLayout

        storageHelper = ProjectSharedPreferencesHelper(requireActivity())

        databaseReference = Firebase.database.reference
        auth = Firebase.auth
        currentUser = auth.currentUser

        goalsList = SugarRecord
            .listAll(Goal::class.java)

        goalsList.sortDescending()

        goalsList = goalsList
            .distinctBy { g -> g.getLabel() }
            .sortedBy { g -> g.getCompleted() }
            .groupBy { g -> g.getPriority() }
            .toSortedMap(compareByDescending { it.value })
            .flatMap { pair -> pair.value }
            .toMutableList()

        Log.d("DEB", "is have key " + storageHelper.getBooleanFromStorage(getString(R.string.isNetworkRestoredKey), false))

        if (storageHelper.getBooleanFromStorage(getString(R.string.isNetworkRestoredKey), false)) {
            doBackup(goalsList)
        }

        return root
    }

    private fun doBackup(localGoals: MutableList<Goal>) {
        val userGoalsDatabase = Firebase
            .database
            .reference
            .child(
                "${getString(R.string.userFolderInDatabase)}" +
                        "/${currentUser?.uid}" +
                        "/${getString(R.string.userGoalsInDatabase)}"
            )

        // берём цели с облака                                            +
        // смотрим, каких нет в телефоне                                  +
        // если есть с другой выполненностью, оставляем телефонную цель   +
        // всё сохраняем в телефон                                        +
        // стираем бд, копируем полностью с телефона                      +

        userGoalsDatabase.get().addOnCompleteListener {
            val cloudGoals = ((it.result.value
                ?: hashMapOf<String, String>()) as HashMap<*, *>)
                .map { pair -> Goal.parseFromString(pair.value.toString()) }
                .toMutableList()

            val uniqueCloudGoals = cloudGoals.filter { g -> !localGoals.contains(g) && !localGoals.contains(g.withChangedCompletion()) }
            Log.d("DEB", uniqueCloudGoals.toString())

            uniqueCloudGoals.forEach { g -> g.save() }

            databaseReference
                .child(
                    "${getString(R.string.userFolderInDatabase)}" +
                            "/${Firebase.auth.currentUser?.uid}" +
                            "/${getString(R.string.userGoalsInDatabase)}"
                ).setValue(null)

            SugarRecord.listAll(Goal::class.java).forEach { g ->
                saveGoalToFirebase(g)
                Log.d("DEB", g.parseToString())
            }

            Log.d("DEB", "\n\n\n\n")
            Log.d("DEB", SugarRecord.listAll(Goal::class.java).toString())
        }
    }

    override fun onResume() {
        super.onResume()

        goalsList = SugarRecord
            .listAll(Goal::class.java)

        goalsList.sortDescending()

        goalsList = goalsList
            .sortedBy { it.getCompleted() }
            .groupBy { g -> g.getPriority() }
            .toSortedMap(compareByDescending { it.value })
            .flatMap { pair -> pair.value }
            .toMutableList()

        goalsProgressSpinner.onItemSelectedListener = this
        initializeGoalLayout(goalsLayout, goalsList)

        if (goalsList.isEmpty()) Snackbar.make(
            goalsLayout,
            getString(R.string.hasNotGoals),
            Snackbar.LENGTH_LONG
        ).show()

        val chosenFilterValueFromStorage =
            storageHelper.getIntFromStorage(getString(R.string.chosenFilterKey), -1)
        val collapsedCategoriesFromStorage =
            storageHelper.getStringSetFromStorage(getString(R.string.collapsedCategoriesKey))
        if (chosenFilterValueFromStorage != -1) goalsProgressSpinner.setSelection(
            chosenFilterValueFromStorage
        )

        collapsedGoalCategories.addAll(collapsedCategoriesFromStorage.map { it.toInt() })
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            GoalProgress.All.value -> initializeGoalLayout(goalsLayout, goalsList)
            GoalProgress.Active.value -> initializeGoalLayout(
                goalsLayout,
                goalsList.filter { !it.getCompleted() })

            GoalProgress.Completed.value -> initializeGoalLayout(
                goalsLayout,
                goalsList.filter { it.getCompleted() })
        }
        storageHelper.addIntToStorage(getString(R.string.chosenFilterKey), position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onPause() {
        super.onPause()

        storageHelper.addStringSetToStorage(
            getString(R.string.collapsedCategoriesKey),
            collapsedGoalCategories.map { it.toString() })
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
        goals: List<Goal>
    ) {
        topIndexesByPriority = mutableListOf(0, 0, 0)
        layout.removeAllViews()
        addGoalsToLayout(layout, goals)
        for (i in goalsList.size - 1 downTo 0)
            topIndexesByPriority[goalsList[i].getPriority().value] = i
        changeGoalCategoryDisplay(collapsedGoalCategories)
        // reversing list index and decrement 1 for get previous position
    }

    /**
     * Добавляет цели в макет (LinearLayout)
     *
     * @param layout Макет для списка целей
     * @param goalsMap Список целей
     */
    private fun addGoalsToLayout(
        layout: LinearLayout,
        goalsList: List<Goal>
    ) {
        goalsList
            .distinctBy { it.getLabel() }
            .sortedWith(
                compareBy(
                    { it.getCompleted() },
                    { it.getPriority().value * -1 },
                    { it.getCreateDate() * -1 })
            )
            .groupBy { it.getCategory() }
            .toSortedMap()
            .forEach { (category, goals) ->
                addCategoryGoalsToLayout(
                    goals,
                    category,
                    layout,
                    collapsedGoalCategories
                )
            }
    }

    /**
     * Добавляет новую категорию в макет
     *
     * @param goals Список целей
     * @param category Категория целей
     * @param layout Макет
     */
    private fun addCategoryGoalsToLayout(
        goals: List<Goal>,
        category: GoalCategory,
        layout: LinearLayout,
        collapseCategoryIndexes: Set<Int>
    ) {
        val view = layoutInflater.inflate(R.layout.simple_category_view, layout, false)
        val goalsInCategoryLayout = view.findViewById<LinearLayout>(R.id.goalsLayout)
        val goalCategoryLabel = view.findViewById<LinearLayout>(R.id.simpleCategoryLabel)
        val goalCategoryIcon = view.findViewById<ImageView>(R.id.simpleCategoryImage)
        val categoryTitle = view.findViewById<TextView>(R.id.simpleCategoryName)
        var isCategoryOpened = !collapseCategoryIndexes.map { it % 10 }.contains(layout.childCount)

        view.tag = category.position % 10
        categoryTitle.text = category.value

        for (goal in goals) {
            val checkbox = createCheckboxWithPosition(goalsInCategoryLayout, goal)
            goalsInCategoryLayout.addView(checkbox.first, checkbox.second)
        }

        goalCategoryLabel.setOnClickListener {
            changeGoalCategoryDisplay(
                isCategoryOpened,
                category,
                goalCategoryIcon,
                goalsInCategoryLayout
            )
            isCategoryOpened = !isCategoryOpened
        }

        layout.addView(view)
    }

    private fun changeGoalCategoryDisplay(
        isOpened: Boolean,
        category: GoalCategory,
        icon: ImageView,
        layout: LinearLayout
    ) {
        icon.background = getDrawable(
            requireContext(),
            if (isOpened) R.drawable.add_icon
            else R.drawable.minus_icon
        )
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            if (isOpened) 0
            else LayoutParams.WRAP_CONTENT
        )
        if (isOpened) collapsedGoalCategories.add(category.position)
        else collapsedGoalCategories.remove(category.position)
    }

    private fun changeGoalCategoryDisplay(collapseCategoryIndexes: Set<Int>) {
        val categoriesInLayout = goalsLayout.children.toList()
        val indexes = collapsedGoalCategories.map { it % 10 }

        for (i in categoriesInLayout.indices)
            if (indexes.contains(categoriesInLayout[i].tag.toString().toInt())) {
                val categoryItem = categoriesInLayout[i] as LinearLayout;
                categoryItem.findViewById<ImageView>(R.id.simpleCategoryImage).background =
                    getDrawable(requireContext(), R.drawable.add_icon)
                categoryItem.findViewById<LinearLayout>(R.id.goalsLayout).layoutParams =
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
            }
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
    ): Pair<View, Int> {
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
        ).apply { bottomMargin = 6; topMargin = 6; leftMargin = 20 }

        viewCheckBox.let {
            it.layoutParams = layoutParameters
            it.text = goal.getLabel()
            it.tag = goal.getLabel()
            it.isChecked = goal.getCompleted()

        }

        viewCheckBox.setOnClickListener {
            customCheckboxHandleClick(viewCheckBox)
            val currentGoal = goalsList
                .find { goal -> goal.getLabel() == viewCheckBox.text.toString() }!!

            currentGoal.setCompleted(viewCheckBox.isChecked)

            initializeGoalLayout(goalsLayout, goalsList)
            changeCompletionInFirebase(currentGoal)
        }

        viewCheckBox.setOnLongClickListener {
            MainActivity.createModalWindow(
                requireContext(),
                "${getString(R.string.doYouWantToDeleteGoal)}\n\n(${viewCheckBox.text})",
                getString(R.string.submitDelete),
                getString(R.string.cancelButtonText),
                {
                    val goalIndex = goalsList.indexOf(
                        Goal(
                            viewCheckBox.text.toString(),
                            viewCheckBox.isChecked
                        )
                    )

                    removeGoalFromFirebase(goal)
                    SugarRecord.delete(goalsList[goalIndex])
                    goalsList.removeAt(goalIndex)
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
        val newGoalVariation = mutableListOf(
            Goal(name, false, priority, category),
            Goal(name, true, priority, category)
        )

        if (newGoalVariation.all { !goalsList.contains(it) }) {
            goalsList.add(newGoalVariation.first())
            createCheckboxWithPosition(goalsLayout, newGoalVariation.first())
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
        dialog.window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.attributes?.width = ViewGroup.LayoutParams.MATCH_PARENT
        }

        val dialogNameInput = dialog.window?.findViewById<EditText>(R.id.goalWizardName)!!
        val dialogButton =
            dialog.window?.findViewById<AppCompatButton>(R.id.goalWizardConfirmButton)!!
        val dialogPriorityList = dialog.window?.findViewById<Spinner>(R.id.goalWizardPriorityList)!!
        val dialogCategoryList = dialog.window?.findViewById<Spinner>(R.id.goalWizardCategoryList)!!

        dialogButton.setOnClickListener {
            if (dialogNameInput.text.trim().isNotEmpty()
                && dialogPriorityList.selectedItemPosition != 0
                && dialogCategoryList.selectedItemPosition != 0
            ) {
                val priority =
                    when ((dialogPriorityList.selectedView as MaterialTextView).text.toString()) {
                        view.context.resources.getStringArray(R.array.goalPriorities)[1] -> GoalPriority.Low
                        view.context.resources.getStringArray(R.array.goalPriorities)[2] -> GoalPriority.Middle
                        else -> GoalPriority.High
                    }

                val category =
                    when ((dialogCategoryList.selectedView as MaterialTextView).text.toString()) {
                        view.context.resources.getStringArray(R.array.goalCategories)[1] -> GoalCategory.Health
                        view.context.resources.getStringArray(R.array.goalCategories)[2] -> GoalCategory.Career
                        view.context.resources.getStringArray(R.array.goalCategories)[3] -> GoalCategory.Finance
                        view.context.resources.getStringArray(R.array.goalCategories)[4] -> GoalCategory.LifeBrightness
                        view.context.resources.getStringArray(R.array.goalCategories)[5] -> GoalCategory.Relationships
                        view.context.resources.getStringArray(R.array.goalCategories)[6] -> GoalCategory.FamilyAndFriends
                        view.context.resources.getStringArray(R.array.goalCategories)[7] -> GoalCategory.Materialist
                        view.context.resources.getStringArray(R.array.goalCategories)[8] -> GoalCategory.Selfbuilding
                        else -> GoalCategory.Other
                    }

                if (!goalsList.map { it.getLabel() }.contains(dialogNameInput.text.toString())) {

                    addGoalToStorage(dialogNameInput.text.toString(), priority, category)
                    saveGoalToFirebase(
                        dialogNameInput.text.toString(),
                        false,
                        Calendar.getInstance().time.time,
                        priority,
                        category
                    )
                    dialog.dismiss()

                    initializeGoalLayout(goalsLayout, goalsList)
                    onItemSelected(null, null, goalsProgressSpinner.selectedItemPosition, 0)
                } else
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.existingTarget),
                        Toast.LENGTH_LONG
                    ).show()
            } else
                Toast.makeText(
                    requireContext(),
                    getString(R.string.incorrectGoal),
                    Toast.LENGTH_LONG
                ).show()
        }

        dialog.create()
        dialog.show()
    }

    private fun saveGoalToFirebase(
        goalName: String,
        completed: Boolean,
        createDate: Long,
        priority: GoalPriority,
        category: GoalCategory
    ) {
        val user = Firebase.auth.currentUser ?: return
        val goal = Goal(goalName, completed, createDate, priority, category)

        databaseReference
            .child(
                "${getString(R.string.userFolderInDatabase)}" +
                        "/${user.uid}" +
                        "/${getString(R.string.userGoalsInDatabase)}"
            )
            .push()
            .setValue(goal.parseToString())
            .addOnSuccessListener {
            }
            .addOnFailureListener { }
    }

    private fun saveGoalToFirebase(
        goal: Goal
    ) {
        saveGoalToFirebase(
            goal.getLabel(),
            goal.getCompleted(),
            goal.getCreateDate(),
            goal.getPriority(),
            goal.getCategory()
        )
    }

    private fun removeGoalFromFirebase(goal: Goal) {
        val query = databaseReference
            .child(
                "${getString(R.string.userFolderInDatabase)}" +
                        "/${(Firebase.auth.currentUser ?: return).uid}" +
                        "/${getString(R.string.userGoalsInDatabase)}"
            )
            .orderByValue()
            .equalTo(goal.parseToString())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children)
                    item.ref
                        .removeValue()
                        .addOnSuccessListener {
                            query.removeEventListener(this)
                        }
                return
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        query.addValueEventListener(listener)
    }

    private fun changeCompletionInFirebase(goal: Goal) {
        databaseReference
            .child(
                "${getString(R.string.userFolderInDatabase)}" +
                        "/${currentUser?.uid}" +
                        "/${getString(R.string.userGoalsInDatabase)}"
            )
            .get()
            .addOnCompleteListener {
                saveGoalToFirebase(goal)
                removeGoalFromFirebase(goal.withChangedCompletion())
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}