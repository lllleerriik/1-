package com.webiki.bucketlist.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.orm.SugarRecord
import com.webiki.bucketlist.Goal
import com.webiki.bucketlist.ProjectSharedPreferencesHelper
import com.webiki.bucketlist.R
import com.webiki.bucketlist.enums.GoalCategory
import com.webiki.bucketlist.enums.GoalPriority
import org.json.JSONArray
import java.util.Scanner
import kotlin.math.ceil

class WelcomeForm : AppCompatActivity() {
    private lateinit var title: TextView
    private lateinit var answersLayout: LinearLayout
    private lateinit var mainLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var skipFormText: TextView

    private lateinit var storageHelper: ProjectSharedPreferencesHelper

    private var titles: MutableList<String> = mutableListOf()
    private var answerVariants: MutableList<MutableList<String>> = mutableListOf(mutableListOf())
    private var proposedGoals: MutableList<MutableList<MutableList<String>>> =
        mutableListOf(mutableListOf())
    private var chosenGoals: MutableList<MutableList<Goal>> = mutableListOf()
    private lateinit var databaseReference: DatabaseReference

    private var currentPage = 0
    private val TRANSITION_DELAY = 200L
    private val GOAL_NAME_SEPARATOR = "\$_$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_form)

        startActivity(Intent(this, GreetingActivity::class.java))

        mainLayout = findViewById(R.id.mainWelcomeLayout)
        title = findViewById(R.id.welcomeFormTitle)
        answersLayout = findViewById(R.id.welcomeFormAnswers)
        progressBar = findViewById(R.id.welcomeFormProgressBar)
        skipFormText = findViewById(R.id.welcomeFormSkipText)

        skipFormText.setOnClickListener { closeWelcomeForm() }

        storageHelper = ProjectSharedPreferencesHelper(this)

        parseFromJsonQuestionData(titles, answerVariants, proposedGoals)
    }

    override fun onBackPressed() {
        if (currentPage != 0) {
            chosenGoals.remove(chosenGoals.last())
            moveOnPage(--currentPage, titles, answerVariants)
        }
    }

    override fun onStart() {
        super.onStart()

        databaseReference = Firebase.database.reference
        moveOnPage(currentPage, titles, answerVariants)
    }

    /**
     * Инициализирует поля вопросов анкеты: заголовок, ответы и т.д.
     *
     * @param titles Список заголовков
     * @param answerVariants Список вариантов ответа
     */
    private fun <T> parseFromJsonQuestionData(
        titles: MutableList<T>,
        answerVariants: MutableList<MutableList<T>>,
        proposedGoals: MutableList<MutableList<MutableList<T>>>
    ) {
        val scan = Scanner(assets.open(getString(R.string.questionsFileName)))
        val jsonLine = StringBuilder()

        while (scan.hasNext()) jsonLine.append(scan.nextLine())
        scan.close()

        val questions = JSONArray(jsonLine.toString())

        for (i in 0 until JSONArray(jsonLine.toString()).length()) {
            val slideInformation = questions.getJSONObject(i)
            val title = slideInformation.getString(getString(R.string.questionsTitleKey)) as T
            val answers = slideInformation.getJSONArray(getString(R.string.questionsAnswersKey))
            val goals = slideInformation.getJSONObject(getString(R.string.questionsGoalsKey))

            titles.add(title)

            if (answerVariants.size == i) answerVariants.add(mutableListOf())
            if (proposedGoals.size == i) proposedGoals.add(mutableListOf(mutableListOf()))

            for (j in 0 until slideInformation.getJSONArray(getString(R.string.questionsAnswersKey))
                .length()) {
                answerVariants[i].add(j, answers[j] as T)
                val goalsOnAnswer = goals.getJSONArray(j.toString())

                if
                        (proposedGoals[i].size == j) proposedGoals[i].add(mutableListOf())

                for (k in 0 until goalsOnAnswer.length()) {
                    proposedGoals[i][j].add(k, goalsOnAnswer[k] as T)
                }
            }
        }
    }

    /** Заменяет заголовок, описание, (картинку), список вариантов ответов на n-ные
     * @param pageNumber Номер страницы (с 0)
     * @param titles Список заголовков
     * @param answers Список вариантов ответа
     * @exception ArrayIndexOutOfBoundsException Если номер страницы меньше либо равен длине переданной коллекции
     */
    private fun <T> switchQuestionData(
        pageNumber: Int,
        titles: MutableList<T>,
        answers: MutableList<MutableList<T>>
    ) {
        currentPage = pageNumber
        if (titles.size != answers.size || titles.size <= pageNumber)
            throw ArrayIndexOutOfBoundsException(getString(R.string.pageNumberMoreThanContentSize) + " $pageNumber")

        answersLayout.removeAllViews()
        title.text = titles[pageNumber].toString()

        for (i in 0 until answers[pageNumber].size) {
            val answerButton = layoutInflater.inflate(R.layout.basic_button, answersLayout, false)

            answerButton.let {
                (it as AppCompatButton).text = answers[pageNumber][i].toString()
            }

            answerButton.setOnClickListener {
                chosenGoals.add(proposedGoals[pageNumber][i].map { goalTitle ->
                    val goalName = goalTitle.split(GOAL_NAME_SEPARATOR)[0]
                    val goalCategory = goalTitle.split(GOAL_NAME_SEPARATOR)[1]

                    Goal(
                        goalName,
                        false,
                        GoalPriority.Middle,
                        enumValues<GoalCategory>().first { it.value == goalCategory })
                }.toMutableList())

                moveOnPage(pageNumber + 1, titles, answers)
            }

            progressBar.progress = ceil(100F * pageNumber / answers.size).toInt()
            answersLayout.addView(answerButton)
        }
    }

    /**
     * Реагирует на ответ на вопрос анкеты
     *
     * @param pageNumber Номер страницы, на которую надо переместиться
     * @param questions Список вопросов
     * @param answers Список ответов
     * @throws IllegalStateException Если не получилось сохранить результат
     */
    private fun <T> moveOnPage(
        pageNumber: Int,
        questions: MutableList<T>,
        answers: MutableList<MutableList<T>>
    ) {
        if (pageNumber != questions.size) {
            switchQuestionData(pageNumber, questions, answers)
// here there is current (correct) page number
            setOpacityToViews(true, TRANSITION_DELAY, title, answersLayout)
        } else closeWelcomeForm()
    }

    private fun closeWelcomeForm() {
        SugarRecord.deleteAll(Goal::class.java)
        chosenGoals.flatten().forEach { SugarRecord.save(it) }

        if (storageHelper.addBooleanToStorage(
                getString(R.string.isUserPassedInitialQuestionnaire),
                true
            )
        ) {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        } else
            throw IllegalStateException(getString(R.string.stateWasNotSave))
    }

    /**
     * Устанавливает параметр видимости группе элементов
     *
     * @param isVisible Будут ли видны элементы?
     * @param transitionDelay Скорость скрытия элементов (миллисекунды)
     * @param views Элементы
     * @return
     * @exception IllegalArgumentException Если список элементов пуст или скорость скрытия слишком большая
     */
    private fun setOpacityToViews(
        isVisible: Boolean,
        transitionDelay: Long,
        vararg views: View
    ) {
        if (views.isEmpty()) throw IllegalArgumentException(getString(R.string.emptySequence))
        if (transitionDelay < 100) throw IllegalArgumentException(getString(R.string.tooSmallDelay))

        views.forEach {
            val fadeOut: Animation = if (isVisible) AlphaAnimation(0f, 1f)
            else AlphaAnimation(1f, 0f)
            fadeOut.interpolator = AccelerateInterpolator()
            fadeOut.duration = transitionDelay

            it.startAnimation(fadeOut)
        }
    }
}