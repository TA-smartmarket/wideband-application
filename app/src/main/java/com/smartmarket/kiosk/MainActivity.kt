package com.smartmarket.kiosk

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smartmarket.kiosk.databinding.ActivityMainBinding
import com.smartmarket.kiosk.ui.CartFragment
import com.smartmarket.kiosk.ui.CatalogFragment
import com.smartmarket.kiosk.ui.HomeFragment
import com.smartmarket.kiosk.ui.IdleFragment
import com.smartmarket.kiosk.ui.MapFragment
import com.smartmarket.kiosk.ui.ProductDetailFragment

/**
 * Single-activity XML host. No login/registration: the app opens on the idle
 * welcome screen, and "Mulai Belanja" starts a local shopping session.
 */
class MainActivity : AppCompatActivity(), ShopNavigator {

    private lateinit var binding: ActivityMainBinding

    private var pendingNavigateProductId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showBeranda()
                R.id.nav_map -> showMap()
                R.id.nav_cart -> showCart()
            }
            true
        }

        if (savedInstanceState == null) {
            showIdle()
        }
    }

    override fun showIdle() {
        setShoppingUi(false)
        replaceRoot(IdleFragment(), TAG_IDLE)
    }

    override fun showBeranda() {
        setShoppingUi(true)
        selectTab(R.id.nav_home)
        replaceRoot(HomeFragment(), TAG_HOME)
    }

    override fun showCatalog(query: String?, categoryId: String?) {
        setShoppingUi(false)
        val fragment = CatalogFragment.newInstance(query, categoryId)
        push(fragment, TAG_CATALOG)
    }

    override fun showProductDetail(productId: String) {
        val fragment = ProductDetailFragment.newInstance(productId)
        push(fragment, TAG_DETAIL)
    }

    override fun showMap(navigateToProductId: String?) {
        setShoppingUi(true)
        selectTab(R.id.nav_map)
        pendingNavigateProductId = navigateToProductId
        replaceRoot(MapFragment(), TAG_MAP)
    }

    override fun showCart() {
        setShoppingUi(true)
        selectTab(R.id.nav_cart)
        replaceRoot(CartFragment(), TAG_CART)
    }

    /** Consumed once by [MapFragment] to auto-navigate to a product's shelf. */
    fun consumePendingMapProduct(): String? {
        val v = pendingNavigateProductId
        pendingNavigateProductId = null
        return v
    }

    override fun endSession() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.end_session_title)
            .setMessage(R.string.end_session_message)
            .setNegativeButton(R.string.stay) { d, _ -> d.dismiss() }
            .setPositiveButton(R.string.yes) { d, _ ->
                d.dismiss()
                binding.bottomNav.selectedItemId = R.id.nav_home
                showIdle()
            }
            .show()
    }

    private fun setShoppingUi(visible: Boolean) {
        binding.bottomNav.visibility = if (visible) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun selectTab(id: Int) {
        if (binding.bottomNav.selectedItemId != id) {
            binding.bottomNav.selectedItemId = id
        }
    }

    private fun replaceRoot(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentHost, fragment, tag)
            .commit()
    }

    private fun push(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out,
            )
            .replace(R.id.fragmentHost, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    private companion object {
        const val TAG_IDLE = "idle"
        const val TAG_HOME = "home"
        const val TAG_CATALOG = "catalog"
        const val TAG_DETAIL = "detail"
        const val TAG_MAP = "map"
        const val TAG_CART = "cart"
    }
}