package com.webiki.bucketlist

class MainActivityFunctions {

    /**
     * Переводит строку с разделителями в лист массивов слов
     *
     * @param text строки текста, разделённые переносами строк; слова в строке разделены сепаратором
     * @param separator разделитель слов
     * @return Лист с массивами слов строки
     * @exception IllegalArgumentException если передана пустая строка
     */
    fun convertLinesToArrays(text: String, separator: Char): List<Array<String>>
    {
        if (text.isEmpty()) throw IllegalArgumentException("Передана пустая строка")
        val lines = text.split('\n')
        val res = mutableListOf<Array<String>>()
        for (line in lines)
            res.add(line.split(separator) as Array<String>)
        return res
    }

    /**
     * Выдаёт число последовательности Фибоначи по его позиции
     *
     * @param position Позиция числа в последовательности (отсчёт с 0)
     * @return Число Фибоначи
     */
    fun searchFibonacciNumber(position: Int): Long {
        val sequence = mutableListOf(1L, 1)
        for (i in 0 until position - 1) {
            sequence.add(sequence[sequence.size - 1] + sequence[sequence.size - 2])
        }
        return sequence[sequence.size - 1]
    }

    /**
     * Выкидывает исключение с указанным сообщением
     *
     * @param message Сообщение об ошибке
     * @return Базовое исключение
     * @exception Exception всегда
     */
    fun throwException(message: String): Exception { throw Exception(message) }
}