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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.NovoRelatorioViewModel
import com.example.aprimortech.ui.viewmodel.NovoRelatorioViewModelFactory
import com.example.aprimortech.model.Cliente
import com.example.aprimortech.model.Contato
import com.example.aprimortech.model.Setor
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
    var showNovoContatoDialog by remember { mutableStateOf(false) }
    var showNovoSetorDialog by remember { mutableStateOf(false) }

    // Estados do ViewModel - dados reais do Firebase
    val clientes by viewModel.clientes.collectAsState()
    val contatos by viewModel.contatos.collectAsState()
    val setores by viewModel.setores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    // Feedback toast
    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.limparMensagem()
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
                    Text("Cliente", style = MaterialTheme.typography.titleMedium, color = Brand)
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

    // Dialog para novo contato
    if (showNovoContatoDialog && clienteSelecionado != null) {
        NovoContatoDialog(
            clienteId = clienteSelecionado!!.id,
            onDismiss = { showNovoContatoDialog = false },
            onConfirm = { novoContato ->
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
            onConfirm = { novoSetor ->
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

                // Informação sobre campos obrigatórios
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
                enabled = nome.isNotBlank(), // Apenas nome é obrigatório
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
