package apolo.tienda.tienda.presentation.offline.inventario.listar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import apolo.tienda.tienda.R
import apolo.tienda.tienda.data.local.response.ListaTomaInventarioResponse
import apolo.tienda.tienda.databinding.ListTomaInventarioItemBinding
import apolo.tienda.tienda.utils.DateUtils

class ListarTomaInventariosAdapter(
    private val items: List<ListaTomaInventarioResponse>,
    private val onContinuarClicked: (ListaTomaInventarioResponse) -> Unit,
    private val onEnviarClicked: (ListaTomaInventarioResponse) -> Unit,
    private val onFinalizarClicked: (ListaTomaInventarioResponse) -> Unit
) : RecyclerView.Adapter<ListarTomaInventariosAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ListTomaInventarioItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListaTomaInventarioResponse) = with(binding) {
            tvId.text = "Toma Nro.: ${item.id}"
            tvNroAjuste.text = "Nro. Ajuste: ${item.nroAjuste}"
            tvComentario.text = item.comentario
            tvTotalRegistros.text = item.totalRegistros.toString()
            tvTotalRegistrosEnviados.text = item.totalRegistrosEnviados.toString()

            val fecha = DateUtils.parseDateFromLocal(item.fecha)
            tvFecha.text = DateUtils.formatDateToShow(fecha)


            if (item.totalRegistros > 0 && item.totalRegistros == item.totalRegistrosEnviados) {
                binding.llFondo.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.verde_enviado)
                )
            } else {
                binding.llFondo.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
            }

            binding.btnContinuar.setOnClickListener {
                onContinuarClicked.invoke(item)
            }

            if(item.totalRegistros == item.totalRegistrosEnviados) {
                btnFinalizar.isClickable = true
                btnEnviar.isClickable = false
                btnFinalizar.isEnabled = true
                btnEnviar.isEnabled = false
            } else {
                btnFinalizar.isClickable = false
                btnEnviar.isClickable = true
                btnFinalizar.isEnabled = false
                btnEnviar.isEnabled = true
            }

            binding.btnEnviar.setOnClickListener {
                onEnviarClicked.invoke(item)
            }

            binding.btnFinalizar.setOnClickListener {
                onFinalizarClicked.invoke(item)
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListTomaInventarioItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
