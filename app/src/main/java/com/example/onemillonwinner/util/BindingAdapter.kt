package com.example.onemillonwinner.util

import android.view.View
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.onemillonwinner.R
import com.example.onemillonwinner.data.State
import com.example.onemillonwinner.data.questionResponse.TriviaResponse
import com.example.onemillonwinner.util.enum.SelectAnswer

@BindingAdapter(value = ["app:disableButton"])
fun disableButton(view: View, value: Boolean?) {
    if (value == true) {
        view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.red_200))
        view.isClickable = false
    } else {
        view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.blue_200))
    }
}

@BindingAdapter(value = ["app:rawAnswersColor"])
fun rawAnswersColor(view: View, selectedAnswer: SelectAnswer?) {
    when (selectedAnswer) {
        SelectAnswer.UNSELECTED_ANSWER -> view.background = ContextCompat
            .getDrawable(view.context, R.drawable.raw_unselected_answers_shape)
        SelectAnswer.CORRECT_ANSWER -> view.background = ContextCompat
            .getDrawable(view.context, R.drawable.raw_correct_answers_shape)
        SelectAnswer.INCORRECT_ANSWER -> view.background = ContextCompat
            .getDrawable(view.context, R.drawable.raw_incorrect_answers_shape)
        else -> view.background = ContextCompat
            .getDrawable(view.context, R.drawable.raw_unselected_answers_shape)
    }
}

@BindingAdapter("app:isLoading")
fun showWhenLoading(view: View, state: State<TriviaResponse>?) {
    if (state is State.Loading) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.INVISIBLE
    }
}

@BindingAdapter("app:isSuccess")
fun showWhenSuccess(view: View, state: State<TriviaResponse>?) {
    if (state is State.Success) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.INVISIBLE
    }
}

@BindingAdapter("app:isFail")
fun showWhenFail(view: View, state: State<TriviaResponse>?) {
    if (state is State.Failure) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.INVISIBLE
    }
}