package es.ukanda.playroll.database.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import es.ukanda.playroll.database.dao.PartyDao.CharacterDao
import es.ukanda.playroll.database.dao.PartyDao.InventarioDao
import es.ukanda.playroll.database.dao.PartyDao.PartyDao
import es.ukanda.playroll.database.dao.PartyDao.PlayerDao
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Inventario
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.entyties.PartieEntities.Player


@Database(
    entities = [CharacterEntity ::class, Party::class, Player::class, Inventario::class],
    version = 6,
)
@TypeConverters(Converters::class,HashMapConverter::class,IntHashMapConverter::class)
abstract class PartyDb: RoomDatabase() {
    abstract fun partyDao(): PartyDao
    abstract fun characterDao(): CharacterDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun playerDao(): PlayerDao

companion object {
        @Volatile
        private var INSTANCE: PartyDb? = null

        fun getDatabase(context: Context): PartyDb {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PartyDb::class.java,
                    "parties_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
}
}