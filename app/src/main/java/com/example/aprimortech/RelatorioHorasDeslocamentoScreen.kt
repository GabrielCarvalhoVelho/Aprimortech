package com.example.aprimortech

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme

@Composable
fun RelatorioHorasDeslocamentoScreen(navController: NavController, modifier: Modifier = Modifier) {
    // Estados - Horas
    var valorHora by remember { mutableStateOf("") }
    var inicio by remember { mutableStateOf("") }
    var fim by remember { mutableStateOf("") }
    var totalHoras by remember { mutableStateOf("") }
    var totalHorasValor by remember { mutableStateOf("") }

    // Estados - Deslocamento
    var valorDeslocamento by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }
    var pedagio by remember { mutableStateOf("") }
    var totalDeslocamento by remember { mutableStateOf("") }

    // Cálculo do total do serviço
    val totalServico = (totalHorasValor.toDoubleOrNull() ?: 0.0) +
            (totalDeslocamento.toDoubleOrNull() ?: 0.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Novo Relatório - Etapa 5",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Horas e Deslocamento",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // --- Horas ---
        Text("Horas", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = valorHora,
            onValueChange = { valorHora = it },
            label = { Text("Valor Hora Técnica (R$)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = inicio,
            onValueChange = { inicio = it },
            label = { Text("Início") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fim,
            onValueChange = { fim = it },
            label = { Text("Fim") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = totalHoras,
            onValueChange = { totalHoras = it },
            label = { Text("Total de Horas") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = totalHorasValor,
            onValueChange = { totalHorasValor = it },
            label = { Text("Total R$ (Horas)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Divider()

        // --- Deslocamento ---
        Text("Deslocamento", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = valorDeslocamento,
            onValueChange = { valorDeslocamento = it },
            label = { Text("Valor Deslocamento (R$)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = km,
            onValueChange = { km = it },
            label = { Text("Quantidade de KM") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = pedagio,
            onValueChange = { pedagio = it },
            label = { Text("Valor Pedágios (R$)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = totalDeslocamento,
            onValueChange = { totalDeslocamento = it },
            label = { Text("Total R$ (Deslocamento)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Divider()

        // Total do Serviço
        Text(
            text = "Total do Serviço: R$ %.2f".format(totalServico),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        // Botões navegação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { navController.popBackStack() }) {
                Text("Anterior")
            }
            Button(onClick = {
                // TODO: salvar dados da etapa 5 e ir para a próxima etapa
                // navController.navigate("relatorioEtapa6")
            }) {
                Text("Próximo")
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioHorasDeslocamentoPreview() {
    AprimortechTheme {
        RelatorioHorasDeslocamentoScreen(navController = rememberNavController())
    }
}
