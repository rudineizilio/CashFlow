package pr.edu.utfpr.cashflow.list

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pr.edu.utfpr.cashflow.R
import pr.edu.utfpr.cashflow.database.DatabaseHelper

class LancamentosActivity : AppCompatActivity() {
    private lateinit var recyclerViewLancamentos: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var cashFlowAdapter: CashFlowAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lancamentos)

        recyclerViewLancamentos = findViewById(R.id.recyclerViewLancamentos)
        dbHelper = DatabaseHelper(this)

        recyclerViewLancamentos.layoutManager = LinearLayoutManager(this)

        val cashFlowList = getAllTransactions()

        cashFlowAdapter = CashFlowAdapter(cashFlowList)
        recyclerViewLancamentos.adapter = cashFlowAdapter
    }

    private fun getAllTransactions(): List<CashFlow> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "cash_flow",
            null,
            null,
            null,
            null,
            null,
            null
        )

        val listCashFlow = mutableListOf<CashFlow>()
        with(cursor) {
            while (moveToNext()) {
                val type = getString(getColumnIndexOrThrow("type"))
                val detail = getString(getColumnIndexOrThrow("detail"))
                val value = getDouble(getColumnIndexOrThrow("value"))
                val date = getString(getColumnIndexOrThrow("date"))

                listCashFlow.add(CashFlow(type, detail, value, date))
            }
        }
        cursor.close()
        return listCashFlow
    }
}