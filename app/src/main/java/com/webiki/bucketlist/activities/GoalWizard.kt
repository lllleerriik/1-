package com.webiki.bucketlist.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.webiki.bucketlist.R


class GoalWizard : AppCompatActivity() {

    private lateinit var label: EditText
    private lateinit var description: EditText
    private lateinit var date: EditText
    private lateinit var deleteButton: Button
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_wizard)

        label = findViewById(R.id.goalWizardLabel)
        description = findViewById(R.id.goalWizardDescription)
        date = findViewById(R.id.goalWizardDate)
        deleteButton = findViewById(R.id.goalWizardDeleteButton)
        saveButton = findViewById(R.id.goalWizardSaveButton)
    }

    override fun onStart() {
        super.onStart()

        date.setOnFocusChangeListener{it, hasFocus -> if (hasFocus) handleDateClick(it as EditText) }
        date.setOnClickListener{ handleDateClick(it as EditText) }

        saveButton.setOnClickListener { handleButtonClick(it as Button) }
        deleteButton.setOnClickListener { handleButtonClick(it as Button) }
    }

    private fun handleDateClick(view: EditText) {
        view.text = getDateViaPickerDialog(view.context)
    }

    private fun handleButtonClick(view: Button) {
        finishActivityWithData(view)
    }

    /**
     * Закрывает Activity, выдавая инициатору данные о текущей цели
     * @param view Кнопка, нажатая на Activity
     */
    private fun finishActivityWithData(view: Button) {
        val returnIntent = Intent()
        val sep = getString(R.string.goalPartsSeparator)

        returnIntent.putExtra(
            getString(R.string.newGoalKey),
            "${label.text}$sep${description.text}$sep${date.text}"
        )

        when (view.id) {
            R.id.goalWizardSaveButton -> setResult(RESULT_OK, returnIntent)
            R.id.goalWizardDeleteButton -> setResult(RESULT_CANCELED, returnIntent)
        }

        finish()
    }

    /**
     * Отдаёт дату, выбранную в диалоговом окне
     * @param ctx Контекст вызова диалогового окна
     * @return Строка в формате день.месяц.год
     */
    private fun getDateViaPickerDialog(ctx: Context): Editable {
        var result = String()
        val datePickerDialog = DatePickerDialog(ctx)

        datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
            result = "$dayOfMonth.${month}.$year"
        }
        datePickerDialog.show()

        return SpannableStringBuilder(result)
    }
}