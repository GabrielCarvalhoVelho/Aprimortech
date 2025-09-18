package com.example.aprimortech

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoRelatorioScreen(
    navController: NavController,
    relatorio: RelatorioUiModel? = null, // se vier nulo = novo relatório, se não = edição
    modifier: Modifier = Modifier
) {
    // Estados iniciais: se for edição, carregam os valores do relatório
    var cliente by remember { mutableStateOf(relatorio?.cliente ?: "") }
    var data by remember { mutableStateOf(relatorio?.data ?: "") }
    var horario by remember { mutableStateOf(relatorio?.horasTrabalhadas ?: "") }
    var contato by remember { mutableStateOf(relatorio?.contato ?: "") }
    var setor by remember { mutableStateOf(relatorio?.setor ?: "") }

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
            // Header dinâmico
            Text(
                text = if (relatorio == null) "Novo Relatório Técnico" else "Editar Relatório Técnico",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )

            // Campos
            InputCard {
                OutlinedTextField(
                    value = cliente,
                    onValueChange = { cliente = it },
                    label = { Text("Cliente") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedNoBorder()
                )
            }

            InputCard {
                OutlinedTextField(
                    value = data,
                    onValueChange = { data = it },
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedNoBorder()
                )
            }

            InputCard {
                OutlinedTextField(
                    value = horario,
                    onValueChange = { horario = it },
                    label = { Text("Horário") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedNoBorder()
                )
            }

            InputCard {
                OutlinedTextField(
                    value = contato,
                    onValueChange = { contato = it },
                    label = { Text("Contato") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedNoBorder()
                )
            }

            InputCard {
                OutlinedTextField(
                    value = setor,
                    onValueChange = { setor = it },
                    label = { Text("Setor") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedNoBorder()
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A4A5C)
                    ),
                    border = BorderStroke(0.dp, Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 4.dp,
                        focusedElevation = 4.dp
                    )
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        // Se for edição, atualiza. Se for novo, cria.
                        val novoOuEditado = relatorio?.copy(
                            cliente = cliente,
                            data = data,
                            horasTrabalhadas = horario,
                            contato = contato,
                            setor = setor
                        ) ?: RelatorioUiModel(
                            id = 999, // mock para protótipo
                            cliente = cliente,
                            data = data,
                            endereco = "Endereço Exemplo",
                            tecnico = "Técnico Exemplo",
                            setor = setor,
                            contato = contato,
                            equipamento = "Equipamento Exemplo",
                            pecasUtilizadas = "Nenhuma",
                            horasTrabalhadas = horario,
                            deslocamento = "0km • R$ 0,00",
                            descricao = "Descrição do serviço"
                        )

                        // Salva o relatório parcial no SavedStateHandle
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("relatorioAtual", novoOuEditado)

                        // Navega para a próxima etapa (dados do equipamento)
                        navController.navigate("relatorioEtapa2")
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A4A5C),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 4.dp,
                        focusedElevation = 4.dp
                    )
                ) {
                    Text(if (relatorio == null) "Continuar" else "Salvar Alterações")
                }
            }
        }
    }
}

@Composable
fun InputCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(2.dp)) {
            content()
        }
    }
}

@Composable
fun outlinedNoBorder() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    errorBorderColor = Color.Transparent
)

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun NovoRelatorioScreenPreview() {
    AprimortechTheme {
        NovoRelatorioScreen(navController = rememberNavController())
    }
}
