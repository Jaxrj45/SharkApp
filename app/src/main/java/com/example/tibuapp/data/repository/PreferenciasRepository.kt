package com.example.tibuapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión que crea el DataStore una sola vez
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tiburoncitos_preferencias"
)

class PreferenciasRepository(private val context: Context) {

    companion object {
        val COLOR_ACENTO = stringPreferencesKey("color_acento")
        val MODO_NOCHE = booleanPreferencesKey("modo_noche")
        val MODO_NOCHE_AUTOMATICO = booleanPreferencesKey("modo_noche_automatico")

        // Colores disponibles con sus nombres
        val COLORES = listOf(
            Pair("#007AFF", "Azul"),
            Pair("#FF9500", "Ámbar"),
            Pair("#34C759", "Verde"),
            Pair("#AF52DE", "Morado"),
            Pair("#FF2D55", "Rosa"),
            Pair("#5856D6", "Índigo"),
            Pair("#00C7BE", "Teal"),
            Pair("#1C1C1E", "Negro")
        )

        const val COLOR_DEFAULT = "#007AFF"
    }

    // Observar el color actual en tiempo real
    val colorAcento: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[COLOR_ACENTO] ?: COLOR_DEFAULT
    }

    // Observar el modo noche
    val modoNoche: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[MODO_NOCHE] ?: false
    }

    // Observar si el modo automático está activo
    val modoNocheAutomatico: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[MODO_NOCHE_AUTOMATICO] ?: true
    }

    suspend fun guardarColor(color: String) {
        context.dataStore.edit { prefs ->
            prefs[COLOR_ACENTO] = color
        }
    }

    suspend fun guardarModoNoche(activo: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MODO_NOCHE] = activo
        }
    }

    suspend fun guardarModoNocheAutomatico(activo: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MODO_NOCHE_AUTOMATICO] = activo
        }
    }
}