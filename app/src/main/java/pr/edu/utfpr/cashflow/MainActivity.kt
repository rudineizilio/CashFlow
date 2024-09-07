package pr.edu.utfpr.cashflow

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pr.edu.utfpr.cashflow.database.DatabaseHelper
import pr.edu.utfpr.cashflow.databinding.ActivityMainBinding
import pr.edu.utfpr.cashflow.list.CashFlow
import pr.edu.utfpr.cashflow.list.LancamentosActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        dbHelper = DatabaseHelper(this)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val types = resources.getStringArray(R.array.tipo_array)

        val typesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        typesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTipo.adapter = typesAdapter

        binding.spTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val typeSelected = types[position]
                updateDetailsOptions(typeSelected)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //
            }
        }

        binding.btLancar.setOnClickListener {
            push()
        }

        binding.btVerLancamentos.setOnClickListener {
            val intent = Intent(this, LancamentosActivity::class.java)
            startActivity(intent)
        }

        binding.btSaldo.setOnClickListener {
            val symbols = DecimalFormatSymbols(Locale("pt", "BR"))
            val formatter = DecimalFormat("R$ #,##0.00", symbols)
            val formattedValue = formatter.format(getBalance() / 100.0).toString()

            Toast.makeText(this, "Saldo atual: ${formattedValue}", Toast.LENGTH_SHORT)
                .show()
        }

        binding.etValor.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (isFormatting) return

                isFormatting = true
                val originalString = s.toString()
                val cleanString = originalString.replace("R$|\\D".toRegex(), "")
                val parsed: Double = if (cleanString.isNotEmpty()) cleanString.toDouble() / 100 else 0.0

                val symbols = DecimalFormatSymbols(Locale("pt", "BR"))
                val formatter = DecimalFormat("R$ #,##0.00", symbols)
                val formattedString = formatter.format(parsed)

                binding.etValor.setText(formattedString)
                binding.etValor.setSelection(formattedString.length)
                isFormatting = false
            }
        })

        binding.etDataLancamento.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (isFormatting) return

                isFormatting = true
                val originalString = s.toString().replace("[^\\d]".toRegex(), "")
                val length = originalString.length
                val formattedString = when {
                    length <= 2 -> originalString
                    length <= 4 -> "${originalString.substring(0, 2)}/${originalString.substring(2)}"
                    else -> {
                        val day = originalString.substring(0, 2)
                        val month = originalString.substring(2, 4)
                        val year = originalString.substring(4, minOf(8, length))
                        if (year.length == 4) "$day/$month/$year" else "$day/$month/$year"
                    }
                }

                binding.etDataLancamento.setText(formattedString)
                binding.etDataLancamento.setSelection(formattedString.length)
                isFormatting = false
            }
        })
    }

    private fun push() {
        if (binding.etValor.text.isEmpty()) {
            binding.etValor.error = "Campo Valor é obrigatório"
            return
        }

        if (binding.etDataLancamento.text.isEmpty()) {
            binding.etDataLancamento.error = "Campo Data Lançamento é obrigatório"
            return
        }

        val typeSelected = binding.spTipo.selectedItem.toString()
        val detailSelected = binding.spDetalhe.selectedItem.toString()
        val value = binding.etValor.text.toString().replace("R$|\\D".toRegex(), "").toDoubleOrNull() ?: 0.0
        val date = binding.etDataLancamento.text.toString()
        dbHelper.insert(typeSelected, detailSelected, value, date)

        Toast.makeText(this, "Lançamento Efetuado!", Toast.LENGTH_SHORT)
            .show()

        binding.etValor.setText("")
        binding.etDataLancamento.setText("")
    }

    private fun updateDetailsOptions(type: String) {
        val detailsOptions = when (type) {
            "Crédito" -> resources.getStringArray(R.array.detalhes_credito_array)
            "Débito" -> resources.getStringArray(R.array.detalhes_debito_array)
            else -> arrayOf()
        }

        val adapterDetails = ArrayAdapter(this, android.R.layout.simple_spinner_item, detailsOptions)
        adapterDetails.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spDetalhe.adapter = adapterDetails
    }

    private fun getBalance(): Double {
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

        var balance = 0.0
        with(cursor) {
            while (moveToNext()) {
                val type = getString(getColumnIndexOrThrow("type"))
                val value = getDouble(getColumnIndexOrThrow("value"))
                if (type.equals("Crédito")) {
                    balance += value
                } else if (type.equals("Débito")) {
                    balance -= value
                }
            }
        }
        cursor.close()
        return balance
    }
}