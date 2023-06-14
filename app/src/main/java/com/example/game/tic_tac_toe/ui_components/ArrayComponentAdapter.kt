package com.example.game.tic_tac_toe.ui_components

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter


abstract class AbstractSetAdapter<T>(private val set: Set<T>) : BaseAdapter() {
    var objects = set.toMutableList()
    private val size = objects.size

    abstract override fun getView(position: Int, convertView: View?, parent: ViewGroup): View

    override fun getItem(position: Int): T = objects[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = objects.count()

    override fun notifyDataSetChanged() {
        if (size == set.size) {
            if (objects.containsAll(set)) return

            set.forEachIndexed { index, element ->
                objects[index] = element
            }
        }
        objects = set.toMutableList()

        super.notifyDataSetChanged()
    }
}