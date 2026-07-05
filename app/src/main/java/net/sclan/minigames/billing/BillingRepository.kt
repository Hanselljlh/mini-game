package net.sclan.minigames.billing

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

const val PRODUCT_REMOVE_ADS = "remove_ads"

class BillingRepository(context: Context) {

    var purchaseState: PurchaseState by mutableStateOf(PurchaseState.Unknown)
        private set

    val areAdsEnabled: Boolean get() = purchaseState !is PurchaseState.Purchased

    @Suppress("DEPRECATION")
    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener { result, purchases ->
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK ->
                    handlePurchases(purchases ?: emptyList())
                else ->
                    // Cancelled or failed mid-flow: never leave the UI stuck on Pending.
                    if (purchaseState is PurchaseState.Pending) purchaseState = PurchaseState.NotPurchased
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

    fun launchPurchaseFlow(activity: Activity) {
        if (!client.isReady) {
            connect()
            return
        }
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_REMOVE_ADS)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()
        client.queryProductDetailsAsync(params) { result, productDetailsList ->
            val details = productDetailsList.firstOrNull()
            if (result.responseCode == BillingClient.BillingResponseCode.OK && details != null) {
                purchaseState = PurchaseState.Pending
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(details)
                                .build()
                        )
                    )
                    .build()
                val launchResult = client.launchBillingFlow(activity, flowParams)
                if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    purchaseState = PurchaseState.NotPurchased
                }
            } else {
                // Product not available (e.g. not published on Play yet) — stay purchasable.
                purchaseState = PurchaseState.NotPurchased
            }
        }
    }

    fun disconnect() = client.endConnection()

    private fun handlePurchases(purchases: List<Purchase>) {
        val ownedPurchases = purchases.filter { p ->
            p.products.contains(PRODUCT_REMOVE_ADS) &&
                p.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        ownedPurchases
            .filter { !it.isAcknowledged }
            .forEach { p ->
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(p.purchaseToken)
                    .build()
                client.acknowledgePurchase(ackParams) {}
            }
        purchaseState = if (ownedPurchases.isNotEmpty()) PurchaseState.Purchased else PurchaseState.NotPurchased
    }
}
