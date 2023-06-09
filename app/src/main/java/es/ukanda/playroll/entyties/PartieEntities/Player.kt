package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.Gson


@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val playerID: Int = 0,
    val name: String,
    val identifier: String,// es el hash del email del jugador
) {
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = result * prime + name.hashCode()
        result = result * prime + identifier.hashCode()

        return result
    }
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
                name = player.name,
                identifier = player.identifier
            )
        }
    }

}