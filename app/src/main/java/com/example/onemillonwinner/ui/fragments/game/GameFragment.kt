package com.example.onemillonwinner.ui.fragments.game

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onemillonwinner.R
import com.example.onemillonwinner.data.GameState
import com.example.onemillonwinner.databinding.FragmentGameBinding
import com.example.onemillonwinner.ui.base.BaseFragment

class GameFragment : BaseFragment<FragmentGameBinding>() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showAlertDialog()
                }
            })
    }

    private fun showAlertDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())

        dialogBuilder.apply {
            setTitle(R.string.dialog_title)
            setMessage(R.string.dialog_message)
            setIcon(R.drawable.ic_alert_icon)
            setPositiveButton(R.string.yes) { _, _ ->
                findNavController().popBackStack()
            }
            setNegativeButton(R.string.no) { p0, _ ->
                p0.cancel()
            }
        }.create().show()
    }


    override fun setup() {
        binding.gameViewModel = gameViewModel

        observeOnGameDone()
    }

    private fun observeOnGameDone() {
        gameViewModel.state.observe(viewLifecycleOwner) {
            it?.let {
                if (it == GameState.GameOver) {
                    gameViewModel.prize.value?.let { prize ->
                        findNavController().navigate(
                            GameFragmentDirections.actionGameFragmentToResultFragment(prize)
                        )
                    }
                }
            }
        }
    }

    override val layoutIdFragment = R.layout.fragment_game
}