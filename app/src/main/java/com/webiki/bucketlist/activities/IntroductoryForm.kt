package com.webiki.bucketlist.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import com.webiki.bucketlist.R
import org.json.JSONArray
import java.util.Scanner

/**
 * Class for creating smth like SPA
 */
class IntroductoryForm : AppCompatActivity() {
    private lateinit var welcomeFormTitle: TextView
    private lateinit var welcomeFormImage: ImageView
    private lateinit var welcomeFormDescription: TextView
    private lateinit var welcomeFormAnswers: LinearLayout
    private lateinit var welcomeFormLayout: ConstraintLayout
    private lateinit var welcomeFormResultButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sPrefEditor: SharedPreferences.Editor

    private lateinit var titles: MutableList<String>
    private lateinit var descriptions: MutableList<String>
    private lateinit var answers: MutableList<MutableList<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introductory_form)

        welcomeFormTitle = findViewById(R.id.welcomeFormTitle)
        welcomeFormImage = findViewById(R.id.welcomeFormImage)
        welcomeFormDescription = findViewById(R.id.welcomeFormDescription)
        welcomeFormAnswers = findViewById(R.id.welcomeFormAnswers)
        welcomeFormLayout = findViewById(R.id.mainWelcomeLayout)
        welcomeFormResultButton= findViewById(R.id.welomeFormResultButton)
        sharedPreferences = getSharedPreferences(R.string.sharedPreferencesName.toString(), Context.MODE_PRIVATE)
        sPrefEditor = sharedPreferences.edit()

        initQuestionData(titles, descriptions, answers)

        moveOnPage(0, titles, descriptions, answers)

        welcomeFormResultButton.setOnClickListener {
            sPrefEditor.putBoolean(R.string.isUserPassedInitialQuestionnaire.toString(), true)
            sPrefEditor.commit()
        }
    }

    /**
     * Инициализирует поля вопросов анкеты: заголовок, ответы и т.д.
     *
     * @param titles Список заголовков
     * @param descriptions Список описаний
     * @param answers Список вариантов ответа
     */
    private fun <T> initQuestionData(titles: MutableList<T>, descriptions: MutableList<T>, answers: MutableList<MutableList<T>>) {
        val scan = Scanner(assets.open("doc.json"))
        val jsonLine = StringBuilder()

        while (scan.hasNext()) jsonLine.append(scan.nextLine())

        val questions = JSONArray(jsonLine.toString())

        for (i in 0 until JSONArray(jsonLine.toString()).length()) {
            val question = questions.getJSONObject(i)
            titles.add(question.getString("1") as T) // TODO: extract strings to file
            descriptions.add(question.getString("2") as T)
            for (j in 0 until question.getJSONArray("3").length())
                answers[i].add(question.getJSONArray("3").get(j) as T)

        }
    }

    /** Заменяет заголовок, описание, (картинку), список вариантов ответов на n-ные
     * @param pageNumber номер страницы (с 0)
     * @param titles список заголовков
     * @param descriptions список описаний
     * @param answers список вариантов ответа
     * @exception ArrayIndexOutOfBoundsException если номер страницы меньше либо равен длине переданной коллекции
     */
    private fun <T> moveOnPage(
        pageNumber: Int,
        titles: List<T>,
        descriptions: List<T>,
        answers: List<List<T>>
    ) {
        val isLastPage = pageNumber + 1 == titles.size

        if (titles.size != descriptions.size
            || descriptions.size != answers.size
            || titles.size <= pageNumber)
            throw ArrayIndexOutOfBoundsException(R.string.pageNumberMoreThanContentSize.toString() + " $pageNumber")

        welcomeFormAnswers.removeAllViews()
        welcomeFormTitle.text = titles[pageNumber].toString()
        welcomeFormDescription.text = descriptions[pageNumber].toString()

        for (i in 0 until answers[pageNumber].size) {
            val answerButton = Button(this)
            val layoutParams = MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            val p = pageNumber + if (isLastPage) 0 else 1

            layoutParams.setMargins(5, 0, 5, 15)
            answerButton.text = answers[pageNumber][i].toString()
            answerButton.layoutParams = layoutParams
            answerButton.background = R.color.purple_700.toDrawable()
            answerButton.setOnClickListener { moveOnPage(p, titles,
                this.descriptions, answers) }

            welcomeFormAnswers.addView(answerButton)
        }

        if (isLastPage) {
            welcomeFormResultButton.isVisible = true
            welcomeFormResultButton.setOnClickListener { finishActivity(0) }
        }
    }
}