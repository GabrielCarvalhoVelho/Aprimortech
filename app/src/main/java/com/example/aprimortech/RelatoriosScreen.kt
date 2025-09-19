package com.example.aprimortech

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme

data class RelatorioListItem(
    val id: Int,
    val cliente: String,
    val data: String,
    val titulo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatoriosScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var relatorios by remember {
        mutableStateOf(
            listOf(
                RelatorioListItem(1, "Indústrias TechFlow", "12/09/2025", "Manutenção Preventiva - Dobradeira"),
                RelatorioListItem(2, "Corporação Acme", "11/09/2025", "Reparo Emergencial - Torno #3"),
                RelatorioListItem(3, "AgroMáquinas LTDA", "10/09/2025", "Instalação - Colheitadeira X200"),
                RelatorioListItem(4, "Construtora Atlas", "09/09/2025", "Vistoria - Grua Principal"),
                RelatorioListItem(5, "Indústrias TechFlow", "08/09/2025", "Troca de Peças - Dobradeira"),
                RelatorioListItem(6, "Corporação Acme", "07/09/2025", "Ajustes - Fresadora CNC #1"),
                RelatorioListItem(7, "AgroMáquinas LTDA", "06/09/2025", "Configuração - Plantadeira"),
                RelatorioListItem(8, "Construtora Atlas", "05/09/2025", "Inspeção - Betoneira #2"),
                RelatorioListItem(9, "Indústrias TechFlow", "04/09/2025", "Treinamento - Operadores"),
                RelatorioListItem(10, "Corporação Acme", "03/09/2025", "Verificação - Torno #5")
            )
        )
    }

    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf<RelatorioListItem?>(null) }

    val filteredRelatorios = remember(searchQuery.text, relatorios) {
        if (searchQuery.text.isBlank()) {
            relatorios
        } else {
            relatorios.filter {
                it.cliente.contains(searchQuery.text, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Relatórios") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Digite o nome do cliente") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRelatorios) { relatorio ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(relatorio.titulo, style = MaterialTheme.typography.titleMedium)
                            Text("Cliente: ${relatorio.cliente}", style = MaterialTheme.typography.bodyMedium)
                            Text("Data: ${relatorio.data}", style = MaterialTheme.typography.bodySmall)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // EDITAR
                                OutlinedButton(
                                    onClick = {
                                        val relatorioUiModel = RelatorioUiModel(
                                            id = relatorio.id,
                                            cliente = relatorio.cliente,
                                            data = relatorio.data,
                                            endereco = "Endereço exemplo",
                                            tecnico = "Técnico exemplo",
                                            setor = "Setor exemplo",
                                            contato = "Contato exemplo",
                                            equipamento = "Equipamento exemplo",
                                            pecasUtilizadas = "Peças exemplo",
                                            horasTrabalhadas = "01:00",
                                            deslocamento = "30km • R$ 80,00",
                                            descricao = relatorio.titulo
                                        )

                                        navController.currentBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("relatorioEdit", relatorioUiModel)

                                        navController.navigate("novoRelatorio")
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    Spacer(Modifier.width(4.dp))
                                    Text("Editar")
                                }

                                // EXCLUIR
                                OutlinedButton(
                                    onClick = { showDeleteDialog = relatorio },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir")
                                    Spacer(Modifier.width(4.dp))
                                    Text("Excluir")
                                }

                                // EXPORTAR PDF
                                Button(
                                    onClick = {
                                        runCatching {
                                            val fake = RelatorioUiModel(
                                                id = relatorio.id,
                                                cliente = relatorio.cliente,
                                                data = relatorio.data,
                                                endereco = "Endereço exemplo",
                                                tecnico = "Técnico exemplo",
                                                setor = "Setor exemplo",
                                                contato = "Contato exemplo",
                                                equipamento = "Equipamento exemplo",
                                                pecasUtilizadas = "Peças exemplo",
                                                horasTrabalhadas = "00:45",
                                                deslocamento = "50km • R$100",
                                                descricao = relatorio.titulo
                                            )
                                            val uri = PdfExporter.exportRelatorio(context, fake)
                                            val share = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/pdf"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(share, "Compartilhar PDF"))
                                        }.onFailure {
                                            Toast.makeText(context, "Erro ao exportar PDF", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1A4A5C), // cor principal Aprimortech
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG DE EXCLUSÃO
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Excluir Relatório") },
            text = { Text("Tem certeza que deseja excluir '${showDeleteDialog?.titulo}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        relatorios = relatorios.filterNot { it.id == showDeleteDialog!!.id }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) { Text("Excluir") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = null },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatoriosScreenPreview() {
    AprimortechTheme {
        RelatoriosScreen(navController = rememberNavController())
    }
}
