package bruhcollective.itaysonlab.jetispot.core.collection.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import bruhcollective.itaysonlab.jetispot.core.collection.db.model.LocalCollectionCategory
import bruhcollective.itaysonlab.jetispot.core.collection.db.model2.*
import bruhcollective.itaysonlab.jetispot.core.collection.db.model2.rootlist.CollectionRootlistItem

@Database(
  entities = [
    LocalCollectionCategory::class,
    CollectionArtist::class,
    CollectionAlbum::class,
    CollectionTrack::class,
    CollectionRootlistItem::class,
    CollectionContentFilter::class,
    CollectionPinnedItem::class,
    CollectionShow::class,
    CollectionEpisode::class,
  ], version = 7, autoMigrations = [
    AutoMigration(from = 1, to = 2),
    AutoMigration(from = 2, to = 3),
    AutoMigration(from = 3, to = 4),
    AutoMigration(from = 4, to = 5, spec = LocalCollectionDatabase.RemoveArtistsMeta::class),
    AutoMigration(from = 5, to = 6),
    AutoMigration(from = 6, to = 7),
  ], exportSchema = true
)
abstract class LocalCollectionDatabase : RoomDatabase() {
  abstract fun dao(): LocalCollectionDao

  @DeleteTable(tableName = "lcMetaArtists")
  class RemoveArtistsMeta: AutoMigrationSpec
}