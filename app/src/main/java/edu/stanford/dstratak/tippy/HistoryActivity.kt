package edu.stanford.dstratak.tippy

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_history.*


private const val TAG = "HistoryActivity"

class HistoryActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val payments = intent.getParcelableArrayListExtra<Payment>("payments")
        rvHistory.adapter = payments?.let { PaymentAdapter(this, it) }
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.addItemDecoration(
            DividerItemDecoration(
                rvHistory.context,
                DividerItemDecoration.VERTICAL
            )
        )
    }
}