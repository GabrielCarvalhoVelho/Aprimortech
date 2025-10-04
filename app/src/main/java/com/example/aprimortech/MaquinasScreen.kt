package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
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
import androidx.compose.foundation.BorderStroke
import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.data.local.entity.ClienteEntity
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.MaquinaViewModel
import com.example.aprimortech.ui.viewmodel.MaquinaViewModelFactory
import java.util.UUID

private val Brand = Color(0xFF1A4A5C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaquinasScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MaquinaViewModel = viewModel(
        factory = MaquinaViewModelFactory(
            buscarMaquinasUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarMaquinasUseCase,
            salvarMaquinaUseCase = (LocalContext.current.applicationContext as AprimortechApplication).salvarMaquinaUseCase,
            excluirMaquinaUseCase = (LocalContext.current.applicationContext as AprimortechApplication).excluirMaquinaUseCase,
            sincronizarMaquinasUseCase = (LocalContext.current.applicationContext as AprimortechApplication).sincronizarMaquinasUseCase,
            buscarClientesUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarClientesUseCase
        )
    )
) {
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    val maquinas by viewModel.maquinas.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val fabricantesDisponiveis by viewModel.fabricantesDisponiveis.collectAsState()
    val modelosDisponiveis by viewModel.modelosDisponiveis.collectAsState()
    val codigosTintaDisponiveis by viewModel.codigosTintaDisponiveis.collectAsState()
    val codigosSolventeDisponiveis by viewModel.codigosSolventeDisponiveis.collectAsState()
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    val listaFiltrada = remember(maquinas, clientes, query) {
        if (query.isBlank()) maquinas else maquinas.filter { maquina ->
            val clienteNome = clientes.find { it.id == maquina.clienteId }?.nome ?: ""

            maquina.nomeMaquina.contains(query, ignoreCase = true) ||
            maquina.modelo.contains(query, ignoreCase = true) ||
            maquina.numeroSerie.contains(query, ignoreCase = true) ||
            maquina.fabricante.contains(query, ignoreCase = true) ||
            clienteNome.contains(query, ignoreCase = true)
        }
    }

    var showAddEdit by remember { mutableStateOf(false) }
    var editingMaquina by remember { mutableStateOf<MaquinaEntity?>(null) }

    var showDelete by remember { mutableStateOf(false) }
    var deletingMaquina by remember { mutableStateOf<MaquinaEntity?>(null) }

    var showView by remember { mutableStateOf(false) }
    var viewingMaquina by remember { mutableStateOf<MaquinaEntity?>(null) }

    // Feedback toast
    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.limparMensagem()
        }
    }

    // Carregar ao entrar
    LaunchedEffect(Unit) { viewModel.carregarTodosDados() }

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
            Text("Máquinas", style = MaterialTheme.typography.headlineMedium, color = Brand)
            Spacer(Modifier.height(12.dp))

            SectionCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Buscar por cliente, nome, fabricante, modelo ou nº série") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                editingMaquina = MaquinaEntity(
                                    id = UUID.randomUUID().toString(),
                                    clienteId = "",
                                    nomeMaquina = "",
                                    fabricante = "",
                                    numeroSerie = "",
                                    modelo = "",
                                    identificacao = "",
                                    anoFabricacao = "",
                                    codigoTinta = "",
                                    codigoSolvente = "",
                                    dataProximaPreventiva = ""
                                )
                                showAddEdit = true
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                        ) { Text("Adicionar Máquina") }

                        IconButton(
                            onClick = { viewModel.sincronizarDadosExistentes() },
                            modifier = Modifier.size(46.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Sync,
                                        contentDescription = "Sincronizar",
                                        tint = Brand,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(listaFiltrada, key = { it.id }) { maq ->
                    val clienteNome = clientes.find { it.id == maq.clienteId }?.nome ?: "Cliente não encontrado"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${maq.nomeMaquina} (${maq.fabricante})", style = MaterialTheme.typography.titleMedium, color = Brand)
                            Spacer(Modifier.height(4.dp))
                            Text("Cliente: $clienteNome", style = MaterialTheme.typography.bodySmall)
                            Text("Modelo: ${maq.modelo} • Nº Série: ${maq.numeroSerie}", style = MaterialTheme.typography.bodySmall)
                            Text("Próx. Preventiva: ${maq.dataProximaPreventiva}", style = MaterialTheme.typography.bodySmall)

                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = {
                                    viewingMaquina = maq
                                    showView = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Visualizar", tint = Brand)
                                }
                                IconButton(onClick = {
                                    deletingMaquina = maq
                                    showDelete = true
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEdit && editingMaquina != null) {
        AddEditMaquinaDialog(
            initial = editingMaquina!!,
            clientes = clientes,
            fabricantesDisponiveis = fabricantesDisponiveis,
            modelosDisponiveis = modelosDisponiveis,
            codigosTintaDisponiveis = codigosTintaDisponiveis,
            codigosSolventeDisponiveis = codigosSolventeDisponiveis,
            onDismiss = { showAddEdit = false; editingMaquina = null },
            onConfirm = { updated ->
                viewModel.salvarMaquina(updated)
                showAddEdit = false
                editingMaquina = null
            }
        )
    }

    if (showDelete && deletingMaquina != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false; deletingMaquina = null },
            title = { Text("Excluir máquina") },
            text = { Text("Tem certeza que deseja excluir \"${deletingMaquina!!.nomeMaquina}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.excluirMaquina(deletingMaquina!!)
                        showDelete = false
                        deletingMaquina = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White)
                ) { Text("Excluir") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDelete = false; deletingMaquina = null }) {
                    Text("Cancelar", color = Brand)
                }
            }
        )
    }

    if (showView && viewingMaquina != null) {
        ViewMaquinaDialog(
            maquina = viewingMaquina!!,
            clienteNome = clientes.find { it.id == viewingMaquina!!.clienteId }?.nome ?: "Cliente não encontrado",
            onDismiss = { showView = false; viewingMaquina = null },
            onEdit = {
                editingMaquina = viewingMaquina
                showView = false
                showAddEdit = true
            },
            onDelete = {
                deletingMaquina = viewingMaquina
                showView = false
                showDelete = true
            }
        )
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) { Column(modifier = Modifier.padding(12.dp), content = content) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditMaquinaDialog(
    initial: MaquinaEntity,
    clientes: List<ClienteEntity>,
    fabricantesDisponiveis: List<String>,
    modelosDisponiveis: List<String>,
    codigosTintaDisponiveis: List<String>,
    codigosSolventeDisponiveis: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (MaquinaEntity) -> Unit
) {
    var clienteId by remember { mutableStateOf(initial.clienteId) }
    var nomeMaquina by remember { mutableStateOf(initial.nomeMaquina) }
    var fabricante by remember { mutableStateOf(initial.fabricante) }
    var numeroSerie by remember { mutableStateOf(initial.numeroSerie) }
    var modelo by remember { mutableStateOf(initial.modelo) }
    var identificacao by remember { mutableStateOf(initial.identificacao) }
    var anoFabricacao by remember { mutableStateOf(initial.anoFabricacao) }
    var codigoTinta by remember { mutableStateOf(initial.codigoTinta) }
    var codigoSolvente by remember { mutableStateOf(initial.codigoSolvente) }
    var dataProximaPreventiva by remember { mutableStateOf(initial.dataProximaPreventiva) }

    val salvarHabilitado = clienteId.isNotBlank() && nomeMaquina.isNotBlank() && fabricante.isNotBlank() &&
            numeroSerie.isNotBlank() && modelo.isNotBlank() && codigoTinta.isNotBlank() &&
            anoFabricacao.isNotBlank() && identificacao.isNotBlank() &&
            codigoSolvente.isNotBlank() && dataProximaPreventiva.isNotBlank()

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
                            .menuAnchor(),
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

                OutlinedTextField(
                    value = nomeMaquina,
                    onValueChange = { nomeMaquina = it },
                    label = { Text("Nome da Máquina *") },
                    placeholder = { Text("Ex: Impressora Linha 1") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

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
                            .menuAnchor(),
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
                            .menuAnchor(),
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
                        // Permitir apenas números e máximo 4 dígitos
                        val filtered = newValue.filter { it.isDigit() }.take(4)
                        anoFabricacao = filtered
                    },
                    label = { Text("Ano de Fabricação *") },
                    placeholder = { Text("Ex: 2020") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                // Campo Código da Tinta com dropdown autocomplete
                var codigoTintaExpanded by remember { mutableStateOf(false) }
                val codigosTintaFiltrados = remember(codigoTinta, codigosTintaDisponiveis) {
                    if (codigoTinta.isBlank()) codigosTintaDisponiveis.take(5)
                    else codigosTintaDisponiveis.filter { it.contains(codigoTinta, ignoreCase = true) }.take(5)
                }

                ExposedDropdownMenuBox(
                    expanded = codigoTintaExpanded && codigosTintaFiltrados.isNotEmpty(),
                    onExpandedChange = { codigoTintaExpanded = it }
                ) {
                    OutlinedTextField(
                        value = codigoTinta,
                        onValueChange = {
                            codigoTinta = it.uppercase()
                            codigoTintaExpanded = it.isNotEmpty() && codigosTintaFiltrados.isNotEmpty()
                        },
                        label = { Text("Código da Tinta *") },
                        placeholder = { Text("Ex: T123, INK-001") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = textFieldColors()
                    )
                    if (codigosTintaFiltrados.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = codigoTintaExpanded,
                            onDismissRequest = { codigoTintaExpanded = false }
                        ) {
                            codigosTintaFiltrados.forEach { sugestao ->
                                DropdownMenuItem(
                                    text = { Text(sugestao) },
                                    onClick = {
                                        codigoTinta = sugestao.uppercase()
                                        codigoTintaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Campo Código do Solvente com dropdown autocomplete
                var codigoSolventeExpanded by remember { mutableStateOf(false) }
                val codigosSolventeFiltrados = remember(codigoSolvente, codigosSolventeDisponiveis) {
                    if (codigoSolvente.isBlank()) codigosSolventeDisponiveis.take(5)
                    else codigosSolventeDisponiveis.filter { it.contains(codigoSolvente, ignoreCase = true) }.take(5)
                }

                ExposedDropdownMenuBox(
                    expanded = codigoSolventeExpanded && codigosSolventeFiltrados.isNotEmpty(),
                    onExpandedChange = { codigoSolventeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = codigoSolvente,
                        onValueChange = {
                            codigoSolvente = it.uppercase()
                            codigoSolventeExpanded = it.isNotEmpty() && codigosSolventeFiltrados.isNotEmpty()
                        },
                        label = { Text("Código do Solvente *") },
                        placeholder = { Text("Ex: S001, SOL-456") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = textFieldColors()
                    )
                    if (codigosSolventeFiltrados.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = codigoSolventeExpanded,
                            onDismissRequest = { codigoSolventeExpanded = false }
                        ) {
                            codigosSolventeFiltrados.forEach { sugestao ->
                                DropdownMenuItem(
                                    text = { Text(sugestao) },
                                    onClick = {
                                        codigoSolvente = sugestao.uppercase()
                                        codigoSolventeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Campo para Data/Horas da Próxima Manutenção
                var tipoManutencao by remember { mutableStateOf("data") } // "data" ou "horas"
                val ctx = LocalContext.current
                val dateFormatter = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }

                fun abrirDatePicker() {
                    val hoje = java.util.Calendar.getInstance()
                    val dataInicial = try {
                        val parsedDate = dateFormatter.parse(dataProximaPreventiva)
                        val cal = java.util.Calendar.getInstance()
                        if (parsedDate != null) {
                            cal.time = parsedDate
                        }
                        cal
                    } catch (e: Exception) { hoje }

                    val dialog = android.app.DatePickerDialog(
                        ctx,
                        { _, year, month, dayOfMonth ->
                            val cal = java.util.Calendar.getInstance()
                            cal.set(year, month, dayOfMonth)
                            dataProximaPreventiva = dateFormatter.format(cal.time)
                        },
                        dataInicial.get(java.util.Calendar.YEAR),
                        dataInicial.get(java.util.Calendar.MONTH),
                        dataInicial.get(java.util.Calendar.DAY_OF_MONTH)
                    )
                    dialog.show()
                }

                // Toggle para escolher entre Data ou Horas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { tipoManutencao = "data" },
                        label = { Text("Data") },
                        selected = tipoManutencao == "data",
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { tipoManutencao = "horas" },
                        label = { Text("Horas") },
                        selected = tipoManutencao == "horas",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (tipoManutencao == "data") {
                    OutlinedTextField(
                        value = dataProximaPreventiva,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Data Próxima Manutenção *") },
                        placeholder = { Text("dd/mm/aaaa") },
                        trailingIcon = {
                            IconButton(onClick = { abrirDatePicker() }) {
                                Icon(Icons.Default.EditCalendar, contentDescription = "Abrir calendário", tint = Brand)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { abrirDatePicker() },
                        colors = textFieldColors()
                    )
                } else {
                    OutlinedTextField(
                        value = dataProximaPreventiva,
                        onValueChange = { newValue ->
                            // Permitir apenas números e máximo 6 dígitos para horas
                            val filtered = newValue.filter { it.isDigit() }.take(6)
                            dataProximaPreventiva = if (filtered.isNotEmpty()) "${filtered}h" else ""
                        },
                        label = { Text("Horas para Próxima Manutenção *") },
                        placeholder = { Text("Ex: 500h, 1000h, 2500h") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = { Text("Digite apenas números (ex: 500 para 500h)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                }

                // Informação sobre campos obrigatórios
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
                            nomeMaquina = nomeMaquina.trim(),
                            fabricante = fabricante.trim(),
                            numeroSerie = numeroSerie.trim(),
                            modelo = modelo.trim(),
                            codigoTinta = codigoTinta.trim(),
                            anoFabricacao = anoFabricacao.trim(),
                            identificacao = identificacao.trim(),
                            codigoSolvente = codigoSolvente.trim(),
                            dataProximaPreventiva = dataProximaPreventiva.trim()
                        )
                    )
                },
                enabled = salvarHabilitado,
                colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
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

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Color.LightGray,
    unfocusedBorderColor = Color.LightGray
)

@Composable
private fun ViewMaquinaDialog(
    maquina: MaquinaEntity,
    clienteNome: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Máquina") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cliente: $clienteNome")
                Text("Nome da Máquina: ${maquina.nomeMaquina}")
                Text("Fabricante: ${maquina.fabricante}")
                Text("Número de Série: ${maquina.numeroSerie}")
                Text("Modelo: ${maquina.modelo}")
                Text("Código da Tinta: ${maquina.codigoTinta}")
                Text("Ano Fabricação: ${maquina.anoFabricacao}")
                Text("Identificação: ${maquina.identificacao}")
                Text("Código do Solvente: ${maquina.codigoSolvente}")
                Text("Próx. Preventiva: ${maquina.dataProximaPreventiva}")
            }
        },
        confirmButton = {
            Button(onClick = onEdit, colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White)) {
                Text("Editar")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss) { Text("Fechar", color = Brand) }
                OutlinedButton(onClick = onDelete, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Excluir")
                }
            }
        }
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MaquinasScreenPreview() {
    AprimortechTheme {
        MaquinasScreen(navController = rememberNavController())
    }
}
