package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val characterID: Int,
    val name: String,
    val description: String,
    val partyID: Int
) {
}