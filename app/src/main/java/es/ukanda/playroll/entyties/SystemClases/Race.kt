package es.ukanda.playroll.entyties.SystemClases

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import es.ukanda.playroll.database.db.Converters
import es.ukanda.playroll.database.db.HashMapConverter

@Entity
data class Race(
    @PrimaryKey var raceNameID :String,
    var raceName :String,
    var subRaza: String,//Hace referencia al nombre de la raza a la que pertenece
    var description :String,
    @TypeConverters(Converters::class)
    val rasgosList: List<String>,
    @TypeConverters(HashMapConverter::class)
    var caracteristicas :HashMap<String,String>,
    ) {
}