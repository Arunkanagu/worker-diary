package dev.nura.app.thozhil.diary.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.nura.app.thozhil.diary.data.AppDatabase
import dev.nura.app.thozhil.diary.data.AttendanceDao
import dev.nura.app.thozhil.diary.data.EmployeeDao
import dev.nura.app.thozhil.diary.data.UserDao
import dev.nura.app.thozhil.diary.util.SharedPrefHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context, prefs: SharedPrefHelper): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "thozhil_diary.db")
            .addCallback(object : RoomDatabase.Callback() {
                // The triggers that stamp each record's owner and times, made fresh on every open
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    AppDatabase.createRecordTriggers(db)
                }
            })
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.migration11To12(prefs::readLegacyAccount),
                AppDatabase.MIGRATION_12_13
            )
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()

    @Provides
    fun provideEmployeeDao(db: AppDatabase): EmployeeDao = db.employeeDao()

    @Provides
    fun provideAttendanceDao(db: AppDatabase): AttendanceDao = db.attendanceDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
}
