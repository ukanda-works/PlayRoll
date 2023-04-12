package es.ukanda.playroll.entyties

import es.ukanda.playroll.entyties.SystemClases.GameClas
import es.ukanda.playroll.entyties.SystemClases.Inventory
import es.ukanda.playroll.entyties.SystemClases.Race
import es.ukanda.playroll.entyties.SystemClases.Stats

//Todo: hacer las modificaciones necesarias para persistir los datos en rom
data class Character(
    //atributos relativos a la personalizacion del personaje
    var characterName: String,
    var playerName: String,//si es un npc el nombre del jugador sera master
    var type: String,//Todo: que sea un enum
    var descripcion: String,

    //atributos relativos a las estadisticas del personaje
    var level: Int,
    var experience: Int,
    var alignment: String,//Todo: que sea un enum ?
    var background: String,//Todo: que sea un enum ?

    //atributos relativos al sistema de juego
    var gameClasses: HashMap<GameClas, Int>,//HashMap<Clase, nivel>
    var race : Race,
    var inventory: Inventory,
    var stats : Stats

    //Todo: agregar mas cosas ?
    ){
    //Variables no persistentes, estas variables se calculan en tiempo de ejecucion
    var speed: Int = 0//inicializar en el constructor
    var initiative: Int = 0//inicializar en el constructor

    //Todo: hacer el objeto del personaje
}
