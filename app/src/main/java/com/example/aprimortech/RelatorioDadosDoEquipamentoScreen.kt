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
import com.example.aprimortech.model.Cliente
import androidx.compose.material3.MenuAnchorType
import android.widget.Toast
import android.app.DatePickerDialog
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.UUID

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
    sharedViewModel: RelatorioSharedViewModel
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

    // Novo: controlar exibição do modal de nova máquina
    var showNovoMaquinaDialog by remember { mutableStateOf(false) }
    // Novo: id pendente para seleção após salvar (aguarda ViewModel atualizar lista)
    var pendingMaquinaId by remember { mutableStateOf<String?>(null) }

    // Listas de tintas e solventes disponíveis
    var tintasDisponiveis by remember { mutableStateOf<List<Tinta>>(emptyList()) }
    var solventesDisponiveis by remember { mutableStateOf<List<Solvente>>(emptyList()) }

    // Estados do ViewModel
    val maquinas by viewModel.maquinas.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val fabricantesDisponiveis by viewModel.fabricantesDisponiveis.collectAsState()
    val modelosDisponiveis by viewModel.modelosDisponiveis.collectAsState()
    val codigosTintaDisponiveis by viewModel.codigosTintaDisponiveis.collectAsState()
    val codigosSolventeDisponiveis by viewModel.codigosSolventeDisponiveis.collectAsState()

    val operacaoEmAndamento by viewModel.operacaoEmAndamento.collectAsState()
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    // Carregar dados do ViewModel ao entrar (para clientes, máquinas e autocompletes)
    LaunchedEffect(Unit) { viewModel.carregarTodosDados() }

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

    // Quando um id de máquina foi salvo/pendente, aguardar até que ViewModel retorne essa máquina e selecioná-la
    LaunchedEffect(pendingMaquinaId, maquinas) {
        val pid = pendingMaquinaId
        if (!pid.isNullOrBlank()) {
            val encontrada = maquinas.find { it.id == pid }
            if (encontrada != null) {
                maquinaSelecionada = encontrada
                pendingMaquinaId = null
            }
        }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Máquina", style = MaterialTheme.typography.titleMedium, color = Brand)
                        IconButton(onClick = { showNovoMaquinaDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Nova Máquina", tint = Brand)
                        }
                    }
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
                                            // ✅ Removido: campos dataProximaPreventiva e horasProximaPreventiva não existem mais na máquina
                                            // Os valores serão preenchidos manualmente pelo usuário
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
                            // Salvar novo código E garantir que o campo receba o valor
                            scope.launch {
                                val novo = codigoTintaSelecionado.trim()
                                if (novo.isNotBlank()) {
                                    // Comparação case-insensitive para evitar duplicatas
                                    val existe = tintasDisponiveis.any { it.codigo.equals(novo, ignoreCase = true) }
                                    if (!existe) {
                                        tintaRepository.salvarTinta(novo)
                                        tintasDisponiveis = tintaRepository.buscarTodas()
                                        Toast.makeText(context, "Código de tinta salvo!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Código de tinta já existe", Toast.LENGTH_SHORT).show()
                                    }
                                }
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
                                val novo = codigoSolventeSelecionado.trim()
                                if (novo.isNotBlank()) {
                                    val existe = solventesDisponiveis.any { it.codigo.equals(novo, ignoreCase = true) }
                                    if (!existe) {
                                        solventeRepository.salvarSolvente(novo)
                                        solventesDisponiveis = solventeRepository.buscarTodos()
                                        Toast.makeText(context, "Código de solvente salvo!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Código de solvente já existe", Toast.LENGTH_SHORT).show()
                                    }
                                }
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
                        // Executar salvamento das collections e salvar no SharedViewModel
                        scope.launch {
                            val novoTinta = codigoTintaSelecionado.trim()
                            if (novoTinta.isNotBlank()) {
                                val existeTinta = tintasDisponiveis.any { it.codigo.equals(novoTinta, ignoreCase = true) }
                                if (!existeTinta) {
                                    tintaRepository.salvarTinta(novoTinta)
                                    tintasDisponiveis = tintaRepository.buscarTodas()
                                }
                            }
                            val novoSolvente = codigoSolventeSelecionado.trim()
                            if (novoSolvente.isNotBlank()) {
                                val existeSolvente = solventesDisponiveis.any { it.codigo.equals(novoSolvente, ignoreCase = true) }
                                if (!existeSolvente) {
                                    solventeRepository.salvarSolvente(novoSolvente)
                                    solventesDisponiveis = solventeRepository.buscarTodos()
                                }
                            }

                            // Agora salvar os dados no SharedViewModel
                            maquinaSelecionada?.let { maquina ->
                                android.util.Log.d("RelatorioEquipamentoScreen", "[DEBUG] Salvando dados do equipamento (dentro do coroutine)")
                                android.util.Log.d("RelatorioEquipamentoScreen", "[DEBUG] codigoTintaSelecionado: $codigoTintaSelecionado")
                                android.util.Log.d("RelatorioEquipamentoScreen", "[DEBUG] codigoSolventeSelecionado: $codigoSolventeSelecionado")
                                android.util.Log.d("RelatorioEquipamentoScreen", "[DEBUG] dataProximaPreventiva: $dataProximaPreventiva")
                                android.util.Log.d("RelatorioEquipamentoScreen", "[DEBUG] horasProximaPreventiva: $horasProximaPreventiva")
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

                            // Navegar após salvar
                            navController.navigate("relatorioEtapa3?clienteId=$clienteId")
                        }
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

    // Dialog para nova máquina (usa implementação idêntica à de MaquinasScreen)
    if (showNovoMaquinaDialog) {
        AddEditMaquinaDialog(
            initial = MaquinaEntity(
                id = UUID.randomUUID().toString(),
                clienteId = clienteId,
                fabricante = "",
                numeroSerie = "",
                modelo = "",
                identificacao = "",
                anoFabricacao = "",
                codigoConfiguracao = ""
            ),
            clientes = clientes,
            fabricantesDisponiveis = fabricantesDisponiveis,
            modelosDisponiveis = modelosDisponiveis,
            onDismiss = { showNovoMaquinaDialog = false },
            onConfirm = { nova ->
                // Marcar pending id e solicitar salvamento via ViewModel
                pendingMaquinaId = nova.id
                viewModel.salvarMaquina(nova)
                showNovoMaquinaDialog = false
            }
        )
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
        expanded = expanded && (filteredSuggestions.isNotEmpty() || value.isNotBlank()),
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

        // Mostrar o menu sempre que estiver expandido e houver sugestão OU valor não vazio
        if (expanded && (filteredSuggestions.isNotEmpty() || value.isNotBlank())) {
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

                // Se o valor atual não está presente nas sugestões e não é vazio,
                // mostrar opção para adicionar novo código
                val alreadyExists = filteredSuggestions.any { it.equals(value, ignoreCase = true) }
                if (value.isNotBlank() && !alreadyExists) {
                    DropdownMenuItem(
                        text = { Text("Adicionar '" + value + "'") },
                        leadingIcon = { Icon(Icons.Filled.Add, contentDescription = "Adicionar") },
                        onClick = {
                            // Chama callback do chamador para salvar/usar o novo valor
                            onAddNew()
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
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    focusedBorderColor = Brand,
    unfocusedBorderColor = Color.LightGray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditMaquinaDialog(
    initial: MaquinaEntity,
    clientes: List<Cliente>,
    fabricantesDisponiveis: List<String>,
    modelosDisponiveis: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (MaquinaEntity) -> Unit
) {
    var clienteId by remember { mutableStateOf(initial.clienteId) }
    var fabricante by remember { mutableStateOf(initial.fabricante) }
    var numeroSerie by remember { mutableStateOf(initial.numeroSerie) }
    var modelo by remember { mutableStateOf(initial.modelo) }
    var identificacao by remember { mutableStateOf(initial.identificacao) }
    var anoFabricacao by remember { mutableStateOf(initial.anoFabricacao) }
    var codigoConfiguracao by remember { mutableStateOf(initial.codigoConfiguracao) }

    val salvarHabilitado = clienteId.isNotBlank() && fabricante.isNotBlank() &&
            numeroSerie.isNotBlank() && modelo.isNotBlank() &&
            anoFabricacao.isNotBlank() && identificacao.isNotBlank() &&
            codigoConfiguracao.isNotBlank()
    // ⚠️ REMOVIDOS das validações: codigoTinta e codigoSolvente

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.fabricante.isBlank()) "Nova Máquina" else "Editar Máquina") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dropdown de Cliente
                var expanded by remember { mutableStateOf(false) }
                val clienteSelecionado = clientes.find { it.id == clienteId }?.nome ?: ""

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = clienteSelecionado,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Cliente *") },
                        placeholder = { Text("Selecione um cliente") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        clientes.forEach { cliente ->
                            DropdownMenuItem(
                                text = { Text(cliente.nome) },
                                onClick = {
                                    clienteId = cliente.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Campo Fabricante com dropdown autocomplete
                var fabricanteExpanded by remember { mutableStateOf(false) }
                val fabricantesFiltrados = remember(fabricante, fabricantesDisponiveis) {
                    if (fabricante.isBlank()) fabricantesDisponiveis.take(5)
                    else fabricantesDisponiveis.filter { it.contains(fabricante, ignoreCase = true) }.take(5)
                }

                ExposedDropdownMenuBox(
                    expanded = fabricanteExpanded && fabricantesFiltrados.isNotEmpty(),
                    onExpandedChange = { fabricanteExpanded = it }
                ) {
                    OutlinedTextField(
                        value = fabricante,
                        onValueChange = {
                            fabricante = it
                            fabricanteExpanded = it.isNotEmpty() && fabricantesFiltrados.isNotEmpty()
                        },
                        label = { Text("Fabricante *") },
                        placeholder = { Text("Ex: Hitachi, Videojet, Domino") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable),
                        colors = textFieldColors()
                    )
                    if (fabricantesFiltrados.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = fabricanteExpanded,
                            onDismissRequest = { fabricanteExpanded = false }
                        ) {
                            fabricantesFiltrados.forEach { sugestao ->
                                DropdownMenuItem(
                                    text = { Text(sugestao) },
                                    onClick = {
                                        fabricante = sugestao
                                        fabricanteExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = numeroSerie,
                    onValueChange = { numeroSerie = it.uppercase() },
                    label = { Text("Número de Série *") },
                    placeholder = { Text("Ex: SN001234") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                // Campo Modelo com dropdown autocomplete
                var modeloExpanded by remember { mutableStateOf(false) }
                val modelosFiltrados = remember(modelo, modelosDisponiveis) {
                    if (modelo.isBlank()) modelosDisponiveis.take(5)
                    else modelosDisponiveis.filter { it.contains(modelo, ignoreCase = true) }.take(5)
                }

                ExposedDropdownMenuBox(
                    expanded = modeloExpanded && modelosFiltrados.isNotEmpty(),
                    onExpandedChange = { modeloExpanded = it }
                ) {
                    OutlinedTextField(
                        value = modelo,
                        onValueChange = {
                            modelo = it.uppercase()
                            modeloExpanded = it.isNotEmpty() && modelosFiltrados.isNotEmpty()
                        },
                        label = { Text("Modelo *") },
                        placeholder = { Text("Ex: UX-D160W, 1550") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable),
                        colors = textFieldColors()
                    )
                    if (modelosFiltrados.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = modeloExpanded,
                            onDismissRequest = { modeloExpanded = false }
                        ) {
                            modelosFiltrados.forEach { sugestao ->
                                DropdownMenuItem(
                                    text = { Text(sugestao) },
                                    onClick = {
                                        modelo = sugestao.uppercase()
                                        modeloExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = identificacao,
                    onValueChange = { identificacao = it },
                    label = { Text("Identificação *") },
                    placeholder = { Text("Ex: Máquina Principal") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = anoFabricacao,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }.take(4)
                        anoFabricacao = filtered
                    },
                    label = { Text("Ano de Fabricação *") },
                    placeholder = { Text("Ex: 2020") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = codigoConfiguracao,
                    onValueChange = { codigoConfiguracao = it.uppercase() },
                    label = { Text("Código de Configuração *") },
                    placeholder = { Text("Ex: CFG001, CONFIG-A") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Text(
                    "* Campos obrigatórios",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        initial.copy(
                            clienteId = clienteId.trim(),
                            fabricante = fabricante.trim(),
                            numeroSerie = numeroSerie.trim(),
                            modelo = modelo.trim(),
                            anoFabricacao = anoFabricacao.trim(),
                            identificacao = identificacao.trim(),
                            codigoConfiguracao = codigoConfiguracao.trim()
                        )
                    )
                },
                enabled = salvarHabilitado,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brand,
                    contentColor = Color.White,
                    disabledContainerColor = Brand.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(6.dp)
            ) { Text("Salvar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(6.dp)) {
                Text("Cancelar", color = Brand)
            }
        }
    )
}
