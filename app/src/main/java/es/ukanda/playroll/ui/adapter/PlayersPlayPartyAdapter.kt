package es.ukanda.playroll.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.ukanda.playroll.databinding.ItemPlayersPartyBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Player
import es.ukanda.playroll.entyties.PartieEntities.PlayerCharacters
import es.ukanda.playroll.ui.fragment.PlayPartyFragment
import es.ukanda.playroll.ui.fragment.PlayPartyPlayersFragment

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
        return players.count()
    }

    override fun onBindViewHolder(holder: PlayersPlayPartyViewHolder, position: Int) {
        val player = players[position]
        val playerCharacter = playerCharacters.find { it.playerID == player.playerID }
        val character = characters.find { it.characterID == playerCharacter?.characterID }
        holder.bind(player!!, character!!)
    }

    inner class PlayersPlayPartyViewHolder(val binding: ItemPlayersPartyBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(player: Player, characterEntity: CharacterEntity){
            binding.tvNameItemCharacter2.text = characterEntity.name
            binding.tvRaceItemCharacter.text = characterEntity.race
            binding.tvClaseItemCharacter.text = characterEntity.clase
            binding.tvLevel.text = characterEntity.level.toString()
            binding.tvNamePlayerItemPlayers.text = player.name
            binding.btPlayerInteract.setOnClickListener {
                if(PlayPartyFragment.isMasterCompanion){
                    //PlayPartyPlayersFragment.getInstance().pedirTirada(PlayPartyFragment.playersIpCompanion.get(characterEntity.name)!!)
                }
            }
        }
    }
}