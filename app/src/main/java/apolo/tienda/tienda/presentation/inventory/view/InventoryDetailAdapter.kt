package apolo.tienda.tienda.presentation.inventory.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import apolo.tienda.tienda.R
import apolo.tienda.tienda.data.remote.response.DetailInventoryResponse
import apolo.tienda.tienda.databinding.ItemInventoryDetailBinding
import apolo.tienda.tienda.utils.Permiso
import apolo.tienda.tienda.utils.PermissionManager

class InventoryDetailAdapter(
    private var items: List<DetailInventoryResponse>
) : RecyclerView.Adapter<InventoryDetailAdapter.ViewHolder>() {

    var onItemLongClick: ((DetailInventoryResponse) -> Unit)? = null


    inner class ViewHolder(private val binding: ItemInventoryDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                if (PermissionManager.hasPermission(Permiso.MODIFICAR_DET.value)) {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemLongClick?.invoke(items[pos])
                    }
                }

                true
            }
        }


        fun bind(item: DetailInventoryResponse) {
            binding.tvDescripcion.text = item.desc_articulo
            binding.tvCantidad.text = "${item.cantidad}"
            binding.tvCodigoArticulo.text = "${item.cod_articulo}"
            binding.tvCodigoBarra.text = "${item.cod_barra}"
            binding.tvUnidad.text = "${item.desc_unidad_medida}"


        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventoryDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(position: Int): DetailInventoryResponse = items[position]

    fun updateList(nuevaLista: List<DetailInventoryResponse>) {
        this.items = nuevaLista
        notifyDataSetChanged()
    }

}
