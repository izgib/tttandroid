package com.example.game.tic_tac_toe.ui_components

import kotlinx.coroutines.flow.Flow

interface UIComponent<T> {
    fun getUserInteractionEvents(): Flow<T>
}