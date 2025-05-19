package apolo.tienda.tienda.presentation.offline.inventario.cargar.fragment


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import apolo.tienda.tienda.data.local.response.ListaProductoResponse
import apolo.tienda.tienda.databinding.ItemProductoSelectorBinding

class ProductoSelectorAdapter(
    private val productos: List<ListaProductoResponse>,
    private val onItemClick: (ListaProductoResponse) -> Unit
) : RecyclerView.Adapter<ProductoSelectorAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemProductoSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(producto: ListaProductoResponse) {
            binding.tvDescripcion.text = producto.descProducto
            binding.tvCodigo.text = "Código: ${producto.codProducto}"
            binding.tvBarra.text = "Barra: ${producto.codBarra}"

            binding.root.setOnClickListener {
                onItemClick(producto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductoSelectorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount(): Int = productos.size
}
