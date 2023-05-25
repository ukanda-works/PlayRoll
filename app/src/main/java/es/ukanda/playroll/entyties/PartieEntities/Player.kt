package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.Gson


@Entity(tableName = "players",
        foreignKeys = [ForeignKey(entity = Party::class,
                                  parentColumns = ["partyID"],
                                  childColumns = ["partyID"],
                                  onDelete = ForeignKey.CASCADE)])
data class Player(
    @PrimaryKey(autoGenerate = true) val playerID: Int = 0,
    val partyID: Int,
    val name: String,
    val identifier: String,// es el hash del email del jugador
) {
    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
    companion object {
        fun fromJson(json: String): Player {
            val gson =  Gson()
            return gson.fromJson(json, Player::class.java)
        }
        fun removeIdFromPlayer(player: Player): Player {
            return Player(
                partyID = player.partyID,
                name = player.name,
                identifier = player.identifier
                // añadir campos adicionales aquí si es necesario
            )
        }
    }

}