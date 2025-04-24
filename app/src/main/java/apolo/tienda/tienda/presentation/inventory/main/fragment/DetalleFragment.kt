package apolo.tienda.tienda.presentation.inventory.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import apolo.tienda.tienda.R
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.data.remote.response.DetailInventoryResponse
import apolo.tienda.tienda.data.remote.response.UnidadMedidaResponse
import apolo.tienda.tienda.databinding.FragmentDetalleBinding
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.domain.model.Inventory
import apolo.tienda.tienda.presentation.inventory.main.SharedInventoryViewModel
import apolo.tienda.tienda.presentation.inventory.view.InventoryDetailAdapter
import apolo.tienda.tienda.presentation.inventory.view.InventoryDetailViewModel
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class DetalleFragment : Fragment() {

    companion object {
        private const val ARG_INVENTARIO = "inventario"

        fun newInstance(inventario: Inventory): DetalleFragment {
            return DetalleFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_INVENTARIO, inventario)
                }
            }
        }
    }

    private var _binding: FragmentDetalleBinding? = null
    private val binding get() = _binding!!

    private lateinit var inventario: Inventory
    private val sharedViewModel: SharedInventoryViewModel by activityViewModels()
    private val viewModel: InventoryDetailViewModel by activityViewModels()
    private lateinit var loadingDialog: LoadingDialog

    private var detallesOriginales: List<DetailInventoryResponse> = emptyList()
    private lateinit var adapterRV: InventoryDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inventario = arguments?.getSerializable(ARG_INVENTARIO) as? Inventory
            ?: run {
                Toast.makeText(requireContext(), "Inventario no válido", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
                return
            }

        loadingDialog = LoadingDialog(requireContext())

        setupDependencies()
        setupObservers()
        setupUI()

        viewModel.getDetalleInventario(
            codEmpresa = inventario.codEmpresa,
            nroRegistro = inventario.nroRegistro.toString()
        )
    }

    private fun setupUI() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getDetalleInventario(inventario.codEmpresa, inventario.nroRegistro.toString())
        }

        binding.etBuscar.addTextChangedListener {
            aplicarFiltro(it.toString().trim().lowercase())
        }
    }

    private fun aplicarFiltro(filtro: String) {
        val filtrados = detallesOriginales.filter { item ->
            item.cod_articulo.lowercase().contains(filtro) ||
                    item.desc_articulo.lowercase().contains(filtro) ||
                    item.cod_barra.lowercase().contains(filtro) ||
                    item.desc_unidad_medida.lowercase().contains(filtro)
        }
        adapterRV.updateList(filtrados)
        binding.tvTotalRegistros.text = "Total de registros: ${filtrados.size}"
    }

    private fun setupDependencies() {
        val retrofit = NetworkModule.provideRetrofit(
            baseUrl = AppContextHolder.getContext().getBackendUrl(),
            preferencesHelper = PreferencesHelper.getInstance()
        )
        val repository = InventoryRepositoryImpl(NetworkModule.provideInventoryApi(retrofit))
        viewModel.setRepository(repository)
    }

    private fun setupObservers() {
        viewModel.detalleState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.swipeRefreshLayout.isRefreshing = true

                is UiState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    detallesOriginales = state.data ?: emptyList()
                    adapterRV = InventoryDetailAdapter(detallesOriginales)
                    binding.rvDetalles.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = adapterRV
                    }



/*
                    adapterRV.onItemLongClick = { item ->
                        // Pedimos las unidades primero
                        viewModel.getUnidadesMedida(item.cod_empresa, item.cod_articulo)

                        // Esperamos a que lleguen las unidades y recién ahí mostramos el diálogo
                        viewModel.unidadesState.observe(viewLifecycleOwner) { unidades ->
                            if (unidades.isNotEmpty()) {
                                showEditDialog(item, unidades)
                            } else {
                                requireContext().showToast("No se encontraron unidades para este artículo")
                            }
                        }
                    }*/
                    adapterRV.onItemLongClick = { item ->
                        viewModel.getUnidadesMedida(item.cod_empresa, item.cod_articulo)

                        viewModel.unidadesState.observeOnce(viewLifecycleOwner) { unidades ->
                            if (unidades.isNotEmpty()) {
                                showEditDialog(item, unidades)
                            } else {
                                requireContext().showToast("No se encontraron unidades para este artículo")
                            }
                        }
                    }

                    binding.tvTotalRegistros.text = "Total de registros: ${detallesOriginales.size}"
                    binding.etBuscar.setText("")
                    setupSwipeToDelete()
                }

                is UiState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    requireContext().showToast(state.message)
                }
            }
        }

        viewModel.deleteItemState.observe(viewLifecycleOwner) { state ->
            handleCrudResult(state, "Detalle eliminado correctamente")
        }

        viewModel.updateItemState.observe(viewLifecycleOwner) { state ->
            handleCrudResult(state, "Detalle actualizado correctamente")
        }

        sharedViewModel.actualizarDetalle.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.getDetalleInventario(
                    codEmpresa = inventario.codEmpresa,
                    nroRegistro = inventario.nroRegistro.toString()
                )
                sharedViewModel.notificacionConsumida()
            }
        }
    }

    private fun handleCrudResult(state: UiState<Unit>, successMessage: String) {
        when (state) {
            is UiState.Loading -> loadingDialog.show()
            is UiState.Success -> {
                loadingDialog.hide()
                requireContext().showToast(successMessage)
                viewModel.getDetalleInventario(
                    codEmpresa = inventario.codEmpresa,
                    nroRegistro = inventario.nroRegistro.toString()
                )
            }
            is UiState.Error -> {
                loadingDialog.hide()
                requireContext().showToast("Error: ${state.message}")
            }
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = adapterRV.getItemAt(position)

                if (!PermissionManager.hasPermission(Permiso.ELIMINAR_DET.value)) {
                    requireContext().showToast("No tiene permiso para eliminar.")
                    adapterRV.notifyItemChanged(position)
                    return
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar detalle")
                    .setMessage("¿Está seguro que desea eliminar este detalle?")
                    .setPositiveButton("Sí") { _, _ ->
                        viewModel.deleteDetInventario(item)
                    }
                    .setNegativeButton("Cancelar") { _, _ ->
                        adapterRV.notifyItemChanged(position)
                    }
                    .setCancelable(false)
                    .show()
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rvDetalles)
    }


    private fun showEditDialog(item: DetailInventoryResponse, unidades: List<UnidadMedidaResponse>) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_editar_detalle, null)
        val tvCodigo = view.findViewById<TextView>(R.id.tvCodigo)
        val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcion)
        val spUnidad = view.findViewById<AutoCompleteTextView>(R.id.spUnidad)
        val etCantidad = view.findViewById<TextInputEditText>(R.id.etCantidad)

        tvCodigo.text = "Cod.: ${item.cod_articulo} - ${item.cod_barra}"
        tvDescripcion.text = item.desc_articulo
        etCantidad.setText(item.cantidad.toString())

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, unidades.map { it.referencia })
        spUnidad.setAdapter(adapter)

        spUnidad.setOnClickListener {
            spUnidad.showDropDown()
        }

        // Seleccionar la unidad actual si existe
        val pos = unidades.indexOfFirst { it.cod_unidad_rel == item.cod_unidad_medida }
        if (pos != -1) spUnidad.setText(unidades[pos].referencia, false)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(true)
            .create()

        view.findViewById<MaterialButton>(R.id.btnGuardar).setOnClickListener {
            val cantidadNueva = etCantidad.text.toString().toDoubleOrNull()
            val unidadSeleccionada = unidades.find { it.referencia == spUnidad.text.toString() }

            if (cantidadNueva == null || unidadSeleccionada == null) {
                requireContext().showToast("Verifique los datos ingresados")
                return@setOnClickListener
            }

            viewModel.updateDetInventario(
                item.copy(
                    cod_unidad_medida = unidadSeleccionada.cod_unidad_rel,
                    cantidad = cantidadNueva
                )
            )
            dialog.dismiss()
        }

        dialog.show()
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}