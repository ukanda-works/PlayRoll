package es.ukanda.playroll.controllers.sysmtem

import android.content.Context
import es.ukanda.playroll.database.db.SystemDb
import es.ukanda.playroll.entyties.SystemClases.Race

class RaceController(context: Context) {
    //Dao
    val raceDao = SystemDb.getDatabase(context).raceDao()

    fun addRace(race: Race){
      raceDao.insertRace(race)
    }

    fun deleteRace(race: Race){
        raceDao.deleteRace(race)
    }

    fun updateRace(race: Race){
        //TODO: Implementar updateRace
    }

    fun addRaceList(raceList: List<Race>){
        raceDao.insertAllRaces(raceList)
    }

    fun getAllRaces():List<Race>{
       return raceDao.getAllRaces()
    }

    fun getRace(race: Race){
        //TODO: Implementar getRace
    }

    fun deleteAllRaces(){
        val list = raceDao.getAllRaces()
        list.forEach {
            raceDao.deleteRace(it)
        }
    }
}