package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventarios",
    foreignKeys = [ForeignKey(
        entity = Party::class,
        parentColumns = ["partyID"],
        childColumns = ["partyID"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = CharacterEntity::class,
        parentColumns = ["characterID"],
        childColumns = ["characterID"],
        onDelete = ForeignKey.CASCADE
    )]
)

class Inventario(
    @PrimaryKey(autoGenerate = true) val inventarioID: Int=0,
    val partyID: Int,
    val characterID: Int,
    val health: Int,
    //movidas varias
    //se aprovecha y se mete aqui la vida
) {
}