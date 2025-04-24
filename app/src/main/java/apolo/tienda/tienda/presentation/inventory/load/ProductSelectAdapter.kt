package apolo.tienda.tienda.presentation.inventory.load

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import apolo.tienda.tienda.data.remote.response.ProductoResponse
import apolo.tienda.tienda.databinding.ItemProductoDialogBinding

class ProductSelectAdapter(
    private val productos: List<ProductoResponse>,
    private val onItemClick: (ProductoResponse) -> Unit
) : RecyclerView.Adapter<ProductSelectAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemProductoDialogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(producto: ProductoResponse) {
            binding.tvCodigoArticulo.text = "Código: ${producto.cod_articulo}"
            binding.tvDescripcion.text = producto.desc_articulo
            binding.tvUnidad.text = "Unidad: ${producto.referencia}"
            binding.tvCodigoBarra.text = "Código de barra: ${producto.cod_barra}"



            binding.root.setOnClickListener {
                onItemClick(producto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductoDialogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount(): Int = productos.size
}
