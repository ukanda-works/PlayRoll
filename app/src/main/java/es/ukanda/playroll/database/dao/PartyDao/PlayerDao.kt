package es.ukanda.playroll.database.dao.PartyDao

import androidx.room.*
import es.ukanda.playroll.entyties.PartieEntities.Player

@Dao
interface PlayerDao {

    @Query("SELECT * FROM players WHERE playerID = :playerId")
    suspend fun getPlayerById(playerId: Int): Player

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player): Long

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    @Query("SELECT * FROM players")
    suspend fun getAllPlayers(): List<Player>
}