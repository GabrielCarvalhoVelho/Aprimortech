package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.DadosEquipamentoViewModel
import com.example.aprimortech.ui.viewmodel.DadosEquipamentoViewModelFactory
import com.example.aprimortech.model.Maquina
import kotlinx.coroutines.delay
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.MenuAnchorType

private val Brand = Color(0xFF1A4A5C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DadosEquipamentoScreen(
    navController: NavController,
    clienteId: String,
    contatoId: String,
    setorId: String,
    modifier: Modifier = Modifier,
    viewModel: DadosEquipamentoViewModel = viewModel(
        factory = DadosEquipamentoViewModelFactory(
            maquinaRepository = (LocalContext.current.applicationContext as AprimortechApplication).maquinaRepository,
            relatorioRepository = (LocalContext.current.applicationContext as AprimortechApplication).relatorioRepository
        )
    )
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Estados do formulário
    var fabricanteSelecionado by remember { mutableStateOf("") }
    var fabricanteBusca by remember { mutableStateOf("") }
    var debouncedFabricanteBusca by remember { mutableStateOf("") }

    var numeroSerie by remember { mutableStateOf("") }
    var anoFabricacao by remember { mutableStateOf("") }
    var codigoConfiguracao by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var identificacao by remember { mutableStateOf("") }

    var codigoTinta by remember { mutableStateOf("") }
    var debouncedCodigoTinta by remember { mutableStateOf("") }
    var codigoSolvente by remember { mutableStateOf("") }
    var debouncedCodigoSolvente by remember { mutableStateOf("") }

    var dataProximaManutencao by remember { mutableStateOf("") }
    var horasProximaManutencao by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Estados do ViewModel
    val fabricantes by viewModel.fabricantes.collectAsState()
    val maquinas by viewModel.maquinas.collectAsState()
    val codigosTinta by viewModel.codigosTinta.collectAsState()
    val codigosSolvente by viewModel.codigosSolvente.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    // Feedback toast
    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.limparMensagem()
        }
    }

    // Debounce para busca de fabricante
    LaunchedEffect(fabricanteBusca) {
        delay(300)
        debouncedFabricanteBusca = fabricanteBusca
    }

    // Debounce para códigos
    LaunchedEffect(codigoTinta) {
        delay(300)
        debouncedCodigoTinta = codigoTinta
    }

    LaunchedEffect(codigoSolvente) {
        delay(300)
        debouncedCodigoSolvente = codigoSolvente
    }

    // Carregar dados ao entrar
    LaunchedEffect(Unit) {
        viewModel.carregarDados()
    }

    // Filtrar fabricantes
    val fabricantesFiltrados = remember(debouncedFabricanteBusca, fabricantes) {
        if (debouncedFabricanteBusca.isBlank()) fabricantes.take(5)
        else fabricantes.filter {
            it.contains(debouncedFabricanteBusca, ignoreCase = true)
        }.take(5)
    }

    // Filtrar códigos de tinta
    val codigosTintaFiltrados = remember(debouncedCodigoTinta, codigosTinta) {
        if (debouncedCodigoTinta.isBlank()) codigosTinta.take(5)
        else codigosTinta.filter {
            it.contains(debouncedCodigoTinta, ignoreCase = true)
        }.take(5)
    }

    // Filtrar códigos de solvente
    val codigosSolventeFiltrados = remember(debouncedCodigoSolvente, codigosSolvente) {
        if (debouncedCodigoSolvente.isBlank()) codigosSolvente.take(5)
        else codigosSolvente.filter {
            it.contains(debouncedCodigoSolvente, ignoreCase = true)
        }.take(5)
    }

    // Buscar máquina por número de série
    LaunchedEffect(numeroSerie) {
        if (numeroSerie.isNotBlank()) {
            val maquina = maquinas.find { it.numeroSerie.equals(numeroSerie, ignoreCase = true) }
            if (maquina != null) {
                anoFabricacao = maquina.anoFabricacao.toString()
                codigoConfiguracao = maquina.codigoConfiguracao
                modelo = maquina.modelo
                identificacao = maquina.nomeMaquina
                codigoTinta = maquina.codigoTinta
                codigoSolvente = maquina.codigoSolvente
            } else {
                // Limpar campos se não encontrar máquina
                anoFabricacao = ""
                codigoConfiguracao = ""
                modelo = ""
                identificacao = ""
                codigoTinta = ""
                codigoSolvente = ""
            }
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Brand)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Dados do Equipamento", style = MaterialTheme.typography.headlineMedium, color = Brand)
            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Brand)
                }
                Spacer(Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // SEÇÃO FABRICANTE
                SectionCard {
                    Text("Fabricante", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    var fabricanteExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = fabricanteExpanded && fabricantesFiltrados.isNotEmpty(),
                        onExpandedChange = { expanded ->
                            fabricanteExpanded = expanded && fabricantesFiltrados.isNotEmpty()
                        }
                    ) {
                        OutlinedTextField(
                            value = fabricanteSelecionado.ifEmpty { fabricanteBusca },
                            onValueChange = { newValue ->
                                if (fabricanteSelecionado.isEmpty()) {
                                    fabricanteBusca = newValue
                                    fabricanteExpanded = newValue.isNotEmpty()
                                } else {
                                    fabricanteSelecionado = ""
                                    fabricanteBusca = newValue
                                    fabricanteExpanded = newValue.isNotEmpty()
                                }
                            },
                            label = { Text("Buscar Fabricante *") },
                            placeholder = { Text("Digite o nome do fabricante") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable),
                            colors = textFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { fabricanteExpanded = false }
                            ),
                            singleLine = true
                        )

                        if (fabricantesFiltrados.isNotEmpty() && fabricanteExpanded) {
                            ExposedDropdownMenu(
                                expanded = true,
                                onDismissRequest = { fabricanteExpanded = false }
                            ) {
                                fabricantesFiltrados.forEach { fabricante ->
                                    DropdownMenuItem(
                                        text = { Text(fabricante) },
                                        onClick = {
                                            fabricanteSelecionado = fabricante
                                            fabricanteBusca = ""
                                            fabricanteExpanded = false
                                            keyboardController?.hide()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (fabricanteSelecionado.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("✓ Fabricante Selecionado", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
                                Text(fabricanteSelecionado, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // SEÇÃO NÚMERO DE SÉRIE E DADOS DA MÁQUINA
                SectionCard {
                    Text("Dados da Máquina", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    // Campo Número de Série com autocomplete baseado no fabricante selecionado
                    if (fabricanteSelecionado.isEmpty()) {
                        OutlinedTextField(
                            value = "",
                            onValueChange = { },
                            label = { Text("Número de Série *") },
                            placeholder = { Text("Selecione um fabricante primeiro") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors(),
                            enabled = false
                        )
                    } else {
                        val numerosSerieFabricante = remember(fabricanteSelecionado) {
                            viewModel.obterNumerosSeriePorFabricante(fabricanteSelecionado)
                        }

                        var numeroSerieExpanded by remember { mutableStateOf(false) }
                        var numeroSerieBusca by remember { mutableStateOf("") }
                        var debouncedNumeroSerieBusca by remember { mutableStateOf("") }

                        // Debounce para busca de número de série
                        LaunchedEffect(numeroSerieBusca) {
                            delay(300)
                            debouncedNumeroSerieBusca = numeroSerieBusca
                        }

                        val numerosSerieFiltrados = remember(debouncedNumeroSerieBusca, numerosSerieFabricante) {
                            if (debouncedNumeroSerieBusca.isBlank()) numerosSerieFabricante.take(5)
                            else numerosSerieFabricante.filter {
                                it.contains(debouncedNumeroSerieBusca, ignoreCase = true)
                            }.take(5)
                        }

                        ExposedDropdownMenuBox(
                            expanded = numeroSerieExpanded && numerosSerieFiltrados.isNotEmpty(),
                            onExpandedChange = { expanded ->
                                numeroSerieExpanded = expanded && numerosSerieFiltrados.isNotEmpty()
                            }
                        ) {
                            OutlinedTextField(
                                value = if (numeroSerie.isNotEmpty()) numeroSerie else numeroSerieBusca,
                                onValueChange = { newValue ->
                                    if (numeroSerie.isEmpty()) {
                                        numeroSerieBusca = newValue.uppercase()
                                        numeroSerieExpanded = newValue.isNotEmpty()
                                    } else {
                                        numeroSerie = ""
                                        numeroSerieBusca = newValue.uppercase()
                                        numeroSerieExpanded = newValue.isNotEmpty()
                                        // Limpar campos dependentes
                                        anoFabricacao = ""
                                        codigoConfiguracao = ""
                                        modelo = ""
                                        identificacao = ""
                                    }
                                },
                                label = { Text("Número de Série *") },
                                placeholder = { Text("Digite ou selecione o número de série") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryEditable),
                                colors = textFieldColors(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { numeroSerieExpanded = false }
                                ),
                                singleLine = true
                            )

                            if (numerosSerieFiltrados.isNotEmpty() && numeroSerieExpanded) {
                                ExposedDropdownMenu(
                                    expanded = true,
                                    onDismissRequest = { numeroSerieExpanded = false }
                                ) {
                                    numerosSerieFiltrados.forEach { serie ->
                                        DropdownMenuItem(
                                            text = { Text(serie) },
                                            onClick = {
                                                numeroSerie = serie
                                                numeroSerieBusca = ""
                                                numeroSerieExpanded = false
                                                keyboardController?.hide()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Mostrar quantos números de série existem para esse fabricante
                        if (numerosSerieFabricante.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${numerosSerieFabricante.size} número(s) de série disponível(is) para ${fabricanteSelecionado}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = anoFabricacao,
                            onValueChange = { },
                            label = { Text("Ano de Fabricação") },
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors(),
                            readOnly = true,
                            enabled = false
                        )

                        OutlinedTextField(
                            value = codigoConfiguracao,
                            onValueChange = { },
                            label = { Text("Código de Configuração") },
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors(),
                            readOnly = true,
                            enabled = false
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = modelo,
                        onValueChange = { },
                        label = { Text("Modelo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(),
                        readOnly = true,
                        enabled = false
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = identificacao,
                        onValueChange = {
                            identificacao = it
                            // Atualizar identificação da máquina no banco quando editado
                            if (numeroSerie.isNotBlank()) {
                                viewModel.atualizarIdentificacaoMaquina(numeroSerie, it)
                            }
                        },
                        label = { Text("Identificação") },
                        placeholder = { Text("Nome/identificação da máquina (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true
                    )
                }

                // SEÇÃO CÓDIGOS DE TINTA E SOLVENTE
                SectionCard {
                    Text("Códigos de Tinta e Solvente", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    // Código da Tinta
                    var codigoTintaExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = codigoTintaExpanded && codigosTintaFiltrados.isNotEmpty(),
                        onExpandedChange = { expanded ->
                            codigoTintaExpanded = expanded && codigosTintaFiltrados.isNotEmpty()
                        }
                    ) {
                        OutlinedTextField(
                            value = codigoTinta,
                            onValueChange = { newValue ->
                                codigoTinta = newValue
                                codigoTintaExpanded = newValue.isNotEmpty()
                            },
                            label = { Text("Código da Tinta") },
                            placeholder = { Text("Digite ou selecione o código da tinta") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable),
                            colors = textFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true
                        )

                        if (codigosTintaFiltrados.isNotEmpty() && codigoTintaExpanded) {
                            ExposedDropdownMenu(
                                expanded = true,
                                onDismissRequest = { codigoTintaExpanded = false }
                            ) {
                                codigosTintaFiltrados.forEach { codigo ->
                                    DropdownMenuItem(
                                        text = { Text(codigo) },
                                        onClick = {
                                            codigoTinta = codigo
                                            codigoTintaExpanded = false
                                            keyboardController?.hide()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Código do Solvente
                    var codigoSolventeExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = codigoSolventeExpanded && codigosSolventeFiltrados.isNotEmpty(),
                        onExpandedChange = { expanded ->
                            codigoSolventeExpanded = expanded && codigosSolventeFiltrados.isNotEmpty()
                        }
                    ) {
                        OutlinedTextField(
                            value = codigoSolvente,
                            onValueChange = { newValue ->
                                codigoSolvente = newValue
                                codigoSolventeExpanded = newValue.isNotEmpty()
                            },
                            label = { Text("Código do Solvente") },
                            placeholder = { Text("Digite ou selecione o código do solvente") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable),
                            colors = textFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true
                        )

                        if (codigosSolventeFiltrados.isNotEmpty() && codigoSolventeExpanded) {
                            ExposedDropdownMenu(
                                expanded = true,
                                onDismissRequest = { codigoSolventeExpanded = false }
                            ) {
                                codigosSolventeFiltrados.forEach { codigo ->
                                    DropdownMenuItem(
                                        text = { Text(codigo) },
                                        onClick = {
                                            codigoSolvente = codigo
                                            codigoSolventeExpanded = false
                                            keyboardController?.hide()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // SEÇÃO PRÓXIMA MANUTENÇÃO PREVENTIVA
                SectionCard {
                    Text("Próxima Manutenção Preventiva", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    // Campo Data da Próxima Manutenção
                    OutlinedTextField(
                        value = dataProximaManutencao,
                        onValueChange = { dataProximaManutencao = it },
                        label = { Text("Data da Próxima Manutenção") },
                        placeholder = { Text("DD/MM/AAAA") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
                            }
                        },
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    // Campo Horas para Próxima Manutenção
                    OutlinedTextField(
                        value = horasProximaManutencao,
                        onValueChange = { horasProximaManutencao = it },
                        label = { Text("Horas para Próxima Manutenção") },
                        placeholder = { Text("Ex: 1000") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // BOTÃO CONTINUAR
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Navegar para próxima página do relatório - Defeitos e Serviços Realizados
                        navController.navigate("relatorioEtapa3?clienteId=$clienteId")
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = fabricanteSelecionado.isNotEmpty() && numeroSerie.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continuar", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        dataProximaManutencao = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) { Column(modifier = Modifier.padding(16.dp), content = content) }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Color.LightGray,
    unfocusedBorderColor = Color.LightGray
)

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DadosEquipamentoScreenPreview() {
    AprimortechTheme {
        DadosEquipamentoScreen(
            navController = rememberNavController(),
            clienteId = "1",
            contatoId = "1",
            setorId = "1"
        )
    }
}
