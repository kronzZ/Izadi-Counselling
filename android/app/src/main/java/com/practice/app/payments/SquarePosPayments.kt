package com.practice.app.payments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.squareup.sdk.pos.ChargeRequest
import com.squareup.sdk.pos.CurrencyCode
import com.squareup.sdk.pos.PosSdk

object SquarePosPayments {
    fun isConfigured(): Boolean = SquareConfig.APPLICATION_ID.isNotBlank()

    fun isSquareInstalled(context: Context): Boolean {
        if (!isConfigured()) return false
        return PosSdk.createClient(context, SquareConfig.APPLICATION_ID).isPointOfSaleInstalled
    }

    fun createChargeIntent(
        context: Context,
        amountCents: Int,
        note: String,
        requestMetadata: String,
    ): Intent {
        val client = PosSdk.createClient(context, SquareConfig.APPLICATION_ID)
        val request = ChargeRequest.Builder(amountCents, CurrencyCode.AUD)
            .note(note.take(500))
            .requestMetadata(requestMetadata.take(200))
            .build()
        return client.createChargeIntent(request)
    }

    fun openPlayStoreListing(context: Context) {
        if (!isConfigured()) return
        PosSdk.createClient(context, SquareConfig.APPLICATION_ID)
            .openPointOfSalePlayStoreListing()
    }

    fun parseSuccess(context: Context, data: Intent): ChargeRequest.Success {
        return PosSdk.createClient(context, SquareConfig.APPLICATION_ID)
            .parseChargeSuccess(data)
    }

    fun parseError(context: Context, data: Intent): ChargeRequest.Error {
        return PosSdk.createClient(context, SquareConfig.APPLICATION_ID)
            .parseChargeError(data)
    }

    fun isSuccessResult(resultCode: Int): Boolean = resultCode == Activity.RESULT_OK
}

class SquareNotInstalledException : ActivityNotFoundException("Square Point of Sale is not installed")
