package es.ukanda.playroll.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import es.ukanda.playroll.databinding.ItemPartidaRvBinding
import es.ukanda.playroll.entyties.PartieEntities.Party

class PartiesListAdapter(val partiesList: List<Party>):RecyclerView.Adapter<PartiesListAdapter.PartiesListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartiesListViewHolder {
       val binding = ItemPartidaRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PartiesListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return partiesList.size
    }

    override fun onBindViewHolder(holder: PartiesListViewHolder, position: Int) {
        val party = partiesList[position]
        holder.bind(party)
    }

    inner class PartiesListViewHolder(val binding: ItemPartidaRvBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(party: Party){
            binding.tvNombrePartida.text = party.partyName
            binding.tvNombreCreador.text = party.partyCreator

            binding.root.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("id", party.partyID)
                Navigation.findNavController(binding.root).navigate(es.ukanda.playroll.R.id.action_nav_partyList_to_nav_PartyManager, bundle)
            }
        }
    }

}