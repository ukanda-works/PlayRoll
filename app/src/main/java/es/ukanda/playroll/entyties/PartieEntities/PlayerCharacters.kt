package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.Gson

@Entity(tableName = "party_player_character",
    primaryKeys = ["partyID", "characterID", "playerID"],
    foreignKeys = [
        ForeignKey(entity = Party::class,
            parentColumns = ["partyID"],
            childColumns = ["partyID"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Player::class,
            parentColumns = ["playerID"],
            childColumns = ["playerID"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CharacterEntity::class,
            parentColumns = ["characterID"],
            childColumns = ["characterID"],
            onDelete = ForeignKey.CASCADE)
    ]
)
data class PlayerCharacters(
    var partyID: Int,
    var playerID: Int,
    var characterID: Int
){
    override fun hashCode(): Int {
        val prime = 31
        var result = 1

        result = result * prime + partyID
        result = result * prime + (playerID ?: 0)
        result = result * prime + characterID

        return result
    }
    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
    companion object{
        fun fromJson(json: String): PlayerCharacters {
            val gson =  Gson()
            return gson.fromJson(json, PlayerCharacters::class.java)
        }
    }
}
