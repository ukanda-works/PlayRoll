package es.ukanda.playroll.database.dao.PartyDao

import androidx.room.*
import es.ukanda.playroll.entyties.PartieEntities.Player

@Dao
interface PlayerDao {

    @Query("SELECT * FROM players WHERE playerID = :playerId")
    suspend fun getPlayerById(playerId: Int): Player

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player): Long


    @Delete
    suspend fun deletePlayer(player: Player)

    @Query("SELECT * FROM players")
    suspend fun getAllPlayers(): List<Player>

    @Query("SELECT * FROM players WHERE identifier = :identifier")
    suspend fun getPlayerByIdentifier(identifier: String): Player

    @Query("SELECT EXISTS(SELECT * FROM players WHERE identifier = :identifier)")
    suspend fun checkPlayerByIdentifier(identifier: String): Boolean

}