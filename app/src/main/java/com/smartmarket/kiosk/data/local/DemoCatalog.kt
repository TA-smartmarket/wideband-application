package com.smartmarket.kiosk.data.local

import com.smartmarket.kiosk.data.model.Category
import com.smartmarket.kiosk.data.model.Product
import com.smartmarket.kiosk.data.model.ProductLocation

/**
 * Demo catalog. Data is consistent and clearly a development dataset; the UI
 * labels it "Data Demo". A repository abstraction ([com.smartmarket.kiosk.data.repository.CatalogRepository])
 * lets a real backend replace this source without touching screens.
 */
object DemoCatalog {

    val categories: List<Category> = listOf(
        Category(id = "sayur", title = "Sayur & Buah", emoji = "\uD83E\uDD6C"),
        Category(id = "minuman", title = "Minuman", emoji = "\uD83E\uDD64"),
        Category(id = "daging", title = "Daging & Seafood", emoji = "\uD83E\uDD69"),
        Category(id = "snack", title = "Snack & Roti", emoji = "\uD83C\uDF7F"),
        Category(id = "sembako", title = "Sembako", emoji = "\uD83C\uDF5A"),
        Category(id = "susu", title = "Susu & Telur", emoji = "\uD83E\uDD5B"),
    )

    val products: List<Product> = listOf(
        Product("p1", "Wortel Segar 500g", "sayur", "Wortel lokal segar kemasan 500 gram.", 8_500, 40, "\uD83E\uDD55"),
        Product("p2", "Brokoli 300g", "sayur", "Brokoli hijau segar kemasan 300 gram.", 12_000, 25, "\uD83E\uDD66"),
        Product("p3", "Apel Fuji 1kg", "sayur", "Apel fuji impor, 1 kg.", 35_000, 18, "\uD83C\uDF4E"),
        Product("p4", "Air Mineral 600ml", "minuman", "Air mineral botol 600 ml.", 3_500, 120, "\uD83D\uDCA7"),
        Product("p5", "Teh Botol 350ml", "minuman", "Teh manis botol 350 ml.", 5_000, 80, "\uD83C\uDF75"),
        Product("p6", "Jus Jeruk 1L", "minuman", "Jus jeruk kemasan 1 liter.", 18_000, 30, "\uD83C\uDF4A"),
        Product("p7", "Daging Sapi 500g", "daging", "Daging sapi segar potong, 500 gram.", 65_000, 15, "\uD83E\uDD69"),
        Product("p8", "Ayam Utuh 1kg", "daging", "Ayam broiler utuh bersih, 1 kg.", 38_000, 20, "\uD83C\uDF57"),
        Product("p9", "Udang Segar 250g", "daging", "Udang segar kupas, 250 gram.", 42_000, 12, "\uD83E\uDD90"),
        Product("p10", "Keripik Kentang 68g", "snack", "Keripik kentang original 68 gram.", 11_000, 60, "\uD83C\uDF5F"),
        Product("p11", "Roti Tawar 400g", "snack", "Roti tawar putih kemasan 400 gram.", 15_000, 35, "\uD83C\uDF5E"),
        Product("p12", "Cokelat Batang 90g", "snack", "Cokelat batang susu 90 gram.", 16_000, 40, "\uD83C\uDF6B"),
        Product("p13", "Beras Premium 5kg", "sembako", "Beras premium kemasan 5 kg.", 68_000, 22, "\uD83C\uDF5A"),
        Product("p14", "Minyak Goreng 2L", "sembako", "Minyak goreng sawit 2 liter.", 32_000, 28, "\uD83E\uDED2"),
        Product("p15", "Gula Pasir 1kg", "sembako", "Gula pasir putih 1 kg.", 17_000, 45, "\uD83C\uDF6C"),
        Product("p16", "Susu Kotak 1L", "susu", "Susu UHT full cream 1 liter.", 21_000, 50, "\uD83E\uDD5B"),
        Product("p17", "Telur Ayam 1kg", "susu", "Telur ayam negeri, 1 kg (sekitar 16 butir).", 28_000, 26, "\uD83E\uDD5A"),
        Product("p18", "Keju Cheddar 170g", "susu", "Keju cheddar kemasan 170 gram.", 24_000, 19, "\uD83E\uDDC0"),
    )

    /**
     * Demo product→location mapping (shelf → destination → graph node). Real
     * mapping replaces this later; the map UI simply resolves the product's node.
     */
    val locations: List<ProductLocation> = listOf(
        ProductLocation("p1", "s1", "d_sayur", "n1"),
        ProductLocation("p2", "s1", "d_sayur", "n1"),
        ProductLocation("p3", "s1", "d_sayur", "n1"),
        ProductLocation("p4", "s2", "d_minuman", "n3"),
        ProductLocation("p5", "s2", "d_minuman", "n3"),
        ProductLocation("p6", "s2", "d_minuman", "n3"),
        ProductLocation("p7", "s3", "d_daging", "n4"),
        ProductLocation("p8", "s3", "d_daging", "n4"),
        ProductLocation("p9", "s3", "d_daging", "n4"),
        ProductLocation("p10", "s4", "d_snack", "n6"),
        ProductLocation("p11", "s4", "d_snack", "n6"),
        ProductLocation("p12", "s4", "d_snack", "n6"),
        ProductLocation("p13", "s5", "d_sembako", "n7"),
        ProductLocation("p14", "s5", "d_sembako", "n7"),
        ProductLocation("p15", "s5", "d_sembako", "n7"),
        ProductLocation("p16", "s6", "d_susu", "n9"),
        ProductLocation("p17", "s6", "d_susu", "n9"),
        ProductLocation("p18", "s6", "d_susu", "n9"),
    )

    val locationByProduct: Map<String, ProductLocation> = locations.associateBy { it.productId }
}
