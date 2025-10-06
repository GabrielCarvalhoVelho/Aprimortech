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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioHorasDeslocamentoScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    defeitos: String = "",
    servicos: String = "",
    observacoes: String = "",
    pecas: String = ""
) {
    val context = LocalContext.current

    // Estados para os campos
    var horarioEntrada by remember { mutableStateOf("") }
    var horarioSaida by remember { mutableStateOf("") }
    var distanciaKm by remember { mutableStateOf("") }
    var valorPorKm by remember { mutableStateOf("") }
    var valorPedagios by remember { mutableStateOf("") }

    // Função para formatar horário
    fun formatarHorario(input: String): String {
        // Remove todos os caracteres não numéricos
        val digitos = input.filter { it.isDigit() }

        // Limita a 4 dígitos
        val digitosLimitados = digitos.take(4)

        return when (digitosLimitados.length) {
            0 -> ""
            1 -> "0${digitosLimitados[0]}:"
            2 -> "${digitosLimitados}:"
            3 -> "${digitosLimitados.substring(0, 2)}:${digitosLimitados[2]}"
            4 -> "${digitosLimitados.substring(0, 2)}:${digitosLimitados.substring(2, 4)}"
            else -> "${digitosLimitados.substring(0, 2)}:${digitosLimitados.substring(2, 4)}"
        }
    }

    // Decodificar observações
    val observacoesDecodificadas = remember {
        try {
            java.net.URLDecoder.decode(observacoes, "UTF-8")
        } catch (e: Exception) {
            observacoes
        }
    }

    // Cálculo do valor total de deslocamento
    val valorDeslocamentoTotal by remember {
        derivedStateOf {
            val valorKm = valorPorKm.replace(",", ".").toDoubleOrNull() ?: 0.0
            val distancia = distanciaKm.replace(",", ".").toDoubleOrNull() ?: 0.0
            val pedagios = valorPedagios.replace(",", ".").toDoubleOrNull() ?: 0.0
            (valorKm * distancia) + pedagios
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Horas e Deslocamento",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )

            // Seção de Horários
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Horários de Atendimento",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1A4A5C)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = horarioEntrada,
                        onValueChange = { newValue ->
                            horarioEntrada = formatarHorario(newValue)
                        },
                        label = { Text("Horário de Entrada") },
                        placeholder = { Text("HH:mm") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = horarioSaida,
                        onValueChange = { newValue ->
                            horarioSaida = formatarHorario(newValue)
                        },
                        label = { Text("Horário de Saída") },
                        placeholder = { Text("HH:mm") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true
                    )
                }
            }

            // Seção de Deslocamento
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Deslocamento",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1A4A5C)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = distanciaKm,
                        onValueChange = { distanciaKm = it },
                        label = { Text("Distância (KM)") },
                        placeholder = { Text("Ex: 50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = valorPorKm,
                        onValueChange = { valorPorKm = it },
                        label = { Text("Valor por KM (R$)") },
                        placeholder = { Text("Ex: 1,50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = valorPedagios,
                        onValueChange = { valorPedagios = it },
                        label = { Text("Valor dos Pedágios (R$)") },
                        placeholder = { Text("Ex: 15,00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    // Card do Valor Total
                    if (valorDeslocamentoTotal > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Valor Total do Deslocamento",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "R$ %.2f".format(valorDeslocamentoTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botões de navegação
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
                    onClick = {
                        // Passar dados para próxima etapa incluindo horas e deslocamento
                        val defeitosString = defeitos
                        val servicosString = servicos
                        val observacoesEncoded = java.net.URLEncoder.encode(observacoesDecodificadas, "UTF-8")
                        val pecasString = pecas
                        val horasData = "${horarioEntrada};${horarioSaida};${distanciaKm};${valorPorKm};${valorPedagios};${valorDeslocamentoTotal}"

                        navController.navigate("relatorioEtapa6?defeitos=$defeitosString&servicos=$servicosString&observacoes=$observacoesEncoded&pecas=$pecasString&horas=$horasData")
                    },
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

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioHorasDeslocamentoScreenPreview() {
    AprimortechTheme {
        RelatorioHorasDeslocamentoScreen(navController = rememberNavController())
    }
}
