package es.ukanda.playroll.ui.ViewHolder

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import es.ukanda.playroll.databinding.ItemPartidaRvBinding
import es.ukanda.playroll.singleton.ControllSocket
import es.ukanda.playroll.ui.ViewModel.ConexionViewModel
import es.ukanda.playroll.ui.fragment.JoinPartyFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress

class PartyViewHolder(val binding: ItemPartidaRvBinding, val ip: InetAddress): RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.setOnClickListener {
            Toast.makeText(binding.root.context, "Conectando a ${ip.toString()}", Toast.LENGTH_SHORT).show()
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    ControllSocket.conectar(ip)
                }
            } catch (e: Exception) {
                Toast.makeText(binding.root.context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
