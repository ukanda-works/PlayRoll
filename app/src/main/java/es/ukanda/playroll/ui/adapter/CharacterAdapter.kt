package es.ukanda.playroll.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.ukanda.playroll.databinding.ItemPlayerBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity

class CharacterAdapter(val characters: List<CharacterEntity>, val maxSelect: Int,private val onCharacterSelected: (CharacterEntity, Boolean) -> Unit): RecyclerView.Adapter<CharacterAdapter.CharacterViewHolderr>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolderr {
        val binding = ItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterViewHolderr(binding, maxSelect)
    }


    private val selectedCharacters = mutableListOf<CharacterEntity>()

    override fun getItemCount(): Int {
        return characters.size
    }

   override fun onBindViewHolder(holder: CharacterViewHolderr, position: Int) {
        val character = characters[position]
        holder.bind(character)
    }

    inner class CharacterViewHolderr(val binding: ItemPlayerBinding, val maxSelct: Int): RecyclerView.ViewHolder(binding.root) {
        fun bind(character: CharacterEntity){
            binding.tvNameItemCharacter.text = character.name
            binding.tvClaseItemCharacter.text = character.clase
            binding.tvRaceItemCharacter.text = character.race
            binding.tvLevel.text = character.level.toString()

            binding.cbAddItenCharacter.setOnCheckedChangeListener(null) // Clear previous listener
            binding.cbAddItenCharacter.isChecked = isSelected(character)

            binding.cbAddItenCharacter.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    handleCharacterSelection(character, true)
                } else {
                    handleCharacterSelection(character, false)
                }
            }
        }

        private fun isSelected(character: CharacterEntity): Boolean {
            return character in selectedCharacters
        }
        private fun handleCharacterSelection(character: CharacterEntity, isSelected: Boolean) {
            val currentSelectionCount = selectedCharacters.size

            if (isSelected && currentSelectionCount >= maxSelct) {
                onCharacterSelected(character, false)
                notifyItemChanged(characters.indexOf(character))
                return
            }

            if (isSelected) {
                selectedCharacters.add(character)
            } else {
                selectedCharacters.remove(character)
            }

            onCharacterSelected(character, isSelected)
        }


    }
    fun getSelectedCharacters(): List<CharacterEntity> {
        return selectedCharacters.toList()
    }

}