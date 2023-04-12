package es.ukanda.playroll.entyties.SystemClases

data class Stats(
    var armorClas :Int,
    var iniciative :Int,//Por default es 0, solo cambia cuando se entra en combate
    var speed: Int,
    var inspiration: Int,
    var hitDice :String,
    var hitPoints :Int,
    var hitPointsMax :Int,
    //estadisticas relacionadas a atributos y demas
    var skills :HashMap<String, Int>,
    var savingThrows :HashMap<String, Int>,
    var characteristics :HashMap<String, Int>,


    //Todo: agregar mas cosas
) {
    //Todo: metodo para comprobar skills, savingThrows y characteristics al crear el objeto
    //Todo: hacer las estadisticas
}