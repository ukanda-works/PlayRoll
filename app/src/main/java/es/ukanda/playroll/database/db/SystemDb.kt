package es.ukanda.playroll.database.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import es.ukanda.playroll.database.dao.RaceDao
import es.ukanda.playroll.database.dao.RasgoDao
import es.ukanda.playroll.entyties.SystemClases.GameClas
import es.ukanda.playroll.entyties.SystemClases.Race
import es.ukanda.playroll.entyties.SystemClases.Rasgos

@Database(
    entities = [Rasgos::class, Race ::class, GameClas::class],
    version = 3
)
@TypeConverters(Converters::class,HashMapConverter::class,IntHashMapConverter::class)
abstract class SystemDb: RoomDatabase() {
    abstract fun rasgoDao(): RasgoDao
    abstract fun raceDao(): RaceDao

    companion object {
        @Volatile
        private var INSTANCE: SystemDb? = null

        fun getDatabase(context: Context): SystemDb {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SystemDb::class.java,
                    "my_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }


    }
}




