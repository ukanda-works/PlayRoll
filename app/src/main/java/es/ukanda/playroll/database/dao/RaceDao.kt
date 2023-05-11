package es.ukanda.playroll.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import es.ukanda.playroll.entyties.SystemClases.Race

@Dao
interface RaceDao {

    @Query("SELECT * FROM Race")
    fun getAllRaces(): List<Race>

    @Insert
    fun insertRace(race: Race)

    @Insert
    fun insertAllRaces(races : List<Race>)

    @Delete
    fun deleteRace(race: Race)

}