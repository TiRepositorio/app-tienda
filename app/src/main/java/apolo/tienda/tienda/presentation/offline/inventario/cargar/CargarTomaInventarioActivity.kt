package apolo.tienda.tienda.presentation.offline.inventario.cargar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import apolo.tienda.tienda.R
import apolo.tienda.tienda.data.local.api.InventarioLocalApiImpl
import apolo.tienda.tienda.databinding.ActivityCargarInventarioBinding
import apolo.tienda.tienda.domain.model.InventarioLocal
import apolo.tienda.tienda.utils.showToast
import apolo.tienda.tienda.utils.toDomain

class CargarTomaInventarioActivity : AppCompatActivity() {



    private lateinit var binding: ActivityCargarInventarioBinding
    private lateinit var inventario: InventarioLocal


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCargarInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)


        obtenerInventarioDesdeIntent()

    }


    private fun obtenerInventarioDesdeIntent()  {
        val id = intent.getIntExtra("ID_INVENTARIO_CABECERA", 0)
        if (id == 0) {
            showToast("No se recibió correctamente el inventario desde el intent.")
            finish()

        }


        val repository = InventarioLocalApiImpl()

        val result = repository.getTomaInventario(applicationContext, id.toLong())

        result.fold(
            onSuccess = {
                inventario = it.toDomain() // si tenés un mapper a tu clase InventarioLocal
                mostrarEncabezado()
                setupViewPager()
                setupBottomNavigation()
            },
            onFailure = {
                showToast("Error al obtener inventario: ${it.message}")
                finish()
            }
        )

    }


    private fun mostrarEncabezado() {
        binding.tvTituloInventario.text = "Toma N° ${inventario.id}"
        binding.tvNroAjuste.text = "Nro. Ajuste: ${inventario.nroAjuste}"
    }

    private fun setupViewPager() {
        val adapter = CargarTomaInventarioPagerAdapter(this, inventario)
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



}