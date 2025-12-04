package com.SarayDani.sidi

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.text.compareTo

class MyViewModel(application: Application) : AndroidViewModel(application) {

    // Tag para los logs
    private val TAG_LOG = "miDebug"

    val estadoActual =MutableStateFlow(Estados.Inicio)
    val secuencia =MutableStateFlow(mutableListOf<Int>())
    val botonEncendido =MutableStateFlow<Int?>(null)
    val secuenciaJugador =MutableStateFlow(mutableListOf<Int>())
    val ronda =MutableStateFlow(0)
    val recordp =MutableStateFlow(0)
    val record: StateFlow<Int> get() = recordp

    // Duraciones para animaciones de luz
    private val duracionEncendido=500L
    private val duracionIntermitencia= 250L
    private val duracionInicial= 900L

    init {
        recordp.value = Controller.obtenerRecord(getApplication()).record
    }

    /**
     * Inicia el juego: reinicia valores, crea el primer color y muestra la secuencia.
     */
    fun empezarJuego() {
        ronda.value=1
        secuencia.value.clear()
        secuenciaJugador.value.clear()
        generarColor()
        reproducirSecuencia()
    }

    /**
     * Añade un nuevo color aleatorio a la secuencia.
     */
    private fun generarColor() {
        val numero = (0..3).random()
        secuencia.value.add(numero)
        estadoActual.value =Estados.GenerarSecuencia
        Log.d(TAG_LOG, "Generando secuencia")
    }

    /**
     * Reproduce la secuencia: ilumina cada botón por turnos.
     */
    fun reproducirSecuencia() {
        viewModelScope.launch {
            estadoActual.value =Estados.GenerarSecuencia
            botonEncendido.value= null
            delay(duracionInicial)

            // Recorre la secuencia mostrando uno a uno
            for (color in secuencia.value) {
                botonEncendido.value =color
                delay(duracionEncendido)
                botonEncendido.value=null
                delay(duracionIntermitencia)
            }

            estadoActual.value=Estados.IntroducirSecuencia
        }
    }

    /**
     * Registra un color que el jugador ha pulsado y lo valida.
     */
    fun introducirSecuencia(color:Int) {
        // Si no es el turno del jugador, se ignora
        if (estadoActual.value!=Estados.IntroducirSecuencia) {
            Log.d(TAG_LOG, "Aún no es tu turno")
            return
        }

        viewModelScope.launch {

            // Pequeño feedback luminoso al pulsar
            botonEncendido.value=color
            delay(200L)
            botonEncendido.value=null

            // Registrar la elección
            secuenciaJugador.value.add(color)
            val index=secuenciaJugador.value.lastIndex

            // Validar: si falla, fin del juego
            if (secuenciaJugador.value[index]!=secuencia.value[index]) {
                gameOver()
                return@launch
            }

            // Si completa toda la secuencia correcta, avanzar ronda
            if (secuenciaJugador.value.size==secuencia.value.size) {
                ronda.value+=1
                secuenciaJugador.value.clear()
                generarColor()
                reproducirSecuencia()
            }
        }
    }

    /**
     * Termina el juego y actualiza récord si corresponde.
     */
    private fun gameOver() {
        estadoActual.value=Estados.GameOver
        if (ronda.value > recordp.value) {
            recordp.value = ronda.value
            Controller.actualizarRecord(recordp.value, Date(), getApplication())
        }
        botonEncendido.value=null // apagar luces por si acaso
        Log.d(TAG_LOG, "GAME OVER. Ronda alcanzada: ${ronda.value}")
    }

    /**
     * Vuelve al estado inicial sin empezar una nueva partida.
     */
    fun resetToInicio() {
        estadoActual.value = Estados.Inicio
        ronda.value = 0
        secuencia.value.clear()
        secuenciaJugador.value.clear()
        botonEncendido.value = null
    }

    fun comprobarRecord() {
        if (ronda.value > Controller.obtenerRecord(getApplication()).record) {
            val fechaActual = java.util.Date() // objeto Date que espera la función
            val fechaFormateada = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            Log.d(TAG_LOG, "Nuevo récord en: $fechaFormateada")
            Controller.actualizarRecord(ronda.value, fechaActual, getApplication())
        }
    }

}

