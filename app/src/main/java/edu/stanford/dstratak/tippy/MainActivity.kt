package edu.stanford.dstratak.tippy

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.icu.util.Currency
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import edu.stanford.dstratak.tippy.Payment as Payment


private const val TAG = "MainActivity"
private const val INITIAL_TIP_PERCENT = 15
private const val INITIAL_SPLIT = 1
private var payments = mutableListOf<Payment>()

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBarTip.progress = INITIAL_TIP_PERCENT
        tvTipPercent.text = "$INITIAL_TIP_PERCENT%"
        updateTipDescription(INITIAL_TIP_PERCENT)
        tvSplit.text = "$INITIAL_SPLIT"
        seekBarSplit.progress = INITIAL_SPLIT - 1
        var dollarString: String = ""
        var oldTip = INITIAL_TIP_PERCENT

        seekBarTip.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, "onProgressChanged (seekBarTip) $progress")
                tvTipPercent.text = "$progress%"
                updateTipDescription(progress)
                computeValues(seekBarTip.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                closeKeyBoard()
                sRoundUp.isChecked = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarSplit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, "onProgressChanged (seekBarSplit) $progress")
                val adjustedProgress = progress + 1
                tvSplit.text = "$adjustedProgress"
                computeValues(seekBarTip.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { closeKeyBoard() }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        etBase.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != dollarString) {
                    etBase.removeTextChangedListener(this)

                    val cleanString: String = s?.replace("""[$,.]""".toRegex(), "") ?: ""
                    val parsed = cleanString.toDouble() / 100
                    val formatted = getMoneyFromDouble(parsed)

                    dollarString = formatted as String
                    etBase.setText(formatted)
                    etBase.setSelection(formatted.length)

                    etBase.addTextChangedListener(this)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "afterTextChanged $s")
                sRoundUp.isChecked = false
                computeValues(seekBarTip.progress)
            }
        })

        sRoundUp.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) oldTip = seekBarTip.progress
            computeValues(oldTip)
        }

    }

    private fun updateTipDescription(tipPercent: Int) {
        val tipDescription = when (tipPercent) {
            in 0..9 -> "Poor"
            in 10..14 -> "Acceptable"
            in 15..19 -> "Good"
            in 20..24 -> "Great"
            else -> "Amazing"
        }
        tvTipDescription.text = tipDescription
        val color = ArgbEvaluator().evaluate(
            tipPercent.toFloat() / seekBarTip.max,
            ContextCompat.getColor(this, R.color.colorWorstTip),
            ContextCompat.getColor(this, R.color.colorBestTip)
        ) as Int
        tvTipDescription.setTextColor(color)
    }

    private fun computeValues(tip: Int) {
        // Get the value of the base and tip percent
        if (etBase.text.toString() == "$0.00") {
            tvTipAmount.text = "$0.00"
            tvTotalAmount.text = "$0.00"
            tvPerPersonAmount.text = "$0.00"
            return
        }

        val baseAmount = etBase.text.toString().replace("""[$,]""".toRegex(), "").toDouble()
        var tipPercent = tip
        var tipAmount = baseAmount * tipPercent / 100
        var totalAmount = baseAmount + tipAmount

        if (sRoundUp.isChecked) {
            totalAmount = kotlin.math.ceil(totalAmount)
            tipAmount = totalAmount - baseAmount
            tipPercent = (tipAmount / baseAmount * 100).toInt()
        }

        val split = seekBarSplit.progress + 1
        val perPersonAmount = totalAmount / split

        seekBarTip.progress = tipPercent
        tvTipAmount.text = getMoneyFromDouble(tipAmount)
        tvTotalAmount.text = getMoneyFromDouble(totalAmount)
        tvPerPersonAmount.text = getMoneyFromDouble(perPersonAmount)
    }

    private fun getMoneyFromDouble(input: Double): CharSequence? {
        return NumberFormat.getCurrencyInstance().format((input))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun savePayment(view: View) {
        val payment = Payment(
            baseAmount = etBase.text.toString(),
            tipPercent = seekBarTip.progress,
            tipAmount = tvTipAmount.text.toString(),
            totalAmount = tvTotalAmount.text.toString(),
            round = sRoundUp.isChecked,
            split = seekBarSplit.progress + 1,
            perPersonAmount = tvPerPersonAmount.text.toString(),
            date = LocalDateTime.now()
        )
        Log.i(TAG, "savePayment $payment")
        payments.add(0, payment)
    }

    fun viewHistory(view: View) {
        Log.i(TAG, "viewHistory")
        val intent = Intent(this, HistoryActivity::class.java).putParcelableArrayListExtra("payments", ArrayList(payments))
        startActivity(intent)
    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
