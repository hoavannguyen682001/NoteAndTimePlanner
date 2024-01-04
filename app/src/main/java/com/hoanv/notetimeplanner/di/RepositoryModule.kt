package com.hoanv.notetimeplanner.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hoanv.notetimeplanner.data.remote.AppApi
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepoIml
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRemoteRepository(
        fireStore: FirebaseFirestore,
        auth: FirebaseAuth,
        api: AppApi,
        firebaseStorage: FirebaseStorage
    ): RemoteRepo {
        return RemoteRepoIml(fireStore, auth, api, firebaseStorage)
    }
}