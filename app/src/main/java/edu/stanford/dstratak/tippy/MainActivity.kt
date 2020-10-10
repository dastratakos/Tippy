package edu.stanford.dstratak.tippy

//import android.R
import android.animation.ArgbEvaluator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.text.NumberFormat


private const val TAG = "MainActivity"
private const val INITIAL_TIP_PERCENT = 15
private const val INITIAL_SPLIT = 1

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
        var round: Boolean = false
        var oldTip = INITIAL_TIP_PERCENT

        seekBarTip.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, "onProgressChanged (seekBarTip) $progress")
                tvTipPercent.text = "$progress%"
                updateTipDescription(progress)
                computeValues(round, seekBarTip.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                sRoundUp.isChecked = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarSplit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, "onProgressChanged (seekBarSplit) $progress")
                val adjustedProgress = progress + 1
                tvSplit.text = "$adjustedProgress"
                computeValues(round, seekBarTip.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

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
                computeValues(round, seekBarTip.progress)
            }
        })

        sRoundUp.setOnCheckedChangeListener { _, isChecked ->
            round = isChecked
            if (round) {
                oldTip = seekBarTip.progress
                computeValues(round, oldTip)
            } else {
                computeValues(round, oldTip)
            }
        }

        // Spinner click listener
        spinner.setOnItemSelectedListener(object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.i(TAG, "position: $position, id: $id")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // TODO
            }

        })

        // Spinner Drop down elements
        val currencies: MutableList<String> = ArrayList()
        currencies.add("USD ($)")
        currencies.add("EUR (€)")
        currencies.add("GBP (£)")
        currencies.add("JPY (¥)")
        currencies.add("CAD (C$)")
        currencies.add("CHF (Fr.)")

        // Creating adapter for spinner
        val dataAdapter = ArrayAdapter(this, R.layout.spinner_item, currencies)

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        // attaching data adapter to spinner
        spinner.adapter = dataAdapter

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

    private fun computeValues(round: Boolean, tip: Int) {
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

        if (round) {
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

}