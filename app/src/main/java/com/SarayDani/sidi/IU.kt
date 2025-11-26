package com.SarayDani.sidi

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IU(vm: MyViewModel) {

    // Observamos los estados del ViewModel
    val estado by vm.estadoActual.collectAsState()
    val ronda by vm.ronda.collectAsState()
    val record by vm.record.collectAsState()
    val botonEncendido by vm.botonEncendido.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4C007A))  // Fondo principal morado
            .statusBarsPadding()            // Evita solaparse con barra superior
            .navigationBarsPadding()        // Evita solaparse con barra inferior
    ) {

        // Muestra un mensaje animado según el estado del juego
        TopNotification(estado = estado)

        // Barra superior con récord y ronda actual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Récord: $record", color = Color.White, fontSize = 20.sp)
                Text("Ronda: $ronda", color = Color.White, fontSize = 20.sp)
            }
        }

        // Zona principal: botonera del juego + botón Start
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {

            // Panel con los 4 botones de colores
            Botonera(
                modifier = Modifier.fillMaxSize(),
                botonEncendido = botonEncendido,     // Indica qué botón debe "encenderse"
                onColorClick = { vm.introducirSecuencia(it) }
            )

            // Botón de inicio solo visible cuando estamos en el estado Inicio
            if (estado == Estados.Inicio) {
                Button(
                    onClick = { vm.empezarJuego() },
                    modifier = Modifier.size(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4C007A)
                    ),
                    shape = CircleShape
                ) {
                    Text("▶", fontSize = 40.sp, color = Color.White)
                }
            }
        }
    }

    // Diálogo de Game Over si el jugador pierde
    if (estado == Estados.GameOver) {
        GameOverDialog(
            rondaActual = ronda,
            record = record,
            onColorClick = { /* botones inactivos en esta pantalla */ },
            onPlayAgain = { vm.empezarJuego() },
            onClose = { vm.resetToInicio() }
        )
    }
}


/** Diálogo que aparece al perder la partida. */
@Composable
fun GameOverDialog(
    rondaActual: Int,
    record: Int,
    onColorClick: (Int) -> Unit,
    onPlayAgain: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,  // Se cierra tocando fuera
        containerColor = Color(0xFF4C007A),

        title = {
            Text(
                text = "¡Has perdido!",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFFFDFBF6)
            )
        },

        text = {
            Column {
                Text(
                    "Llegaste a la ronda: $rondaActual",
                    fontSize = 18.sp,
                    color = Color(0xFFFDFBF6)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tu récord es: $record",
                    fontSize = 18.sp,
                    color = Color(0xFFFDFBF6)
                )
            }
        },

        confirmButton = {
            Button(onClick = onPlayAgain) { Text("Jugar de Nuevo") }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text("Cerrar") }
        }
    )
}


/** Mensaje superior que aparece con animación según el estado del juego. */
@Composable
fun TopNotification(estado: Estados) {
    var visible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Color del fondo según el estado del juego
    val bgColor = when (estado) {
        Estados.GenerarSecuencia -> Color(0xFF2E7D32)
        Estados.IntroducirSecuencia -> Color(0xFF1565C0)
        Estados.GameOver -> Color(0xFFB00020)
        Estados.Inicio -> Color(0xFF6A1B9A)
        Estados.Pausa -> Color(0xFF37474F)
    }

    // Cuando el estado cambia, actualizamos el mensaje
    LaunchedEffect(estado) {
        message = when (estado) {
            Estados.GenerarSecuencia -> "Reproduciendo secuencia..."
            Estados.IntroducirSecuencia -> "Tu turno: introduce la secuencia"
            Estados.GameOver -> "¡Game Over!"
            Estados.Inicio -> "Pulsa START para comenzar"
            Estados.Pausa -> "Pausa"
        }
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(350)
        ) + fadeIn(tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(message, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}


/** Cuadrícula de 4 botones tipo "Simon". */
@Composable
fun Botonera(
    modifier: Modifier = Modifier,
    botonEncendido: Int?,
    onColorClick: (Int) -> Unit
) {
    Column(modifier = modifier) {

        // Fila 1: rojo y amarillo
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(Modifier.weight(1f).padding(2.dp)) {
                Boton(Colores.CLASE_ROJO, 0, botonEncendido, onColorClick)
            }
            Box(Modifier.weight(1f).padding(2.dp)) {
                Boton(Colores.CLASE_AMARILLO, 3, botonEncendido, onColorClick)
            }
        }

        // Fila 2: verde y azul
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(Modifier.weight(1f).padding(2.dp)) {
                Boton(Colores.CLASE_VERDE, 1, botonEncendido, onColorClick)
            }
            Box(Modifier.weight(1f).padding(2.dp)) {
                Boton(Colores.CLASE_AZUL, 2, botonEncendido, onColorClick)
            }
        }
    }
}


/** Botón individual: muestra el color normal o el "encendido". */
@Composable
fun Boton(color: Colores, index: Int, botonEncendido: Int?, onColorClick: (Int) -> Unit) {

    // Determina si este botón debe verse brillante
    val isOn = botonEncendido == index

    val displayColor = if (isOn) {
        // Seleccionamos la versión "luz" del color
        when (index) {
            0 -> Colores_luz.CLASE_ROJO_LUZ.color
            1 -> Colores_luz.CLASE_VERDE_LUZ.color
            2 -> Colores_luz.CLASE_AZUL_LUZ.color
            3 -> Colores_luz.CLASE_AMARILLO_LUZ.color
            else -> color.color
        }
    } else color.color

    Button(
        onClick = {
            Log.d("IU", "Click en ${color.txt}")
            onColorClick(index)     // Avisamos al ViewModel del botón pulsado
        },
        modifier = Modifier.fillMaxSize(),
        colors = ButtonDefaults.buttonColors(containerColor = displayColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        // Si está encendido, el texto se vuelve blanco para contraste
        Text(color.txt, fontSize = 10.sp, color = if (isOn) Color.White else Color.Black)
    }
}
