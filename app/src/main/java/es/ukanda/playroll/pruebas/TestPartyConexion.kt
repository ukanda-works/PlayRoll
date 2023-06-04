package es.ukanda.playroll.pruebas

import android.content.Context
import es.ukanda.playroll.controllers.helpers.ComunicationHelpers
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.entyties.PartieEntities.Player
import es.ukanda.playroll.entyties.PartieEntities.PlayerCharacters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestPartyConexion(context: Context) {

   val context = context


    fun load(){
        CoroutineScope(Dispatchers.IO).launch {
            val db = PartyDb.getDatabase(context)

            //se crea una partida y se añade a la base de datos
            val party = Party(7,"partyName","partyCreator","partyDescription",HashMap(),0)
            db.partyDao().insertParty(party)

            val player = Player(name ="ukanda", identifier = ComunicationHelpers.getHashFromUser())
            val playerId = db.playerDao().insertPlayer(player)

            val PlayerCharacters = PlayerCharacters(playerID = playerId.toInt(), characterID = 1, partyID = 7)
            db.playerCharacterDao().insertPartyPlayerCharacter(PlayerCharacters)

        }

    }

}