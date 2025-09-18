package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.BorderStroke
import com.example.aprimortech.ui.theme.AprimortechTheme

private val Brand = Color(0xFF1A4A5C)

data class MaquinaUiModel(
    val id: Int,
    var fabricante: String,
    var codigoConfiguracao: String,
    var numeroSerie: String,
    var modelo: String,
    var codigoTinta: String,
    var anoFabricacao: String,
    var identificacao: String,
    var codigoSolvente: String,
    var dataProximaPreventiva: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaquinasScreen(navController: NavController, modifier: Modifier = Modifier) {
    var maquinas by remember {
        mutableStateOf(
            listOf(
                MaquinaUiModel(1, "Hitachi", "CFG123", "SN001", "UX-D160W", "T123", "2020", "Máquina Principal", "S001", "2025-10-10"),
                MaquinaUiModel(2, "Videojet", "CFG456", "SN002", "1550", "T456", "2019", "Linha 2", "S002", "2025-11-20")
            )
        )
    }
    var idCounter by remember { mutableIntStateOf(3) }

    var query by remember { mutableStateOf("") }
    val listaFiltrada = remember(maquinas, query) {
        if (query.isBlank()) maquinas else maquinas.filter { it.modelo.contains(query, ignoreCase = true) }
    }

    var showAddEdit by remember { mutableStateOf(false) }
    var editingMaquina by remember { mutableStateOf<MaquinaUiModel?>(null) }

    var showDelete by remember { mutableStateOf(false) }
    var deletingMaquina by remember { mutableStateOf<MaquinaUiModel?>(null) }

    var showView by remember { mutableStateOf(false) }
    var viewingMaquina by remember { mutableStateOf<MaquinaUiModel?>(null) }

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
                        label = { Text("Buscar por modelo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )

                    Button(
                        onClick = {
                            editingMaquina = MaquinaUiModel(
                                id = idCounter,
                                fabricante = "",
                                codigoConfiguracao = "",
                                numeroSerie = "",
                                modelo = "",
                                codigoTinta = "",
                                anoFabricacao = "",
                                identificacao = "",
                                codigoSolvente = "",
                                dataProximaPreventiva = ""
                            )
                            showAddEdit = true
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) { Text("Adicionar Máquina") }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(listaFiltrada, key = { it.id }) { maq ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${maq.fabricante} • ${maq.modelo}", style = MaterialTheme.typography.titleMedium, color = Brand)
                            Spacer(Modifier.height(4.dp))
                            Text("Nº Série: ${maq.numeroSerie}", style = MaterialTheme.typography.bodySmall)
                            Text("Ano: ${maq.anoFabricacao}", style = MaterialTheme.typography.bodySmall)
                            Text("Próx. Preventiva: ${maq.dataProximaPreventiva}", style = MaterialTheme.typography.bodySmall)

                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = {
                                    viewingMaquina = maq
                                    showView = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Brand)
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
            onDismiss = { showAddEdit = false; editingMaquina = null },
            onConfirm = { updated ->
                maquinas = if (maquinas.any { it.id == updated.id }) {
                    maquinas.map { if (it.id == updated.id) updated else it }
                } else {
                    maquinas + updated.also { idCounter += 1 }
                }
                showAddEdit = false
                editingMaquina = null
            }
        )
    }

    if (showDelete && deletingMaquina != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false; deletingMaquina = null },
            title = { Text("Excluir máquina") },
            text = { Text("Tem certeza que deseja excluir \"${deletingMaquina!!.modelo}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        maquinas = maquinas.filterNot { it.id == deletingMaquina!!.id }
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
    initial: MaquinaUiModel,
    onDismiss: () -> Unit,
    onConfirm: (MaquinaUiModel) -> Unit
) {
    var fabricante by remember { mutableStateOf(initial.fabricante) }
    var codigoConfiguracao by remember { mutableStateOf(initial.codigoConfiguracao) }
    var numeroSerie by remember { mutableStateOf(initial.numeroSerie) }
    var modelo by remember { mutableStateOf(initial.modelo) }
    var codigoTinta by remember { mutableStateOf(initial.codigoTinta) }
    var anoFabricacao by remember { mutableStateOf(initial.anoFabricacao) }
    var identificacao by remember { mutableStateOf(initial.identificacao) }
    var codigoSolvente by remember { mutableStateOf(initial.codigoSolvente) }
    var dataProximaPreventiva by remember { mutableStateOf(initial.dataProximaPreventiva) }

    val salvarHabilitado = fabricante.isNotBlank() && codigoConfiguracao.isNotBlank() &&
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
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = fabricante, onValueChange = { fabricante = it }, label = { Text("Fabricante") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())
                OutlinedTextField(value = codigoConfiguracao, onValueChange = { codigoConfiguracao = it }, label = { Text("Código de Configuração") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())
                OutlinedTextField(value = numeroSerie, onValueChange = { numeroSerie = it }, label = { Text("Número de Série") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())
                OutlinedTextField(value = modelo, onValueChange = { modelo = it }, label = { Text("Modelo") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())
                OutlinedTextField(value = codigoTinta, onValueChange = { codigoTinta = it }, label = { Text("Código da Tinta") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())
                OutlinedTextField(value = anoFabricacao, onValueChange = { anoFabricacao = it }, label = { Text("Ano de Fabricação") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors())
                OutlinedTextField(value = identificacao, onValueChange = { identificacao = it }, label = { Text("Identificação") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())
                OutlinedTextField(value = codigoSolvente, onValueChange = { codigoSolvente = it }, label = { Text("Código do Solvente") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())
                OutlinedTextField(value = dataProximaPreventiva, onValueChange = { dataProximaPreventiva = it }, label = { Text("Data Próxima Manutenção Preventiva") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        initial.copy(
                            fabricante = fabricante.trim(),
                            codigoConfiguracao = codigoConfiguracao.trim(),
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
    maquina: MaquinaUiModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Máquina") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fabricante: ${maquina.fabricante}")
                Text("Código Configuração: ${maquina.codigoConfiguracao}")
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
