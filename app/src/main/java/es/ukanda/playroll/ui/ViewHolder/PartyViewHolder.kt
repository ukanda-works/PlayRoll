package es.ukanda.playroll.ui.ViewHolder

import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import es.ukanda.playroll.databinding.ItemPartidaRvBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PartyViewHolder(val binding: ItemPartidaRvBinding): RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.setOnClickListener {
            //sustituir por una llamada al viewmodel
            CoroutineScope(Dispatchers.IO).launch {
                //Toast.makeText(binding.root.context, "Partida JAJA", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
