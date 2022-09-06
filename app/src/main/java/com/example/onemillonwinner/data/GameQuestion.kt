package com.example.onemillonwinner.data

import com.example.onemillonwinner.data.questionResponse.Question
import com.example.onemillonwinner.util.extension.htmlText

class GameQuestion {
    private var questionDescription: String = ""
    private var questionNumber: Int = 0
    private var difficulty: String = ""
    private val answers: MutableList<Choice> = mutableListOf()
    private var selectedAnswer = -1
    private var correctAnswer: String = ""

    fun setQuestion(question: Question) {
        questionDescription = question.question?.htmlText() ?: ""
        difficulty = question.difficulty ?: ""
        answers.clear()
        question.incorrectAnswers?.forEach { incorrectAnswer ->
            answers.add(Choice(incorrectAnswer.htmlText(), ChoicesState.NOT_SELECTED))
        }
        question.correctAnswer?.let { correct ->
            correctAnswer = correct.htmlText()
            answers.add(Choice(correctAnswer, ChoicesState.NOT_SELECTED))
        }
        answers.shuffle()
    }

    fun getQuestion() = questionDescription

    fun setQuestionNumber(number: Int) {
        questionNumber = number
    }

    fun getQuestionNumber() = questionNumber

    fun getAnswers() = answers.toList()

    fun removeWrongAnswer(index: Int): Boolean {
        return if (index in answers.indices
            && answers[index].answer != correctAnswer
            && answers[index].answer.isNotBlank()
        ) {
            answers[index] = Choice("",ChoicesState.DISABLE_SELECTION)

            true
        } else {
            false
        }
    }

    fun getDifficulty() = difficulty

    fun getCorrectAnswer() = correctAnswer

    fun getSelectedAnswer() = selectedAnswer

    fun setSelectedAnswer(index: Int) {
        selectedAnswer = index
    }
}