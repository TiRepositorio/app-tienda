package apolo.tienda.tienda.presentation.offline.inventario.cargar.fragment


import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import apolo.tienda.tienda.data.local.api.InventarioLocalApiImpl
import apolo.tienda.tienda.data.local.request.NuevoInventarioDetalleLocalRequest
import apolo.tienda.tienda.databinding.FragmentCargarTomaInventarioBinding
import apolo.tienda.tienda.domain.model.InventarioLocal
import apolo.tienda.tienda.presentation.offline.inventario.cargar.SharedCargarTomaInventarioViewModel
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.showToast
import android.view.KeyEvent
import android.widget.LinearLayout
import apolo.tienda.tienda.R
import com.google.android.material.button.MaterialButton

class CargarFragment : Fragment() {

    private var _binding: FragmentCargarTomaInventarioBinding? = null
    private val binding get() = _binding!!


    private lateinit var inventario: InventarioLocal


    private val sharedViewModel: SharedCargarTomaInventarioViewModel by activityViewModels()
    private val viewModel: CargarViewModel by activityViewModels()

    var ignorarSiguienteVacioProducto = false

    companion object {
        private const val ARG_INVENTARIO = "inventario"

        fun newInstance(inventario: InventarioLocal): CargarFragment {
            return CargarFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_INVENTARIO, inventario)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCargarTomaInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inventario = arguments?.getSerializable(ARG_INVENTARIO) as? InventarioLocal ?: run {
            Toast.makeText(requireContext(), "Inventario no válido", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }

        setupDependencies()
        setupUi()
        setupObservers()
    }

    private fun setupDependencies() {
        val repository = InventarioLocalApiImpl()
        viewModel.setRepository(repository)
    }

    private fun setupUi() {

        val barraTeclado = requireActivity().findViewById<LinearLayout>(R.id.barraTeclado)
        val btnAceptarTeclado = requireActivity().findViewById<MaterialButton>(R.id.btnAceptarTeclado)
        val btnCancelarTeclado = requireActivity().findViewById<MaterialButton>(R.id.btnCancelarTeclado)
        barraTeclado.visibility = View.VISIBLE

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.root.rootView.height

            val tecladoVisible = screenHeight - rect.bottom > screenHeight * 0.15

            if (!tecladoVisible) {
                barraTeclado.visibility = View.GONE
            } else {
                if(binding.etCantidad.hasFocus()) {
                    barraTeclado.visibility = View.VISIBLE
                }
            }
        }


        binding.etCantidad.setOnFocusChangeListener { _, hasFocus ->
            barraTeclado.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }

        binding.etBuscadorProducto.setOnFocusChangeListener { _, hasFocus ->
            barraTeclado.visibility = if (hasFocus) View.GONE else View.VISIBLE
        }


        btnAceptarTeclado.setOnClickListener {
            if(guardarProducto()) {
                barraTeclado.visibility = View.GONE
                //ocultarTeclado()
            }

        }

        btnCancelarTeclado.setOnClickListener {
            binding.etCantidad.setText("")
            binding.etCantidad.clearFocus()
            barraTeclado.visibility = View.GONE
            ocultarTeclado()
        }

        binding.etBuscadorProducto.setOnEditorActionListener { v, actionId, event ->

            val isActionDone = actionId == EditorInfo.IME_ACTION_DONE
            val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

            if (isActionDone || isEnterKey) {
                val codigo = binding.etBuscadorProducto.text.toString()
                if (codigo.isNotEmpty()) {

                    viewModel.getProductos(requireContext(), codigo, inventario.nroAjuste.toString())

                }
                true
            } else false
        }

        binding.etCantidad.setOnEditorActionListener { _, actionId, _ ->


            if (actionId == EditorInfo.IME_ACTION_DONE) {
                guardarProducto()
                true
            } else false
        }

        binding.btnGuardar.setOnClickListener {
            guardarProducto()
        }


    }

    private fun ocultarTeclado() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etBuscadorProducto.windowToken, 0)
    }

    private fun setupObservers() {
        viewModel.productosState.observe(viewLifecycleOwner) { productos ->
            if (productos.isEmpty()) {
                if (ignorarSiguienteVacioProducto) {
                    ignorarSiguienteVacioProducto = false
                    return@observe
                }

                requireContext().showToast("Producto no encontrado")
                binding.etBuscadorProducto.postDelayed({
                    binding.etBuscadorProducto.requestFocus()
                }, 200)
                clearProductoFields()
            } else if (productos.size == 1) {

                val producto = productos.first()
                binding.etBuscadorProducto.setText("")
                binding.etCodigoArticulo.setText(producto.codProducto)
                binding.etCodigoBarra.setText(producto.codBarra)
                binding.tvDescripcion.text = producto.descProducto

                if (binding.cbIngresarCantidad.isChecked) {
                    binding.etCantidad.postDelayed({
                        binding.etCantidad.requestFocus()
                    }, 200)


                } else {
                    binding.etCantidad.setText("1")
                    guardarProducto()


                }


            } else if (productos.size > 1) {
                requireContext().showToast("Múltiples productos encontrados")
            }
        }



        viewModel.errorState.observe(viewLifecycleOwner) {
            requireContext().showToast(it)
        }

        viewModel.saveItemState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Success -> {
                    requireContext().showToast("Guardado correctamente")
                    clearProductoFields()

                    binding.etBuscadorProducto.postDelayed({
                        binding.etBuscadorProducto.requestFocus()
                    }, 200)
                    sharedViewModel.notificarActualizacion()
                }
                is UiState.Error -> requireContext().showToast("Error: ${state.message}")
            }
        }
    }

    private fun guardarProducto() : Boolean {
        val producto = viewModel.productosState.value?.firstOrNull()
        val codArticulo = producto?.codProducto ?: run {
            requireContext().showToast("Debe ingresar un código de artículo válido")
            return false
        }


        val cantidad = binding.etCantidad.text.toString().toDoubleOrNull()
        if (cantidad == null || cantidad == 0.toDouble()) {
            requireContext().showToast("Debe completar correctamente los campos")
            return false
        }

        val request = NuevoInventarioDetalleLocalRequest(
            idInventarioCabecera = inventario.id,
            nroAjuste = inventario.nroAjuste,
            codProducto = codArticulo,
            cantidad = cantidad,
            codBarra = producto.codBarra,
            stockActual = producto.cantidad.toDouble(),
            codDeposito = producto.codDeposito,
            sector = PreferencesHelper.getInstance().getIdDispositivo(),
            movil = PreferencesHelper.getInstance().getUUID(),
            enviado = "N",
            descProducto = producto.descProducto
        )

        viewModel.guardarProducto(requireContext(), request)
        return true
    }

    private fun clearProductoFields() {
        binding.etBuscadorProducto.setText("")
        binding.etCodigoArticulo.setText("")
        binding.etCodigoBarra.setText("")
        binding.tvDescripcion.text = "(sin descripción aún)"
        binding.etCantidad.setText("")
        ignorarSiguienteVacioProducto = true
        viewModel.limpiarProductos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
