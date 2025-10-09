package com.example.aprimortech.data.local.converters

import androidx.room.TypeConverter
import com.example.aprimortech.model.ContatoCliente
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ContatoClienteListConverter {
    @TypeConverter
    fun fromList(list: List<ContatoCliente>?): String {
        return Gson().toJson(list ?: emptyList<ContatoCliente>())
    }

    @TypeConverter
    fun toList(json: String?): List<ContatoCliente> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<ContatoCliente>>() {}.type
        return Gson().fromJson(json, type)
    }
}

