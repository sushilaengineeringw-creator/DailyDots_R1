package com.yourname.dailydots

import android.app.Activity
import android.content.Context
import androidx.glance.appwidget.updateAll
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Product IDs must match what you create in Play Console > Monetize. */
object Products {
    const val LIFETIME = "lifetime_unlock"   // one-time product
    const val ANNUAL = "premium_annual"      // subscription (optional)
    const val FREE_LIMIT = 3                 // free habits

    private const val PREFS = "dailydots_prefs"
    fun cachedPremium(c: Context): Boolean =
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("premium", false)

    fun savePremium(c: Context, v: Boolean) =
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("premium", v).apply()
}

/**
 * Handles purchases entirely through Google Play (no server needed).
 * Premium status is cached locally so the app keeps working offline.
 */
class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _isPremium = MutableStateFlow(Products.cachedPremium(context))
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _prices = MutableStateFlow<Map<String, String>>(emptyMap())
    val prices: StateFlow<Map<String, String>> = _prices

    private val details = mutableMapOf<String, ProductDetails>()
    private var inappOwned: Boolean? = null
    private var subsOwned: Boolean? = null

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    fun connect() {
        if (client.isReady) { refresh(); return }
        if (client.connectionState == BillingClient.ConnectionState.CONNECTING) return
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadProducts()
                    refresh()
                }
            }
            override fun onBillingServiceDisconnected() { /* retried on next refresh() */ }
        })
    }

    /** Re-check what the user owns (also used for "Restore purchase"). */
    fun refresh() {
        if (!client.isReady) { connect(); return }
        query(BillingClient.ProductType.INAPP)
        query(BillingClient.ProductType.SUBS)
    }

    private fun query(type: String) {
        val params = QueryPurchasesParams.newBuilder().setProductType(type).build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { acknowledge(it) }
                val owned = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                if (type == BillingClient.ProductType.INAPP) inappOwned = owned else subsOwned = owned
                applyOwnership()
            }
        }
    }

    private fun applyOwnership() {
        val i = inappOwned
        val s = subsOwned
        val premium = when {
            i == true || s == true -> true
            i != null && s != null -> false
            else -> return // wait until both checks succeed before removing premium
        }
        setPremium(premium)
    }

    private fun loadProducts() {
        load(BillingClient.ProductType.INAPP, Products.LIFETIME)
        load(BillingClient.ProductType.SUBS, Products.ANNUAL)
    }

    private fun load(type: String, id: String) {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(id).setProductType(type).build()
        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        client.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                list.firstOrNull()?.let { d ->
                    details[id] = d
                    val price = d.oneTimePurchaseOfferDetails?.formattedPrice
                        ?: d.subscriptionOfferDetails?.firstOrNull()
                            ?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice
                    if (price != null) _prices.value = _prices.value + (id to price)
                }
            }
        }
    }

    fun purchase(activity: Activity, productId: String) {
        val d = details[productId] ?: return
        val pb = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(d)
        d.subscriptionOfferDetails?.firstOrNull()?.let { pb.setOfferToken(it.offerToken) }
        val flow = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(pb.build())).build()
        client.launchBillingFlow(activity, flow)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { acknowledge(it) }
            if (purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }) setPremium(true)
        }
        // USER_CANCELED and other codes: do nothing, app keeps working in free mode.
    }

    /** Unacknowledged purchases are refunded by Google after 3 days, so always acknowledge. */
    private fun acknowledge(p: Purchase) {
        if (p.purchaseState == Purchase.PurchaseState.PURCHASED && !p.isAcknowledged) {
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()
            ) { }
        }
    }

    private fun setPremium(value: Boolean) {
        if (_isPremium.value != value) {
            Products.savePremium(context, value)
            _isPremium.value = value
            scope.launch { HabitWidget().updateAll(context) }
        }
    }
}
