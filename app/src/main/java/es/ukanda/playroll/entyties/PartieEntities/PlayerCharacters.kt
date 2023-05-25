package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.ForeignKey
import com.google.gson.Gson

@Entity(tableName = "party_player_character",
    primaryKeys = ["partyID", "characterID"],
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
    var playerID: Int? = null,
    var characterID: Int
){
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
