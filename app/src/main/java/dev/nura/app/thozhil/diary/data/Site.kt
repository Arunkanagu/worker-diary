package dev.nura.app.thozhil.diary.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** A work site or building. */
@Entity(tableName = "sites", indices = [Index("userId")])
data class Site(
    @PrimaryKey
    val id: String = newId(),
    /** The account the record belongs to; the database fills it in on insert. */
    val userId: String = "",
    val name: String,
    val createdAt: Long = nowMillis(),
    val updatedAt: Long = nowMillis()
)

/** A contact person (owner, engineer, supervisor…) for a site. */
@Entity(tableName = "site_contacts", indices = [Index("siteId"), Index("userId")])
data class SiteContact(
    @PrimaryKey
    val id: String = newId(),
    /** The account the record belongs to; the database fills it in on insert. */
    val userId: String = "",
    val siteId: String = "",
    val name: String,
    val mobile: String,
    val createdAt: Long = nowMillis(),
    val updatedAt: Long = nowMillis()
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
@Entity(tableName = "site_notes", indices = [Index("siteId"), Index("userId")])
data class SiteNote(
    @PrimaryKey
    val id: String = newId(),
    /** The account the record belongs to; the database fills it in on insert. */
    val userId: String = "",
    val siteId: String,
    val title: String,
    val createdAt: Long = nowMillis(),
    val updatedAt: Long = nowMillis(),
    @ColumnInfo(defaultValue = "0")
    val isBill: Boolean = false,
    /**
     * Whether a bill's PDF ends with a line to sign on. New bills start without it. The column default
     * stays 1 (bills from before the switch keep their signature); Room always writes this value.
     */
    @ColumnInfo(defaultValue = "1")
    val showSignature: Boolean = false
)

/** How many tables and items a note or bill holds, for its card in a list. */
data class NoteCounts(val noteId: String, val tables: Int, val items: Int)

/** How many bills a site has and what they add up to. */
data class SiteBillSummary(val siteId: String, val bills: Int, val total: Double?)
