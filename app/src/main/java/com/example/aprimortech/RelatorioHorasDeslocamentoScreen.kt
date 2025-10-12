package com.example.aprimortech

import android.app.TimePickerDialog
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.RelatorioSharedViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class Cliente(
    val id: String = "",
    val nome: String = "",
    val endereco: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioHorasDeslocamentoScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    defeitos: String = "",
    servicos: String = "",
    observacoes: String = "",
    pecas: String = "",
    clienteId: String = "",
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    // Estados para os campos
    var horarioEntrada by remember { mutableStateOf("") }
    var horarioSaida by remember { mutableStateOf("") }
    var distanciaKm by remember { mutableStateOf("") }
    var valorPorKm by remember { mutableStateOf("") }
    var valorPedagios by remember { mutableStateOf("") }
    var valorHoraTecnica by remember { mutableStateOf("") }
    var isCalculatingDistance by remember { mutableStateOf(false) }
    var cliente by remember { mutableStateOf<Cliente?>(null) }

    // Coordenadas da empresa Aprimortech
    val empresaLatitude = -23.4994 // Rua Plínio Pasqui, 186, Vila Dom Pedro II, São Paulo - SP
    val empresaLongitude = -46.6107

    // Função para buscar dados do cliente
    suspend fun buscarCliente(clienteId: String): Cliente? {
        return try {
            val documento = firestore.collection("clientes").document(clienteId).get().await()
            if (documento.exists()) {
                Cliente(
                    id = documento.id,
                    nome = documento.getString("nome") ?: "",
                    endereco = documento.getString("endereco") ?: "",
                    latitude = documento.getDouble("latitude") ?: 0.0,
                    longitude = documento.getDouble("longitude") ?: 0.0
                )
            } else null
        } catch (e: Exception) {
            Log.e("RelatorioHoras", "Erro ao buscar cliente: ${e.message}")
            null
        }
    }

    // Função para calcular distância usando Google Maps Distance Matrix API
    suspend fun calcularDistancia(origemLat: Double, origemLng: Double, destinoLat: Double, destinoLng: Double): Double? {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = "AIzaSyAszSnNMNUoDRrhV3hyDg2l96g75cB5V6s" // Você precisa adicionar sua chave da API
                val url = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                        "origins=$origemLat,$origemLng" +
                        "&destinations=$destinoLat,$destinoLng" +
                        "&units=metric" +
                        "&key=$apiKey"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonObject = JSONObject(response)

                    val rows = jsonObject.getJSONArray("rows")
                    if (rows.length() > 0) {
                        val elements = rows.getJSONObject(0).getJSONArray("elements")
                        if (elements.length() > 0) {
                            val element = elements.getJSONObject(0)
                            if (element.getString("status") == "OK") {
                                val distance = element.getJSONObject("distance")
                                val distanceInMeters = distance.getInt("value")
                                return@withContext distanceInMeters / 1000.0 // Converter para km
                            }
                        }
                    }
                }
                null
            } catch (e: Exception) {
                Log.e("RelatorioHoras", "Erro ao calcular distância: ${e.message}")
                null
            }
        }
    }

    // Carregar dados do cliente e calcular distância quando a tela carrega
    LaunchedEffect(clienteId) {
        if (clienteId.isNotEmpty()) {
            isCalculatingDistance = true

            // Buscar dados do cliente
            val clienteData = buscarCliente(clienteId)
            cliente = clienteData

            clienteData?.let {
                if (it.latitude != 0.0 && it.longitude != 0.0) {
                    // Calcular distância
                    val distancia = calcularDistancia(
                        empresaLatitude, empresaLongitude,
                        it.latitude, it.longitude
                    )

                    distancia?.let { dist ->
                        distanciaKm = String.format(Locale.getDefault(), "%.1f", dist)
                    } ?: run {
                        Toast.makeText(context, "Erro ao calcular distância", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Cliente não possui coordenadas cadastradas", Toast.LENGTH_SHORT).show()
                }
            }

            isCalculatingDistance = false
        }
    }

    // Função para mostrar o TimePickerDialog simples
    fun mostrarSeletorHorario(
        horarioAtual: String,
        onHorarioSelecionado: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()

        // Se já existe um horário, usar como base
        if (horarioAtual.isNotEmpty() && horarioAtual.contains(":")) {
            try {
                val partes = horarioAtual.split(":")
                val hora = partes[0].toInt()
                val minuto = partes[1].toInt()
                calendar.set(Calendar.HOUR_OF_DAY, hora)
                calendar.set(Calendar.MINUTE, minuto)
            } catch (_: Exception) {
                // Se der erro, usar horário atual
            }
        }

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val horarioFormatado = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                onHorarioSelecionado(horarioFormatado)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    // Decodificar observações
    val observacoesDecodificadas = remember {
        try {
            java.net.URLDecoder.decode(observacoes, "UTF-8")
        } catch (_: Exception) {
            observacoes
        }
    }

    // Cálculo do valor total de deslocamento
    val valorDeslocamentoTotal by remember {
        derivedStateOf {
            val valorKmEmCentavos = valorPorKm.toLongOrNull() ?: 0L
            val valorKmEmReais = valorKmEmCentavos.toDouble() / 100

            val valorPedagiosEmCentavos = valorPedagios.toLongOrNull() ?: 0L
            val valorPedagiosEmReais = valorPedagiosEmCentavos.toDouble() / 100

            val distancia = distanciaKm.replace(",", ".").toDoubleOrNull() ?: 0.0

            (valorKmEmReais * distancia) + valorPedagiosEmReais
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

                    // Campo Horário de Entrada com seletor
                    OutlinedTextField(
                        value = horarioEntrada,
                        onValueChange = { },
                        label = { Text("Horário de Entrada") },
                        placeholder = { Text("Toque para selecionar") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    mostrarSeletorHorario(horarioEntrada) { novoHorario ->
                                        horarioEntrada = novoHorario
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = "Selecionar horário",
                                    tint = Color(0xFF1A4A5C)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF1A4A5C),
                            unfocusedBorderColor = Color.LightGray,
                            disabledContainerColor = Color.White,
                            disabledBorderColor = Color.LightGray,
                            disabledTextColor = Color.Black
                        ),
                        interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect { interaction ->
                                    if (interaction is PressInteraction.Press) {
                                        mostrarSeletorHorario(horarioEntrada) { novoHorario ->
                                            horarioEntrada = novoHorario
                                        }
                                    }
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo Horário de Saída com seletor
                    OutlinedTextField(
                        value = horarioSaida,
                        onValueChange = { },
                        label = { Text("Horário de Saída") },
                        placeholder = { Text("Toque para selecionar") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    mostrarSeletorHorario(horarioSaida) { novoHorario ->
                                        horarioSaida = novoHorario
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = "Selecionar horário",
                                    tint = Color(0xFF1A4A5C)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF1A4A5C),
                            unfocusedBorderColor = Color.LightGray,
                            disabledContainerColor = Color.White,
                            disabledBorderColor = Color.LightGray,
                            disabledTextColor = Color.Black
                        ),
                        interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect { interaction ->
                                    if (interaction is PressInteraction.Press) {
                                        mostrarSeletorHorario(horarioSaida) { novoHorario ->
                                            horarioSaida = novoHorario
                                        }
                                    }
                                }
                            }
                        }
                    )

                    // Mostrar duração do atendimento se ambos os horários estiverem preenchidos
                    if (horarioEntrada.isNotEmpty() && horarioSaida.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Duração do Atendimento",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF1976D2)
                                )
                                val duracao = calcularDuracao(horarioEntrada, horarioSaida)
                                Text(
                                    text = duracao,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF0D47A1)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo Valor Hora Técnica com formatação monetária brasileira
                    CampoValorMonetario(
                        valor = valorHoraTecnica,
                        onValorChange = { valorHoraTecnica = it },
                        label = "Valor Hora Técnica",
                        placeholder = "Digite apenas números: 5000 = R$ 50,00"
                    )

                    // Card do Valor Total da Hora Técnica
                    if (horarioEntrada.isNotEmpty() && horarioSaida.isNotEmpty() && valorHoraTecnica.isNotEmpty()) {
                        val duracaoEmMinutos = calcularDuracaoEmMinutos(horarioEntrada, horarioSaida)
                        val duracaoEmHoras = duracaoEmMinutos / 60.0
                        val valorHoraTecnicaEmCentavos = valorHoraTecnica.toLongOrNull() ?: 0L
                        val valorHoraTecnicaEmReais = valorHoraTecnicaEmCentavos.toDouble() / 100
                        val valorTotalHoraTecnica = duracaoEmHoras * valorHoraTecnicaEmReais

                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Valor Total da Hora Técnica",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "%.1fh × R$ %.2f = R$ %.2f".format(
                                        duracaoEmHoras,
                                        valorHoraTecnicaEmReais,
                                        valorTotalHoraTecnica
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }
                    }
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

                    // Campo de Distância - Calculado automaticamente
                    OutlinedTextField(
                        value = if (isCalculatingDistance) "Calculando..." else "${distanciaKm} km",
                        onValueChange = { },
                        label = { Text("Distância") },
                        placeholder = { Text("Calculado automaticamente") },
                        readOnly = true,
                        leadingIcon = {
                            if (isCalculatingDistance) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF1A4A5C)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                                    contentDescription = "Localização",
                                    tint = Color(0xFF1A4A5C)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F8FF),
                            unfocusedContainerColor = Color(0xFFF0F8FF),
                            focusedBorderColor = Color(0xFF1A4A5C),
                            unfocusedBorderColor = Color.LightGray,
                            disabledContainerColor = Color(0xFFF0F8FF),
                            disabledBorderColor = Color.LightGray,
                            disabledTextColor = Color.Black
                        )
                    )

                    // Mostrar informações do cliente se disponível
                    cliente?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cliente: ${it.nome}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "Endereço: ${it.endereco}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo Valor por KM com formatação monetária brasileira
                    CampoValorMonetario(
                        valor = valorPorKm,
                        onValorChange = { valorPorKm = it },
                        label = "Valor por KM",
                        placeholder = "Digite apenas números: 150 = R$ 1,50"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo Valor dos Pedágios com formatação monetária brasileira
                    CampoValorMonetario(
                        valor = valorPedagios,
                        onValorChange = { valorPedagios = it },
                        label = "Valor dos Pedágios",
                        placeholder = "Digite apenas números: 1500 = R$ 15,00"
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

            // Botão de navegação
            Button(
                onClick = {
                    // Validar se os horários foram preenchidos
                    if (horarioEntrada.isEmpty() || horarioSaida.isEmpty()) {
                        Toast.makeText(context, "Por favor, preencha os horários de entrada e saída", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Calcular total de horas trabalhadas
                    val totalHoras = calcularTotalHoras(horarioEntrada, horarioSaida)
                    val valorHoraTec = valorHoraTecnica.toLongOrNull()?.toDouble()?.div(100.0) ?: 150.0

                    // Salvar dados de horas e deslocamento no ViewModel compartilhado
                    sharedViewModel.setHorasDeslocamento(
                        horarioEntrada = horarioEntrada,
                        horarioSaida = horarioSaida,
                        valorHoraTecnica = valorHoraTec,
                        totalHoras = totalHoras,
                        quantidadeKm = distanciaKm.toDoubleOrNull() ?: 0.0,
                        valorPorKm = valorPorKm.toLongOrNull()?.toDouble()?.div(100.0) ?: 0.0,
                        valorPedagios = valorPedagios.toLongOrNull()?.toDouble()?.div(100.0) ?: 0.0,
                        valorTotalDeslocamento = valorDeslocamentoTotal
                    )

                    // Passar dados para próxima etapa incluindo horas e deslocamento
                    val defeitosString = defeitos
                    val servicosString = servicos
                    val observacoesEncoded = java.net.URLEncoder.encode(observacoesDecodificadas, "UTF-8")
                    val pecasString = pecas
                    val horasData = "${horarioEntrada};${horarioSaida};${distanciaKm};${valorPorKm};${valorPedagios};${valorDeslocamentoTotal}"

                    navController.navigate("relatorioEtapa6?defeitos=$defeitosString&servicos=$servicosString&observacoes=$observacoesEncoded&pecas=$pecasString&horas=$horasData&clienteId=$clienteId")
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A4A5C),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Próximo", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// Função para calcular a duração entre dois horários
fun calcularDuracao(entrada: String, saida: String): String {
    return try {
        val partesEntrada = entrada.split(":")
        val partesSaida = saida.split(":")

        val horaEntrada = partesEntrada[0].toInt()
        val minutoEntrada = partesEntrada[1].toInt()
        val horaSaida = partesSaida[0].toInt()
        val minutoSaida = partesSaida[1].toInt()

        val minutosEntrada = horaEntrada * 60 + minutoEntrada
        val minutosSaida = horaSaida * 60 + minutoSaida

        val duracaoMinutos = if (minutosSaida >= minutosEntrada) {
            minutosSaida - minutosEntrada
        } else {
            // Caso a saída seja no dia seguinte
            (24 * 60) - minutosEntrada + minutosSaida
        }

        val horas = duracaoMinutos / 60
        val minutos = duracaoMinutos % 60

        when {
            horas == 0 -> "${minutos}min"
            minutos == 0 -> "${horas}h"
            else -> "${horas}h ${minutos}min"
        }
    } catch (_: Exception) {
        "Duração inválida"
    }
}

// Função para calcular a duração em minutos entre dois horários
fun calcularDuracaoEmMinutos(entrada: String, saida: String): Int {
    return try {
        val partesEntrada = entrada.split(":")
        val partesSaida = saida.split(":")

        val horaEntrada = partesEntrada[0].toInt()
        val minutoEntrada = partesEntrada[1].toInt()
        val horaSaida = partesSaida[0].toInt()
        val minutoSaida = partesSaida[1].toInt()

        val minutosEntrada = horaEntrada * 60 + minutoEntrada
        val minutosSaida = horaSaida * 60 + minutoSaida

        if (minutosSaida >= minutosEntrada) {
            minutosSaida - minutosEntrada
        } else {
            // Caso a saída seja no dia seguinte
            (24 * 60) - minutosEntrada + minutosSaida
        }
    } catch (_: Exception) {
        0
    }
}

// Função configurar formatação monetária nativa
fun setupCurrencyInput(editText: EditText, onValueChange: (String) -> Unit) {
    val locale = Locale("pt", "BR")
    val numberFormat = NumberFormat.getCurrencyInstance(locale)

    editText.addTextChangedListener(object : TextWatcher {
        private var current = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (s.toString() != current) {
                editText.removeTextChangedListener(this)

                val cleanString = s.toString().replace("[R$,.\\s]".toRegex(), "")

                if (cleanString.isNotEmpty()) {
                    try {
                        val parsed = BigDecimal(cleanString).divide(BigDecimal(100))
                        val formatted = numberFormat.format(parsed)

                        current = formatted
                        editText.setText(formatted)
                        editText.setSelection(formatted.length)

                        // Retorna apenas os dígitos para armazenamento
                        onValueChange(cleanString)
                    } catch (e: Exception) {
                        current = ""
                        editText.setText("")
                        onValueChange("")
                    }
                } else {
                    current = ""
                    editText.setText("")
                    onValueChange("")
                }

                editText.addTextChangedListener(this)
            }
        }
    })
}

// Componente personalizado para input de valores monetários brasileiros usando EditText nativo
@Composable
fun CampoValorMonetario(
    valor: String,
    onValorChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1A4A5C),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Card com estilo similar aos OutlinedTextField
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            // EditText nativo com formatação monetária
            AndroidView(
                factory = { ctx ->
                    EditText(ctx).apply {
                        // Configurações básicas
                        inputType = InputType.TYPE_CLASS_NUMBER
                        textSize = 16f
                        setTextColor(android.graphics.Color.BLACK)
                        setHintTextColor(android.graphics.Color.GRAY)
                        hint = placeholder
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL

                        // Remove background padrão para usar o Card como container
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        setPadding(48, 16, 16, 16) // Espaço para o R$

                        // Configurar formatação monetária
                        setupCurrencyInput(this, onValorChange)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Ícone R$ sobreposto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp),
                contentAlignment = androidx.compose.ui.Alignment.CenterStart
            ) {
                Text(
                    text = "R$",
                    color = Color(0xFF1A4A5C),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Feedback visual
        if (valor.isNotEmpty()) {
            val valorFormatado = try {
                val parsed = BigDecimal(valor).divide(BigDecimal(100))
                NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(parsed)
            } catch (e: Exception) {
                "R$ 0,00"
            }

            Text(
                text = "✓ $valorFormatado",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        } else {
            Text(
                text = "Digite o valor (ex: 150 = R$ 1,50)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
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

// Função auxiliar para calcular total de horas
private fun calcularTotalHoras(entrada: String, saida: String): Double {
    return try {
        val entradaParts = entrada.split(":")
        val saidaParts = saida.split(":")

        val entradaMinutos = entradaParts[0].toInt() * 60 + entradaParts[1].toInt()
        val saidaMinutos = saidaParts[0].toInt() * 60 + saidaParts[1].toInt()

        val diferencaMinutos = if (saidaMinutos >= entradaMinutos) {
            saidaMinutos - entradaMinutos
        } else {
            (24 * 60 + saidaMinutos) - entradaMinutos
        }

        diferencaMinutos / 60.0
    } catch (e: Exception) {
        0.0
    }
}
