package dev.fiedri.vibe.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Database(
    entities = [
        Playlist::class,
        PlaylistSongs::class
    ],
    version = 1,
    //exportSchema = true
)
@TypeConverters(CategoryConverter::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        playlistDaoProvider: Provider<PlaylistDao>
    ): AppDataBase {
        return Room.databaseBuilder(
            context,
            AppDataBase::class.java,
            "vibe_db.db"
        ).addCallback(object : RoomDatabase.Callback(){
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch{
                    val defaultPlaylist = Playlist(
                        id = 1,
                        name = "Favorites",
                        category = Category.FAVORITE,
                        dateCreated = ""
                    )
                    playlistDaoProvider.get().createPlaylist(defaultPlaylist)
                }
            }
        }).build()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(db: AppDataBase): PlaylistDao {
        return db.playlistDao()
    }
}