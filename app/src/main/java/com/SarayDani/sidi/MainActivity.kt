// kotlin
package com.SarayDani.sidi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val TAG = "JuegoLogs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciar el simulador de juego que funciona solo con logs
        val simulator = GameSimulator()
        simulator.start()
    }
}

/**
 * Simula un juego únicamente a partir de logs:
 * - Alterna entre INACTIVE/ACTIVE
 * - Simula pulsaciones de 4 botones (rojo, amarillo, verde, azul)
 * - Incrementa el score mientras está ACTIVE
 */
class GameSimulator {
    private val tag = "Juego"
    private var score = 0
    private var state = GameState.INACTIVE
    private val botones = listOf("ROJO", "AMARILLO", "VERDE", "AZUL")

    fun start() {
        // Usar el lifecycleScope del Activity no es posible desde aquí sin referencia;
        // lanzamos una coroutine global corta para la simulación.
        // En la app real se recomienda usar lifecycleScope del Activity.
        kotlinx.coroutines.GlobalScope.launch {
            log("Simulador iniciado")
            repeat(3) { ronda ->
                toggleState() // activa
                log("Ronda ${ronda + 1} - estado: $state")
                // Mientras está activo, simular eventos
                val pasos = 8
                repeat(pasos) { paso ->
                    if (state == GameState.ACTIVE) {
                        delay(400L)
                        score += Random.nextInt(1, 4) // sumar puntos aleatorios
                        val boton = botones.random()
                        log("Paso ${paso + 1}: Click en $boton -> score = $score")
                    } else {
                        delay(200L)
                    }
                }
                toggleState() // pausa
                log("Ronda ${ronda + 1} finalizada - estado: $state - score = $score")
                delay(700L)
            }
            log("Simulación terminada. Score final = $score")
        }
    }

    private fun toggleState() {
        state = if (state == GameState.INACTIVE) GameState.ACTIVE else GameState.INACTIVE
        log("Estado cambiado a $state")
    }

    private fun log(message: String) {
        Log.d(tag, message)
    }
}
