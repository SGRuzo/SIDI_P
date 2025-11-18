package com.SarayDani.sidi

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IU(vm: MyViewModel) {

    val estado by vm.estadoActual.collectAsState()
    val ronda by vm.ronda.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF360157)) // Fondo morado ORIGINAL
    ) {

        // ------------------------------
        // BARRA SUPERIOR: RONDA (antes Score)
        // ------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Ronda: $ronda",
                color = Color.White,
                fontSize = 20.sp
            )
        }

        // ------------------------------
        // ÁREA PRINCIPAL (Botonera + Start en medio)
        // ------------------------------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {

            // 4 botones originales
            Botonera(
                modifier = Modifier.fillMaxSize(),
                onColorClick = { vm.introducirSecuencia(it) }
            )

            // Botón START pequeño y centrado COMO EL ORIGINAL
            if (estado == Estados.Inicio || estado == Estados.GameOver) {
                Button(
                    onClick = { vm.empezarJuego() },
                    modifier = Modifier.size(80.dp),  // Tamaño original
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF360157) // El mismo color que el original
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = "▶",
                        fontSize = 40.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * BOTONERA EXACTA A LA ORIGINAL
 */
@Composable
fun Botonera(
    modifier: Modifier = Modifier,
    onColorClick: (Int) -> Unit
) {
    Column(modifier = modifier) {

        // Fila superior: ROJO - AMARILLO
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                Boton(Colores.CLASE_ROJO, 0, onColorClick)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                Boton(Colores.CLASE_AMARILLO, 3, onColorClick)
            }
        }

        // Fila inferior: VERDE - AZUL
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                Boton(Colores.CLASE_VERDE, 1, onColorClick)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                Boton(Colores.CLASE_AZUL, 2, onColorClick)
            }
        }
    }
}

/**
 * BOTÓN DE COLOR EXACTO AL ORIGINAL
 */
@Composable
fun Boton(color: Colores, index: Int, onColorClick: (Int) -> Unit) {
    Button(
        onClick = {
            Log.d("IU", "Click en ${color.txt}")
            onColorClick(index)
        },
        modifier = Modifier.fillMaxSize(),
        colors = ButtonDefaults.buttonColors(color.color),
        shape = RectangleShape  // Igual al original
    ) {
        Text(color.txt, fontSize = 10.sp, color = Color.Black)
    }
}
