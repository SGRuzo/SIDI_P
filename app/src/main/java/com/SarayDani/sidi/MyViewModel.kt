package com.SarayDani.sidi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MyViewModel() : ViewModel() {

    private val TAG_LOG = "miDebug"

    val estadoActual = MutableStateFlow(Estados.Inicio)
    val secuencia =
        MutableStateFlow(mutableListOf<Int>())     // Representa la secuencia que genera el juego

    val secuenciaJugador =
        MutableStateFlow(mutableListOf<Int>())     // Secuencia que introduce el jugador


    var ronda = MutableStateFlow(0)

    init {
        // estado inicial
        Log.d(TAG_LOG, "Inicializando el ViewModel - Estado: ${Estados.Inicio}")
    }

    /**
     * Función para empezar el juego, reinicia los valores
     */
    fun empezarJuego() {
        ronda.value = 1
        secuencia.value.clear()
        secuenciaJugador.value.clear()
        generarColor()
        reproducirSecuencia()

    }

    /**
     * Función para generar un nuevo color en la secuencia, private porque solo la usa el ViewModel
     */
    private fun generarColor() {
        val numero = (0..3).random()
        secuencia.value.add(numero)
        estadoActual.value = Estados.GenerarSecuencia
        Log.d(TAG_LOG, "Generando secuencia - Estado: ${Estados.GenerarSecuencia}")
    }

    fun reproducirSecuencia() {
        viewModelScope.launch {
            estadoActual.value = Estados.GenerarSecuencia

            Log.d(TAG_LOG, "Reproduciendo secuencia - Estado: ${Estados.GenerarSecuencia}")
            for (color in secuencia.value) {
                Log.d(TAG_LOG, "El color es: $color")
                delay(1000)
            }
            Log.d(TAG_LOG, "Fin de la secuencia - Estado: ${Estados.IntroducirSecuencia}")
            estadoActual.value = Estados.IntroducirSecuencia
        }
    }

    fun introducirSecuencia(corlor: Int) {
        if (estadoActual.value != Estados.IntroducirSecuencia) {
            Log.d(TAG_LOG, "Aún no es tu turno, ESPERA!: ${estadoActual.value}")
            return
        }
        Log.d(TAG_LOG, "Jugador: ${corlor}")

        secuenciaJugador.value.add(corlor)

        val index = secuenciaJugador.value.lastIndex

        if (secuenciaJugador.value[index] != secuencia.value[index]) {
            gameOver()
            return
        }

        if (secuenciaJugador.value.size == secuencia.value.size) {
            ronda.value += 1
            secuenciaJugador.value.clear()

            generarColor()
            reproducirSecuencia()
        }
    }

    private fun gameOver() {
        estadoActual.value = Estados.GameOver
        Log.d(TAG_LOG, "GAME OVER - Estado: ${Estados.GameOver}")
    }

}


