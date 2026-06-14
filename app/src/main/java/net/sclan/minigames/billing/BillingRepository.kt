package net.sclan.minigames.billing

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams

const val PRODUCT_REMOVE_ADS = "remove_ads"

class BillingRepository(context: Context) {

    var purchaseState: PurchaseState by mutableStateOf(PurchaseState.Unknown)
        private set

    val areAdsEnabled: Boolean get() = purchaseState !is PurchaseState.Purchased

    @Suppress("DEPRECATION")
    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases ?: emptyList())
            }
        }
        .enablePendingPurchases()
        .build()

    fun connect() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    checkExistingPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    fun checkExistingPurchases() {
        if (!client.isReady) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    /** Initiates a purchase flow. In production, query ProductDetails first. */
    @Suppress("UNUSED_PARAMETER")
    fun launchPurchaseFlow(activity: Activity) {
        purchaseState = PurchaseState.Pending
        // Real implementation: queryProductDetailsAsync → launchBillingFlow
    }

    fun disconnect() = client.endConnection()

    private fun handlePurchases(purchases: List<Purchase>) {
        val owned = purchases.any { p ->
            p.products.contains(PRODUCT_REMOVE_ADS) &&
                p.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        purchaseState = if (owned) PurchaseState.Purchased else PurchaseState.NotPurchased
    }
}
