package es.ukanda.playroll.database.dao.PartyDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCharacter(character: CharacterEntity)

    @Query("SELECT * FROM characters WHERE partyID = :partyId")
    fun getCharactersForParty(partyId: Int): List<CharacterEntity>

    @Query("SELECT * FROM parties WHERE partyID = (SELECT partyID FROM characters WHERE characterID = :characterId)")
    fun getPartyForCharacter(characterId: Int): Party?
}