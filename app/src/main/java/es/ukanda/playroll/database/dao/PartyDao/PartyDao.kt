package es.ukanda.playroll.database.dao.PartyDao

import androidx.room.*
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity

@Dao
interface PartyDao {
    @Transaction
    @Query("SELECT * FROM parties")
    fun getAllParties(): List<Party>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParty(party: Party)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCharacter(character: CharacterEntity)

    @Query("SELECT * FROM characters WHERE partyID = :partyId")
    fun getCharactersForParty(partyId: Int): List<CharacterEntity>
}