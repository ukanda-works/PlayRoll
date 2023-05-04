package es.ukanda.playroll.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import es.ukanda.playroll.databinding.ItemPartidaRvBinding
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.ui.ViewHolder.PartyViewHolder
import es.ukanda.playroll.ui.ViewModel.ConexionViewModel
import es.ukanda.playroll.ui.fragment.JoinPartyFragment
import java.net.InetAddress

class PartyAdapter(val parties: Map<InetAddress, Party>): RecyclerView.Adapter<PartyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyViewHolder {
        val binding = ItemPartidaRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PartyViewHolder(binding, parties.keys.elementAt(viewType))
    }

    override fun getItemCount(): Int {
        return parties.size
    }

    override fun onBindViewHolder(holder: PartyViewHolder, position: Int) {
        with(holder){
            val ip = parties.keys.elementAt(position)
            val party = parties[ip]
            binding.tvNombrePartida.text = party?.partyName
            binding.tvNombreCreador.text = ip.toString()
        }
    }
}