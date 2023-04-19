package es.ukanda.playroll.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.ukanda.playroll.databinding.ItemPartidaRvBinding
import es.ukanda.playroll.ui.ViewHolder.PartyViewHolder

class PartyAdapter(val parties: List<String>): RecyclerView.Adapter<PartyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyViewHolder {
    val binding = ItemPartidaRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PartyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return parties.size
    }

    override fun onBindViewHolder(holder: PartyViewHolder, position: Int) {
        with(holder){
            val item = parties[position]
            binding.tvNombrePartida.text = item
        }
    }
}