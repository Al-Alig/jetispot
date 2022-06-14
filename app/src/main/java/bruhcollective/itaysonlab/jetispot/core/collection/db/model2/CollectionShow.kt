package bruhcollective.itaysonlab.jetispot.core.collection.db.model2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lcShows")
data class CollectionShow(
  @PrimaryKey val uri: String,
  val name: String,
  val publisher: String,
  val picture: String,
  val addedAt: Int
): CollectionEntry {
  override fun ceId() = uri
  override fun ceUri() = uri
  override fun ceTimestamp() = addedAt.toLong()
}