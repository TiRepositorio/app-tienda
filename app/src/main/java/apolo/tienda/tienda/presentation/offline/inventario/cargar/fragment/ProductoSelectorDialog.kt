package apolo.tienda.tienda.presentation.offline.inventario.cargar.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import apolo.tienda.tienda.data.local.response.ListaProductoResponse
import apolo.tienda.tienda.databinding.DialogSeleccionarProductoBinding

class ProductoSelectorDialog(
    private val productos: List<ListaProductoResponse>,
    private val onProductoSeleccionado: (ListaProductoResponse) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSeleccionarProductoBinding.inflate(LayoutInflater.from(context))

        val adapter = ProductoSelectorAdapter(productos) {
            onProductoSeleccionado(it)
            dismiss()
        }

        binding.rvProductos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProductos.adapter = adapter

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .create()
    }
}
