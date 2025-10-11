package com.example.aprimortech

import android.app.Application
import androidx.room.Room
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
import com.example.aprimortech.domain.usecase.*
import com.example.aprimortech.util.NetworkConnectivityObserver
import com.example.aprimortech.worker.ClienteSyncWorker
import com.example.aprimortech.worker.MaquinaSyncWorker
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import android.util.Log

class AprimortechApplication : Application() {

    companion object {
        private const val TAG = "AprimortechApp"
    }

    // Inicialização lazy das dependências
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "aprimortech.db"
        ).fallbackToDestructiveMigration().build()
    }

    // Monitor de conectividade
    private val networkObserver: NetworkConnectivityObserver by lazy {
        NetworkConnectivityObserver(this)
    }

    // Repositories
    val maquinaRepository: MaquinaRepository by lazy {
        MaquinaRepository(firestore, database.maquinaDao())
    }

    val relatorioRepository: RelatorioRepository by lazy {
        RelatorioRepository(firestore)
    }

    val clienteRepository: ClienteRepository by lazy {
        ClienteRepository(firestore, database.clienteDao())
    }

    val pecaRepository: PecaRepository by lazy {
        PecaRepository(firestore)
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
        BuscarClientesUseCase(clienteRepository)
    }

    val salvarClienteUseCase: SalvarClienteUseCase by lazy {
        SalvarClienteUseCase(clienteRepository)
    }

    val excluirClienteUseCase: ExcluirClienteUseCase by lazy {
        ExcluirClienteUseCase(clienteRepository)
    }

    val sincronizarClientesUseCase: SincronizarClientesUseCase by lazy {
        SincronizarClientesUseCase(clienteRepository)
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
        BuscarProximasManutencoesPreventivasUseCase(maquinaRepository)
    }

    val offlineAuthManager: OfflineAuthManager by lazy { OfflineAuthManager(this) }

    override fun onCreate() {
        super.onCreate()

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Inicializar sincronização periódica em background
        ClienteSyncWorker.schedulePeriodicSync(this)
        MaquinaSyncWorker.schedulePeriodicSync(this)
        Log.d(TAG, "✅ WorkManager para sincronização periódica iniciado (Clientes e Máquinas)")

        // Observar conectividade e sincronizar quando online
        observarConectividade()
    }

    /**
     * Observa mudanças na conectividade e sincroniza automaticamente quando online
     */
    private fun observarConectividade() {
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            networkObserver.observe()
                .distinctUntilChanged() // Evita eventos duplicados
                .collect { isOnline ->
                    if (isOnline) {
                        Log.d(TAG, "🌐 Conexão restaurada - Sincronizando dados...")
                        ClienteSyncWorker.syncNow(this@AprimortechApplication)
                        MaquinaSyncWorker.syncNow(this@AprimortechApplication)
                    } else {
                        Log.d(TAG, "📵 Modo offline - Dados serão salvos localmente")
                    }
                }
        }
    }
}
