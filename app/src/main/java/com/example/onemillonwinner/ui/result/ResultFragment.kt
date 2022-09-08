package com.example.onemillonwinner.ui.result

import android.media.MediaPlayer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.onemillonwinner.R
import com.example.onemillonwinner.databinding.FragmentResultBinding
import com.example.onemillonwinner.ui.base.BaseFragment

class ResultFragment : BaseFragment<FragmentResultBinding, ResultViewModel>() {

    override val layoutIdFragment = R.layout.fragment_result
    override val viewModelClass = ResultViewModel::class.java
    private val arguments: ResultFragmentArgs by navArgs()

    override fun setup() {
        viewModel.setPrize(arguments.prize)

        navigateToGameFragment()
        navigateToHomeFragment()
        effectWhenDisplayTheResult()
    }

    private fun navigateToGameFragment() {
        viewModel.navigateGame.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.gameFragment)
        }
    }


    private fun navigateToHomeFragment() {
        viewModel.navigateHome.observe(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }


    private fun effectWhenDisplayTheResult() {
        if (arguments.prize == 0) {
            playMusic(R.raw.loss)
        } else {
            playMusic(R.raw.result_game_winner)
        }
    }

    private fun playMusic(resourcesId: Int) {
        val mediaPlayer = MediaPlayer.create(context, resourcesId)
        mediaPlayer.start()
    }
}