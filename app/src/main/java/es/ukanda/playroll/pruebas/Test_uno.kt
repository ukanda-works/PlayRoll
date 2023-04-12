package es.ukanda.playroll.pruebas

import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import es.ukanda.playroll.database.db.SystemDb
import es.ukanda.playroll.entyties.SystemClases.GameClas
import es.ukanda.playroll.entyties.SystemClases.Race
import es.ukanda.playroll.entyties.SystemClases.Rasgos

class Test_uno(context : Context) {
    val rasgoDao = SystemDb.getDatabase(context).rasgoDao()
    val raceDao = SystemDb.getDatabase(context).raceDao()

    fun test() {
        var rasgosList = mutableListOf<Rasgos>()
        rasgosList.add(Rasgos("incremento_caracteristica_enana_constitucion",
            "Incremento de característica",
            " Tu puntuación en Constitución es aumentada en 2",
            listOf("constitucion","2"),
            listOf("caracteristica"),
            listOf("incremento")))

        rasgosList.add(Rasgos("vision_oscuridad",
            "Visión en la Oscuridad",
            "Tienes ventaja en las tiradas de sal vación contra veneno, y posees resistencia contra el daño por veneno (explicado en el Capítulo 9).",
            listOf(),
            listOf("NIC"),
            listOf()))

        rasgosList.add(Rasgos("entrenamiento_combate_enano",
            "Entrenamiento de Combate Enano.",
            "Eres competente con el hacha de batalla, hacha de mano, martillo arrojadizo y martillo de guerra",
            listOf("hacha de batalla", "hacha de mano", "martillo arrojadizo", "martillo de guerra"),
            listOf("competencia","0"),
            listOf("armas")))

        rasgosList.add(Rasgos("afinidad_con_la_piedra",
            "Afinidad con la Piedra.",
"Cuando quiera que hagas una prueba de Inteligencia (Historia) relacionada con el origen de un trabajo hecho en piedra, eres considerado competente en la habilidad de Historia y añades el doble de tu bonificador de competencia a la tirada, en lugar de tu bonificador de competencia normal.",
            listOf("2"),
            listOf("competencia"),
            listOf("historia")))

        rasgosList.add(Rasgos("competencia_herramientas",
            "Competencia con Herramientas",
            "Ganas competencia con unas herramientas de artesano a tu elección: herramientas de herrero, materiales de cervecería o herramientas de al bañil.",
            listOf("herramientas de herrero", "materiales de cervecería", "herramientas de al bañil"),
            listOf("competencia","1"),
            listOf("heramientas")))

        rasgosList.add(Rasgos("incremento_caracteristica_enana_sabiduria",
            "Incremento de Puntuación de Característica.",
            "Tu puntuación de Sabiduría aumenta en 1.",
            listOf("sabiduria","1"),
            listOf("caracteristica"),
            listOf("incremento")))

        rasgosList.add(Rasgos("dureza_enana",
            "Dureza Enana.",
            "Tus Puntos de Golpe máximos aumentan en 1, y aumentan en 1 cada vez que ganes un nivel.",
            listOf("1"),
            listOf("hit_points"),
            listOf("incremento")))

        rasgosList.add(Rasgos("incremento_caracteristica_enana_fuerza",
            "Incremento de Puntuación de Característica.",
            "Tu puntuación en fuerza aumenta en 2.",
            listOf("fuerza","2"),
            listOf("caracteristica"),
            listOf("aumento")))
        rasgosList.add(Rasgos("competencia_armaduras_enanas",
            "Entrenamiento con Armadura Enana.",
            "Tienes competencia con las armaduras ligeras y medias",
            listOf("armaduras ligeras", "armaduras medias"),
            listOf("competencia","0"),
            listOf("armaduras")))

        rasgoDao.insertAllRasgos(rasgosList)
    }

    fun createRace(){
        var raceList= mutableListOf<Race>()
        raceList.add(Race(
            "enano",
            "Enano",
            "",
            "Los enanos son un pueblo de pequeña estatura, pero de gran fortaleza y constitución. Su vida se centra en la supervivencia y la protección de su hogar, la montaña subterránea en la que viven. Los enanos son tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros y a sus familias, y son muy orgullosos de su pasado. Los enanos son muy tradicionalistas y conservadores, y su cultura está profundamente arraigada en la historia de su raza. Los enanos son muy leales a sus compañeros",
            listOf("incremento_caracteristica_enana_constitucion","vision_oscuridad","resistencia_enana","entrenamiento_combate_enano","afinidad_con_la_piedra","competencia_herramientas"),
            hashMapOf("edad" to "250",
                "tamaño" to "4-5",
                "velocidad" to "25",
                "idiomas" to "comun-enano",)
        ))
        raceList.add(Race(
                "enano_colinas",
                "Enano de las Colinas",
                "enano",
                "Como enano de las colinas tienes sentidos perspicaces, una profunda intuición y una notable resistencia. Los Enanos dorados de Faerûn en su poderoso  reino del sur son enanos de las colinas, como son el exiliado Neidar y el envilecido Klar of Krynn en el escenario de Dragonlance.",
                listOf("incremento_caracteristica_enana_sabiduria","dureza_enana"),
                hashMapOf()
        ))

        raceList.add(Race(
                "enano_montaña",
                "Enano de la Montaña",
                "enano",
            "Como enano de las montañas, eres fuerte y duro, acostumbrado a una vida difícil en un terreno áspero. Probablemente eres alto (para un enano) y tiendes hacia una coloración de piel más clara. Los enanos escudo del norte de Faerûn a la vez que el dominante clan Hylar y el noble clan Daewar de Dragonlance, son enanos de las montañas.",
                listOf("incremento_caracteristica_enana_fuerza","competencia_armaduras_enanas"),
                hashMapOf()
        ))
        raceDao.insertAllRaces(raceList)
    }

    public fun createClasses(){
        var classesList= mutableListOf<GameClas>()
        classesList.add(GameClas(
            "barbaro",
            "Barbaro",
            "El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia le permiten sobrevivir en batalla y luchar contra enemigos más poderosos. El barbaro es un guerrero que se enfoca en la fuerza bruta y la resistencia. Aunque no es un experto en armas, su fuerza y resistencia",
            "1d12",
            listOf("armas_simples","armas_marciales"),
            listOf("armaduras_ligeras","armaduras_medias","escudos"),
            listOf(),
            listOf(),
            0,
            listOf("fuerza","constitucion"),
            listOf("atletismo","intimidacion","naturaleza","percepcion","supervivencia", "trato_animales"),
            2,
            hashMapOf(
                "furia" to 1,)
        ))

    }

    public fun rasgosClase() {
        var rasgosList = mutableListOf<Rasgos>()
        rasgosList.add(
            Rasgos(
                "furia",
                "Furia",
                " Tu puntuación en Constitución es aumentada en 2",
                listOf("constitucion", "2"),
                listOf("caracteristica"),
                listOf("incremento")
            )
        )

    }
}
