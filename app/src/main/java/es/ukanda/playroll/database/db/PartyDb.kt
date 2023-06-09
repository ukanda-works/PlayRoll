package es.ukanda.playroll.database.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import es.ukanda.playroll.database.dao.PartyDao.*
import es.ukanda.playroll.entyties.PartieEntities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(
    entities = [CharacterEntity ::class, Party::class, Player::class, Inventario::class, PlayerCharacters::class],
    version = 16,
)
@TypeConverters(Converters::class,HashMapConverter::class,IntHashMapConverter::class)
abstract class PartyDb: RoomDatabase() {
    abstract fun partyDao(): PartyDao
    abstract fun characterDao(): CharacterDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun playerDao(): PlayerDao
    abstract fun playerCharacterDao(): PlayerCharacterDao

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
    fun cleanDb() {
        CoroutineScope(Dispatchers.IO).launch {
            clearAllTables()
        }
    }
}