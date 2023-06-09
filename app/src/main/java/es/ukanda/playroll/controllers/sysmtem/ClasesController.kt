package es.ukanda.playroll.controllers.sysmtem

import android.content.Context

class ClasesController(context: Context) {
    fun getAllClases(): List<String> {
        return listOf("Barbaro", "Bardo", "Clerigo", "Druida", "Guerrero", "Mago", "Monje", "Paladin", "Picaro", "Explorador", "Brujo")
    }
}