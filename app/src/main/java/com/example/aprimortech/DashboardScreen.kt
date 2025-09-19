package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, modifier: Modifier = Modifier) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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

                Spacer(Modifier.weight(1f))
                Divider()

                // USUÁRIO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF1A4A5C)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("João Silva", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "joao.silva@email.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            // Faz logout do Firebase
                            FirebaseAuth.getInstance().signOut()
                            // Redireciona para tela de login
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
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
                    .padding(16.dp)
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

                Text(
                    text = "Indicadores",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF1A4A5C)
                )
                Spacer(Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard("Total de Relatórios", "3", Icons.Default.List)
                    MetricCard("Clientes Ativos", "3", Icons.Default.Person)
                    MetricCard("Máquinas", "3", Icons.Default.Build)
                    MetricCard("Peças em Estoque", "98", Icons.Default.Inventory)
                }

                Spacer(Modifier.height(24.dp))

                RecentReportsSection(
                    reports = listOf(
                        ReportUiModel(
                            "Instalação - Configuração da Dobradeira",
                            "Indústrias TechFlow • Dobradeira • 22 fev 2024",
                            ReportStatus.Draft
                        ),
                        ReportUiModel(
                            "Reparo de Emergência - Torno #3",
                            "Corporação Acme • Torno #3 • 20 fev 2024",
                            ReportStatus.Pending
                        ),
                        ReportUiModel(
                            "Manutenção Preventiva - Fresadora CNC #1",
                            "Corporação Acme • Fresadora CNC #1 • 15 fev 2024",
                            ReportStatus.Done
                        )
                    ),
                    onVerTodos = { navController.navigate("relatorios") } // ✅ agora leva para RelatoriosScreen
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
fun MetricCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium)
                Text(value, style = MaterialTheme.typography.headlineSmall)
            }
            Icon(icon, contentDescription = title, tint = Color(0xFF1A4A5C))
        }
    }
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
