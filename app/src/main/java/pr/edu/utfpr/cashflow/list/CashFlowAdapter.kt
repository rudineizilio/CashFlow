package pr.edu.utfpr.cashflow.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pr.edu.utfpr.cashflow.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class CashFlowAdapter (private val cashFlowList: List<CashFlow>) :
    RecyclerView.Adapter<CashFlowAdapter.CashFlowViewHolder>() {

        class CashFlowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvType: TextView = itemView.findViewById(R.id.tvTipo)
            val tvDetail: TextView = itemView.findViewById(R.id.tvDetalhe)
            val tvValue: TextView = itemView.findViewById(R.id.tvValor)
            val tvDate: TextView = itemView.findViewById(R.id.tvData)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CashFlowViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lancamento, parent, false)
            return CashFlowViewHolder(view)
        }

        override fun onBindViewHolder(holder: CashFlowViewHolder, position: Int) {
            val transaction = cashFlowList[position]
            val symbols = DecimalFormatSymbols(Locale("pt", "BR"))
            val formatter = DecimalFormat("R$ #,##0.00", symbols)
            val formattedValue = formatter.format(transaction.value / 100.0)

            holder.tvType.text = "Tipo: ${transaction.type}"
            holder.tvDetail.text = "Detalhe: ${transaction.details}"
            holder.tvValue.text = "Valor: $formattedValue"
            holder.tvDate.text = "Data: ${transaction.date}"
        }

        override fun getItemCount(): Int = cashFlowList.size
}