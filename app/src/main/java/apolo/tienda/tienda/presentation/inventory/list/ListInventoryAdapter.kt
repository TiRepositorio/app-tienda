package apolo.tienda.tienda.presentation.inventory.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import apolo.tienda.tienda.data.remote.response.ListInventoryResponse
import apolo.tienda.tienda.databinding.ListInventoryItemBinding
import apolo.tienda.tienda.utils.DateUtils
import apolo.tienda.tienda.utils.Permiso
import apolo.tienda.tienda.utils.PermissionManager
import apolo.tienda.tienda.utils.showToast

class ListInventoryAdapter(
    private val items: List<ListInventoryResponse>,
    private val isSoloLectura: Boolean = false,
    private val onCerrarClicked: (ListInventoryResponse) -> Unit,
    private val onCargarClicked: (ListInventoryResponse) -> Unit,
    private val onItemClicked: (ListInventoryResponse) -> Unit
) : RecyclerView.Adapter<ListInventoryAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ListInventoryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListInventoryResponse) = with(binding) {
            tvNroInventario.text = "Nro. Registro: ${item.nro_registro}"
            tvDescripcion.text = item.descripcion

            val fecha = DateUtils.parseDateFromApi(item.fecha)
            tvFecha.text = DateUtils.formatDateToShow(fecha)

            tvEmpresa.text = "Empresa: ${item.cod_empresa}"
            tvSucursal.text = "Sucursal: ${item.cod_sucursal}"

            if (isSoloLectura) {
                binding.btnCerrar.visibility = View.GONE
                binding.btnCargar.visibility = View.GONE
                binding.root.setOnClickListener {
                    onItemClicked.invoke(item)
                }
            } else {
                if (!PermissionManager.hasPermission(Permiso.CERRAR_INV.value)) {
                    binding.btnCerrar.visibility = View.GONE
                } else {
                    binding.btnCerrar.visibility = View.VISIBLE
                }

                binding.btnCerrar.setOnClickListener {
                    onCerrarClicked.invoke(item)
                }
                binding.btnCargar.setOnClickListener {
                    onCargarClicked.invoke(item)
                }
            }

            //btnCerrar.setOnClickListener { onCerrarClicked(item) }
            //btnCargar.setOnClickListener { onCargarClicked(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListInventoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
