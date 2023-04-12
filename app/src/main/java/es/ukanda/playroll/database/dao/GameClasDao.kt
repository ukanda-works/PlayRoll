package es.ukanda.playroll.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import es.ukanda.playroll.entyties.SystemClases.GameClas

@Dao
interface GameClasDao {

    @Query("SELECT * FROM GameClas")
    fun getAllGameClases(): List<GameClas>

    @Insert
    fun insertGameClas(gameClas: GameClas)

    @Insert
    fun insertAllGameClases(gameClases : List<GameClas>)
}