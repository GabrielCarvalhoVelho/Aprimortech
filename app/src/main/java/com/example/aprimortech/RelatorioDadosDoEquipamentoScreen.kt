package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aprimortech.ui.viewmodel.MaquinaViewModel
import com.example.aprimortech.ui.viewmodel.MaquinaViewModelFactory
import com.example.aprimortech.ui.viewmodel.RelatorioSharedViewModel
import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.model.Tinta
import com.example.aprimortech.model.Solvente
import androidx.compose.material3.MenuAnchorType
import android.widget.Toast
import android.app.DatePickerDialog
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

private val Brand = Color(0xFF1A4A5C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioEquipamentoScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    clienteId: String = "",
    viewModel: MaquinaViewModel = viewModel(
        factory = MaquinaViewModelFactory(
            buscarMaquinasUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarMaquinasUseCase,
            salvarMaquinaUseCase = (LocalContext.current.applicationContext as AprimortechApplication).salvarMaquinaUseCase,
            excluirMaquinaUseCase = (LocalContext.current.applicationContext as AprimortechApplication).excluirMaquinaUseCase,
            sincronizarMaquinasUseCase = (LocalContext.current.applicationContext as AprimortechApplication).sincronizarMaquinasUseCase,
            buscarClientesUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarClientesUseCase
        )
    ),
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    val context = LocalContext.current
    val app = context.applicationContext as AprimortechApplication
    val scope = rememberCoroutineScope()

    // Repositórios
    val tintaRepository = remember { app.tintaRepository }
    val solventeRepository = remember { app.solventeRepository }

    // Estados do formulário
    var maquinaSelecionada by remember { mutableStateOf<MaquinaEntity?>(null) }
    var codigoTintaSelecionado by remember { mutableStateOf("") }
    var codigoSolventeSelecionado by remember { mutableStateOf("") }
    var dataProximaPreventiva by remember { mutableStateOf("") }
    var horasProximaPreventiva by remember { mutableStateOf("") }

    // Listas de tintas e solventes disponíveis
    var tintasDisponiveis by remember { mutableStateOf<List<Tinta>>(emptyList()) }
    var solventesDisponiveis by remember { mutableStateOf<List<Solvente>>(emptyList()) }

    // Estados do ViewModel
    val maquinas by viewModel.maquinas.collectAsState()
    val operacaoEmAndamento by viewModel.operacaoEmAndamento.collectAsState()
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    // Carregar tintas e solventes ao iniciar
    LaunchedEffect(Unit) {
        scope.launch {
            tintasDisponiveis = tintaRepository.buscarTodas()
            solventesDisponiveis = solventeRepository.buscarTodos()
        }
    }

    // Filtrar máquinas do cliente atual
    val maquinasDoCliente = remember(maquinas, clienteId) {
        maquinas.filter { it.clienteId == clienteId }
    }

    // Feedback toast
    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.limparMensagem()
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

            if (operacaoEmAndamento) {
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

                // SEÇÃO MÁQUINA
                SectionCard {
                    Text("Máquina", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    if (maquinasDoCliente.isEmpty()) {
                        Text("Nenhuma máquina cadastrada para este cliente",
                             style = MaterialTheme.typography.bodyMedium,
                             color = Color.Gray)
                    } else {
                        var maquinaExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = maquinaExpanded,
                            onExpandedChange = { maquinaExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = maquinaSelecionada?.identificacao ?: "",
                                onValueChange = { },
                                label = { Text("Selecionar Máquina *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                colors = textFieldColors(),
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = maquinaExpanded) }
                            )

                            ExposedDropdownMenu(
                                expanded = maquinaExpanded,
                                onDismissRequest = { maquinaExpanded = false }
                            ) {
                                maquinasDoCliente.forEach { maquina ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(maquina.identificacao, style = MaterialTheme.typography.bodyMedium)
                                                Text("${maquina.fabricante} - ${maquina.modelo}",
                                                     style = MaterialTheme.typography.bodySmall,
                                                     color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            maquinaSelecionada = maquina
                                            dataProximaPreventiva = maquina.dataProximaPreventiva
                                            horasProximaPreventiva = maquina.horasProximaPreventiva
                                            maquinaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (maquinaSelecionada != null) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("✓ Máquina Selecionada", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
                                Text(maquinaSelecionada!!.identificacao, style = MaterialTheme.typography.bodyMedium)
                                Text("${maquinaSelecionada!!.fabricante} - ${maquinaSelecionada!!.modelo} (N/S: ${maquinaSelecionada!!.numeroSerie})",
                                     style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }

                // SEÇÃO CÓDIGO DA TINTA com Autocomplete
                SectionCard {
                    Text("Código da Tinta", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    AutocompleteTextField(
                        value = codigoTintaSelecionado,
                        onValueChange = { codigoTintaSelecionado = it },
                        suggestions = tintasDisponiveis.map { it.codigo },
                        label = "Código da Tinta *",
                        placeholder = "Digite ou selecione",
                        onAddNew = {
                            scope.launch {
                                tintaRepository.salvarTinta(codigoTintaSelecionado)
                                tintasDisponiveis = tintaRepository.buscarTodas()
                                Toast.makeText(context, "Código de tinta salvo!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // SEÇÃO CÓDIGO DO SOLVENTE com Autocomplete
                SectionCard {
                    Text("Código do Solvente", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    AutocompleteTextField(
                        value = codigoSolventeSelecionado,
                        onValueChange = { codigoSolventeSelecionado = it },
                        suggestions = solventesDisponiveis.map { it.codigo },
                        label = "Código do Solvente *",
                        placeholder = "Digite ou selecione",
                        onAddNew = {
                            scope.launch {
                                solventeRepository.salvarSolvente(codigoSolventeSelecionado)
                                solventesDisponiveis = solventeRepository.buscarTodos()
                                Toast.makeText(context, "Código de solvente salvo!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // SEÇÃO PRÓXIMA MANUTENÇÃO PREVENTIVA
                SectionCard {
                    Text("Próxima Manutenção Preventiva", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    // Campo de data
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val calendar = Calendar.getInstance()
                                if (dataProximaPreventiva.isNotBlank()) {
                                    try {
                                        val parts = dataProximaPreventiva.split("/")
                                        if (parts.size == 3) {
                                            calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                                        }
                                    } catch (_: Exception) { }
                                }

                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        dataProximaPreventiva = String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            day, month + 1, year
                                        )
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    ) {
                        OutlinedTextField(
                            value = dataProximaPreventiva,
                            onValueChange = { },
                            label = { Text("Data *") },
                            placeholder = { Text("DD/MM/AAAA") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors(),
                            trailingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = "Selecionar Data", tint = Brand)
                            },
                            readOnly = true,
                            enabled = false
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = horasProximaPreventiva,
                        onValueChange = { horasProximaPreventiva = it.filter { char -> char.isDigit() } },
                        label = { Text("Horas *") },
                        placeholder = { Text("Ex: 500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(),
                        trailingIcon = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        val atual = horasProximaPreventiva.toIntOrNull() ?: 0
                                        horasProximaPreventiva = (atual + 100).toString()
                                    },
                                    modifier = Modifier.size(32.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Brand.copy(alpha = 0.1f),
                                        contentColor = Brand
                                    )
                                ) {
                                    Text("+100", fontSize = 9.sp)
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        val atual = horasProximaPreventiva.toIntOrNull() ?: 0
                                        horasProximaPreventiva = (atual + 500).toString()
                                    },
                                    modifier = Modifier.size(32.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Brand.copy(alpha = 0.15f),
                                        contentColor = Brand
                                    )
                                ) {
                                    Text("+500", fontSize = 9.sp)
                                }
                            }
                        }
                    )
                }

                // BOTÃO CONTINUAR
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Salvar códigos nas collections
                        scope.launch {
                            if (codigoTintaSelecionado.isNotBlank()) {
                                tintaRepository.salvarTinta(codigoTintaSelecionado)
                            }
                            if (codigoSolventeSelecionado.isNotBlank()) {
                                solventeRepository.salvarSolvente(codigoSolventeSelecionado)
                            }
                        }

                        // Salvar dados no SharedViewModel
                        maquinaSelecionada?.let { maquina ->
                            sharedViewModel.setEquipamentoData(
                                fabricante = maquina.fabricante,
                                numeroSerie = maquina.numeroSerie,
                                codigoConfiguracao = maquina.codigoConfiguracao,
                                modelo = maquina.modelo,
                                identificacao = maquina.identificacao,
                                anoFabricacao = maquina.anoFabricacao,
                                codigoTinta = codigoTintaSelecionado,
                                codigoSolvente = codigoSolventeSelecionado,
                                dataProximaPreventiva = dataProximaPreventiva,
                                horaProximaPreventiva = horasProximaPreventiva
                            )
                        }

                        navController.navigate("relatorioEtapa3?clienteId=$clienteId")
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = maquinaSelecionada != null &&
                              codigoTintaSelecionado.isNotBlank() &&
                              codigoSolventeSelecionado.isNotBlank() &&
                              dataProximaPreventiva.isNotBlank() &&
                              horasProximaPreventiva.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continuar", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Campo de texto com autocomplete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    label: String,
    placeholder: String,
    onAddNew: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = remember(value, suggestions) {
        if (value.isBlank()) suggestions
        else suggestions.filter { it.contains(value, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filteredSuggestions.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            colors = textFieldColors(),
            trailingIcon = {
                if (filteredSuggestions.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
        )

        if (filteredSuggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredSuggestions.take(10).forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onValueChange(suggestion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFFF0F8FF),
    unfocusedContainerColor = Color(0xFFF0F8FF),
    focusedBorderColor = Brand,
    unfocusedBorderColor = Color.LightGray
)
