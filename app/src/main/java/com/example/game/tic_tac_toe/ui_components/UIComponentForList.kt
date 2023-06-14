package com.example.game.tic_tac_toe.ui_components

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class UIComponentForList<S>(itemView: ViewGroup): RecyclerView.ViewHolder(itemView)

abstract class ListComponentAdapter<S>() : RecyclerView.Adapter<UIComponentForList<S>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = getComponentForList(viewType)

    abstract override fun onBindViewHolder(holder: UIComponentForList<S>, position: Int)

    abstract fun getComponentForList(viewType: Int): UIComponentForList<S>

    //protected abstract fun getItem(position: Int): S
}