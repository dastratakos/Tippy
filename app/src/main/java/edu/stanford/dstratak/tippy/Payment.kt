package edu.stanford.dstratak.tippy

import android.icu.util.Currency
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime

@Parcelize
data class Payment(
    val baseAmount: String,
    val tipPercent: Int,
    val tipAmount: String,
    val totalAmount: String,
    val round: Boolean,
    val split: Int,
    val perPersonAmount: String,
    val currency: Currency,
    val date: LocalDateTime
) : Parcelable