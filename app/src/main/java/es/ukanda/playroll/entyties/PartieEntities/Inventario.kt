package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.Gson

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
    var partyID: Int,
    var characterID: Int,
    var health: Int,
    //movidas varias
    //se aprovecha y se mete aqui la vida
) {
    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
    companion object {
        fun removeIdFromInventario(inventario: Inventario): Inventario {
            return Inventario(
                partyID = inventario.partyID,
                characterID = inventario.characterID,
                health = inventario.health
            )
        }
        fun fromJson(json: String): Inventario {
            val gson =  Gson()
            return gson.fromJson(json, Inventario::class.java)
        }
    }

}