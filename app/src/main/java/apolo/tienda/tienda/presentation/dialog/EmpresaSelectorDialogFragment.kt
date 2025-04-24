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

class EmpresaSelectorDialogFragment(
    private val onEmpresaSelected: (codigo: String, descripcion: String) -> Unit
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
        builder.setTitle("Seleccione una empresa")

        val dialog = builder.create()
        dialog.setMessage("Cargando empresas...")
        dialog.show()

        viewModel.empresasState.observe(this) { empresas ->
            if (empresas.isEmpty()) {
                dialog.setMessage("No se encontraron empresas")
                return@observe
            }

            val nombres = empresas.map { it.desc_empresa }
            val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, nombres)

            // En lugar de intentar usar listView manualmente, reconstruimos el dialog con setAdapter
            AlertDialog.Builder(context)
                .setTitle("Seleccione una empresa")
                .setAdapter(adapter) { dialogInterface, position ->
                    val empresa = empresas[position]
                    onEmpresaSelected(empresa.cod_empresa, empresa.desc_empresa)
                    dialogInterface.dismiss()
                }
                .setNegativeButton("Cancelar") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()

            dialog.dismiss() // Cerramos el primer diálogo de "cargando"
        }

        viewModel.errorEmpresaState.observe(this) {
            dialog.dismiss()
            requireActivity().showToast("Error al obtener empresas: $it")
        }

        viewModel.getEmpresas()

        return dialog
    }

}
