package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(tableName = "players",
        foreignKeys = [ForeignKey(entity = Party::class,
                                  parentColumns = ["partyID"],
                                  childColumns = ["partyID"],
                                  onDelete = ForeignKey.CASCADE)])
data class Player(
    @PrimaryKey(autoGenerate = true) val playerID: Int = 0,
    val partyID: Int,
    val name: String,
) {
}