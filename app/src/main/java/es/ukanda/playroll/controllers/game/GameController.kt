package es.ukanda.playroll.controllers.game

import android.content.Context
import es.ukanda.playroll.controllers.helpers.ComunicationHelpers
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.entyties.PartieEntities.Player

class GameController(context: Context) {
    val db = PartyDb.getDatabase(context)
    val partyDao = db.partyDao()
    val characterDao = db.characterDao()
    val playerDao = db.playerDao()
    val playerCharacterDao = db.playerCharacterDao()
    val inventarioDao = db.inventarioDao()

    suspend fun getCurrentPlayer(): Player {
        val identifier = ComunicationHelpers.getHashFromUser()
        val playerAlocated = playerDao.getAllPlayers()
        playerAlocated.find { it.identifier == identifier }?.let {
            return it
        } ?: run {
            val newPlayer = Player( name = "alias", identifier = identifier)
            val id = playerDao.insertPlayer(newPlayer)
            return playerDao.getPlayerById(id.toInt())
        }
    }

}