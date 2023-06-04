package es.ukanda.playroll.entyties.PartieEntities

import android.text.Layout.Alignment
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import es.ukanda.playroll.database.db.Converters
import es.ukanda.playroll.database.db.IntHashMapConverter

@Entity(tableName = "characters"
)
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
    var background: String,
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
                "", "", "", "", 0, 0, 0,"", HashMap(), listOf(), listOf())

    fun toJson(): String {
        val gson = com.google.gson.Gson()
        return gson.toJson(this)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1

        result = result * prime + name.hashCode()
        result = result * prime + playerName.hashCode()
        result = result * prime + description.hashCode()
        result = result * prime + clase.hashCode()
        result = result * prime + race.hashCode()
        result = result * prime + alignment
        result = result * prime + level
        result = result * prime + experience
        result = result * prime + background.hashCode()
        result = result * prime + statistics.hashCode()
        result = result * prime + salvaciones.hashCode()
        result = result * prime + skills.hashCode()

        return result
    }
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

        fun getAlignment(alignment: Int): String {
            return typeAlignment[alignment]!!
        }

        fun getAlignment(alignment: String): Int {
            return typeAlignment.filterValues { it == alignment }.keys.first()
        }

        fun fromJson(json: String): CharacterEntity {
            val gson = com.google.gson.Gson()
            return gson.fromJson(json, CharacterEntity::class.java)
        }
        fun removeIdFromCharacter(character: CharacterEntity): CharacterEntity {
            return CharacterEntity(
                name = character.name,
                playerName = character.playerName,
                description = character.description,
                clase = character.clase,
                race = character.race,
                alignment = character.alignment,
                level = character.level,
                experience = character.experience,
                background = character.background,
                statistics = character.statistics,
                salvaciones = character.salvaciones,
                skills = character.skills
            )
        }

    }

}