package com.example.aprimortech

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.aprimortech.data.local.AppDatabase
import com.example.aprimortech.data.repository.MaquinaRepository
import com.example.aprimortech.data.repository.RelatorioRepository
import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.data.repository.PecaRepository
import com.example.aprimortech.data.repository.ContatoRepository
import com.example.aprimortech.data.repository.SetorRepository
import com.example.aprimortech.data.repository.DefeitoRepository
import com.example.aprimortech.data.repository.ServicoRepository
import com.example.aprimortech.data.repository.TintaRepository
import com.example.aprimortech.data.repository.SolventeRepository
import com.example.aprimortech.domain.usecase.*
import com.example.aprimortech.util.NetworkConnectivityObserver
import com.example.aprimortech.worker.ClienteSyncWorker
import com.example.aprimortech.worker.MaquinaSyncWorker
import com.example.aprimortech.worker.PecaSyncWorker
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import android.util.Log

class AprimortechApplication : Application() {

    companion object {
        private const val TAG = "AprimortechApp"
    }

    // Inicialização eager do Firestore para garantir configuração correta
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            // Configurar Firestore com cache persistente habilitado
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()

            Log.d(TAG, "✅ FirebaseFirestore inicializado com cache persistente")
        }
    }

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this).also {
            Log.d(TAG, "✅ AppDatabase Room inicializado")
        }
    }

    // Monitor de conectividade
    private val networkObserver: NetworkConnectivityObserver by lazy {
        NetworkConnectivityObserver(this)
    }

    // Repositories
    val maquinaRepository: MaquinaRepository by lazy {
        MaquinaRepository(firestore, database.maquinaDao()).also {
            Log.d(TAG, "✅ MaquinaRepository inicializado")
        }
    }

    val relatorioRepository: RelatorioRepository by lazy {
        RelatorioRepository(firestore).also {
            Log.d(TAG, "✅ RelatorioRepository inicializado")
        }
    }

    val clienteRepository: ClienteRepository by lazy {
        ClienteRepository(firestore, database.clienteDao()).also {
            Log.d(TAG, "✅ ClienteRepository inicializado")
        }
    }

    val pecaRepository: PecaRepository by lazy {
        PecaRepository(firestore, database.pecaDao()).also {
            Log.d(TAG, "✅ PecaRepository inicializado")
        }
    }

    val contatoRepository: ContatoRepository by lazy {
        ContatoRepository(firestore)
    }

    val setorRepository: SetorRepository by lazy {
        SetorRepository(firestore)
    }

    val defeitoRepository: DefeitoRepository by lazy {
        DefeitoRepository(firestore)
    }

    val servicoRepository: ServicoRepository by lazy {
        ServicoRepository(firestore)
    }

    val tintaRepository: TintaRepository by lazy {
        TintaRepository()
    }

    val solventeRepository: SolventeRepository by lazy {
        SolventeRepository()
    }

    // Use Cases para Máquinas
    val buscarMaquinasUseCase: BuscarMaquinasUseCase by lazy {
        BuscarMaquinasUseCase(maquinaRepository)
    }

    val salvarMaquinaUseCase: SalvarMaquinaUseCase by lazy {
        SalvarMaquinaUseCase(maquinaRepository)
    }

    val excluirMaquinaUseCase: ExcluirMaquinaUseCase by lazy {
        ExcluirMaquinaUseCase(maquinaRepository)
    }

    val sincronizarMaquinasUseCase: SincronizarMaquinasUseCase by lazy {
        SincronizarMaquinasUseCase(maquinaRepository)
    }

    // Use Cases para Clientes
    val buscarClientesUseCase: BuscarClientesUseCase by lazy {
        BuscarClientesUseCase(clienteRepository).also {
            Log.d(TAG, "✅ BuscarClientesUseCase inicializado")
        }
    }

    val salvarClienteUseCase: SalvarClienteUseCase by lazy {
        SalvarClienteUseCase(clienteRepository).also {
            Log.d(TAG, "✅ SalvarClienteUseCase inicializado")
        }
    }

    val excluirClienteUseCase: ExcluirClienteUseCase by lazy {
        ExcluirClienteUseCase(clienteRepository).also {
            Log.d(TAG, "✅ ExcluirClienteUseCase inicializado")
        }
    }

    val sincronizarClientesUseCase: SincronizarClientesUseCase by lazy {
        SincronizarClientesUseCase(clienteRepository).also {
            Log.d(TAG, "✅ SincronizarClientesUseCase inicializado")
        }
    }

    // Use Cases para Peças
    val buscarPecasUseCase: BuscarPecasUseCase by lazy {
        BuscarPecasUseCase(pecaRepository)
    }

    val salvarPecaUseCase: SalvarPecaUseCase by lazy {
        SalvarPecaUseCase(pecaRepository)
    }

    val excluirPecaUseCase: ExcluirPecaUseCase by lazy {
        ExcluirPecaUseCase(pecaRepository)
    }

    val sincronizarPecasUseCase: SincronizarPecasUseCase by lazy {
        SincronizarPecasUseCase(pecaRepository)
    }

    // Use Cases para Relatórios
    val buscarRelatoriosUseCase: BuscarRelatoriosUseCase by lazy {
        BuscarRelatoriosUseCase(relatorioRepository)
    }

    val salvarRelatorioUseCase: SalvarRelatorioUseCase by lazy {
        SalvarRelatorioUseCase(relatorioRepository)
    }

    val excluirRelatorioUseCase: ExcluirRelatorioUseCase by lazy {
        ExcluirRelatorioUseCase(relatorioRepository)
    }

    val sincronizarRelatoriosUseCase: SincronizarRelatoriosUseCase by lazy {
        SincronizarRelatoriosUseCase(relatorioRepository)
    }

    val buscarProximasManutencoesPreventivasUseCase: BuscarProximasManutencoesPreventivasUseCase by lazy {
        BuscarProximasManutencoesPreventivasUseCase(relatorioRepository)
    }

    val offlineAuthManager: OfflineAuthManager by lazy { OfflineAuthManager(this) }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "🚀 Iniciando AprimortechApplication...")

        try {
            // Inicializar Firebase PRIMEIRO
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "✅ Firebase inicializado")

            // --- Garantir que o FirebaseAuth esteja com algum usuário autenticado ---
            try {
                val auth = FirebaseAuth.getInstance()
                val current = auth.currentUser
                if (current == null) {
                    Log.d(TAG, "ℹ️ Nenhum usuário autenticado no FirebaseAuth, tentando signInAnonymously()...")
                    auth.signInAnonymously().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            Log.d(TAG, "✅ Sign-in anônimo realizado: uid=${user?.uid}")
                        } else {
                            Log.w(TAG, "⚠️ Falha no sign-in anônimo do FirebaseAuth: ${task.exception?.message}", task.exception)
                        }
                    }
                } else {
                    Log.d(TAG, "✅ FirebaseAuth já autenticado: uid=${current.uid}")
                }
            } catch (authEx: Exception) {
                Log.w(TAG, "⚠️ Erro ao inicializar FirebaseAuth", authEx)
            }

            // Forçar inicialização do Firestore
            firestore

            // Forçar inicialização do Database
            database

            // Forçar inicialização dos repositórios críticos
            clienteRepository

            Log.d(TAG, "✅ Todos os componentes essenciais inicializados")

            // Inicializar sincronização periódica em background
            ClienteSyncWorker.schedulePeriodicSync(this)
            MaquinaSyncWorker.schedulePeriodicSync(this)
            PecaSyncWorker.schedulePeriodicSync(this)
            Log.d(TAG, "✅ WorkManager configurado")

            // Observar conectividade
            observarConectividade()

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERRO CRÍTICO na inicialização: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private fun observarConectividade() {
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            try {
                networkObserver.observe()
                    .distinctUntilChanged()
                    .collect { isOnline ->
                        if (isOnline) {
                            Log.d(TAG, "🌐 Conexão restaurada - Sincronizando dados...")
                            ClienteSyncWorker.syncNow(this@AprimortechApplication)
                            MaquinaSyncWorker.syncNow(this@AprimortechApplication)
                            PecaSyncWorker.syncNow(this@AprimortechApplication)
                        } else {
                            Log.d(TAG, "📵 Modo offline - Dados serão salvos localmente")
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao observar conectividade: ${e.message}", e)
            }
        }
    }
}
