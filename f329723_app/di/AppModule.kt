package com.example.f329723_app.di

import com.example.f329723_app.data.repository.AppointmentRepositoryFirebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.room.Room
import com.example.f329723_app.data.database.AppDatabase
import com.example.f329723_app.data.repository.PrivateNoteDao

// Dagger Hilt module for providing dependencies throughout the app
@Module
@InstallIn(SingletonComponent::class) // Indicates that these dependencies live as long as the app
object AppModule {

    // Provides a singleton instance of AppointmentRepositoryFirebase with application context
    @Provides
    @Singleton
    fun provideAppointmentRepository(
        @ApplicationContext context: Context
    ): AppointmentRepositoryFirebase {
        return AppointmentRepositoryFirebase(context)
    }

    // Provides a singleton instance of the Room database
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "smart_schedule_db" // Name of the local database file
        ).build()
    }

    // Provides the DAO used for interacting with private notes in the database
    @Provides
    fun providePrivateNoteDao(db: AppDatabase): PrivateNoteDao {
        return db.privateNoteDao()
    }
}
