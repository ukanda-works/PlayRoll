package es.ukanda.playroll.entyties.PartieEntities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import com.google.gson.Gson
import es.ukanda.playroll.database.db.HashMapConverter
import es.ukanda.playroll.database.db.IntHashMapConverter

@Entity(tableName = "parties")
data class Party(
    @PrimaryKey(autoGenerate = true) val partyID: Int=0,
    val partyName: String,
    val partyCreator: String,
    val partyDescription: String? = null,
    @TypeConverters(HashMapConverter::class)
    var partyConfig: HashMap<String, String>? = HashMap(),
    var sessions: Int = 0,
    var own : Boolean = false
    //añadir campo para configuracion
) {
    override fun hashCode(): Int {
        val prime = 31
        var result = 1

        result = result * prime + partyName.hashCode()
        result = result * prime + partyCreator.hashCode()
        result = result * prime + (partyDescription?.hashCode() ?: 0)
        result = result * prime + (partyConfig?.hashCode() ?: 0)
        result = result * prime + sessions

        return result
    }
    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }



    fun getPartyConfig(tipe:String){
        partyConfig?.get(tipe)
    }

    companion object{
        enum class configType {
            OnlyOwn, //Solo se puden usar personajes del master true/false
            Pass, //Indica si la partida tiene contraseña true/false
        }

        fun fromJson(json: String): Party {
            val gson = Gson()
            return gson.fromJson(json, Party::class.java)
        }

        fun removeIdFromParty(party: Party): Party {
            return Party(
                partyName = party.partyName,
                partyCreator = party.partyCreator,
                partyDescription = party.partyDescription,
                partyConfig = party.partyConfig,
                sessions = party.sessions,
                own = party.own
            )
        }

    }

}