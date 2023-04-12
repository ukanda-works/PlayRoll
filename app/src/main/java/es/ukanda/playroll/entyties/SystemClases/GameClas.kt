package es.ukanda.playroll.entyties.SystemClases

import android.widget.Switch
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import es.ukanda.playroll.database.db.HashMapConverter
import es.ukanda.playroll.database.db.IntHashMapConverter

@Entity
data class GameClas(
    @PrimaryKey val clasNameId: String,
    var clasName :String,
    var description: String,
    var hitDice :String,
    var competenciaArmas: List<String>, //donde se guardara el nombre de las armas
    var competenciaArmaduras: List<String>, //donde se guardara el nombre de las armaduras
    var competenciaHeramientas: List<String>, //donde se guardara el nombre de las herramientas
    var caracteristicasPrimarias: List<String>, //donde se guardara el nombre de las caracteristicas
    var numCaracteristicasPrimarias: Int,// si es 0 se usan todas las caracteristicas si no se usan las que se indiquen
    var salvaciones: List<String>, //donde se guardara el nombre de las salvaciones
    var habliidades: List<String>, //donde se guardara el nombre de las habilidades
    var numHabliidades: Int,// si es 0 se usan todas las habilidades si no se usan las que se indiquen
    @TypeConverters(IntHashMapConverter::class)
    var rasgosClase: HashMap<String, Int>, //donde se guardara el nombre de las habilidades y el nivel de bonificador


){
    fun getBonificadorCompetencia(level :Int):Int{
        if (level in 1..4){
            return 2
        }else if (level in 5..8){
            return 3
        }else if (level in 9..12){
            return 4
        }else if (level in 13..16){
            return 5
        }else if (level in 17..20){
            return 6
        }
        return 0
    }


}
