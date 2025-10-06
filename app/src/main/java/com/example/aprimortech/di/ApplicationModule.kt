package com.example.aprimortech.di

import com.example.aprimortech.data.repository.DefeitoRepository
import com.example.aprimortech.data.repository.ServicoRepository
import com.example.aprimortech.ui.viewmodel.DefeitoViewModelFactory
import com.example.aprimortech.ui.viewmodel.ServicoViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

object ApplicationModule {

    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun provideDefeitoRepository(firestore: FirebaseFirestore): DefeitoRepository {
        return DefeitoRepository(firestore)
    }

    fun provideServicoRepository(firestore: FirebaseFirestore): ServicoRepository {
        return ServicoRepository(firestore)
    }

    fun provideDefeitoViewModelFactory(defeitoRepository: DefeitoRepository): DefeitoViewModelFactory {
        return DefeitoViewModelFactory(defeitoRepository)
    }

    fun provideServicoViewModelFactory(servicoRepository: ServicoRepository): ServicoViewModelFactory {
        return ServicoViewModelFactory(servicoRepository)
    }
}
