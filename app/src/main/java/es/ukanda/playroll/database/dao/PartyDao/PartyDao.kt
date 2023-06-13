package es.ukanda.playroll.database.dao.PartyDao

import androidx.room.*
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity

@Dao
interface PartyDao {
    @Transaction
    @Query("SELECT * FROM parties")
    fun getAllParties(): MutableList<Party>

    @Query("SELECT * FROM parties WHERE partyID = :partyId")
    suspend fun getPartyById(partyId: Int): Party

    @Transaction
    @Query("SELECT * FROM parties WHERE partyID = :id")
    fun getParty(id: Int): Party

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParty(party: Party) : Long

    @Query("SELECT * FROM parties WHERE identifier = :identifier")
    fun getPartyByIdentifier(identifier: String): Party

    @Query("SELECT EXISTS(SELECT * FROM parties WHERE identifier = :identifier)")
    fun checkPartyByIdentifier(identifier: String): Boolean
}