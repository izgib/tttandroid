package com.example.game.tic_tac_toe.ui_components

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter


abstract class AbstractListAdapter<T>(list: List<T>) : BaseAdapter() {
    val objects: List<T> = list
    abstract override fun getView(position: Int, convertView: View?, parent: ViewGroup): View

    override fun getItem(position: Int): T = objects[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = objects.count()
}