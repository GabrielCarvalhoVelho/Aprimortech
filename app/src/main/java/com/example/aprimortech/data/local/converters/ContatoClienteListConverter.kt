package com.example.aprimortech.data.local.converters

import androidx.room.TypeConverter
import com.example.aprimortech.model.ContatoCliente
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Conversor para armazenar lista de contatos no Room Database
 * Converte entre List<ContatoCliente> e String JSON
 */
class ContatoClienteListConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromContatoList(contatos: List<ContatoCliente>?): String {
        if (contatos == null) return "[]"
        return gson.toJson(contatos)
    }

    @TypeConverter
    fun toContatoList(contatosJson: String?): List<ContatoCliente> {
        if (contatosJson.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<ContatoCliente>>() {}.type
        return gson.fromJson(contatosJson, type)
    }
}

