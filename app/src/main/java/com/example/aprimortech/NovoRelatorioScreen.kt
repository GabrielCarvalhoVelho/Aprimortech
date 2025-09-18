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
fun NovoRelatorioScreen(navController: NavController, modifier: Modifier = Modifier) {
    var cliente by remember { mutableStateOf("") }
    var dataField by remember { mutableStateOf(TextFieldValue("")) }
    var horarioField by remember { mutableStateOf(TextFieldValue("")) }
    var contato by remember { mutableStateOf("") }
    var setor by remember { mutableStateOf("") }

    // Clientes, contatos e setores fictícios relacionados
    val clientesComContatos = mapOf(
        "Indústrias TechFlow" to listOf("Marina Souza", "João Oliveira", "Carlos Pereira"),
        "Corporação Acme" to listOf("Ana Martins", "Felipe Costa", "Lucas Lima"),
        "Metalúrgica Alfa" to listOf("Paula Reis", "Rodrigo Alves", "Fernanda Santos")
    )

    val clientesComSetores = mapOf(
        "Indústrias TechFlow" to listOf("Financeiro", "Comercial", "Produção"),
        "Corporação Acme" to listOf("Logística", "TI", "RH"),
        "Metalúrgica Alfa" to listOf("Manutenção", "Qualidade", "Engenharia")
    )

    val empresasExemplo = clientesComContatos.keys.toList()

    var expandedCliente by remember { mutableStateOf(false) }
    var expandedContato by remember { mutableStateOf(false) }
    var expandedSetor by remember { mutableStateOf(false) }

    val sugestoesClientes = empresasExemplo.filter { it.contains(cliente, ignoreCase = true) }
    val sugestoesContatos = if (cliente.isNotBlank() && clientesComContatos.containsKey(cliente)) {
        clientesComContatos[cliente]!!.filter { it.contains(contato, ignoreCase = true) }
    } else emptyList()

    val sugestoesSetores = if (cliente.isNotBlank() && clientesComSetores.containsKey(cliente)) {
        clientesComSetores[cliente]!!.filter { it.contains(setor, ignoreCase = true) }
    } else emptyList()

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
                text = "Relatório Técnico",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )

            // Campo Cliente com autocomplete
            InputCard {
                ExposedDropdownMenuBox(
                    expanded = expandedCliente && sugestoesClientes.isNotEmpty(),
                    onExpandedChange = { expandedCliente = it }
                ) {
                    OutlinedTextField(
                        value = cliente,
                        onValueChange = {
                            cliente = it
                            expandedCliente = true
                        },
                        label = { Text("Cliente") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCliente) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = outlinedNoBorder()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCliente && sugestoesClientes.isNotEmpty(),
                        onDismissRequest = { expandedCliente = false }
                    ) {
                        sugestoesClientes.forEach { sugestao ->
                            DropdownMenuItem(
                                text = { Text(sugestao) },
                                onClick = {
                                    cliente = sugestao
                                    expandedCliente = false
                                }
                            )
                        }
                    }
                }
            }

            // Campo Data com formatação dd/MM/yyyy
            InputCard {
                OutlinedTextField(
                    value = dataField,
                    onValueChange = { newValue ->
                        val old = dataField.text
                        val isDeleting = newValue.text.length < old.length

                        val digits = newValue.text.filter { it.isDigit() }.take(8)
                        val builder = StringBuilder()
                        for (i in digits.indices) {
                            builder.append(digits[i])
                            if (!isDeleting && (i == 1 || i == 3)) {
                                builder.append('/')
                            }
                        }
                        val formatted = builder.toString()
                        dataField = TextFieldValue(
                            text = formatted,
                            selection = TextRange(formatted.length)
                        )
                    },
                    label = { Text("Data") },
                    placeholder = { Text("dd/MM/yyyy") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = outlinedNoBorder()
                )
            }

            // Campo Horário com formatação HH:mm
            InputCard {
                OutlinedTextField(
                    value = horarioField,
                    onValueChange = { newValue ->
                        val old = horarioField.text
                        val isDeleting = newValue.text.length < old.length

                        val digits = newValue.text.filter { it.isDigit() }.take(4)
                        val builder = StringBuilder()
                        for (i in digits.indices) {
                            builder.append(digits[i])
                            if (!isDeleting && i == 1) {
                                builder.append(':')
                            }
                        }
                        val formatted = builder.toString()
                        horarioField = TextFieldValue(
                            text = formatted,
                            selection = TextRange(formatted.length)
                        )
                    },
                    label = { Text("Horário") },
                    placeholder = { Text("HH:mm") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = outlinedNoBorder()
                )
            }

            // Campo Contato dependente do Cliente
            InputCard {
                ExposedDropdownMenuBox(
                    expanded = expandedContato && (sugestoesContatos.isNotEmpty() || contato.isNotBlank()),
                    onExpandedChange = { expandedContato = it }
                ) {
                    OutlinedTextField(
                        value = contato,
                        onValueChange = {
                            contato = it
                            expandedContato = true
                        },
                        label = { Text("Contato") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedContato) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = outlinedNoBorder()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedContato && (sugestoesContatos.isNotEmpty() || contato.isNotBlank()),
                        onDismissRequest = { expandedContato = false }
                    ) {
                        sugestoesContatos.forEach { sugestao ->
                            DropdownMenuItem(
                                text = { Text(sugestao) },
                                onClick = {
                                    contato = sugestao
                                    expandedContato = false
                                }
                            )
                        }
                        if (contato.isNotBlank() && sugestoesContatos.none { it.equals(contato, true) }) {
                            DropdownMenuItem(
                                text = { Text("Adicionar \"$contato\"") },
                                onClick = {
                                    // Aqui salvaria no banco vinculado ao cliente
                                    expandedContato = false
                                }
                            )
                        }
                    }
                }
            }

            // Campo Setor dependente do Cliente
            InputCard {
                ExposedDropdownMenuBox(
                    expanded = expandedSetor && (sugestoesSetores.isNotEmpty() || setor.isNotBlank()),
                    onExpandedChange = { expandedSetor = it }
                ) {
                    OutlinedTextField(
                        value = setor,
                        onValueChange = {
                            setor = it
                            expandedSetor = true
                        },
                        label = { Text("Setor") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedSetor) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = outlinedNoBorder()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSetor && (sugestoesSetores.isNotEmpty() || setor.isNotBlank()),
                        onDismissRequest = { expandedSetor = false }
                    ) {
                        sugestoesSetores.forEach { sugestao ->
                            DropdownMenuItem(
                                text = { Text(sugestao) },
                                onClick = {
                                    setor = sugestao
                                    expandedSetor = false
                                }
                            )
                        }
                        if (setor.isNotBlank() && sugestoesSetores.none { it.equals(setor, true) }) {
                            DropdownMenuItem(
                                text = { Text("Adicionar \"$setor\"") },
                                onClick = {
                                    // Aqui salvaria no banco vinculado ao cliente
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
                    Text("Continuar")
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
