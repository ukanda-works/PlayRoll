package es.ukanda.playroll.pruebas

import android.content.Context
import es.ukanda.playroll.controllers.RaceController
import es.ukanda.playroll.entyties.SystemClases.Race
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//esta clase se encarga de cargar los datos de la base de datos
class LoadData(context: Context) {
    val controller = RaceController(context)
    fun load(){
        CoroutineScope(Dispatchers.IO).launch {
            createRace()
        }
    }
    fun createRace(){
        controller.deleteAllRaces()
        var raceList= mutableListOf<Race>()
        raceList.add(
            Race(
            "enano",
            "Enano",
            "",
            "Los enanos son un pueblo de pequeña estatura, pero de gran fortaleza y constitución. Su vida se centra en la supervivencia y la protección de su hogar, la montaña subterránea en la que viven. Los enanos son tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros",
            listOf("incremento_caracteristica_enana_constitucion","vision_oscuridad","resistencia_enana","entrenamiento_combate_enano","afinidad_con_la_piedra","competencia_herramientas"),
            hashMapOf("edad" to "250",
                "tamaño" to "4-5",
                "velocidad" to "25",
                "idiomas" to "comun-enano",)
        )
        )
        raceList.add(
            Race(
            "enano_colinas",
            "Enano de las Colinas",
            "enano",
            "Como enano de las colinas tienes sentidos perspicaces, una profunda intuición y una notable resistencia. Los Enanos dorados de Faerûn en su poderoso  reino del sur son enanos de las colinas, como son el exiliado Neidar y el envilecido Klar of Krynn en el escenario de Dragonlance.",
            listOf("incremento_caracteristica_enana_sabiduria","dureza_enana"),
            hashMapOf()
        )
        )

        raceList.add(
            Race(
            "enano_montaña",
            "Enano de la Montaña",
            "enano",
            "Como enano de las montañas, eres fuerte y duro, acostumbrado a una vida difícil en un terreno áspero. Probablemente eres alto (para un enano) y tiendes hacia una coloración de piel más clara. Los enanos escudo del norte de Faerûn a la vez que el dominante clan Hylar y el noble clan Daewar de Dragonlance, son enanos de las montañas.",
            listOf("incremento_caracteristica_enana_fuerza","competencia_armaduras_enanas"),
            hashMapOf()
        )
        )
        raceList.add(
            Race(
                "elfo",
                "Elfo",
                "",
                "Los elfos son seres mágicos y elegantes, conocidos por su gracia y destreza. Son expertos arqueros y poseen una conexión especial con la naturaleza. Los elfos son inmortales y valoran la belleza y el conocimiento. Son sabios y pacientes, pero también pueden ser orgullosos y distantes.",
                listOf("incremento_caracteristica_elfo_destreza", "visión_en_la_oscuridad", "resistencia_encantamientos", "trance_élfico", "afinidad_natural"),
                hashMapOf("edad" to "700", "tamaño" to "5-6", "velocidad" to "30", "idiomas" to "común-élfico")
            )
        );

        raceList.add(
            Race(
                "mediano",
                "Mediano",
                "",
                "Los medianos, también conocidos como hobbits, son criaturas pacíficas y amantes de la comodidad. Son expertos en el arte de la evasión y prefieren una vida sencilla y tranquila. Los medianos son muy hospitalarios y valoran la amistad y el buen comer. A pesar de su tamaño pequeño, los medianos son valientes y tenaces cuando se enfrentan a peligros inminentes.",
                listOf("incremento_caracteristica_mediano_destreza", "sigiloso", "resistencia_mediano", "suerte_halfling"),
                hashMapOf("edad" to "100", "tamaño" to "3-4", "velocidad" to "25", "idiomas" to "común")
            )
        );

        raceList.add(
            Race(
                "humano",
                "Humano",
                "",
                "Los humanos son una raza diversa y adaptable. Son conocidos por su capacidad para adaptarse a diferentes entornos y circunstancias. Los humanos son ambiciosos y emprendedores, y su espíritu les impulsa a explorar y conquistar nuevos territorios. Son capaces de destacar en diversas disciplinas y tienen una gran capacidad de aprendizaje.",
                listOf("incremento_caracteristica_humano", "versatilidad_humana"),
                hashMapOf("edad" to "80", "tamaño" to "5-6", "velocidad" to "30", "idiomas" to "común + un idioma adicional")
            )
        );

        raceList.add(
            Race(
                "draconido",
                "Draconido",
                "",
                "Los draconidos son descendientes de dragones y poseen características de estas poderosas criaturas. Tienen escamas, garras y a menudo una habilidad especial de respirar fuego o frío. Los draconidos son fuertes y resistentes, y tienen una conexión innata con la magia. Son orgullosos y valientes, y a menudo se destacan como líderes y guerreros temibles.",
                listOf("incremento_caracteristica_draconido_fuerza", "resistencia_draconido", "aliento_draconico"),
                hashMapOf("edad" to "120", "tamaño" to "5-6", "velocidad" to "30", "idiomas" to "común + un idioma adicional")
            ))
        controller.addRaceList(raceList)
    }

}