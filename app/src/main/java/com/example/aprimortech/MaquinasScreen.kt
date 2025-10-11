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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.BorderStroke
import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.model.Cliente
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.MaquinaViewModel
import com.example.aprimortech.ui.viewmodel.MaquinaViewModelFactory
import java.util.UUID
import androidx.compose.material3.MenuAnchorType

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
    val operacaoEmAndamento by viewModel.operacaoEmAndamento.collectAsState()
    val itensPendentes by viewModel.itensPendentesSincronizacao.collectAsState()

    val listaFiltrada = remember(maquinas, clientes, query) {
        if (query.isBlank()) maquinas else maquinas.filter { maquina ->
            val clienteNome = clientes.find { it.id == maquina.clienteId }?.nome ?: ""

            maquina.modelo.contains(query, ignoreCase = true) ||
            maquina.numeroSerie.contains(query, ignoreCase = true) ||
            maquina.fabricante.contains(query, ignoreCase = true) ||
            maquina.identificacao.contains(query, ignoreCase = true) ||
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
                                    fabricante = "",
                                    numeroSerie = "",
                                    modelo = "",
                                    identificacao = "",
                                    anoFabricacao = "",
                                    codigoTinta = "",
                                    codigoSolvente = "",
                                    dataProximaPreventiva = "",
                                    codigoConfiguracao = "",
                                    horasProximaPreventiva = ""
                                )
                                showAddEdit = true
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                        ) { Text("Adicionar Máquina") }
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
                            Text("${maq.fabricante} ${maq.modelo}", style = MaterialTheme.typography.titleMedium, color = Brand)
                            Spacer(Modifier.height(4.dp))
                            Text("Cliente: $clienteNome", style = MaterialTheme.typography.bodySmall)
                            Text("Nº Série: ${maq.numeroSerie} • Identificação: ${maq.identificacao}", style = MaterialTheme.typography.bodySmall)
                            Text("Próx. Preventiva: ${maq.dataProximaPreventiva} • ${maq.horasProximaPreventiva}h", style = MaterialTheme.typography.bodySmall)

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
            text = { Text("Tem certeza que deseja excluir \"${deletingMaquina!!.identificacao}\"?") },
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
            cliente = clientes.find { it.id == viewingMaquina!!.clienteId },
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
    clientes: List<Cliente>,
    fabricantesDisponiveis: List<String>,
    modelosDisponiveis: List<String>,
    codigosTintaDisponiveis: List<String>,
    codigosSolventeDisponiveis: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (MaquinaEntity) -> Unit
) {
    var clienteId by remember { mutableStateOf(initial.clienteId) }
    var fabricante by remember { mutableStateOf(initial.fabricante) }
    var numeroSerie by remember { mutableStateOf(initial.numeroSerie) }
    var modelo by remember { mutableStateOf(initial.modelo) }
    var identificacao by remember { mutableStateOf(initial.identificacao) }
    var anoFabricacao by remember { mutableStateOf(initial.anoFabricacao) }
    var codigoTinta by remember { mutableStateOf(initial.codigoTinta) }
    var codigoSolvente by remember { mutableStateOf(initial.codigoSolvente) }
    var dataProximaPreventiva by remember { mutableStateOf(initial.dataProximaPreventiva) }
    var codigoConfiguracao by remember { mutableStateOf(initial.codigoConfiguracao) }
    var horasProximaPreventiva by remember { mutableStateOf(initial.horasProximaPreventiva) }

    val salvarHabilitado = clienteId.isNotBlank() && fabricante.isNotBlank() &&
            numeroSerie.isNotBlank() && modelo.isNotBlank() && codigoTinta.isNotBlank() &&
            anoFabricacao.isNotBlank() && identificacao.isNotBlank() &&
            codigoSolvente.isNotBlank() && dataProximaPreventiva.isNotBlank() &&
            codigoConfiguracao.isNotBlank() && horasProximaPreventiva.isNotBlank()

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
                            .menuAnchor(MenuAnchorType.PrimaryEditable),
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
                            .menuAnchor(MenuAnchorType.PrimaryEditable),
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

                // Seção de Manutenção Preventiva com Interface Moderna
                Text(
                    "Manutenção Preventiva",
                    style = MaterialTheme.typography.titleSmall,
                    color = Brand,
                    modifier = Modifier.padding(top = 8.dp)
                )

                ManutencaoPreventivaSection(
                    dataProximaPreventiva = dataProximaPreventiva,
                    horasProximaPreventiva = horasProximaPreventiva,
                    onDataChange = { dataProximaPreventiva = it },
                    onHorasChange = { horasProximaPreventiva = it }
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
                            codigoTinta = codigoTinta.trim(),
                            anoFabricacao = anoFabricacao.trim(),
                            identificacao = identificacao.trim(),
                            codigoSolvente = codigoSolvente.trim(),
                            dataProximaPreventiva = dataProximaPreventiva.trim(),
                            codigoConfiguracao = codigoConfiguracao.trim(),
                            horasProximaPreventiva = horasProximaPreventiva.trim()
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

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Brand,
    unfocusedBorderColor = Color.LightGray
)

@Composable
private fun ManutencaoPreventivaSection(
    dataProximaPreventiva: String,
    horasProximaPreventiva: String,
    onDataChange: (String) -> Unit,
    onHorasChange: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Campo de Data com DatePicker nativo - estilo padronizado
        OutlinedTextField(
            value = dataProximaPreventiva,
            onValueChange = { },
            label = { Text("Data da Próxima Preventiva *") },
            placeholder = { Text("DD/MM/AAAA") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    mostrarDatePicker(context, dataProximaPreventiva) { novaData ->
                        onDataChange(novaData)
                    }
                },
            readOnly = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = Color.LightGray,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = Color.White
            ),
            trailingIcon = {
                IconButton(onClick = {
                    mostrarDatePicker(context, dataProximaPreventiva) { novaData ->
                        onDataChange(novaData)
                    }
                }) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                        contentDescription = "Selecionar data",
                        tint = Brand
                    )
                }
            }
        )

        // Campo de Horas com botões elegantes integrados
        OutlinedTextField(
            value = horasProximaPreventiva,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() }.take(6)
                onHorasChange(filtered)
            },
            label = { Text("Horas até a Próxima Preventiva *") },
            placeholder = { Text("Ex: 1000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors(),
            trailingIcon = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    // Botão +100 moderno e compacto
                    FilledTonalIconButton(
                        onClick = {
                            val atual = horasProximaPreventiva.toIntOrNull() ?: 0
                            onHorasChange((atual + 100).toString())
                        },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Brand.copy(alpha = 0.1f),
                            contentColor = Brand
                        )
                    ) {
                        Text(
                            text = "+100",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp
                        )
                    }

                    // Botão +500 moderno e compacto
                    FilledTonalIconButton(
                        onClick = {
                            val atual = horasProximaPreventiva.toIntOrNull() ?: 0
                            onHorasChange((atual + 500).toString())
                        },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Brand.copy(alpha = 0.15f),
                            contentColor = Brand
                        )
                    ) {
                        Text(
                            text = "+500",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        )
    }
}

// Função auxiliar para mostrar DatePicker nativo do Android
private fun mostrarDatePicker(
    context: android.content.Context,
    dataAtual: String,
    onDataSelecionada: (String) -> Unit
) {
    val calendario = java.util.Calendar.getInstance()

    // Tenta parsear a data atual se existir
    if (dataAtual.isNotBlank()) {
        try {
            val partes = dataAtual.split("/")
            if (partes.size == 3) {
                calendario.set(partes[2].toInt(), partes[1].toInt() - 1, partes[0].toInt())
            }
        } catch (_: Exception) {
            // Mantém data atual do calendário
        }
    }

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, ano, mes, dia ->
            // Formata como DD/MM/YYYY
            val dataFormatada = String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", dia, mes + 1, ano)
            onDataSelecionada(dataFormatada)
        },
        calendario.get(java.util.Calendar.YEAR),
        calendario.get(java.util.Calendar.MONTH),
        calendario.get(java.util.Calendar.DAY_OF_MONTH)
    )

    // Define data mínima como hoje (não permitir datas passadas)
    datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

    datePickerDialog.show()
}

@Composable
private fun ViewMaquinaDialog(
    maquina: MaquinaEntity,
    cliente: Cliente?,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalhes da Máquina") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cliente: ${cliente?.nome ?: "Não encontrado"}")
                Text("Fabricante: ${maquina.fabricante}")
                Text("Modelo: ${maquina.modelo}")
                Text("Número de Série: ${maquina.numeroSerie}")
                Text("Identificação: ${maquina.identificacao}")
                Text("Ano de Fabricação: ${maquina.anoFabricacao}")
                Text("Código da Tinta: ${maquina.codigoTinta}")
                Text("Código do Solvente: ${maquina.codigoSolvente}")
                Text("Código de Configuração: ${maquina.codigoConfiguracao}")
                Text("Próxima Preventiva: ${maquina.dataProximaPreventiva}")
                Text("Horas para Preventiva: ${maquina.horasProximaPreventiva}h")
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                shape = RoundedCornerShape(6.dp)
            ) { Text("Editar") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(6.dp)) {
                    Text("Fechar", color = Brand)
                }
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
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
