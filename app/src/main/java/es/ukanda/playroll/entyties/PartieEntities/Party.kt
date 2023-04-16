package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "parties")
data class Party(
    @PrimaryKey val partyID: Int,
    val partyName: String,
    val partyDescription: String,
    @Relation(
        parentColumn = "partyID",
        entityColumn = "partyID"
    )
    val characterList: List<CharacterEntity>,
) {

}