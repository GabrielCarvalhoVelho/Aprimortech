package com.example.aprimortech

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.model.Relatorio
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AprimortechTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val app = context.applicationContext as AprimortechApplication
    val offlineAuth = app.offlineAuthManager
    val initialRoute by remember {
        mutableStateOf(
            if (offlineAuth.hasOfflineUser() && offlineAuth.isSessionValid()) "dashboard" else "login"
        )
    }
    val navController = rememberNavController()

    // CRIAR UMA ÚNICA INSTÂNCIA DO SHAREDVIEWMODEL PARA TODA A NAVEGAÇÃO
    val sharedViewModel: com.example.aprimortech.ui.viewmodel.RelatorioSharedViewModel = viewModel()

    LaunchedEffect(initialRoute) {
        if (initialRoute == "dashboard") {
            // Renovar a sessão no auto-skip
            offlineAuth.refreshSessionTimestamp()
        }
    }

    NavHost(navController = navController, startDestination = initialRoute) {
        composable("login") { LoginScreen(navController = navController) }
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }
        composable("relatorios") {
            RelatoriosScreen(navController = navController)
        }
        composable("novoRelatorio") {
            NovoRelatorioScreen(navController = navController)
        }
        composable(
            "dadosEquipamento/{clienteId}/{contatoNome}",
            arguments = listOf(
                navArgument("clienteId") { type = NavType.StringType },
                navArgument("contatoNome") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""

            RelatorioEquipamentoScreen(
                navController = navController,
                clienteId = clienteId,
                sharedViewModel = sharedViewModel
            )
        }
        composable(
            "relatorioEtapa2?clienteId={clienteId}",
            arguments = listOf(
                navArgument("clienteId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
            RelatorioEquipamentoScreen(
                navController = navController,
                clienteId = clienteId,
                sharedViewModel = sharedViewModel
            )
        }
        composable(
            "relatorioEtapa3?clienteId={clienteId}",
            arguments = listOf(
                navArgument("clienteId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
            RelatorioDefeitoServicosScreen(
                navController = navController,
                clienteId = clienteId
            )
        }
        composable(
            "relatorioEtapa4?defeitos={defeitos}&servicos={servicos}&observacoes={observacoes}&clienteId={clienteId}",
            arguments = listOf(
                navArgument("defeitos") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("servicos") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("observacoes") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("clienteId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val defeitos = backStackEntry.arguments?.getString("defeitos") ?: ""
            val servicos = backStackEntry.arguments?.getString("servicos") ?: ""
            val observacoes = backStackEntry.arguments?.getString("observacoes") ?: ""
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""

            RelatorioPecasScreen(
                navController = navController,
                defeitos = defeitos,
                servicos = servicos,
                observacoes = observacoes,
                clienteId = clienteId
            )
        }
        composable(
            "relatorioEtapa5?defeitos={defeitos}&servicos={servicos}&observacoes={observacoes}&pecas={pecas}&clienteId={clienteId}",
            arguments = listOf(
                navArgument("defeitos") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("servicos") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("observacoes") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("pecas") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("clienteId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val defeitos = backStackEntry.arguments?.getString("defeitos") ?: ""
            val servicos = backStackEntry.arguments?.getString("servicos") ?: ""
            val observacoes = backStackEntry.arguments?.getString("observacoes") ?: ""
            val pecas = backStackEntry.arguments?.getString("pecas") ?: ""
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""

            RelatorioHorasDeslocamentoScreen(
                navController = navController,
                defeitos = defeitos,
                servicos = servicos,
                observacoes = observacoes,
                pecas = pecas,
                clienteId = clienteId
            )
        }
        composable(
            "relatorioEtapa6?defeitos={defeitos}&servicos={servicos}&observacoes={observacoes}&pecas={pecas}&horas={horas}&clienteId={clienteId}",
            arguments = listOf(
                navArgument("defeitos") { type = NavType.StringType },
                navArgument("servicos") { type = NavType.StringType },
                navArgument("observacoes") { type = NavType.StringType },
                navArgument("pecas") { type = NavType.StringType },
                navArgument("horas") { type = NavType.StringType },
                navArgument("clienteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val defeitos = backStackEntry.arguments?.getString("defeitos") ?: ""
            val servicos = backStackEntry.arguments?.getString("servicos") ?: ""
            val observacoes = backStackEntry.arguments?.getString("observacoes") ?: ""
            val pecas = backStackEntry.arguments?.getString("pecas") ?: ""
            val horasData = backStackEntry.arguments?.getString("horas") ?: ""
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""

            // Parse dos dados de horas
            val horasParts = horasData.split(";")
            val horarioEntrada = if (horasParts.size > 0) horasParts[0] else null
            val horarioSaida = if (horasParts.size > 1) horasParts[1] else null
            val distanciaKm = if (horasParts.size > 2) horasParts[2].toDoubleOrNull() else null
            val valorPorKm = if (horasParts.size > 3) horasParts[3].toDoubleOrNull() else null
            val valorPedagios = if (horasParts.size > 4) horasParts[4].toDoubleOrNull() else null
            val valorDeslocamentoTotal = if (horasParts.size > 5) horasParts[5].toDoubleOrNull() else null

            // ⭐ CORREÇÃO: Construir o RelatorioCompleto ANTES de criar o Relatorio
            // Isso garante que todos os dados do SharedViewModel sejam consolidados
            LaunchedEffect(Unit) {
                sharedViewModel.buildRelatorioCompleto()
            }

            // USAR A INSTÂNCIA COMPARTILHADA DO SHAREDVIEWMODEL
            val relatorioCompleto by sharedViewModel.relatorioCompleto.collectAsState()

            // Debug log
            android.util.Log.d("MainActivity", "=== ETAPA 6 - CRIANDO RELATÓRIO ===")
            android.util.Log.d("MainActivity", "RelatorioCompleto do SharedViewModel: $relatorioCompleto")
            android.util.Log.d("MainActivity", "Código Tinta (SharedViewModel): ${relatorioCompleto?.equipamentoCodigoTinta}")
            android.util.Log.d("MainActivity", "Código Solvente (SharedViewModel): ${relatorioCompleto?.equipamentoCodigoSolvente}")

            // ⭐ IMPORTANTE: Reconstrói o relatório preservando os códigos de tinta e solvente
            // que o usuário preencheu manualmente na tela de equipamento
            val relatorioFinal = Relatorio(
                id = "", // Será gerado quando salvar
                clienteId = clienteId,
                maquinaId = "", // Por enquanto vazio, será necessário adicionar nas etapas anteriores
                pecaIds = pecas.split(",").filter { it.isNotEmpty() },
                descricaoServico = servicos,
                recomendacoes = observacoes,
                horarioEntrada = horarioEntrada,
                horarioSaida = horarioSaida,
                distanciaKm = distanciaKm,
                valorDeslocamentoPorKm = valorPorKm,
                valorDeslocamentoTotal = valorDeslocamentoTotal,
                valorPedagios = valorPedagios,
                observacoes = observacoes,
                // ⭐ CÓDIGOS PREENCHIDOS MANUALMENTE PELO USUÁRIO - Agora virão do RelatorioCompleto
                codigoTinta = relatorioCompleto?.equipamentoCodigoTinta,
                codigoSolvente = relatorioCompleto?.equipamentoCodigoSolvente,
                syncPending = true
            )

            android.util.Log.d("MainActivity", "=== RELATÓRIO FINAL CRIADO ===")
            android.util.Log.d("MainActivity", "Código Tinta (relatorioFinal): ${relatorioFinal.codigoTinta}")
            android.util.Log.d("MainActivity", "Código Solvente (relatorioFinal): ${relatorioFinal.codigoSolvente}")

            // Passa o relatório para a tela de assinaturas COM O SHAREDVIEWMODEL COMPARTILHADO
            RelatorioAssinaturaScreen(
                navController = navController,
                relatorioInicial = relatorioFinal,
                sharedViewModel = sharedViewModel
            )
        }
        composable(
            "relatorioFinalizado?relatorioId={relatorioId}",
            arguments = listOf(
                navArgument("relatorioId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val relatorioId = backStackEntry.arguments?.getString("relatorioId")
            RelatorioFinalizadoScreen(
                navController = navController,
                relatorioId = relatorioId
            )
        }
        // 🚀 NOVAS ROTAS
        composable("clientes") {
            ClientesScreen(navController = navController)
        }
        composable("maquinas") {
            MaquinasScreen(navController = navController)
        }
        composable("pecas") {
            PecasScreen(navController = navController)
        }
        composable("defeitos") {
            DefeitosScreen(navController = navController)
        }
        composable("servicos") {
            ServicosScreen(navController = navController)
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as? AprimortechApplication
    val offlineAuth = app?.offlineAuthManager

    var email by rememberSaveable { mutableStateOf(offlineAuth?.getStoredEmail() ?: "") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var sessionExpired by remember { mutableStateOf(false) }

    val isInPreview = LocalInspectionMode.current
    val auth = if (!isInPreview) FirebaseAuth.getInstance() else null

    fun isOnline(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Checa expiração quando a tela é aberta
    LaunchedEffect(Unit) {
        if (offlineAuth != null && offlineAuth.hasOfflineUser() && !offlineAuth.isSessionValid()) {
            sessionExpired = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_aprimortech),
            contentDescription = "Logo Aprimortech",
            modifier = Modifier
                .size(height = 160.dp, width = 360.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Excelência em codificação industrial",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Ícone de Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !isProcessing
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Ícone de Senha") },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = "Mostrar/Esconder Senha")
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isProcessing
        )

        TextButton(
            onClick = { /* TODO: recuperar senha */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Esqueceu a senha?")
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (sessionExpired) {
            Text(
                text = "Sessão expirada. Conecte-se à internet para renovar o login.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = {
                if (isProcessing) return@Button
                val emailTrim = email.trim()
                val passTrim = password.trim()
                if (emailTrim.isBlank() || passTrim.isBlank()) {
                    Toast.makeText(context, "Por favor, preencha email e senha.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isProcessing = true
                val online = isOnline()
                if (online) {
                    auth?.signInWithEmailAndPassword(emailTrim, passTrim)
                        ?.addOnCompleteListener { task ->
                            isProcessing = false
                            if (task.isSuccessful) {
                                offlineAuth?.saveCredentials(emailTrim, passTrim)
                                Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                val errorMessage = task.exception?.message ?: "Erro desconhecido. Tente novamente."
                                Toast.makeText(context, "Falha no login: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        } ?: run {
                        isProcessing = false
                        Toast.makeText(context, "FirebaseAuth indisponível.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (sessionExpired) {
                        isProcessing = false
                        Toast.makeText(
                            context,
                            "Sessão expirada. Faça login online para renovar.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }
                    val valid = offlineAuth?.validateCredentials(emailTrim, passTrim) ?: false
                    isProcessing = false
                    if (valid) {
                        offlineAuth?.refreshSessionTimestamp()
                        Toast.makeText(context, "Login offline bem-sucedido.", Toast.LENGTH_SHORT).show()
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Credenciais inválidas offline.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A4A5C),
                contentColor = Color.White
            ),
            enabled = !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            } else {
                Text("ENTRAR")
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LoginScreenPreview() {
    AprimortechTheme {
        LoginScreen(navController = rememberNavController())
    }
}
