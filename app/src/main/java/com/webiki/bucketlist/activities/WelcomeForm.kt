package com.webiki.bucketlist.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.snackbar.Snackbar
import com.webiki.bucketlist.R
import org.json.JSONArray
import java.util.*
import kotlin.math.ceil


class WelcomeForm : AppCompatActivity() {
    private lateinit var title: TextView
    private lateinit var answersLayout: LinearLayout
    private lateinit var mainLayout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sPrefEditor: SharedPreferences.Editor

    private var titles: MutableList<String> = mutableListOf()
    private var answerVariants: MutableList<MutableList<String>> = mutableListOf(mutableListOf())
    private val TRANSITION_DELAY = 200L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_form)

        mainLayout = findViewById(R.id.mainWelcomeLayout)
        title = findViewById(R.id.welcomeFormTitle)
        answersLayout = findViewById(R.id.welcomeFormAnswers)
        progressBar = findViewById(R.id.welcomeFormProgressBar)

        sharedPreferences = getSharedPreferences(R.string.sharedPreferencesName.toString(), Context.MODE_PRIVATE)
        sPrefEditor = sharedPreferences.edit()

        initQuestionData(titles, answerVariants)

        moveOnPage(0, titles, answerVariants)
    } //TODO handle exit ability from app (at now every back pressed move to start page)


    /**
     * Инициализирует поля вопросов анкеты: заголовок, ответы и т.д.
     *
     * @param titles Список заголовков
     * @param descriptions Список описаний
     * @param answerVariants Список вариантов ответа
     */
    private fun <T> initQuestionData(
        titles: MutableList<T>,
        answerVariants: MutableList<MutableList<T>>
    ) {
        val scan = Scanner(assets.open("doc.json"))
        val jsonLine = StringBuilder()

        while (scan.hasNext()) jsonLine.append(scan.nextLine())

        val questions = JSONArray(jsonLine.toString())

        for (i in 0 until JSONArray(jsonLine.toString()).length()) {
            val slideInformation = questions.getJSONObject(i)

            titles.add(slideInformation.getString("title") as T) // TODO: extract strings to file
            if (answerVariants.size == i) answerVariants.add(mutableListOf())

            for (j in 0 until slideInformation.getJSONArray("answers").length())
                answerVariants[i].add(j, slideInformation.getJSONArray("answers").get(j) as T)
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
    private fun <T> moveOnPage(
        pageNumber: Int,
        titles: MutableList<T>,
        answers: MutableList<MutableList<T>>
    ) {
        if (titles.size != answers.size || titles.size <= pageNumber)
            throw ArrayIndexOutOfBoundsException(getString(R.string.pageNumberMoreThanContentSize) + " $pageNumber")

        this.answersLayout.removeAllViews()
        title.text = titles[pageNumber].toString()

        for (i in 0 until answers[pageNumber].size) {
            val ctw = ContextThemeWrapper(this, R.style.questionnaire_button_style)
            val answerButton = AppCompatButton(ctw)
            val layoutParams =
                LinearLayout.LayoutParams(840, 160).apply { gravity = Gravity.CENTER }

            layoutParams.setMargins(5, 0, 5, 15)
            answerButton.text = answers[pageNumber][i].toString()
            answerButton.layoutParams = layoutParams
            answerButton.gravity = Gravity.CENTER
            answerButton.backgroundTintMode = PorterDuff.Mode.ADD
            answerButton.background = AppCompatResources.getDrawable(this, R.drawable.basic_button)

            answerButton.setOnClickListener {
                    onAnswerToQuestion(pageNumber + 1, titles, answers)
            }

            this.answersLayout.addView(answerButton)
        }
    }

    /**
     * Реагирует на ответ на вопрос анкеты
     *
     * @param pageNumber Номер страницы, на которую надо переместиться
     * @param questions Список вопросов
     * @param answers Список ответов
     * @param questionsCount Количество вопросов
     */
    private fun <T> onAnswerToQuestion(
        pageNumber: Int,
        questions: MutableList<T>,
        answers: MutableList<MutableList<T>>
    ) {
        progressBar.progress += ceil(100F / questions.size).toInt()
        if (pageNumber != questions.size){
            setOpacityToViews(true, TRANSITION_DELAY, title, answersLayout) // TODO make fade-in-out transition
            moveOnPage(pageNumber, questions, answers)
            setOpacityToViews(true, TRANSITION_DELAY, title, answersLayout)
        } else {
            sPrefEditor.putBoolean(getString(R.string.isUserPassedInitialQuestionnaire), true)
            sPrefEditor.commit()
            if (sharedPreferences.getBoolean(getString(R.string.isUserPassedInitialQuestionnaire), false))
                startActivity(Intent(this, MainActivity::class.java))
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
            
            if (!isVisible) Log.d("DEB", "")
            
            fadeOut.interpolator = AccelerateInterpolator()
            fadeOut.duration = transitionDelay
            
            it.startAnimation(fadeOut)
        }
    }
}