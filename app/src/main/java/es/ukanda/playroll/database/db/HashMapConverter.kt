package es.ukanda.playroll.database.db

import androidx.room.TypeConverter

class HashMapConverter {
    @TypeConverter
    fun fromString(value: String): HashMap<String, String> {
        val map = HashMap<String, String>()
        val pairs = value.split(",").map { it.split("=") }
        pairs.forEach {
            if (it.size == 2) {
                map[it[0]] = it[1]
            }
        }
        return map
    }

    @TypeConverter
    fun fromHashMap(map: HashMap<String, String>): String {
        val pairs = map.map { "${it.key}=${it.value}" }
        return pairs.joinToString(",")
    }
}