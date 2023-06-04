package es.ukanda.playroll.database.dao.PartyDao

import androidx.room.*
import es.ukanda.playroll.entyties.PartieEntities.Inventario

@Dao
interface InventarioDao {
    @Query("SELECT * FROM inventarios WHERE inventarioID = :inventarioId")
    suspend fun getInventarioById(inventarioId: Int): Inventario

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventario(inventario: Inventario) : Long

    @Update
    suspend fun updateInventario(inventario: Inventario)

    @Delete
    suspend fun deleteInventario(inventario: Inventario)

    @Query("SELECT * FROM inventarios")
    suspend fun getAllInventarios(): List<Inventario>

}