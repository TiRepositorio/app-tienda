package apolo.tienda.tienda.presentation.inventory.main


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import apolo.tienda.tienda.databinding.ActivityInventoryMainBinding
import apolo.tienda.tienda.domain.model.Inventory
import apolo.tienda.tienda.R
import apolo.tienda.tienda.data.remote.response.ListInventoryResponse
import apolo.tienda.tienda.utils.toDomain

class InventoryMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryMainBinding
    private lateinit var inventario: Inventory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inventario = obtenerInventarioDesdeIntent()
        mostrarEncabezado()

        setupViewPager()
        setupBottomNavigation()
    }

    private fun mostrarEncabezado() {
        binding.tvTituloInventario.text = "Inventario N° ${inventario.nroRegistro}"
        binding.tvEmpresa.text = "${inventario.codEmpresa} - ${inventario.descEmpresa}"
        binding.tvSucursal.text = "${inventario.codSucursal} - ${inventario.descSucursal}"
    }

    private fun setupViewPager() {
        val adapter = InventoryPagerAdapter(this, inventario)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = true // permite swipe

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNavigation.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_cargar -> binding.viewPager.currentItem = 0
                R.id.nav_detalle -> binding.viewPager.currentItem = 1
                //R.id.nav_cerrar -> binding.viewPager.currentItem = 2
            }
            true
        }
    }

    private fun obtenerInventarioDesdeIntent(): Inventory {
        val response = intent.getSerializableExtra("INVENTARIO") as? ListInventoryResponse
        requireNotNull(response) {
            finish()
            "No se recibió correctamente el inventario desde el intent."
        }
        return response.toDomain()
    }
}
