package com.example.aprimortech

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme

data class SignatureState(
    val paths: MutableList<MutableList<Offset>> = mutableListOf(mutableListOf())
)

@Composable
fun RelatorioAssinaturaScreen(navController: NavController, modifier: Modifier = Modifier) {
    var clienteSignature by remember { mutableStateOf(SignatureState()) }
    var tecnicoSignature by remember { mutableStateOf(SignatureState()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Text(
            text = "Assinaturas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Assinatura do Cliente
        Text("Assinatura do Cliente", style = MaterialTheme.typography.titleMedium)
        SignaturePad(
            state = clienteSignature,
            onClear = { clienteSignature = SignatureState() }
        )

        // Assinatura do Técnico
        Text("Assinatura do Técnico", style = MaterialTheme.typography.titleMedium)
        SignaturePad(
            state = tecnicoSignature,
            onClear = { tecnicoSignature = SignatureState() }
        )

        Spacer(Modifier.height(24.dp))

        // Botões de navegação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { navController.popBackStack() }) {
                Text("Anterior")
            }
            Button(onClick = {
                // TODO: exportar assinaturas e salvar no Firebase Storage
                // Depois salvar URLs no Firestore dentro do relatório
            }) {
                Text("Finalizar")
            }
        }
    }
}

@Composable
fun SignaturePad(state: SignatureState, onClear: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.White)
            .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small) // borda fina cinza
    ) {
        var currentPath by remember { mutableStateOf(mutableListOf<Offset>()) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = mutableListOf(offset)
                            state.paths.add(currentPath)
                        },
                        onDrag = { change, _ ->
                            currentPath.add(change.position)
                        }
                    )
                }
        ) {
            state.paths.forEach { pathPoints ->
                val path = Path()
                if (pathPoints.isNotEmpty()) {
                    path.moveTo(pathPoints[0].x, pathPoints[0].y)
                    for (i in 1 until pathPoints.size) {
                        path.lineTo(pathPoints[i].x, pathPoints[i].y)
                    }
                    drawPath(path, Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                }
            }
        }

        OutlinedButton(
            onClick = onClear,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Text("Limpar")
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioAssinaturaPreview() {
    AprimortechTheme {
        RelatorioAssinaturaScreen(navController = rememberNavController())
    }
}
