package apolo.tienda.tienda.presentation.offline.inventario.cargar.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import apolo.tienda.tienda.data.local.api.InventarioLocalApiImpl
import apolo.tienda.tienda.data.local.response.ListaDetalleTomaInventarioResponse
import apolo.tienda.tienda.databinding.FragmentDetalleTomaInventarioBinding
import apolo.tienda.tienda.domain.model.InventarioLocal
import apolo.tienda.tienda.presentation.offline.inventario.cargar.SharedCargarTomaInventarioViewModel
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.LoadingDialog
import apolo.tienda.tienda.utils.showToast

class DetalleFragment : Fragment() {

    companion object {
        private const val ARG_INVENTARIO = "inventario"

        fun newInstance(inventario: InventarioLocal): DetalleFragment {
            return DetalleFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_INVENTARIO, inventario)
                }
            }
        }
    }

    private var _binding: FragmentDetalleTomaInventarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var inventario: InventarioLocal


    private val sharedViewModel: SharedCargarTomaInventarioViewModel by activityViewModels()
    private val viewModel: DetalleViewModel by activityViewModels()
    private lateinit var loadingDialog: LoadingDialog

    private var detallesOriginales: List<ListaDetalleTomaInventarioResponse> = emptyList()
    private lateinit var adapterRV: DetalleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleTomaInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inventario = arguments?.getSerializable(ARG_INVENTARIO) as? InventarioLocal
            ?: run {
                Toast.makeText(requireContext(), "Inventario no válido", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
                return
            }



        loadingDialog = LoadingDialog(requireContext())

        setupDependencies()
        setupObservers()
        setupUI()

        viewModel.getDetalleTomaInventario(
            requireContext(),
            inventario.id
        )
    }



    private fun setupUI() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getDetalleTomaInventario(
                requireContext(),
                inventario.id
            )
        }

        binding.etBuscar.addTextChangedListener {
            aplicarFiltro(it.toString().trim().lowercase())
        }
    }

    private fun aplicarFiltro(filtro: String) {
        val filtrados = detallesOriginales.filter { item ->
            item.codBarra.lowercase().contains(filtro) ||
                    //item.desc_articulo.lowercase().contains(filtro) ||
                    item.codProducto.lowercase().contains(filtro)
        }
        adapterRV.updateList(filtrados)
        binding.tvTotalRegistros.text = "Total de registros: ${filtrados.size}"
    }

    private fun setupDependencies() {

        val repository = InventarioLocalApiImpl()
        viewModel.setRepository(repository)
    }

    private fun setupObservers() {
        viewModel.detalleState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.swipeRefreshLayout.isRefreshing = true

                is UiState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    detallesOriginales = state.data ?: emptyList()
                    adapterRV = DetalleAdapter(detallesOriginales)
                    binding.rvDetalles.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = adapterRV
                    }



                    binding.tvTotalRegistros.text = "Total de registros: ${detallesOriginales.size}"
                    binding.etBuscar.setText("")
                }

                is UiState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    requireContext().showToast(state.message)
                }
            }
        }

        sharedViewModel.actualizarDetalle.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.getDetalleTomaInventario(
                    requireContext(),
                   inventario.id
                )
                sharedViewModel.notificacionConsumida()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}