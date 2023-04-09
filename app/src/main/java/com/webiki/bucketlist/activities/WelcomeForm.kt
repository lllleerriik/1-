package com.webiki.bucketlist.activities

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.marginBottom
import com.webiki.bucketlist.ProjectSharedPreferencesHelper
import com.webiki.bucketlist.R
import org.json.JSONArray
import java.lang.reflect.Type
import java.util.*
import kotlin.math.ceil

@Suppress("UNCHECKED_CAST")
class WelcomeForm : AppCompatActivity() {
    private lateinit var title: TextView
    private lateinit var answersLayout: LinearLayout
    private lateinit var mainLayout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var storageHelper: ProjectSharedPreferencesHelper

    private var titles: MutableList<String> = mutableListOf()
    private var answerVariants: MutableList<MutableList<String>> = mutableListOf(mutableListOf())
    private var proposedGoals: MutableList<MutableList<String>> = mutableListOf(mutableListOf())
    private var chosenGoals: MutableList<String> = mutableListOf() // TODO change to Goal class

    private var currentPage = 0
    private val TRANSITION_DELAY = 200L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_form)

        mainLayout = findViewById(R.id.mainWelcomeLayout)
        title = findViewById(R.id.welcomeFormTitle)
        answersLayout = findViewById(R.id.welcomeFormAnswers)
        progressBar = findViewById(R.id.welcomeFormProgressBar)

        storageHelper = ProjectSharedPreferencesHelper(this)

        parseFromJsonQuestionData(titles, answerVariants, proposedGoals)
    }

    override fun onBackPressed() {
        if (currentPage != 0) moveOnPage(--currentPage, titles, answerVariants)
    }

    override fun onStart() {
        super.onStart()

        moveOnPage(currentPage, titles, answerVariants)
    }

    /**
     * Инициализирует поля вопросов анкеты: заголовок, ответы и т.д.
     *
     * @param titles Список заголовков
     * @param descriptions Список описаний
     * @param answerVariants Список вариантов ответа
     */
    private fun <T> parseFromJsonQuestionData(
        titles: MutableList<T>,
        answerVariants: MutableList<MutableList<T>>,
        proposedGoals: MutableList<MutableList<T>>
    ) {
        val scan = Scanner(assets.open(getString(R.string.questionsFileName)))
        val jsonLine = StringBuilder()

        while (scan.hasNext()) jsonLine.append(scan.nextLine())
        scan.close()

        val questions = JSONArray(jsonLine.toString())

        for (i in 0 until JSONArray(jsonLine.toString()).length()) {
            val slideInformation = questions.getJSONObject(i)
            val title   = slideInformation.getString(getString(R.string.questionsTitleKey)) as T
            val answers = slideInformation.getJSONArray(getString(R.string.questionsAnswersKey))
            val goals   = slideInformation.getJSONObject(getString(R.string.questionsGoalsKey))

            titles.add(title)

            if (answerVariants.size == i) answerVariants.add(mutableListOf())

            for (j in 0 until slideInformation.getJSONArray(getString(R.string.questionsAnswersKey)).length()) {
                answerVariants[i].add(j, answers[j] as T)
                val goalsOnAnswer = goals.getJSONArray(j.toString())

                if (proposedGoals.size == j) proposedGoals.add(mutableListOf())

                for (k in 0 until goalsOnAnswer.length()) {
                    proposedGoals[j].add(k, goalsOnAnswer[k] as T)
                }
            }
        }
    }

    /** Заменяет заголовок, описание, (картинку), список вариантов ответов на n-ные
     * @param pageNumber Номер страницы (с 0)
     * @param titles Список заголовков
     * @param descriptions Список описаний
     * @param answers Список вариантов ответа
     * @param transitionSpeed Скорость перехода на новую страницу
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

        this.answersLayout.removeAllViews()
        title.text = titles[pageNumber].toString()

        for (i in 0 until answers[pageNumber].size) {
            val answerButton = layoutInflater.inflate(R.layout.basic_button, answersLayout, false)

            answerButton.let { (it as AppCompatButton).text = answers[pageNumber][i].toString() }

            answerButton.setOnClickListener {
                moveOnPage(pageNumber + 1, titles, answers)
            }

            progressBar.progress = ceil(100F * pageNumber / answers.size).toInt()
            this.answersLayout.addView(answerButton)
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
        if (pageNumber != questions.size){
            switchQuestionData(pageNumber, questions, answers)
            Log.d("DEB", currentPage.toString()) // here there is current (correct) page number
            setOpacityToViews(true, TRANSITION_DELAY, title, answersLayout)
        } else {
            storageHelper.addBooleanToStorage(getString(R.string.isUserPassedInitialQuestionnaire), true)



            if (storageHelper.getBooleanFromStorage(getString(R.string.isUserPassedInitialQuestionnaire), false))
                startActivity(Intent(this, MainActivity::class.java))
            else
                throw IllegalStateException(getString(R.string.stateWasNotSave))
        }
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