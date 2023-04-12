package es.ukanda.playroll.database.dao

import androidx.room.*
import es.ukanda.playroll.entyties.SystemClases.Rasgos

@Dao
interface RasgoDao {

    @Query("SELECT * FROM Rasgos")
    fun getAllRasgos(): List<Rasgos>

    @Query("SELECT * FROM Rasgos WHERE nombreIdentify IN (:nombreIdentify)")
    fun getByNombreIdentify(nombreIdentify: String): Rasgos

    @Update
    fun updateRasgo(rasgo: Rasgos)

    @Insert
    fun insertRasgo(rasgo: Rasgos)

    @Insert
    fun insertAllRasgos(rasgos: List<Rasgos>)

    @Delete
    fun deleteRasgo(rasgo: Rasgos)
}