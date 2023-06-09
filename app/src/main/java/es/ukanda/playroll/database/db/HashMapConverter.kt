package es.ukanda.playroll.database.db

import androidx.room.TypeConverter

class HashMapConverter {
    @TypeConverter
    fun fromString(value: String?): HashMap<String, String> {
        val map = HashMap<String, String>()
        value?.let {
            val pairs = it.split(",").map { pair ->
                pair.split("=")
            }
            pairs.forEach { pair ->
                if (pair.size == 2) {
                    map[pair[0]] = pair[1]
                }
            }
        }
        return map
    }

    @TypeConverter
    fun toString(map: HashMap<String, String>): String {
        val pairs = map.map { "${it.key}=${it.value}" }
        return pairs.joinToString(",")
    }
}


