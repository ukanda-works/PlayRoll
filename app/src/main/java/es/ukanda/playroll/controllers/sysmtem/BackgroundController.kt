package es.ukanda.playroll.controllers.sysmtem

import android.content.Context

class BackgroundController(context: Context) {
    fun getAllBackgrounds(): List<String> {
        return listOf("Acolito", "Criminal", "Artista", "Erudito", "Heroe del Pueblo", "Eres un forastero", "Marinero", "Soldado", "Urbano")
    }

}