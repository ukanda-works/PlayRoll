package es.ukanda.playroll.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import es.ukanda.playroll.databinding.ItemPlayerBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity

class CharacterListAdapter(val characters: List<CharacterEntity>): RecyclerView.Adapter<CharacterListAdapter.CharacterListViewHolderr>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterListViewHolderr {
        val binding = ItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterListViewHolderr(binding)
    }

    override fun getItemCount(): Int {
        return characters.size
    }

    override fun onBindViewHolder(holder: CharacterListViewHolderr, position: Int) {
        val character = characters[position]
        holder.bind(character)
    }

    inner class CharacterListViewHolderr(val binding: ItemPlayerBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(character: CharacterEntity){
            binding.tvNameItemCharacter.text = character.name
            binding.tvClaseItemCharacter.text = character.clase
            binding.tvRaceItemCharacter.text = character.race
            binding.tvLevel.text = character.level.toString()

            binding.root.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("id", character.characterID)
                bundle.putString("from", "edit")
                Navigation.findNavController(binding.root).navigate(es.ukanda.playroll.R.id.action_nav_characterList_to_nav_CharacterCreator, bundle)
            }
        }
    }

}