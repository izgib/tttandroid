package com.example.game.tic_tac_toe.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.tic_tac_toe.utils.SingleLiveEvent
import kotlinx.coroutines.launch

enum class DualResponse {
    Yes, No
}

class NavigationViewModel : ViewModel() {
    private val responseListener = SingleLiveEvent<DualResponse>()
    val dualResponse: LiveData<DualResponse> = responseListener

    fun result(response: DualResponse) {
        viewModelScope.launch {
            responseListener.value = response
        }
    }
}