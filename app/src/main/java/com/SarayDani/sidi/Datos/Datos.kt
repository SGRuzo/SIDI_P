package com.SarayDani.sidi.Datos

import androidx.compose.ui.graphics.Color

data class Datos (
    var ronda: Int = 0,                       // Número de ronda actual
    var secuencia: MutableList<Int> = mutableListOf(),  // Secuencia generada por el juego
    var secuenciaIntroducida: MutableList<Int> = mutableListOf(), // Secuencia del jugador
    var record: Int = 0,                      // Máximo número de rondas superadas
    var estados: Estados = Estados.Inicio,    // Estado actual del juego
)

enum class Colores_luz(val color: Color, val txt: String) {
    CLASE_ROJO_LUZ(color = Color(0xFFB90000), txt = "roxo"),
    CLASE_VERDE_LUZ(color = Color(0xFF0EB40E), txt = "verde"),
    CLASE_AZUL_LUZ(color = Color(0xFF168BE7), txt = "azul"),
    CLASE_AMARILLO_LUZ(color = Color(0xFFFFC11B), txt = "melo"),
    CLASE_START(color = Color.Magenta, txt = "Start")
}

enum class Colores(val color: Color, val txt: String) {
    CLASE_ROJO(color = Color(0xFF5D0000), txt = "roxo"),
    CLASE_VERDE(color = Color(0xFF003600), txt = "verde"),
    CLASE_AZUL(color = Color(0xFF003259), txt = "azul"),
    CLASE_AMARILLO(color = Color(0xFF574000), txt = "melo"),
    CLASE_START(color = Color.Magenta, txt = "Start")
}

enum class Estados {
    Inicio,
    GenerarSecuencia,
    IntroducirSecuencia,
    GameOver,
    Pausa
}