package com.example.aprimortech

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

@Composable
fun RelatorioFinalizadoScreen(
    navController: NavController,
    relatorio: RelatorioUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Relatório Finalizado",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
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
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar Relatório")
            }

            OutlinedButton(
                onClick = {
                    // TODO: lógica para deletar relatório
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Deletar Relatório")
            }

            Button(
                onClick = {
                    // TODO: confirmar relatório e encerrar fluxo
                    // navController.navigate("dashboard")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Relatório")
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
