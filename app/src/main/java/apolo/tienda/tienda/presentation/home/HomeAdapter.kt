package apolo.tienda.tienda.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import apolo.tienda.tienda.databinding.ItemHomeOptionBinding

class HomeAdapter(
    private val options: List<HomeOption>,
    private val onClick: (HomeOption) -> Unit
) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    inner class HomeViewHolder(private val binding: ItemHomeOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(option: HomeOption) {
            binding.tvTitle.text = option.title
            binding.ivIcon.setImageResource(option.icon)
            binding.root.setOnClickListener { onClick(option) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemHomeOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(options[position])
    }

    override fun getItemCount() = options.size
}
