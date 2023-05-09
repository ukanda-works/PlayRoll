package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.gson.Gson

@Entity(tableName = "parties")
data class Party(
    @PrimaryKey(autoGenerate = true) val partyID: Int=0,
    val partyName: String,
    val partyDescription: String? = null,
) {
    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}