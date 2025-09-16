package com.example.aprimortech

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme

// Modelo simplificado do relatório finalizado
data class RelatorioUiModel(
    val cliente: String,
    val data: String,
    val endereco: String,
    val tecnico: String,
    val descricao: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioFinalizadoScreen(
    navController: NavController,
    relatorio: RelatorioUiModel,
    modifier: Modifier = Modifier
) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Relatório Finalizado",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )
            Text(
                text = "O relatório foi finalizado e salvo no sistema",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            // Card com dados do relatório
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cliente: ${relatorio.cliente}", style = MaterialTheme.typography.bodyLarge)
                    Text("Data: ${relatorio.data}", style = MaterialTheme.typography.bodyLarge)
                    Text("Endereço: ${relatorio.endereco}", style = MaterialTheme.typography.bodyLarge)
                    Text("Técnico: ${relatorio.tecnico}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Descrição do Serviço:", style = MaterialTheme.typography.titleSmall)
                    Text(relatorio.descricao, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botões de ação
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        // TODO: navegação para edição do relatório
                        // navController.navigate("editarRelatorio/${relatorio.id}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A4A5C)
                    ),
                    border = BorderStroke(0.dp, Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text("Editar Relatório")
                }

                OutlinedButton(
                    onClick = {
                        // TODO: lógica para deletar relatório
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(0.dp, Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text("Deletar Relatório")
                }

                Button(
                    onClick = {
                        // TODO: confirmar relatório e encerrar fluxo
                        // navController.navigate("dashboard")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A4A5C),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text("Confirmar Relatório")
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioFinalizadoPreview() {
    AprimortechTheme {
        RelatorioFinalizadoScreen(
            navController = rememberNavController(),
            relatorio = RelatorioUiModel(
                cliente = "Indústrias TechFlow",
                data = "13/09/2025",
                endereco = "Rua das Máquinas, 123",
                tecnico = "Alan Silva",
                descricao = "Manutenção preventiva realizada, troca de filtro e teste de funcionamento."
            )
        )
    }
}
