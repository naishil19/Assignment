package com.example.expensetracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ExpenseAdapter(private val context: Context, private val expenses: List<Expense>) : BaseAdapter() {

    override fun getCount(): Int {
        return expenses.size
    }

    override fun getItem(position: Int): Any {
        return expenses[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)
        val expense = expenses[position]
        view.findViewById<TextView>(android.R.id.text1).text = expense.category
        view.findViewById<TextView>(android.R.id.text2).text = "₹${expense.amount}"
        return view
    }
}
