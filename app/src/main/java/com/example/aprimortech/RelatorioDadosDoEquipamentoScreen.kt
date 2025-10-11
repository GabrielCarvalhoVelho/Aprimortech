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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.MaquinaViewModel
import com.example.aprimortech.ui.viewmodel.MaquinaViewModelFactory
import com.example.aprimortech.data.local.entity.MaquinaEntity
import androidx.compose.material3.MenuAnchorType
import android.widget.Toast
import java.util.UUID
import java.util.Calendar
import java.util.Locale
import android.app.DatePickerDialog

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
    )
) {
    val context = LocalContext.current

    // Estados do formulário
    var maquinaSelecionada by remember { mutableStateOf<MaquinaEntity?>(null) }
    var codigoTintaSelecionado by remember { mutableStateOf("") }
    var codigoSolventeSelecionado by remember { mutableStateOf("") }
    var dataProximaPreventiva by remember { mutableStateOf("") }
    var horasProximaPreventiva by remember { mutableStateOf("") }

    // Estados para dialogs
    var showNovaMaquinaDialog by remember { mutableStateOf(false) }
    var showNovoCodigoTintaDialog by remember { mutableStateOf(false) }
    var showNovoCodigoSolventeDialog by remember { mutableStateOf(false) }

    // Estados do ViewModel
    val maquinas by viewModel.maquinas.collectAsState()
    val operacaoEmAndamento by viewModel.operacaoEmAndamento.collectAsState()
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    // Filtrar máquinas do cliente atual
    val maquinasDoCliente = remember(maquinas, clienteId) {
        maquinas.filter { it.clienteId == clienteId }
    }

    // Obter códigos únicos de tinta e solvente
    val codigosTintaDisponiveis = remember(maquinas) {
        maquinas.map { it.codigoTinta }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val codigosSolventeDisponiveis = remember(maquinas) {
        maquinas.map { it.codigoSolvente }.filter { it.isNotBlank() }.distinct().sorted()
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
                        IconButton(onClick = { showNovaMaquinaDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Nova Máquina", tint = Brand)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    if (maquinasDoCliente.isEmpty()) {
                        Text("Nenhuma máquina cadastrada para este cliente", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
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
                                            // Preencher automaticamente os campos da máquina
                                            codigoTintaSelecionado = maquina.codigoTinta
                                            codigoSolventeSelecionado = maquina.codigoSolvente
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

                // SEÇÃO CÓDIGO DA TINTA
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Código da Tinta", style = MaterialTheme.typography.titleMedium, color = Brand)
                        IconButton(onClick = { showNovoCodigoTintaDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Novo Código de Tinta", tint = Brand)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    var codigoTintaExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = codigoTintaExpanded,
                        onExpandedChange = { codigoTintaExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = codigoTintaSelecionado,
                            onValueChange = { },
                            label = { Text("Selecionar Código da Tinta *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = textFieldColors(),
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = codigoTintaExpanded) }
                        )

                        if (codigosTintaDisponiveis.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = codigoTintaExpanded,
                                onDismissRequest = { codigoTintaExpanded = false }
                            ) {
                                codigosTintaDisponiveis.forEach { codigo ->
                                    DropdownMenuItem(
                                        text = { Text(codigo) },
                                        onClick = {
                                            codigoTintaSelecionado = codigo
                                            codigoTintaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // SEÇÃO CÓDIGO DO SOLVENTE
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Código do Solvente", style = MaterialTheme.typography.titleMedium, color = Brand)
                        IconButton(onClick = { showNovoCodigoSolventeDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Novo Código de Solvente", tint = Brand)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    var codigoSolventeExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = codigoSolventeExpanded,
                        onExpandedChange = { codigoSolventeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = codigoSolventeSelecionado,
                            onValueChange = { },
                            label = { Text("Selecionar Código do Solvente *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = textFieldColors(),
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = codigoSolventeExpanded) }
                        )

                        if (codigosSolventeDisponiveis.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = codigoSolventeExpanded,
                                onDismissRequest = { codigoSolventeExpanded = false }
                            ) {
                                codigosSolventeDisponiveis.forEach { codigo ->
                                    DropdownMenuItem(
                                        text = { Text(codigo) },
                                        onClick = {
                                            codigoSolventeSelecionado = codigo
                                            codigoSolventeExpanded = false
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

                    // Campo de data com DatePickerDialog
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Abrir o DatePickerDialog
                                val calendar = Calendar.getInstance()

                                // Tentar parsear a data existente se houver
                                if (dataProximaPreventiva.isNotBlank()) {
                                    try {
                                        val parts = dataProximaPreventiva.split("/")
                                        if (parts.size == 3) {
                                            calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                                        }
                                    } catch (_: Exception) {
                                        // Ignorar erro e usar data atual
                                    }
                                }

                                val ano = calendar.get(Calendar.YEAR)
                                val mes = calendar.get(Calendar.MONTH)
                                val dia = calendar.get(Calendar.DAY_OF_MONTH)

                                DatePickerDialog(
                                    context,
                                    { _, selectedYear, selectedMonth, selectedDay ->
                                        // Atualizar o campo de data com a data selecionada
                                        dataProximaPreventiva = String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            selectedDay,
                                            selectedMonth + 1,
                                            selectedYear
                                        )
                                    },
                                    ano, mes, dia
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
                            enabled = false // Desabilitar interação direta com o TextField
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = horasProximaPreventiva,
                        onValueChange = { newValue ->
                            // Aceitar apenas números
                            val filtered = newValue.filter { it.isDigit() }
                            horasProximaPreventiva = filtered
                        },
                        label = { Text("Horas *") },
                        placeholder = { Text("Ex: 500") },
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
                                        horasProximaPreventiva = (atual + 100).toString()
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
                                        horasProximaPreventiva = (atual + 500).toString()
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

                // BOTÃO CONTINUAR
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Navegar para a próxima etapa
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

    // Dialog para nova máquina
    if (showNovaMaquinaDialog) {
        NovaMaquinaDialog(
            clienteId = clienteId,
            onDismiss = { showNovaMaquinaDialog = false },
            onConfirm = { novaMaquina ->
                viewModel.salvarMaquina(novaMaquina)
                maquinaSelecionada = novaMaquina
                codigoTintaSelecionado = novaMaquina.codigoTinta
                codigoSolventeSelecionado = novaMaquina.codigoSolvente
                dataProximaPreventiva = novaMaquina.dataProximaPreventiva
                horasProximaPreventiva = novaMaquina.horasProximaPreventiva
                showNovaMaquinaDialog = false
            }
        )
    }

    // Dialog para novo código de tinta
    if (showNovoCodigoTintaDialog) {
        NovoCodigoDialog(
            titulo = "Novo Código de Tinta",
            label = "Código da Tinta",
            onDismiss = { showNovoCodigoTintaDialog = false },
            onConfirm = { codigo ->
                codigoTintaSelecionado = codigo
                showNovoCodigoTintaDialog = false
            }
        )
    }

    // Dialog para novo código de solvente
    if (showNovoCodigoSolventeDialog) {
        NovoCodigoDialog(
            titulo = "Novo Código de Solvente",
            label = "Código do Solvente",
            onDismiss = { showNovoCodigoSolventeDialog = false },
            onConfirm = { codigo ->
                codigoSolventeSelecionado = codigo
                showNovoCodigoSolventeDialog = false
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
    ) { Column(modifier = Modifier.padding(16.dp), content = content) }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    focusedBorderColor = Color.LightGray,
    unfocusedBorderColor = Color.LightGray,
    disabledBorderColor = Color.LightGray,
    disabledTextColor = Color.Black,
    disabledLabelColor = Color.Gray,
    disabledPlaceholderColor = Color.Gray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaMaquinaDialog(
    clienteId: String,
    onDismiss: () -> Unit,
    onConfirm: (MaquinaEntity) -> Unit
) {
    var fabricante by remember { mutableStateOf("") }
    var numeroSerie by remember { mutableStateOf("") }
    var codigoConfiguracao by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var identificacao by remember { mutableStateOf("") }
    var anoFabricacao by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Máquina") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = fabricante,
                    onValueChange = { fabricante = it },
                    label = { Text("Fabricante *") },
                    placeholder = { Text("Ex: Hitachi, Videojet") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = numeroSerie,
                    onValueChange = { numeroSerie = it.uppercase() },
                    label = { Text("Número de Série *") },
                    placeholder = { Text("Ex: SN001234") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = codigoConfiguracao,
                    onValueChange = { codigoConfiguracao = it.uppercase() },
                    label = { Text("Código de Configuração *") },
                    placeholder = { Text("Ex: CFG001") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = modelo,
                    onValueChange = { modelo = it.uppercase() },
                    label = { Text("Modelo *") },
                    placeholder = { Text("Ex: UX-D160W") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

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
                        MaquinaEntity(
                            id = UUID.randomUUID().toString(),
                            clienteId = clienteId,
                            fabricante = fabricante.trim(),
                            numeroSerie = numeroSerie.trim(),
                            codigoConfiguracao = codigoConfiguracao.trim(),
                            modelo = modelo.trim(),
                            identificacao = identificacao.trim(),
                            anoFabricacao = anoFabricacao.trim(),
                            codigoTinta = "",
                            codigoSolvente = "",
                            dataProximaPreventiva = "",
                            horasProximaPreventiva = "",
                            pendenteSincronizacao = true
                        )
                    )
                },
                enabled = fabricante.isNotBlank() && numeroSerie.isNotBlank() &&
                          codigoConfiguracao.isNotBlank() && modelo.isNotBlank() &&
                          identificacao.isNotBlank() && anoFabricacao.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White)
            ) { Text("Criar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar", color = Brand)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovoCodigoDialog(
    titulo: String,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var codigo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it.uppercase() },
                    label = { Text("$label *") },
                    placeholder = { Text("Ex: T664, INK001") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Text(
                    "* Campo obrigatório",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(codigo.trim()) },
                enabled = codigo.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White)
            ) { Text("Adicionar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar", color = Brand)
            }
        }
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioEquipamentoPreview() {
    AprimortechTheme {
        RelatorioEquipamentoScreen(navController = rememberNavController())
    }
}
