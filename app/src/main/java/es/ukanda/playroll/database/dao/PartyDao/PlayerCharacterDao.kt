package es.ukanda.playroll.database.dao.PartyDao

import androidx.room.*
import es.ukanda.playroll.entyties.PartieEntities.PlayerCharacters

@Dao
interface PlayerCharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartyPlayerCharacter(partyPlayerCharacter: PlayerCharacters)

    @Delete
    suspend fun deletePartyPlayerCharacter(partyPlayerCharacter: PlayerCharacters)
    @Query("SELECT * FROM party_player_character WHERE partyID = :partyId")
    suspend fun getPlayersAndCharactersByPartyId(partyId: Int): List<PlayerCharacters>


}