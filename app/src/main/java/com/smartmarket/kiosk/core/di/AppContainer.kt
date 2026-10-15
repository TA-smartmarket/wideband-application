package com.smartmarket.kiosk.core.di

import android.content.Context
import com.smartmarket.kiosk.BuildConfig
import com.smartmarket.kiosk.data.repository.CatalogRepository
import com.smartmarket.kiosk.data.repository.DemoCatalogRepository
import com.smartmarket.kiosk.data.uwb.UwbPositioningRepository
import com.smartmarket.kiosk.integration.DemoTrolleyNavigationRepository
import com.smartmarket.kiosk.integration.TrolleyNavigationRepository
import com.smartmarket.kiosk.ui.viewmodel.CartViewModel

/**
 * Manual dependency container (no DI framework — keeps the build dependency-light
 * and low-risk). Single instance owned by
 * [com.smartmarket.kiosk.SmartMarketApplication].
 *
 * Catalog is demo-local (the Smart Market backend is not required). Trolley
 * navigation is the explicit simulator; a real hardware adapter replaces it
 * behind the same [TrolleyNavigationRepository] seam once firmware is verified.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    /** Demo catalog, explicitly labelled "Data Demo" in the UI. */
    val catalogRepository: CatalogRepository = DemoCatalogRepository()

    /** Simulated trolley navigation. Never issues motor commands. */
    val trolleyRepository: TrolleyNavigationRepository =
        DemoTrolleyNavigationRepository().also { it.enterDemoMode() }

    /** Polls the UWB positioning server (`GET /api/v1/state`) when available. */
    val uwbRepository: UwbPositioningRepository =
        UwbPositioningRepository(
            baseUrl = BuildConfig.UWB_BASE_URL,
            tagId = BuildConfig.UWB_TAG_ID,
        )

    /** App-scoped shopping cart (survives fragment navigation). */
    val cartViewModel: CartViewModel = CartViewModel()
}