package com.mraximentertainment.balliq.store

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * GoogleBilling is a utility object for managing Google Play Billing.
 * It supports querying product details, handling purchases, and maintaining billing client connections.
 */
object GoogleBilling {

    private lateinit var billingClient: BillingClient
    private val productDetailsMap: MutableMap<String, ProductDetails> = mutableMapOf()
    private lateinit var productDetails: ProductDetails


    /**
     * Initializes the BillingClient and sets up the connection.
     *
     * @param context The application or activity context.
     * @param productId The product ID to query details for.
     * @param nameView TextView to display the product name.
     * @param priceView TextView to display the product price.
     * @param onComplete Callback executed after a successful setup or purchase.
     */
    fun initializeBilling(
        context: Context,
        productMap: Map<String, Pair<(productDetails: ProductDetails) -> Unit, (purchase: Purchase) -> Unit>>
    ) {
        val purchasesUpdatedListener = createPurchaseUpdateListener(productMap)

        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Billing setup successful.")
                    productMap.forEach { (s, pair) ->
                        queryProductDetails(s, pair.first)
                    }

                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected. Retrying connection...")
            }
        })
    }

    /**
     * Queries product details for the given product ID and updates the UI.
     *
     * @param productId The product ID to query.
     * @param nameView TextView to display the product name.
     * @param priceView TextView to display the product price.
     */
    private fun queryProductDetails(productId: String, onQuery: (productDetails: ProductDetails) -> Unit) {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ImmutableList.of(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                productDetailsMap[productId] = productDetailsList[0]
                productDetailsMap[productId]?.let { onQuery(it) }
                Log.e(TAG, "${productDetailsList[0]}")
            } else {
                Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Initiates a purchase flow for the product.
     *
     * @param context The activity context to launch the purchase flow.
     */
    fun launchPurchaseFlow(context: Context, productId: String) {
        if (productDetailsMap[productId] != null) {
            productDetails = productDetailsMap[productId]!!
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                )
                .build()
            billingClient.launchBillingFlow(context as Activity, billingFlowParams)
        } else {
            Log.e(TAG, "Product details are not initialized. Cannot start purchase flow.")
        }
    }

    /**
     * Creates a listener to handle purchase updates.
     *
     * @param context The application or activity context.
     * @param onComplete Callback executed after a successful purchase.
     * @return PurchasesUpdatedListener
     */
    private fun createPurchaseUpdateListener(
        productMap: Map<String, Pair<(productDetails: ProductDetails) -> Unit, (purchase: Purchase) -> Unit>>
    ): PurchasesUpdatedListener {
        return PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    purchases?.forEach { purchase ->
                        handlePurchase(purchase, productMap)
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Log.i(TAG, "Purchase canceled by user.")
                }
                else -> {
                    Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                }
            }
        }
    }

    /**
     * Handles a successful purchase.
     *
     * @param purchase The completed purchase.
     * @param context The application or activity context.
     * @param onComplete Callback executed after completing the purchase.
     */
    private fun handlePurchase(purchase: Purchase, productMap: Map<String, Pair<(productDetails: ProductDetails) -> Unit, (purchase: Purchase) -> Unit>>) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            Log.i(TAG, "Purchase successful: ${purchase.orderId}")

            // Acknowledge the purchase to comply with Google Play policies
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Purchase acknowledged.")
                    val productIDs = purchase.products
                    for (productID in productIDs) run {
                        productMap[productID]?.second?.let { it(purchase) }
                    }
                } else {
                    Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
                }
            }
        }
    }

    fun consumePurchase(purchase: Purchase) {

        val coroutineScope = CoroutineScope(Dispatchers.IO)
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        coroutineScope.launch {
            val result = billingClient.consumePurchase(consumeParams)

            if (result.billingResult.responseCode ==
                BillingClient.BillingResponseCode.OK) {
            }
        }
    }

    /**
     * Shuts down the BillingClient connection.
     */
    fun release() {
        if (GoogleBilling::billingClient.isInitialized && billingClient.isReady) {
            billingClient.endConnection()
            Log.i(TAG, "BillingClient connection closed.")
        }
    }
}
