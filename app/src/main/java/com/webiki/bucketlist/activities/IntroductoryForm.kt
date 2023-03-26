package com.webiki.bucketlist.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.webiki.bucketlist.R
import org.json.JSONArray
import java.util.Scanner

/**
 * Class for creating smth like SPA
 */
class IntroductoryForm : AppCompatActivity() {
    private lateinit var welcomeFormTitle: TextView
    private lateinit var welcomeFormAnswers: LinearLayout
    private lateinit var welcomeFormLayout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sPrefEditor: SharedPreferences.Editor

    private var titles: MutableList<String> = mutableListOf()
    private var answerVariants: MutableList<MutableList<String>> = mutableListOf(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introductory_form)

        welcomeFormTitle = findViewById(R.id.welcomeFormTitle)
        welcomeFormAnswers = findViewById(R.id.welcomeFormAnswers)
        welcomeFormLayout = findViewById(R.id.mainWelcomeLayout)
        sharedPreferences = getSharedPreferences(R.string.sharedPreferencesName.toString(), Context.MODE_PRIVATE)
        sPrefEditor = sharedPreferences.edit()
        initQuestionData(titles, answerVariants)

        moveOnPage(0, titles, answerVariants)
    }

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

            for (j in 0 until slideInformation.getJSONArray("answers").length())
                answerVariants[i].add(j, slideInformation.getJSONArray("answers").get(j) as T)
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
        answers: List<List<T>>
    ) {
        val isLastPage = pageNumber + 1 == titles.size //plug

        Log.d("DEB", titles.size.toString())
        Log.d("DEB", answers.size.toString())

        if (titles.size != answers.size || titles.size <= pageNumber)
            throw ArrayIndexOutOfBoundsException(getString(R.string.pageNumberMoreThanContentSize) + " $pageNumber")

        welcomeFormAnswers.removeAllViews()
        welcomeFormTitle.text = titles.get(pageNumber)?.toString()

        for (i in 0 until answers[pageNumber].size) {
            val answerButton = Button(this)
            val layoutParams = MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            val p = pageNumber + if (isLastPage) 0 else 1 //plug

            layoutParams.setMargins(5, 0, 5, 15)
            answerButton.text = answers[pageNumber][i].toString()
            answerButton.layoutParams = layoutParams
            answerButton.background = R.color.purple_dark.toDrawable() //TODO reverse to theme color
            answerButton.setOnClickListener { moveOnPage(p, titles, answers) }

            welcomeFormAnswers.addView(answerButton)
        }
    }
}