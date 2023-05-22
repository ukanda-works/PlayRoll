package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.ForeignKey

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
    val partyID: Int,
    val playerID: Int? = null,
    val characterID: Int
)
