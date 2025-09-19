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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoRelatorioScreen(
    navController: NavController,
    relatorio: RelatorioUiModel? = null,
    modifier: Modifier = Modifier
) {
    // Mock de dados
    val clientesMock = remember {
        listOf(
            "AprimorTech S.A.",
            "TechFlow Indústrias",
            "Metalúrgica Orion",
            "Grupo Valente",
            "Fábrica Nova Era",
            "AutoLine Sistemas",
            "Omega Processos"
        )
    }
    val contatosPorCliente = remember {
        mapOf(
            "AprimorTech S.A." to listOf("Marina Souza", "Carlos Lima", "Renata Alves"),
            "TechFlow Indústrias" to listOf("João Pedro", "Luana Silva"),
            "Metalúrgica Orion" to listOf("Ana Clara", "Roberto Teixeira"),
            "Grupo Valente" to listOf("Paula Rocha"),
            "Fábrica Nova Era" to listOf("Tiago Martins", "Beatriz Prado"),
            "AutoLine Sistemas" to listOf("Gustavo Nunes"),
            "Omega Processos" to listOf("Cecília Ramos", "Pedro Henrique")
        )
    }
    val setoresPorContato = remember {
        mapOf(
            "Marina Souza" to listOf("Financeiro", "Compras", "Produção"),
            "Carlos Lima" to listOf("Manutenção", "Logística"),
            "Renata Alves" to listOf("TI", "Qualidade"),
            "João Pedro" to listOf("Manutenção"),
            "Luana Silva" to listOf("Operações"),
            "Ana Clara" to listOf("Produção"),
            "Roberto Teixeira" to listOf("Suprimentos"),
            "Paula Rocha" to listOf("Financeiro"),
            "Tiago Martins" to listOf("Produção"),
            "Beatriz Prado" to listOf("RH"),
            "Gustavo Nunes" to listOf("Logística"),
            "Cecília Ramos" to listOf("Engenharia"),
            "Pedro Henrique" to listOf("Comercial")
        )
    }

    // Estados iniciais
    var cliente by remember { mutableStateOf(relatorio?.cliente ?: "") }
    var dataField by remember { mutableStateOf(TextFieldValue(relatorio?.data ?: "")) }
    var horarioField by remember { mutableStateOf(TextFieldValue(relatorio?.horasTrabalhadas ?: "")) }
    var contato by remember { mutableStateOf(relatorio?.contato ?: "") }
    var setor by remember { mutableStateOf(relatorio?.setor ?: "") }

    // Estados de dropdown
    var expandedCliente by remember { mutableStateOf(false) }
    var expandedContato by remember { mutableStateOf(false) }
    var expandedSetor by remember { mutableStateOf(false) }

    // Sugestões dinâmicas
    val sugestoesCliente = remember(cliente) {
        if (cliente.isBlank()) emptyList()
        else clientesMock.filter { it.contains(cliente, ignoreCase = true) }
    }
    val baseContatos = contatosPorCliente[cliente] ?: emptyList()
    val sugestoesContato = remember(contato, cliente) {
        if (contato.isBlank()) baseContatos
        else baseContatos.filter { it.contains(contato, ignoreCase = true) }
    }
    val baseSetores = setoresPorContato[contato] ?: emptyList()
    val sugestoesSetor = remember(setor, contato) {
        if (setor.isBlank()) baseSetores
        else baseSetores.filter { it.contains(setor, ignoreCase = true) }
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
            Text(
                text = if (relatorio == null) "Novo Relatório Técnico" else "Editar Relatório Técnico",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )

            // CLIENTE
            InputCard {
                ExposedDropdownMenuBox(
                    expanded = expandedCliente && sugestoesCliente.isNotEmpty(),
                    onExpandedChange = { expandedCliente = it }
                ) {
                    OutlinedTextField(
                        value = cliente,
                        onValueChange = {
                            cliente = it.trimStart()
                            expandedCliente = it.isNotBlank()
                            contato = ""
                            setor = ""
                            expandedContato = false
                            expandedSetor = false
                        },
                        label = { Text("Cliente") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCliente) },
                        colors = outlinedNoBorder()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCliente && sugestoesCliente.isNotEmpty(),
                        onDismissRequest = { expandedCliente = false }
                    ) {
                        sugestoesCliente.forEach { opc ->
                            DropdownMenuItem(
                                text = { Text(opc) },
                                onClick = {
                                    cliente = opc
                                    expandedCliente = false
                                }
                            )
                        }
                    }
                }
            }

            // DATA
            InputCard {
                OutlinedTextField(
                    value = dataField,
                    onValueChange = { dataField = formatDateValueNovoRelatorio(it) },
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = outlinedNoBorder()
                )
            }

            // HORÁRIO
            InputCard {
                OutlinedTextField(
                    value = horarioField,
                    onValueChange = { horarioField = formatHoraValueNovoRelatorio(it) },
                    label = { Text("Horário") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = outlinedNoBorder()
                )
            }

            // CONTATO
            InputCard {
                ExposedDropdownMenuBox(
                    expanded = expandedContato && sugestoesContato.isNotEmpty(),
                    onExpandedChange = { if (cliente.isNotBlank()) expandedContato = it }
                ) {
                    OutlinedTextField(
                        value = contato,
                        onValueChange = {
                            contato = it.trimStart()
                            expandedContato = cliente.isNotBlank() && it.isNotEmpty()
                            setor = ""
                            expandedSetor = false
                        },
                        label = { Text("Contato") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        enabled = cliente.isNotBlank(),
                        singleLine = true,
                        trailingIcon = {
                            if (cliente.isNotBlank())
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedContato)
                        },
                        colors = outlinedNoBorder()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedContato && sugestoesContato.isNotEmpty(),
                        onDismissRequest = { expandedContato = false }
                    ) {
                        sugestoesContato.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    contato = c
                                    expandedContato = false
                                }
                            )
                        }
                    }
                }
            }

            // SETOR
            InputCard {
                ExposedDropdownMenuBox(
                    expanded = expandedSetor && sugestoesSetor.isNotEmpty(),
                    onExpandedChange = { if (contato.isNotBlank()) expandedSetor = it }
                ) {
                    OutlinedTextField(
                        value = setor,
                        onValueChange = {
                            setor = it.trimStart()
                            expandedSetor = contato.isNotBlank() && it.isNotEmpty()
                        },
                        label = { Text("Setor") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        enabled = contato.isNotBlank(),
                        singleLine = true,
                        trailingIcon = {
                            if (contato.isNotBlank())
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSetor)
                        },
                        colors = outlinedNoBorder()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSetor && sugestoesSetor.isNotEmpty(),
                        onDismissRequest = { expandedSetor = false }
                    ) {
                        sugestoesSetor.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    setor = s
                                    expandedSetor = false
                                }
                            )
                        }
                    }
                }
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
                    elevation = ButtonDefaults.buttonElevation(3.dp)
                ) { Text("Cancelar") }

                Button(
                    onClick = {
                        val novoOuEditado = relatorio?.copy(
                            cliente = cliente,
                            data = dataField.text,
                            horasTrabalhadas = horarioField.text,
                            contato = contato,
                            setor = setor
                        ) ?: RelatorioUiModel(
                            id = 999,
                            cliente = cliente,
                            data = dataField.text,
                            endereco = "Endereço Exemplo",
                            tecnico = "Técnico Exemplo",
                            setor = setor,
                            contato = contato,
                            equipamento = "Equipamento Exemplo",
                            pecasUtilizadas = "Nenhuma",
                            horasTrabalhadas = horarioField.text,
                            deslocamento = "0km • R$ 0,00",
                            descricao = "Descrição do serviço"
                        )

                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("relatorioAtual", novoOuEditado)

                        navController.navigate("relatorioEtapa2")
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A4A5C),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(3.dp)
                ) {
                    Text(if (relatorio == null) "Continuar" else "Salvar Alterações")
                }
            }
        }
    }
}

// Funções utilitárias de formatação
fun formatDateValueNovoRelatorio(input: TextFieldValue): TextFieldValue {
    val digits = input.text.filter { it.isDigit() }.take(8)
    val sb = StringBuilder()
    for (i in digits.indices) {
        sb.append(digits[i])
        if ((i == 1 && digits.length > 2) || (i == 3 && digits.length > 4)) sb.append('/')
    }
    val formatted = sb.toString()
    return TextFieldValue(formatted, TextRange(formatted.length))
}

fun formatHoraValueNovoRelatorio(input: TextFieldValue): TextFieldValue {
    val digits = input.text.filter { it.isDigit() }.take(4)
    val sb = StringBuilder()
    for (i in digits.indices) {
        sb.append(digits[i])
        if (i == 1 && digits.length > 2) sb.append(':')
    }
    val formatted = sb.toString()
    return TextFieldValue(formatted, TextRange(formatted.length))
}

// Helpers visuais
@Composable
fun InputCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) { Box(Modifier.padding(2.dp)) { content() } }
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
    AprimortechTheme { NovoRelatorioScreen(navController = rememberNavController()) }
}
