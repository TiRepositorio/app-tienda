package apolo.tienda.tienda.presentation.offline.inventario.cargar

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import apolo.tienda.tienda.domain.model.InventarioLocal
import apolo.tienda.tienda.presentation.offline.inventario.cargar.fragment.CargarFragment
import apolo.tienda.tienda.presentation.offline.inventario.cargar.fragment.DetalleFragment

class CargarTomaInventarioPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val inventario: InventarioLocal
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CargarFragment.newInstance(inventario)
            1 -> DetalleFragment.newInstance(inventario)
            //2 -> CerrarFragment.newInstance(inventario)
            else -> throw IllegalArgumentException("Invalid fragment position")
        }
    }
}