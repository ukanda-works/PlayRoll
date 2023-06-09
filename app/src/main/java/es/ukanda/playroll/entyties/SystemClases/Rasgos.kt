package es.ukanda.playroll.entyties.SystemClases

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import es.ukanda.playroll.database.db.Converters
import org.jetbrains.annotations.Nullable

@Entity
data class Rasgos(
    @PrimaryKey
    val nombreIdentify: String,
    val nombre: String,
    val descripcion: String,
    @TypeConverters(Converters::class)
    val modifyer: List<String>,
    @TypeConverters(Converters::class)
    val type :List<String>,//sera un enum
    @TypeConverters(Converters::class)
    val typeModifyer :List<String>//sera un enum
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Rasgos

        if (nombreIdentify != other.nombreIdentify) return false
        if (nombre != other.nombre) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nombreIdentify.hashCode()
        result = 31 * result + nombre.hashCode()
        return result
    }
}
