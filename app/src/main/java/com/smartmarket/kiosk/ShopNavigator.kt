package com.smartmarket.kiosk

import androidx.fragment.app.Fragment
import com.smartmarket.kiosk.core.di.AppContainer

/** Navigation surface the screens call into (single Activity host). */
interface ShopNavigator {
    fun showIdle()
    fun showBeranda()
    fun showCatalog(query: String? = null, categoryId: String? = null)
    fun showProductDetail(productId: String)
    fun showMap(navigateToProductId: String? = null)
    fun showCart()
    fun showAccount()
    fun endSession()
}

/** Shared app-scoped dependencies (manual DI container). */
val Fragment.appContainer: AppContainer
    get() = (requireActivity().application as SmartMarketApplication).container