package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.RelatorioViewModel
import com.example.aprimortech.ui.viewmodel.RelatorioViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, modifier: Modifier = Modifier) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val app = context.applicationContext as? AprimortechApplication
    val offlineAuth = app?.offlineAuthManager

    // ViewModel para buscar relatórios
    val relatorioViewModel: RelatorioViewModel = viewModel(
        factory = RelatorioViewModelFactory(
            buscarRelatoriosUseCase = (context.applicationContext as AprimortechApplication).buscarRelatoriosUseCase,
            salvarRelatorioUseCase = (context.applicationContext as AprimortechApplication).salvarRelatorioUseCase,
            excluirRelatorioUseCase = (context.applicationContext as AprimortechApplication).excluirRelatorioUseCase,
            sincronizarRelatoriosUseCase = (context.applicationContext as AprimortechApplication).sincronizarRelatoriosUseCase,
            buscarProximasManutencoesPreventivasUseCase = (context.applicationContext as AprimortechApplication).buscarProximasManutencoesPreventivasUseCase,
            relatorioRepository = (context.applicationContext as AprimortechApplication).relatorioRepository
        )
    )

    // Observa a lista de relatórios
    val relatorios by relatorioViewModel.relatorios.collectAsState()

    // Helper de parsing com SimpleDateFormat (compatível com minSdk)
    val parseDateSafe: (String?) -> Date? = remember {
        { dateStr ->
            if (dateStr.isNullOrBlank()) null
            else {
                val patterns = listOf("yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd")
                var parsed: Date? = null
                for (pattern in patterns) {
                    try {
                        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                        sdf.isLenient = false
                        parsed = sdf.parse(dateStr)
                        if (parsed != null) break
                    } catch (_: Exception) {
                        // tentar próximo
                    }
                }
                parsed
            }
        }
    }

    // Prepara os 5 relatórios mais recentes para exibição
    val recentReportsUi = remember(relatorios) {
        relatorios
            .sortedByDescending { parseDateSafe(it.dataRelatorio)?.time ?: 0L }
            .take(5)
            .map { r ->
                ReportUiModel(
                    title = r.descricaoServico.takeIf { it.isNotBlank() } ?: "Relatório ${r.id.take(6)}",
                    subtitle = "${r.clienteId} • ${r.maquinaId} • ${r.dataRelatorio}",
                    status = if (r.syncPending) ReportStatus.Pending else ReportStatus.Done
                )
            }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                offlineAuth?.refreshSessionTimestamp()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                // LOGO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_aprimortech),
                        contentDescription = "Logo Aprimortech",
                        modifier = Modifier.height(40.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // MENU ITENS
                DrawerMenuItem(Icons.Default.Dashboard, "Painel", true) {
                    scope.launch { drawerState.close() }
                    navController.navigate("dashboard")
                }
                DrawerMenuItem(Icons.Default.Description, "Relatórios", false) {
                    scope.launch { drawerState.close() }
                    navController.navigate("relatorios")
                }
                DrawerMenuItem(Icons.Default.People, "Clientes", false) {
                    scope.launch { drawerState.close() }
                    navController.navigate("clientes")
                }
                DrawerMenuItem(Icons.Default.Build, "Máquinas", false) {
                    scope.launch { drawerState.close() }
                    navController.navigate("maquinas")
                }
                DrawerMenuItem(Icons.Default.Inventory, "Peças", false) {
                    scope.launch { drawerState.close() }
                    navController.navigate("pecas")
                }
                DrawerMenuItem(Icons.Default.Warning, "Defeitos", false) {
                    scope.launch { drawerState.close() }
                    navController.navigate("defeitos")
                }
                DrawerMenuItem(Icons.Default.Settings, "Serviços", false) {
                    scope.launch { drawerState.close() }
                    navController.navigate("servicos")
                }

                Spacer(Modifier.weight(1f))
                HorizontalDivider()

                // USUÁRIO
                // Keep only logout button in the drawer (no avatar/name/email)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            offlineAuth?.clearCredentials()
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sair",
                            tint = Color(0xFF1A4A5C)
                        )
                    }
                }
            }
        }
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
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
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
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Ações Rápidas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF1A4A5C)
                )

                Spacer(Modifier.height(16.dp))

                QuickActionButton("Criar Relatório", Icons.Default.Description) {
                    navController.navigate("novoRelatorio")
                }
                Spacer(Modifier.height(8.dp))
                QuickActionButton("Gerenciar Clientes", Icons.Default.People) {
                    navController.navigate("clientes")
                }
                Spacer(Modifier.height(8.dp))
                QuickActionButton("Ver Máquinas", Icons.Default.Settings) {
                    navController.navigate("maquinas")
                }
                Spacer(Modifier.height(8.dp))
                QuickActionButton("Verificar Peças", Icons.Default.Inventory) {
                    navController.navigate("pecas")
                }

                Spacer(Modifier.height(24.dp))

                // Relatórios recentes (até 5)
                RecentReportsSection(
                    reports = recentReportsUi,
                    onVerTodos = { navController.navigate("relatorios") }
                )
            }
        }
    }
}

@Composable
fun DrawerMenuItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun QuickActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color(0xFF1A4A5C))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color(0xFF1A4A5C))
    }
}

@Composable
fun RecentReportsSection(
    reports: List<ReportUiModel>,
    onVerTodos: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Relatórios Recentes",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )
            TextButton(onClick = onVerTodos) {
                Text("Ver Todos", color = Color(0xFF1A4A5C))
            }
        }
        Spacer(Modifier.height(16.dp))

        reports.forEach { report ->
            ReportCard(report)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun ReportCard(report: ReportUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = report.icon,
                    contentDescription = null,
                    tint = Color(0xFF1A4A5C)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    report.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1A4A5C)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                report.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            StatusBadge(report.status)
        }
    }
}

@Composable
fun StatusBadge(status: ReportStatus) {
    val (label, bgColor, textColor) = when (status) {
        ReportStatus.Draft -> Triple("Rascunho", Color(0xFFE0E0E0), Color.Black)
        ReportStatus.Pending -> Triple("Pendente", Color(0xFFFFF59D), Color.Black)
        ReportStatus.Done -> Triple("Concluído", Color(0xFFA5D6A7), Color.Black)
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

enum class ReportStatus { Draft, Pending, Done }

data class ReportUiModel(
    val title: String,
    val subtitle: String,
    val status: ReportStatus,
    val icon: ImageVector = Icons.Default.Description
)

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardScreenPreview() {
    AprimortechTheme {
        DashboardScreen(navController = rememberNavController())
    }
}
