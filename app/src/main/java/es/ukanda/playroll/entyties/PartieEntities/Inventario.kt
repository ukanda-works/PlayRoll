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
    @PrimaryKey(autoGenerate = true)
    val inventarioID: Int=0,
    var partyID: Int,
    var characterID: Int,
    var health: Int,
) {
    override fun hashCode(): Int {
        val prime = 31
        var result = 1

        result = result * prime + partyID
        result = result * prime + characterID
        result = result * prime + health

        return result
    }
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