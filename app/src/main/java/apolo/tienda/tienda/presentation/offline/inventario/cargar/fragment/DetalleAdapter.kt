package apolo.tienda.tienda.presentation.offline.inventario.cargar.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import apolo.tienda.tienda.R
import apolo.tienda.tienda.data.local.response.ListaDetalleTomaInventarioResponse
import apolo.tienda.tienda.databinding.ItemDetalleTomaInventarioBinding
import apolo.tienda.tienda.databinding.ItemInventoryDetailBinding
import java.text.NumberFormat
import java.util.Locale

class DetalleAdapter (
    private var items: List<ListaDetalleTomaInventarioResponse>
) : RecyclerView.Adapter<DetalleAdapter.ViewHolder>() {


    inner class ViewHolder(private val binding: ItemDetalleTomaInventarioBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(item: ListaDetalleTomaInventarioResponse) {
            binding.tvDescripcion.text = item.descProducto
            binding.tvCantidad.text = "${item.cantidad}"
            //binding.tvCodigoArticulo.text = "${item.codProducto}"
            val formato = NumberFormat.getNumberInstance(Locale.getDefault())
            formato.minimumFractionDigits = 2
            formato.maximumFractionDigits = 2

            binding.tvCantidad.text = formato.format(item.cantidad)

            binding.tvCodigoBarra.text = "${item.codBarra}"


            if (item.enviado == "S") {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.verde_enviado)
                )
            } else {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                    //ContextCompat.getColor(binding.root.context, R.color.verde_enviado)
                )
            }


        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetalleTomaInventarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(position: Int): ListaDetalleTomaInventarioResponse = items[position]

    fun updateList(nuevaLista: List<ListaDetalleTomaInventarioResponse>) {
        this.items = nuevaLista
        notifyDataSetChanged()
    }

}
