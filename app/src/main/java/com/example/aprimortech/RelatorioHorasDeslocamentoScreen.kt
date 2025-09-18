package com.example.aprimortech

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioHorasDeslocamentoScreen(navController: NavController, modifier: Modifier = Modifier) {
    // Estados - Horas
    var valorHora by remember { mutableStateOf("") }
    var inicioField by remember { mutableStateOf(TextFieldValue("")) }
    var fimField by remember { mutableStateOf(TextFieldValue("")) }
    var totalHoras by remember { mutableStateOf("00:00") }
    var totalHorasValor by remember { mutableStateOf(0.0) }

    // Estados - Deslocamento
    var valorDeslocamento by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }
    var pedagio by remember { mutableStateOf("") }
    var totalDeslocamento by remember { mutableStateOf(0.0) }

    // Cálculo automático de horas
    LaunchedEffect(inicioField.text, fimField.text, valorHora) {
        val totalMin = calcularMinutos(inicioField.text, fimField.text)
        totalHoras = formatMinutosAsHora(totalMin)
        val valorH = valorHora.replace(",", ".").toDoubleOrNull() ?: 0.0
        totalHorasValor = (totalMin / 60.0) * valorH
    }

    // Cálculo automático de deslocamento
    LaunchedEffect(valorDeslocamento, km, pedagio) {
        val v = valorDeslocamento.replace(",", ".").toDoubleOrNull() ?: 0.0
        val k = km.toDoubleOrNull() ?: 0.0
        val p = pedagio.replace(",", ".").toDoubleOrNull() ?: 0.0
        totalDeslocamento = (v * k) + p
    }

    // Total do serviço
    val totalServico = totalHorasValor + totalDeslocamento

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logo_aprimortech),
                        contentDescription = "Logo Aprimortech",
                        modifier = Modifier.height(40.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Horas e Deslocamento",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )

            // --- Horas ---
            Text("Horas", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1A4A5C))

            FormCard {
                OutlinedTextField(
                    value = valorHora,
                    onValueChange = { valorHora = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Valor Hora Técnica (R$)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = borderlessColors()
                )
            }

            FormCard {
                OutlinedTextField(
                    value = inicioField,
                    onValueChange = { newValue -> inicioField = formatHoraValue(newValue) },
                    label = { Text("Início (HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = borderlessColors()
                )
            }

            FormCard {
                OutlinedTextField(
                    value = fimField,
                    onValueChange = { newValue -> fimField = formatHoraValue(newValue) },
                    label = { Text("Fim (HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = borderlessColors()
                )
            }

            FormCard {
                OutlinedTextField(
                    value = totalHoras,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Total de Horas") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = borderlessColors()
                )
            }

            FormCard {
                OutlinedTextField(
                    value = String.format("%.2f", totalHorasValor),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Total R$ (Horas)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = borderlessColors()
                )
            }

            Divider()

            // --- Deslocamento ---
            Text("Deslocamento", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1A4A5C))

            FormCard {
                OutlinedTextField(
                    value = valorDeslocamento,
                    onValueChange = { valorDeslocamento = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Valor Deslocamento (R$/KM)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = borderlessColors()
                )
            }

            FormCard {
                OutlinedTextField(
                    value = km,
                    onValueChange = { km = it.filter { c -> c.isDigit() } },
                    label = { Text("Quantidade de KM") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = borderlessColors()
                )
            }

            FormCard {
                OutlinedTextField(
                    value = pedagio,
                    onValueChange = { pedagio = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Valor Pedágios (R$)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = borderlessColors()
                )
            }

            FormCard {
                OutlinedTextField(
                    value = String.format("%.2f", totalDeslocamento),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Total R$ (Deslocamento)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = borderlessColors()
                )
            }

            Divider()

            Text(
                text = "Total do Serviço: R$ %.2f".format(totalServico),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1A4A5C)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A4A5C)
                    ),
                    border = BorderStroke(0.dp, Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text("Anterior")
                }

                Button(
                    onClick = { navController.navigate("relatorioEtapa6") },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A4A5C),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text("Próximo")
                }
            }
        }
    }
}

/** Formata entrada como HH:mm */
fun formatHoraValue(input: TextFieldValue): TextFieldValue {
    val digits = input.text.filter { it.isDigit() }.take(4)
    val builder = StringBuilder()
    for (i in digits.indices) {
        builder.append(digits[i])
        if (i == 1 && digits.length > 2) builder.append(':')
    }
    val formatted = builder.toString()
    return TextFieldValue(
        text = formatted,
        selection = TextRange(formatted.length)
    )
}

/** Calcula diferença em minutos entre HH:mm */
fun calcularMinutos(inicio: String, fim: String): Int {
    try {
        if (inicio.length == 5 && fim.length == 5) {
            val (h1, m1) = inicio.split(":").map { it.toIntOrNull() ?: 0 }
            val (h2, m2) = fim.split(":").map { it.toIntOrNull() ?: 0 }
            val totalMinInicio = h1 * 60 + m1
            val totalMinFim = h2 * 60 + m2
            return (totalMinFim - totalMinInicio).coerceAtLeast(0)
        }
    } catch (_: Exception) { }
    return 0
}

/** Converte minutos em HH:mm */
fun formatMinutosAsHora(totalMin: Int): String {
    val horas = totalMin / 60
    val minutos = totalMin % 60
    return String.format("%02d:%02d", horas, minutos)
}

@Composable
private fun FormCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(2.dp)) {
            content()
        }
    }
}

@Composable
private fun borderlessColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    errorBorderColor = Color.Transparent
)

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioHorasDeslocamentoPreview() {
    AprimortechTheme {
        RelatorioHorasDeslocamentoScreen(navController = rememberNavController())
    }
}
