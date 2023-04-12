package es.ukanda.playroll.entyties.SystemClases

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration

@Entity
data class RasgosClase(
    @PrimaryKey val nombreIdentify: String,
    val nombre: String,
    val descripcion: String,
    val condicion : String,
    val condicionValor : String,
    val duration: Int,
    val rasgos : List<RasgosClase>,
) {


}