package es.ukanda.playroll.database.dao.PartyDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters WHERE characterID = :characterId")
    suspend fun getCharacterById(characterId: Int): CharacterEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCharacter(character: CharacterEntity) : Long

    @Query("SELECT * FROM characters")
    fun getAllCharacters(): List<CharacterEntity>

    @Query("SELECT * FROM characters WHERE identifier = :identifier")
    fun getCharacterByIdentifier(identifier: String): CharacterEntity

    @Query("SELECT EXISTS(SELECT * FROM characters WHERE identifier = :identifier)")
    fun checkCharacterByIdentifier(identifier: String): Boolean
}