package es.ukanda.playroll.entyties.PartieEntities

import android.text.Layout.Alignment
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import es.ukanda.playroll.database.db.Converters
import es.ukanda.playroll.database.db.IntHashMapConverter

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val characterID: Int=0,
    var name: String,
    var playerName: String,
    var description: String,
    var clase: String,
    var race: String,
    var alignment: Int,
    var level: Int,
    var experience: Int,
    @TypeConverters(IntHashMapConverter::class)
    var statistics : HashMap<String,Int>,//hacer un controller para esto
    @TypeConverters(Converters::class)
    var salvaciones : List<String>,//hacer un controller para esto
    @TypeConverters(Converters::class)
    var skills : List<String>,//hacer un controller para esto
    ) {
    //constructor vacio
    constructor() : this(0,
                    "",
                "", "", "", "", 0, 0, 0, HashMap(), listOf(), listOf())

    companion object{
        val typeAlignment = mapOf<Int,String>(
            0 to "lawful_good",
            1 to "neutral_good",
            2 to "chaotic_good",
            3 to "lawful_neutral",
            4 to "neutral",
            5 to "chaotic_neutral",
            6 to "lawful_evil",
            7 to "neutral_evil",
            8 to "chaotic_evil"
        )
    }

}