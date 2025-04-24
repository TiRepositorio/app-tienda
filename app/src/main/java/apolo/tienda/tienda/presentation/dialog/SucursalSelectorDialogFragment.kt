package apolo.tienda.tienda.presentation.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.presentation.home.HomeViewModel
import apolo.tienda.tienda.utils.AppContextHolder
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.getBackendUrl
import apolo.tienda.tienda.utils.showToast

class SucursalSelectorDialogFragment(
    private val codEmpresa: String,
    private val onSucursalSelected: (codigo: String, descripcion: String) -> Unit
) : DialogFragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val retrofit = NetworkModule.provideRetrofit(
            AppContextHolder.getContext().getBackendUrl(),
            PreferencesHelper.getInstance()
        )
        val repository = InventoryRepositoryImpl(NetworkModule.provideInventoryApi(retrofit))
        viewModel.setRepository(repository)

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Seleccione una sucursal")

        val dialog = builder.create()
        dialog.setMessage("Cargando sucursales...")
        dialog.show()

        viewModel.sucursalesState.observe(this) { sucursales ->
            if (sucursales.isEmpty()) {
                dialog.setMessage("No se encontraron sucursales")
                return@observe
            }

            val nombres = sucursales.map { it.desc_sucursal }
            val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, nombres)

            // Reemplazamos el diálogo anterior con uno con opciones
            AlertDialog.Builder(context)
                .setTitle("Seleccione una sucursal")
                .setAdapter(adapter) { dialogInterface, position ->
                    val sucursal = sucursales[position]
                    onSucursalSelected(sucursal.cod_sucursal, sucursal.desc_sucursal)
                    dialogInterface.dismiss()
                }
                .setNegativeButton("Cancelar") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()

            dialog.dismiss()
        }

        viewModel.errorSucursalState.observe(this) {
            dialog.dismiss()
            requireActivity().showToast("Error al obtener sucursales: $it")
        }

        viewModel.getSucursales(codEmpresa)

        return dialog
    }
}
