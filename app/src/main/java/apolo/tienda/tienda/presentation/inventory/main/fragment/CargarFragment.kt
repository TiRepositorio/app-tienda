package apolo.tienda.tienda.presentation.inventory.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.data.remote.request.LoadInventoryRequest
import apolo.tienda.tienda.databinding.FragmentCargarBinding
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.domain.model.Inventory
import apolo.tienda.tienda.presentation.inventory.load.InventoryLoadViewModel
import apolo.tienda.tienda.presentation.inventory.main.SharedInventoryViewModel
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.AppContextHolder
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.getBackendUrl
import apolo.tienda.tienda.utils.showToast

class CargarFragment : Fragment() {

    private var _binding: FragmentCargarBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInventoryViewModel by activityViewModels()
    private val viewModel: InventoryLoadViewModel by activityViewModels()

    private lateinit var inventario: Inventory
    private var unidadesMap: Map<String, String> = emptyMap()
    private var unidadSeleccionadaPos: Int? = null

    companion object {
        private const val ARG_INVENTARIO = "inventario"

        fun newInstance(inventario: Inventory): CargarFragment {
            return CargarFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_INVENTARIO, inventario)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCargarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inventario = arguments?.getSerializable(ARG_INVENTARIO) as? Inventory ?: run {
            Toast.makeText(requireContext(), "Inventario no válido", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }

        setupDependencies()
        setupUi()
        setupObservers()
    }

    private fun setupDependencies() {
        val retrofit = NetworkModule.provideRetrofit(
            baseUrl = AppContextHolder.getContext().getBackendUrl(),
            preferencesHelper = PreferencesHelper.getInstance()
        )
        val api = NetworkModule.provideInventoryApi(retrofit)
        val repository = InventoryRepositoryImpl(api)
        viewModel.setRepository(repository)
    }

    private fun setupUi() {
        binding.etCodigoBarra.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val codigo = binding.etCodigoBarra.text.toString()
                if (codigo.isNotEmpty()) {
                    viewModel.getProducto(inventario.codEmpresa, "", codigo)
                }
                true
            } else false
        }

        binding.etCodigoArticulo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val codigo = binding.etCodigoArticulo.text.toString()
                if (codigo.isNotEmpty()) {
                    viewModel.getProducto(inventario.codEmpresa, codigo, "")
                }
                true
            } else false
        }

        binding.spUnidad.setOnItemClickListener { parent, _, position, _ ->
            unidadSeleccionadaPos = position
            val codUnidad = viewModel.unidadesState.value?.get(position)?.cod_unidad_rel
            codUnidad?.let {
                val codigoBarra = unidadesMap[it]
                binding.etCodigoBarra.setText(codigoBarra ?: "")
            }
        }

        binding.btnGuardar.setOnClickListener {
            guardarProducto()
        }
    }

    private fun setupObservers() {
        viewModel.productosState.observe(viewLifecycleOwner) { productos ->
            if (productos.size == 1) {
                val producto = productos.first()
                binding.etCodigoArticulo.setText(producto.cod_articulo)
                binding.tvDescripcion.text = producto.desc_articulo
                viewModel.getUnidadesMedida(inventario.codEmpresa, producto.cod_articulo)
            } else if (productos.size > 1) {
                AppContextHolder.getContext().showToast("Múltiples productos encontrados")
            } else {
                AppContextHolder.getContext().showToast("Producto no encontrado")
                clearProductoFields()
            }
        }

        viewModel.unidadesState.observe(viewLifecycleOwner) { unidades ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                unidades.map { it.referencia }
            )
            binding.spUnidad.setAdapter(adapter)
            binding.spUnidad.setOnClickListener {
                binding.spUnidad.showDropDown()
            }

            unidadesMap = unidades.associate { it.cod_unidad_rel to it.cod_barra }

            val producto = viewModel.productosState.value?.firstOrNull()
            val defaultCodUnidad = producto?.cod_unidad_rel

            if (defaultCodUnidad != null) {
                val index = unidades.indexOfFirst { it.cod_unidad_rel == defaultCodUnidad }
                if (index != -1) {
                    binding.spUnidad.setText(unidades[index].referencia, false)
                    unidadSeleccionadaPos = index
                } else if (unidades.isNotEmpty()) {
                    binding.spUnidad.setText(unidades.first().referencia, false)
                    unidadSeleccionadaPos = 0
                }

                val codUnidad = viewModel.unidadesState.value?.get(unidadSeleccionadaPos!!)?.cod_unidad_rel
                codUnidad?.let {
                    val codigoBarra = unidadesMap[it]
                    binding.etCodigoBarra.setText(codigoBarra ?: "")
                }
            }

            binding.etCantidad.requestFocus()
        }

        viewModel.errorState.observe(viewLifecycleOwner) {
            AppContextHolder.getContext().showToast(it)
        }

        viewModel.saveItemState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Success -> {
                    AppContextHolder.getContext().showToast("Guardado correctamente")
                    clearProductoFields()
                    binding.etCodigoBarra.requestFocus()
                    sharedViewModel.notificarActualizacion()
                }
                is UiState.Error -> AppContextHolder.getContext().showToast("Error: ${state.message}")
            }
        }
    }

    private fun guardarProducto() {
        val producto = viewModel.productosState.value?.firstOrNull()
        val codArticulo = producto?.cod_articulo ?: run {
            AppContextHolder.getContext().showToast("Debe ingresar un código de artículo válido")
            return
        }

        val codUnidad = unidadSeleccionadaPos?.let {
            viewModel.unidadesState.value?.get(it)?.cod_unidad_rel
        }

        val codBarra = unidadSeleccionadaPos?.let {
            viewModel.unidadesState.value?.get(it)?.cod_barra
        }

        val cantidad = binding.etCantidad.text.toString().toDoubleOrNull()
        if (codUnidad == null || cantidad == null || cantidad <= 0) {
            AppContextHolder.getContext().showToast("Debe completar correctamente los campos")
            return
        }

        val request = LoadInventoryRequest(
            nro_registro = inventario.nroRegistro.toString(),
            cod_barra = codBarra ?: "",
            cod_empresa = inventario.codEmpresa,
            cod_sucursal = inventario.codSucursal,
            cod_articulo = codArticulo,
            cod_unidad_rel = codUnidad,
            cantidad = cantidad
        )

        viewModel.guardarProducto(request)
    }

    private fun clearProductoFields() {
        binding.etCodigoArticulo.setText("")
        binding.etCodigoBarra.setText("")
        binding.tvDescripcion.text = "(sin descripción aún)"
        binding.etCantidad.setText("")
        binding.spUnidad.setText("")
        unidadSeleccionadaPos = null
        unidadesMap = emptyMap()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
