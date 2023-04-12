package es.ukanda.playroll.entyties
//Todo: hacer las modificaciones necesarias para persistir los datos en rom

data class Party(
    val systemType: String,
    var nameParty: String,
    var numSesions: Int,
    var characterList: List<Character>,
    var map: Map
){
    //Todo: hacer el objeto de la partida
}
