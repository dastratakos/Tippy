package edu.stanford.dstratak.tippy

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_payment.view.*
import java.time.format.DateTimeFormatter

class PaymentAdapter(private val context: Context, private val payments: ArrayList<Payment>)
    : RecyclerView.Adapter<PaymentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_payment, parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val payment = payments[position]
        holder.bind(payment)
    }

    override fun getItemCount() = payments.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(payment: Payment) {
            itemView.tvPaymentPerPerson.text = payment.perPersonAmount

            val formatter = DateTimeFormatter.ofPattern("EEE, M/d/yy 'at' h:mm a")
            val formatted = payment.date.format(formatter)
            itemView.tvPaymentDate.text = formatted

            val baseText = formatText("Base", payment.baseAmount)
            val tipText = formatText("Tip", payment.tipAmount) + "(${payment.tipPercent}%)"
            val splitText = formatText("Split", payment.split.toString())
            val perPersonText = formatText("Per Person", payment.perPersonAmount)
            val text = "$baseText, $tipText, $splitText, $perPersonText"
            itemView.tvPaymentCalculation.text = Html.fromHtml(text)
        }

        private fun formatText(description: String, value: String): String {
            return "<font color='#858585'>$description:</font> $value"
        }

    }
}
