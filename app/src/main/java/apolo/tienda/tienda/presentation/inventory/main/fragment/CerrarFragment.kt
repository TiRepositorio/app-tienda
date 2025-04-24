package apolo.tienda.tienda.presentation.inventory.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import apolo.tienda.tienda.databinding.FragmentCerrarBinding
import apolo.tienda.tienda.domain.model.Inventory

class CerrarFragment : Fragment() {


    companion object {
        private const val ARG_INVENTARIO = "inventario"

        fun newInstance(inventario: Inventory): CerrarFragment {
            val fragment = CerrarFragment()
            val args = Bundle().apply {
                putSerializable(ARG_INVENTARIO, inventario)
            }
            fragment.arguments = args
            return fragment
        }
    }


    private var _binding: FragmentCerrarBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCerrarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*arguments?.getSerializable("inventario")?.let {
            inventario = it as Inventory
        } ?: run {
            Toast.makeText(requireContext(), "Inventario no válido", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }


        setupDependencies()
        setupUi()
        setupObservers()*/
    }

}