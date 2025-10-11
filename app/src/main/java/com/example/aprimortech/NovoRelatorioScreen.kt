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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.aprimortech.ui.viewmodel.NovoRelatorioViewModel
import com.example.aprimortech.ui.viewmodel.NovoRelatorioViewModelFactory
import com.example.aprimortech.ui.viewmodel.ClienteViewModel
import com.example.aprimortech.ui.viewmodel.ClienteViewModelFactory
import com.example.aprimortech.model.Cliente
import com.example.aprimortech.model.Contato
import com.example.aprimortech.model.Setor
import com.example.aprimortech.model.ContatoCliente
import com.example.aprimortech.ui.components.AutoCompleteEnderecoField
import kotlinx.coroutines.delay
import java.util.UUID
import android.widget.Toast
import androidx.compose.material3.MenuAnchorType

private val Brand = Color(0xFF1A4A5C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoRelatorioScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: NovoRelatorioViewModel = viewModel(
        factory = NovoRelatorioViewModelFactory(
            clienteRepository = (LocalContext.current.applicationContext as AprimortechApplication).clienteRepository,
            contatoRepository = (LocalContext.current.applicationContext as AprimortechApplication).contatoRepository,
            setorRepository = (LocalContext.current.applicationContext as AprimortechApplication).setorRepository
        )
    ),
    clienteViewModel: ClienteViewModel = viewModel(
        factory = ClienteViewModelFactory(
            buscarClientesUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarClientesUseCase,
            salvarClienteUseCase = (LocalContext.current.applicationContext as AprimortechApplication).salvarClienteUseCase,
            excluirClienteUseCase = (LocalContext.current.applicationContext as AprimortechApplication).excluirClienteUseCase,
            sincronizarClientesUseCase = (LocalContext.current.applicationContext as AprimortechApplication).sincronizarClientesUseCase
        )
    )
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Estados do formulário - Página 1: Cliente, Contatos e Setor
    var clienteSelecionado by remember { mutableStateOf<Cliente?>(null) }
    var contatoSelecionado by remember { mutableStateOf<Contato?>(null) }
    var setorSelecionado by remember { mutableStateOf<Setor?>(null) }

    // Estados para busca e autocomplete
    var clienteBusca by remember { mutableStateOf("") }
    var debouncedClienteBusca by remember { mutableStateOf("") }

    // Estados para dialogs
    var showNovoClienteDialog by remember { mutableStateOf(false) }
    var showNovoContatoDialog by remember { mutableStateOf(false) }
    var showNovoSetorDialog by remember { mutableStateOf(false) }

    // Estados do ViewModel - dados reais do Firebase
    val clientes by viewModel.clientes.collectAsState()
    val contatos by viewModel.contatos.collectAsState()
    val setores by viewModel.setores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    // Observar mensagem do ClienteViewModel para feedback de criação de cliente
    val mensagemCliente by clienteViewModel.mensagemOperacao.collectAsState()

    // Feedback toast
    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.limparMensagem()
        }
    }

    // Feedback toast para criação de cliente
    LaunchedEffect(mensagemCliente) {
        mensagemCliente?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            clienteViewModel.limparMensagem()
        }
    }

    // Debounce para busca de cliente
    LaunchedEffect(clienteBusca) {
        delay(300)
        debouncedClienteBusca = clienteBusca
    }

    // Filtrar clientes baseado na busca - agora usando dados reais do Firebase
    val clientesFiltrados = remember(debouncedClienteBusca, clientes) {
        if (debouncedClienteBusca.isBlank()) clientes.take(5)
        else clientes.filter {
            it.nome.contains(debouncedClienteBusca, ignoreCase = true) ||
            it.cnpjCpf.contains(debouncedClienteBusca, ignoreCase = true)
        }.take(5)
    }

    // Carregar contatos e setores quando cliente muda
    LaunchedEffect(clienteSelecionado) {
        if (clienteSelecionado != null) {
            viewModel.carregarContatosPorCliente(clienteSelecionado!!.id)
            viewModel.carregarSetoresPorCliente(clienteSelecionado!!.id)
        }
        contatoSelecionado = null
        setorSelecionado = null
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
            Text("Novo Relatório", style = MaterialTheme.typography.headlineMedium, color = Brand)
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

                // SEÇÃO CLIENTE
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cliente", style = MaterialTheme.typography.titleMedium, color = Brand)
                        IconButton(onClick = { showNovoClienteDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Novo Cliente", tint = Brand)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    var clienteExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = clienteExpanded && clientesFiltrados.isNotEmpty(),
                        onExpandedChange = { expanded ->
                            clienteExpanded = expanded && clientesFiltrados.isNotEmpty()
                        }
                    ) {
                        OutlinedTextField(
                            value = clienteSelecionado?.nome ?: clienteBusca,
                            onValueChange = { newValue ->
                                if (clienteSelecionado == null) {
                                    clienteBusca = newValue
                                    clienteExpanded = newValue.isNotEmpty()
                                } else {
                                    clienteSelecionado = null
                                    clienteBusca = newValue
                                    clienteExpanded = newValue.isNotEmpty()
                                }
                            },
                            label = { Text("Buscar Cliente *") },
                            placeholder = { Text("Digite o nome ou CNPJ do cliente") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable),
                            colors = textFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { clienteExpanded = false }
                            ),
                            singleLine = true
                        )

                        if (clientesFiltrados.isNotEmpty() && clienteExpanded) {
                            ExposedDropdownMenu(
                                expanded = true,
                                onDismissRequest = { clienteExpanded = false }
                            ) {
                                clientesFiltrados.forEach { cliente ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(cliente.nome, style = MaterialTheme.typography.bodyMedium)
                                                Text("${cliente.cnpjCpf} - ${cliente.cidade}/${cliente.estado}",
                                                     style = MaterialTheme.typography.bodySmall,
                                                     color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            clienteSelecionado = cliente
                                            clienteBusca = ""
                                            clienteExpanded = false
                                            keyboardController?.hide()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (clienteSelecionado != null) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("✓ Cliente Selecionado", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
                                Text(clienteSelecionado!!.nome, style = MaterialTheme.typography.bodyMedium)
                                Text("${clienteSelecionado!!.cnpjCpf} - ${clienteSelecionado!!.cidade}/${clienteSelecionado!!.estado}",
                                     style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }

                // SEÇÃO CONTATOS
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contato", style = MaterialTheme.typography.titleMedium, color = Brand)
                        if (clienteSelecionado != null) {
                            IconButton(onClick = { showNovoContatoDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Novo Contato", tint = Brand)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    if (clienteSelecionado == null) {
                        Text("Selecione um cliente primeiro", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    } else {
                        if (contatos.isEmpty()) {
                            Text("Nenhum contato cadastrado para este cliente", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        } else {
                            var contatoExpanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = contatoExpanded,
                                onExpandedChange = { contatoExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = contatoSelecionado?.nome ?: "",
                                    onValueChange = { },
                                    label = { Text("Selecionar Contato") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                    colors = textFieldColors(),
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contatoExpanded) }
                                )

                                ExposedDropdownMenu(
                                    expanded = contatoExpanded,
                                    onDismissRequest = { contatoExpanded = false }
                                ) {
                                    contatos.forEach { contato ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(contato.nome, style = MaterialTheme.typography.bodyMedium)
                                                    Text("${contato.cargo} - ${contato.telefone}",
                                                         style = MaterialTheme.typography.bodySmall,
                                                         color = Color.Gray)
                                                }
                                            },
                                            onClick = {
                                                contatoSelecionado = contato
                                                contatoExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (contatoSelecionado != null) {
                            Spacer(Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("✓ Contato Selecionado", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
                                    Text(contatoSelecionado!!.nome, style = MaterialTheme.typography.bodyMedium)
                                    Text("${contatoSelecionado!!.cargo} - ${contatoSelecionado!!.telefone}",
                                         style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // SEÇÃO SETOR
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Setor", style = MaterialTheme.typography.titleMedium, color = Brand)
                        if (clienteSelecionado != null) {
                            IconButton(onClick = { showNovoSetorDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Novo Setor", tint = Brand)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    if (clienteSelecionado == null) {
                        Text("Selecione um cliente primeiro", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    } else {
                        if (setores.isEmpty()) {
                            Text("Nenhum setor cadastrado para este cliente", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        } else {
                            var setorExpanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = setorExpanded,
                                onExpandedChange = { setorExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = setorSelecionado?.nome ?: "",
                                    onValueChange = { },
                                    label = { Text("Selecionar Setor") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                    colors = textFieldColors(),
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = setorExpanded) }
                                )

                                ExposedDropdownMenu(
                                    expanded = setorExpanded,
                                    onDismissRequest = { setorExpanded = false }
                                ) {
                                    setores.forEach { setor ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(setor.nome, style = MaterialTheme.typography.bodyMedium)
                                                    Text(setor.descricao,
                                                         style = MaterialTheme.typography.bodySmall,
                                                         color = Color.Gray)
                                                }
                                            },
                                            onClick = {
                                                setorSelecionado = setor
                                                setorExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (setorSelecionado != null) {
                            Spacer(Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("✓ Setor Selecionado", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
                                    Text(setorSelecionado!!.nome, style = MaterialTheme.typography.bodyMedium)
                                    Text(setorSelecionado!!.descricao,
                                         style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // BOTÃO CONTINUAR
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Navegar para a página de Dados do Equipamento
                        navController.navigate("dadosEquipamento/${clienteSelecionado!!.id}/${contatoSelecionado!!.id}/${setorSelecionado!!.id}")
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = clienteSelecionado != null && contatoSelecionado != null && setorSelecionado != null,
                    colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continuar", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Dialog para novo cliente - usando o modal completo do ClientesScreen
    if (showNovoClienteDialog) {
        AddEditClienteDialog(
            initial = Cliente(
                id = UUID.randomUUID().toString(),
                nome = "",
                cnpjCpf = "",
                contatos = emptyList(),
                endereco = "",
                cidade = "",
                estado = "",
                telefone = "",
                celular = "",
                latitude = null,
                longitude = null
            ),
            onDismiss = { showNovoClienteDialog = false },
            onConfirm = { novoCliente ->
                clienteViewModel.salvarCliente(novoCliente)
                clienteSelecionado = novoCliente
                showNovoClienteDialog = false
            }
        )
    }

    // Dialog para novo contato
    if (showNovoContatoDialog && clienteSelecionado != null) {
        NovoContatoDialog(
            clienteId = clienteSelecionado!!.id,
            onDismiss = { showNovoContatoDialog = false },
            onConfirm = { novoContato: Contato ->
                viewModel.salvarContato(novoContato)
                contatoSelecionado = novoContato
                showNovoContatoDialog = false
            }
        )
    }

    // Dialog para novo setor
    if (showNovoSetorDialog && clienteSelecionado != null) {
        NovoSetorDialog(
            clienteId = clienteSelecionado!!.id,
            onDismiss = { showNovoSetorDialog = false },
            onConfirm = { novoSetor: Setor ->
                viewModel.salvarSetor(novoSetor)
                setorSelecionado = novoSetor
                showNovoSetorDialog = false
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
    focusedBorderColor = Color.LightGray,
    unfocusedBorderColor = Color.LightGray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovoContatoDialog(
    clienteId: String,
    onDismiss: () -> Unit,
    onConfirm: (Contato) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cargo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Contato") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome *") },
                    placeholder = { Text("Nome do contato") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("Telefone") },
                    placeholder = { Text("(11) 99999-9999") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    placeholder = { Text("contato@empresa.com") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = cargo,
                    onValueChange = { cargo = it },
                    label = { Text("Cargo") },
                    placeholder = { Text("Gerente, Técnico, Supervisor...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Text(
                    "* Apenas o nome é obrigatório",
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
                        Contato(
                            id = UUID.randomUUID().toString(),
                            clienteId = clienteId,
                            nome = nome.trim(),
                            telefone = telefone.trim(),
                            email = email.trim(),
                            cargo = cargo.trim()
                        )
                    )
                },
                enabled = nome.isNotBlank(),
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
private fun NovoSetorDialog(
    clienteId: String,
    onDismiss: () -> Unit,
    onConfirm: (Setor) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Setor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do Setor *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        Setor(
                            id = UUID.randomUUID().toString(),
                            clienteId = clienteId,
                            nome = nome,
                            descricao = descricao
                        )
                    )
                },
                enabled = nome.isNotBlank(),
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

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun NovoRelatorioScreenPreview() {
    AprimortechTheme {
        NovoRelatorioScreen(navController = rememberNavController())
    }
}

// Modal completo de adicionar/editar cliente (reutilizado do ClientesScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditClienteDialog(
    initial: Cliente,
    onDismiss: () -> Unit,
    onConfirm: (Cliente) -> Unit
) {
    var nome by remember { mutableStateOf(initial.nome) }
    var telefone by remember { mutableStateOf(initial.telefone) }
    var celular by remember { mutableStateOf(initial.celular) }
    var contatos by remember { mutableStateOf(initial.contatos) }
    var endereco by remember { mutableStateOf(initial.endereco) }
    var cidade by remember { mutableStateOf(initial.cidade) }
    var estado by remember { mutableStateOf(initial.estado) }
    var latitude by remember { mutableStateOf(initial.latitude) }
    var longitude by remember { mutableStateOf(initial.longitude) }

    var enderecoPreenchido by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var editingContactIndex by remember { mutableStateOf<Int?>(null) }

    val salvarHabilitado = nome.isNotBlank() && endereco.isNotBlank() && cidade.isNotBlank() && estado.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.nome.isBlank()) "Novo Cliente" else "Editar Cliente") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                AutoCompleteEnderecoField(
                    endereco = endereco,
                    onEnderecoChange = { enderecoCompleto ->
                        endereco = enderecoCompleto.endereco
                        // Somente atualizar cidade e estado se vieram preenchidos do Google Places
                        if (enderecoCompleto.cidade.isNotBlank()) {
                            cidade = enderecoCompleto.cidade
                        }
                        if (enderecoCompleto.estado.isNotBlank()) {
                            estado = enderecoCompleto.estado
                        }
                        if (enderecoCompleto.latitude != null) {
                            latitude = enderecoCompleto.latitude
                        }
                        if (enderecoCompleto.longitude != null) {
                            longitude = enderecoCompleto.longitude
                        }
                        // Marcar que o endereço foi preenchido pelo Google Places
                        enderecoPreenchido = enderecoCompleto.cidade.isNotBlank() && enderecoCompleto.estado.isNotBlank()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Digite o nome da rua ou avenida"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cidade,
                        onValueChange = { cidade = it },
                        label = { Text("Cidade *") },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors(),
                        enabled = !enderecoPreenchido // Bloqueia se veio do Google Places
                    )
                    OutlinedTextField(
                        value = estado,
                        onValueChange = { estado = it },
                        label = { Text("Estado *") },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors(),
                        enabled = !enderecoPreenchido // Bloqueia se veio do Google Places
                    )
                }
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("Telefone do cliente (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = celular,
                    onValueChange = { celular = it },
                    label = { Text("Celular do cliente (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = textFieldColors()
                )

                // Nova seção de contatos simplificada
                ContatosSectionSimplificada(
                    contatos = contatos,
                    onAddClick = {
                        editingContactIndex = null
                        showAddContactDialog = true
                    },
                    onEditContact = { index ->
                        editingContactIndex = index
                        showAddContactDialog = true
                    },
                    onRemoveContact = { index ->
                        contatos = contatos.toMutableList().apply { removeAt(index) }
                    }
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
                            nome = nome.trim(),
                            telefone = telefone.trim(),
                            celular = celular.trim(),
                            contatos = contatos,
                            endereco = endereco.trim(),
                            cidade = cidade.trim(),
                            estado = estado.trim(),
                            latitude = latitude,
                            longitude = longitude
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

    // Modal para adicionar ou editar contato
    if (showAddContactDialog) {
        val contatoParaEditar = editingContactIndex?.let { contatos.getOrNull(it) }
        AddEditContatoDialog(
            contatoInicial = contatoParaEditar,
            onDismiss = {
                showAddContactDialog = false
                editingContactIndex = null
            },
            onConfirm = { novoContato ->
                contatos = if (editingContactIndex != null) {
                    // Editando contato existente
                    contatos.toMutableList().apply {
                        set(editingContactIndex!!, novoContato)
                    }
                } else {
                    // Adicionando novo contato
                    contatos + novoContato
                }
                showAddContactDialog = false
                editingContactIndex = null
            }
        )
    }
}

// Nova seção de contatos simplificada
@Composable
private fun ContatosSectionSimplificada(
    contatos: List<ContatoCliente>,
    onAddClick: () -> Unit,
    onEditContact: (Int) -> Unit,
    onRemoveContact: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Contatos do cliente",
                style = MaterialTheme.typography.titleSmall,
                color = Brand
            )
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Adicionar Contato",
                    tint = Brand,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (contatos.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    contatos.forEachIndexed { index, contato ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    contato.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2C3E50)
                                )
                                if (!contato.setor.isNullOrBlank()) {
                                    Text(
                                        "Setor: ${contato.setor}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                if (!contato.celular.isNullOrBlank()) {
                                    Text(
                                        "Celular: ${contato.celular}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Row {
                                IconButton(
                                    onClick = { onEditContact(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Editar Contato",
                                        tint = Brand,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onRemoveContact(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remover Contato",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        if (index < contatos.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = Color.LightGray.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                "Nenhum contato adicionado",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

// Novo modal dedicado para adicionar/editar contatos do cliente
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditContatoDialog(
    contatoInicial: ContatoCliente?,
    onDismiss: () -> Unit,
    onConfirm: (ContatoCliente) -> Unit
) {
    var nome by remember { mutableStateOf(contatoInicial?.nome ?: "") }
    var setor by remember { mutableStateOf(contatoInicial?.setor ?: "") }
    var celular by remember { mutableStateOf(contatoInicial?.celular ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contatoInicial == null) "Novo Contato" else "Editar Contato") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do contato *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = setor,
                    onValueChange = { setor = it },
                    label = { Text("Setor (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = celular,
                    onValueChange = { celular = it },
                    label = { Text("Celular (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = textFieldColors(),
                    singleLine = true
                )
                Text(
                    "* Campos obrigatórios",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nome.isNotBlank()) {
                        onConfirm(
                            ContatoCliente(
                                nome = nome.trim(),
                                setor = setor.trim().ifBlank { null },
                                celular = celular.trim().ifBlank { null }
                            )
                        )
                    }
                },
                enabled = nome.isNotBlank(),
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
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Cancelar", color = Brand)
            }
        }
    )
}
