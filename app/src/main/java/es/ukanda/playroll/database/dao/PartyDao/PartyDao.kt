package es.ukanda.playroll.database.dao.PartyDao

import androidx.room.*
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity

@Dao
interface PartyDao {
    @Transaction
    @Query("SELECT * FROM parties")
    fun getAllParties(): MutableList<Party>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParty(party: Party)

    @Query("SELECT * FROM characters WHERE partyID = :partyId")
    fun getCharactersForParty(partyId: Int): List<CharacterEntity>
}