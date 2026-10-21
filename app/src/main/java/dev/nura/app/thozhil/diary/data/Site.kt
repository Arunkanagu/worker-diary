package dev.nura.app.thozhil.diary.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** A work site or building. */
@Entity(tableName = "sites")
data class Site(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

/** A contact person (owner, engineer, supervisor…) for a site. */
@Entity(tableName = "site_contacts", indices = [Index("siteId")])
data class SiteContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val siteId: Long = 0,
    val name: String,
    val mobile: String
)

data class SiteWithContacts(
    @Embedded val site: Site,
    @Relation(parentColumn = "id", entityColumn = "siteId")
    val contacts: List<SiteContact>
)

/**
 * A note or a bill kept against a site, newest first. Both hold tables of items; a bill's items
 * also carry a rate, so each row has an amount.
 */
@Entity(tableName = "site_notes", indices = [Index("siteId")])
data class SiteNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val siteId: Long,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0")
    val isBill: Boolean = false
)

/** Sum of item amounts in one bill. */
data class NoteTotal(val noteId: Long, val total: Double?)

/** How many bills a site has and what they add up to. */
data class SiteBillSummary(val siteId: Long, val bills: Int, val total: Double?)
