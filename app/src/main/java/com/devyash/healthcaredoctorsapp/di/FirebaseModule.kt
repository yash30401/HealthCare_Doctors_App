package com.devyash.healthcaredoctorsapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class FirebaseModule {

    @Singleton
    @Provides
    fun providesFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun providesFirebaseFirestore() = Firebase.firestore

    @Provides
    fun provideFirebaseStorage():FirebaseStorage = Firebase.storage

    @Provides
    fun provideStorageReference():StorageReference = provideFirebaseStorage().reference

    @Provides
    fun providesFirebaseMessageing() = FirebaseMessaging.getInstance()

}