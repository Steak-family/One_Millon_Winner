package com.example.onemillonwinner.ui.fragments.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.onemillonwinner.data.*
import com.example.onemillonwinner.data.questionResponse.TriviaResponse
import com.example.onemillonwinner.network.Repository
import com.example.onemillonwinner.ui.base.BaseViewModel
import com.example.onemillonwinner.util.Constants.QUESTION_TIME
import com.example.onemillonwinner.util.extension.addTo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class GameViewModel : BaseViewModel() {

    private val triviaQuestions: TriviaQuestion by lazy { TriviaQuestion() }
    private val repository: Repository by lazy { Repository() }
    private lateinit var timerDisposable: Disposable

    val isChangeQuestion = MutableLiveData(false)
    val isDeleteHalfOfAnswers = MutableLiveData(false)
    val isHelpByFriends = MutableLiveData(false)


    private val _gameState = MutableLiveData<GameState>()
    val state: LiveData<GameState>
        get() = _gameState


    private val _questionState = MutableLiveData<QuestionState>()
    val questionState: LiveData<QuestionState> = _questionState

    private val _question = MutableLiveData<GameQuestion>()
    val question: LiveData<GameQuestion>
        get() = _question

    private val _questionTime = MutableLiveData(QUESTION_TIME)
    val questionTime: LiveData<Int>
        get() = _questionTime

    private val _prize = MutableLiveData(0)
    val prize: LiveData<Int>
        get() = _prize


    init {
        _gameState.postValue(GameState.Loading)
        repository.getAllQuestions().observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(::onSuccessUpdateQuestion, ::onErrorUpdateQuestion).addTo(disposable)
    }

    private fun onSuccessUpdateQuestion(state: State<TriviaResponse>) {
        _gameState.postValue(GameState.Success)
        startTimer()
        state.toData()?.let {
            triviaQuestions.setQuestions(it.questions)
            updateView()
        }
    }

    private fun onErrorUpdateQuestion(throwable: Throwable) {
        _gameState.postValue(GameState.Failure)
    }

    fun onClickToUpdateView() {
        _questionState.value?.let { state ->
            when (state) {
                QuestionState.QUESTION_SUBMITTED -> {
                    updateView()
                }
                QuestionState.WRONG_ANSWER_SUBMITTED -> {
                    _questionState.postValue(QuestionState.GAME_OVER)
                }
                QuestionState.QUESTION_START -> {
                    if (isAnyChoiceSelected()) {
                        showAnswer()
                    } else {
                        null
                    }
                }
                QuestionState.GAME_OVER -> {
                    null
                }
            }
        }
    }

    private fun calculatePrize() {
        _prize.postValue(triviaQuestions.getPrize())
    }

    private fun showAnswer() {
        displayCorrectAnswer(triviaQuestions.getIndexCorrectAnswer())
        setGameState()
        calculatePrize()
        _question.postValue(triviaQuestions.getCurrentQuestion())
        timerDisposable.dispose()
    }

    private fun setGameState() {
        if (triviaQuestions.isGameOver())
            _questionState.postValue(QuestionState.WRONG_ANSWER_SUBMITTED)
        else
            _questionState.postValue(QuestionState.QUESTION_SUBMITTED)
    }

    private fun updateView() {
        if (!triviaQuestions.isGameOver()) {
            _questionState.postValue(QuestionState.QUESTION_START)
            _question.postValue(triviaQuestions.updateQuestion())
            restartTimer()
            choices.postValue(triviaQuestions.getAnswersCurrentQuestion())
        } else {
            timerDisposable.dispose()
            _questionState.postValue(QuestionState.GAME_OVER)
        }
    }

    fun replaceQuestion() {
        if (_questionState.value != QuestionState.QUESTION_SUBMITTED
            && _questionState.value != QuestionState.WRONG_ANSWER_SUBMITTED
        ) {
            _questionState.postValue(QuestionState.QUESTION_START)
            isChangeQuestion.postValue(true)
            _question.postValue(triviaQuestions.replaceQuestion())
            restartTimer()
            choices.postValue(triviaQuestions.getAnswersCurrentQuestion())
        }
    }

    fun deleteHalfOfAnswers() {
        if (_questionState.value != QuestionState.QUESTION_SUBMITTED
            && _questionState.value != QuestionState.WRONG_ANSWER_SUBMITTED
        ) {
            _questionState.postValue(QuestionState.QUESTION_START)
            isDeleteHalfOfAnswers.postValue(true)
            choices.postValue(triviaQuestions.deleteTwoWrongAnswersRandomly())
            _question.postValue(triviaQuestions.getCurrentQuestion())
        }
    }

    private fun restartTimer() {
        timerDisposable.dispose()
        startTimer()
    }

    fun helpByFriends() {
        isHelpByFriends.postValue(true)
    }

    private fun startTimer() {
        _questionTime.postValue(QUESTION_TIME)
        val timeInSecond: Long = QUESTION_TIME.toLong()
        timerDisposable = Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .take(timeInSecond).map {
                ((timeInSecond - 1) - it)
            }.subscribe {
                _questionTime.postValue(it.toInt())
                if (it.toInt() == 0) {
                    endTheCountDown()
                }
            }
    }

    private fun endTheCountDown() {
        timerDisposable.dispose()
        triviaQuestions.getCurrentQuestion().setSelectedAnswer(-1)
        _prize.postValue(triviaQuestions.getPrize())
        _questionState.postValue(QuestionState.GAME_OVER)
    }

    fun getFriendHelp() = triviaQuestions.getFriendHelp()

    // ********************* the New way **********************\\
    val choices = MutableLiveData(triviaQuestions.getAnswersCurrentQuestion())

    fun isAnyChoiceSelected() = triviaQuestions.getAnswersCurrentQuestion().any { it.state == ChoicesState.SELECTED }

    private fun displayCorrectAnswer(correctChoiceIndex: Int) {
        val choiceIndex = triviaQuestions.getAnswersCurrentQuestion().indexOfFirst { it.state == ChoicesState.SELECTED }
        triviaQuestions.getAnswersCurrentQuestion().forEachIndexed { index, choice ->
            if (correctChoiceIndex == index) {
                choice.state = ChoicesState.CORRECT
            } else if (choiceIndex == index) {
                choice.state = ChoicesState.WRONG
            } else {
                choice.state = ChoicesState.DISABLE_SELECTION
            }
        }
        choices.postValue(triviaQuestions.getAnswersCurrentQuestion())
    }


    fun updateChoice(choiceNumber: Int) {
        triviaQuestions.getCurrentQuestion().setSelectedAnswer(choiceNumber)
        triviaQuestions.getAnswersCurrentQuestion().forEachIndexed { index, choice ->
            if (index == choiceNumber) {
                choice.state = ChoicesState.SELECTED
            } else if (choice.state == ChoicesState.DISABLE_SELECTION) {
                choice.state = ChoicesState.DISABLE_SELECTION
            } else {
                choice.state = ChoicesState.NOT_SELECTED
            }
        }
        choices.postValue(triviaQuestions.getAnswersCurrentQuestion())
    }
}