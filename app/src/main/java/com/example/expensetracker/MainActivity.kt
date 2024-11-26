package com.example.expensetracker

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

data class Expense(
    val amount: Double,
    val category: String,
    val timestamp: Long = System.currentTimeMillis() // Store the current time
)

class MainActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etCategory: EditText
    private lateinit var btnAddExpense: Button
    private lateinit var tvTotalSpending: TextView
    private lateinit var btnDay: Button
    private lateinit var btnWeek: Button
    private lateinit var btnMonth: Button
    private lateinit var lvExpenses: ListView

    private val expenseList = mutableListOf<Expense>()
    private lateinit var adapter: ExpenseAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    private var selectedPosition: Int? = null // For updating an expense

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        etAmount = findViewById(R.id.etAmount)
        etCategory = findViewById(R.id.etCategory)
        btnAddExpense = findViewById(R.id.btnAddExpense)
        tvTotalSpending = findViewById(R.id.tvTotalSpending)
        btnDay = findViewById(R.id.btnDay)
        btnWeek = findViewById(R.id.btnWeek)
        btnMonth = findViewById(R.id.btnMonth)
        lvExpenses = findViewById(R.id.lvExpenses)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ExpenseTrackerPrefs", MODE_PRIVATE)

        // Initialize ListView adapter with custom adapter
        adapter = ExpenseAdapter(this, expenseList)
        lvExpenses.adapter = adapter

        // Load expenses from SharedPreferences
        loadExpenses()

        // Button click listener for adding expenses
        btnAddExpense.setOnClickListener {
            val amountText = etAmount.text.toString()
            val categoryText = etCategory.text.toString()

            if (amountText.isNotBlank() && categoryText.isNotBlank()) {
                val amount = amountText.toDouble()
                if (selectedPosition != null) {
                    updateExpense(amount, categoryText) // Update existing expense
                } else {
                    addExpense(amount, categoryText) // Add new expense
                }
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Event listeners for summary buttons
        btnDay.setOnClickListener {
            val total = calculateTotalSpending(getStartOfDay())
            tvTotalSpending.text = "Total Spending Today: ₹$total"
        }

        btnWeek.setOnClickListener {
            val total = calculateTotalSpending(getStartOfWeek())
            tvTotalSpending.text = "Total Spending This Week: ₹$total"
        }

        btnMonth.setOnClickListener {
            val total = calculateTotalSpending(getStartOfMonth())
            tvTotalSpending.text = "Total Spending This Month: ₹$total"
        }

        // Long click listener for deleting an expense
        lvExpenses.setOnItemLongClickListener { _, _, position, _ ->
            val expenseToDelete = expenseList[position]
            deleteExpense(expenseToDelete)
            true
        }

        // Click listener for selecting an expense to update
        lvExpenses.setOnItemClickListener { _, _, position, _ ->
            val selectedExpense = expenseList[position]
            etAmount.setText(selectedExpense.amount.toString())
            etCategory.setText(selectedExpense.category)
            selectedPosition = position // Set position for updating
        }

    }

    private fun addExpense(amount: Double, category: String) {
        // Add expense to the list
        val expense = Expense(amount, category)
        expenseList.add(expense)

        // Notify adapter of data change
        adapter.notifyDataSetChanged()

        // Save expenses to SharedPreferences
        saveExpenses()

        // Clear input fields
        etAmount.text.clear()
        etCategory.text.clear()
    }

    private fun updateExpense(amount: Double, category: String) {
        selectedPosition?.let { position ->
            val updatedExpense = Expense(amount, category, expenseList[position].timestamp)
            expenseList[position] = updatedExpense // Update the expense in the list

            // Notify adapter of data change
            adapter.notifyDataSetChanged()

            // Save updated list to SharedPreferences
            saveExpenses()

            // Clear input fields and reset the selected position
            etAmount.text.clear()
            etCategory.text.clear()
            selectedPosition = null
        }
    }

    private fun deleteExpense(expense: Expense) {
        expenseList.remove(expense) // Remove the expense from the list

        // Notify adapter of data change
        adapter.notifyDataSetChanged()

        // Save the updated list to SharedPreferences
        saveExpenses()
    }

    private fun calculateTotalSpending(startTime: Long): Double {
        return expenseList
            .filter { it.timestamp >= startTime }
            .sumOf { it.amount }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Save the expenses list to SharedPreferences
    private fun saveExpenses() {
        val editor = sharedPreferences.edit()
        val expensesJson = gson.toJson(expenseList)
        editor.putString("expenses", expensesJson)
        editor.apply()
    }

    // Load expenses from SharedPreferences
    private fun loadExpenses() {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<List<Expense>>() {}.type
        val savedExpenses: List<Expense> = gson.fromJson(expensesJson, type)
        expenseList.clear()
        expenseList.addAll(savedExpenses)

        // Notify the adapter to refresh the list
        adapter.notifyDataSetChanged()
    }
}
