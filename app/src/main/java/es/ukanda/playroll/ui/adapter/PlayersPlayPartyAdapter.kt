package es.ukanda.playroll.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.ukanda.playroll.databinding.ItemPlayersPartyBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Player
import es.ukanda.playroll.entyties.PartieEntities.PlayerCharacters

class PlayersPlayPartyAdapter(val players: List<Player>,
                       val characters: List<CharacterEntity>,
                       val playerCharacters: List<PlayerCharacters>): RecyclerView.Adapter<PlayersPlayPartyAdapter.PlayersPlayPartyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayersPlayPartyViewHolder {
        val binding = ItemPlayersPartyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayersPlayPartyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return playerCharacters.count()
    }

    override fun onBindViewHolder(holder: PlayersPlayPartyViewHolder, position: Int) {
        val playerCharacter = playerCharacters[position]
        val player = players.find { it.playerID == playerCharacter.playerID }
        val character = characters.find { it.characterID == playerCharacter.characterID }
        holder.bind(player!!, character!!)
    }

    inner class PlayersPlayPartyViewHolder(val binding: ItemPlayersPartyBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(player: Player, characterEntity: CharacterEntity){
            binding.tvNameItemCharacter2.text = characterEntity.name
            binding.tvRaceItemCharacter.text = characterEntity.race
            binding.tvClaseItemCharacter.text = characterEntity.clase
            binding.tvLevel.text = characterEntity.level.toString()
            binding.tvNamePlayerItemPlayers.text = player.name
        }
    }
}