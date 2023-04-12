package es.ukanda.playroll.database.db

import androidx.room.TypeConverter

class IntHashMapConverter {
    @TypeConverter
    fun fromString(value: String?): HashMap<String, Int> {
        val map = HashMap<String, Int>()
        value?.let {
            val pairs = it.split(",").map { pair ->
                pair.split("=")
            }
            pairs.forEach { pair ->
                if (pair.size == 2) {
                    map[pair[0]] = pair[1].toInt()
                }
            }
        }
        return map
    }

    @TypeConverter
    fun toString(map: HashMap<String, Int>): String {
        val pairs = map.map { "${it.key}=${it.value}" }
        return pairs.joinToString(",")
    }
}