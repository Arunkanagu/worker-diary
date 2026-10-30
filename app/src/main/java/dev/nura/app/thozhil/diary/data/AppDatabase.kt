package dev.nura.app.thozhil.diary.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        User::class, Session::class, Employee::class, Attendance::class, Site::class,
        SiteContact::class, SiteNote::class, NoteItem::class, NoteTable::class, BillAdjustment::class
    ],
    version = 13,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun siteDao(): SiteDao
    abstract fun noteItemDao(): NoteItemDao
    abstract fun noteTableDao(): NoteTableDao
    abstract fun billAdjustmentDao(): BillAdjustmentDao
    abstract fun userDao(): UserDao

    companion object {
        /** Adds the advances table. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `advances` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`employeeId` INTEGER NOT NULL, " +
                        "`date` TEXT NOT NULL, " +
                        "`amount` REAL NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_advances_employeeId` ON `advances` (`employeeId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_advances_date` ON `advances` (`date`)")
            }
        }

        /** Adds the sites and site_contacts tables. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `sites` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `site_contacts` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`siteId` INTEGER NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`mobile` TEXT NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_site_contacts_siteId` ON `site_contacts` (`siteId`)")
            }
        }

        /** Adds the site_notes table. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `site_notes` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`siteId` INTEGER NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_site_notes_siteId` ON `site_notes` (`siteId`)")
            }
        }

        /** Adds the note_items table (measured items inside a site note). */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `note_items` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`noteId` INTEGER NOT NULL, " +
                        "`item` TEXT NOT NULL, " +
                        "`thickness` REAL, " +
                        "`thicknessUnit` TEXT NOT NULL, " +
                        "`width` REAL, " +
                        "`widthUnit` TEXT NOT NULL, " +
                        "`length` REAL, " +
                        "`lengthUnit` TEXT NOT NULL, " +
                        "`quantity` INTEGER, " +
                        "`remarks` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_items_noteId` ON `note_items` (`noteId`)")
            }
        }

        /**
         * Sizes change from numbers to text so fractions like "2.1/8" can be stored.
         * SQLite can't change a column's type, so the table is rebuilt; whole numbers lose their ".0".
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            private fun asText(column: String) =
                "COALESCE(CASE WHEN `$column` = CAST(`$column` AS INTEGER) " +
                    "THEN CAST(CAST(`$column` AS INTEGER) AS TEXT) ELSE CAST(`$column` AS TEXT) END, '')"

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE `note_items_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`noteId` INTEGER NOT NULL, " +
                        "`item` TEXT NOT NULL, " +
                        "`thickness` TEXT NOT NULL, " +
                        "`thicknessUnit` TEXT NOT NULL, " +
                        "`width` TEXT NOT NULL, " +
                        "`widthUnit` TEXT NOT NULL, " +
                        "`length` TEXT NOT NULL, " +
                        "`lengthUnit` TEXT NOT NULL, " +
                        "`quantity` INTEGER, " +
                        "`remarks` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "INSERT INTO `note_items_new` (`id`, `noteId`, `item`, `thickness`, `thicknessUnit`, " +
                        "`width`, `widthUnit`, `length`, `lengthUnit`, `quantity`, `remarks`, `createdAt`) " +
                        "SELECT `id`, `noteId`, `item`, ${asText("thickness")}, `thicknessUnit`, " +
                        "${asText("width")}, `widthUnit`, ${asText("length")}, `lengthUnit`, " +
                        "`quantity`, `remarks`, `createdAt` FROM `note_items`"
                )
                db.execSQL("DROP TABLE `note_items`")
                db.execSQL("ALTER TABLE `note_items_new` RENAME TO `note_items`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_items_noteId` ON `note_items` (`noteId`)")
            }
        }

        /**
         * Notes can hold several tables. Adds note_tables and items' tableId; each note that
         * already has items gets a "Table 1" holding them.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `note_tables` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`noteId` INTEGER NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_tables_noteId` ON `note_tables` (`noteId`)")
                db.execSQL("ALTER TABLE `note_items` ADD COLUMN `tableId` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_items_tableId` ON `note_items` (`tableId`)")
                db.execSQL(
                    "INSERT INTO `note_tables` (`noteId`, `name`, `createdAt`) " +
                        "SELECT `noteId`, 'Table 1', MIN(`createdAt`) FROM `note_items` GROUP BY `noteId`"
                )
                db.execSQL(
                    "UPDATE `note_items` SET `tableId` = " +
                        "(SELECT `id` FROM `note_tables` t WHERE t.`noteId` = `note_items`.`noteId`)"
                )
            }
        }

        /** Bills: notes get an isBill flag and items a rate. */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `site_notes` ADD COLUMN `isBill` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `note_items` ADD COLUMN `rate` REAL")
            }
        }

        /** Adds designation/department to employees and remarks to attendance. */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `employees` ADD COLUMN `designation` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `employees` ADD COLUMN `department` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `attendance` ADD COLUMN `remarks` TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Adds bill adjustments (advance, discount, tax) and a hide-from-PDF flag on note tables. */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `bill_adjustments` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`noteId` INTEGER NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`amount` REAL NOT NULL, " +
                        "`hideInPdf` INTEGER NOT NULL DEFAULT 0, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bill_adjustments_noteId` ON `bill_adjustments` (`noteId`)")
                db.execSQL("ALTER TABLE `note_tables` ADD COLUMN `hideInPdf` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Bills can leave the signature line off their PDF; existing bills keep it. */
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `site_notes` ADD COLUMN `showSignature` INTEGER NOT NULL DEFAULT 1")
            }
        }

        /** The tables that hold a user's records: each has userId, createdAt and updatedAt. */
        private val RecordTables = listOf(
            "employees", "attendance", "sites", "site_contacts",
            "site_notes", "note_tables", "note_items", "bill_adjustments"
        )

        /** Now in milliseconds, inside SQL. */
        private const val SqlNow = "CAST((julianday('now') - 2440587.5) * 86400000 AS INTEGER)"

        /** A random (version 4) UUID, inside SQL; a new one for every row it's used on. */
        private const val SqlUuid =
            "lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4' || " +
                "substr(lower(hex(randomblob(2))), 2) || '-' || substr('89ab', 1 + (abs(random()) % 4), 1) || " +
                "substr(lower(hex(randomblob(2))), 2) || '-' || lower(hex(randomblob(6)))"

        /**
         * Each table's own columns: everything but id, userId, createdAt and updatedAt. A change to any
         * of them stamps the record; the stamp itself only writes the other four, so it never sets
         * itself off again (Android's SQLite lets triggers fire themselves).
         */
        private val DataColumns = mapOf(
            "employees" to listOf("name", "mobile", "designation", "department"),
            "attendance" to listOf("employeeId", "date", "status", "inTime", "outTime", "overtimeHours", "absentReason", "remarks"),
            "sites" to listOf("name"),
            "site_contacts" to listOf("siteId", "name", "mobile"),
            "site_notes" to listOf("siteId", "title", "isBill", "showSignature"),
            "note_tables" to listOf("noteId", "name", "hideInPdf"),
            "note_items" to listOf(
                "noteId", "tableId", "item", "thickness", "thicknessUnit", "width", "widthUnit",
                "length", "lengthUnit", "quantity", "rate", "remarks"
            ),
            "bill_adjustments" to listOf("noteId", "type", "amount", "hideInPdf"),
            "users" to listOf(
                "name", "mobile", "email", "passwordSalt", "passwordHash", "businessName",
                "trade", "businessAddress", "gstNumber", "photoPath"
            )
        )

        /**
         * Keeps every record's userId and times right whatever code saves it:
         * - a new record with no userId gets the logged-in user's
         * - a change moves updatedAt on and keeps createdAt (and the owner, and attendance's id)
         *   as they were, since a day's attendance is often saved as a fresh object
         * Dropped and made again on every open, so the database always has this version of them.
         */
        fun createRecordTriggers(db: SupportSQLiteDatabase) {
            (RecordTables + "users").forEach { table ->
                db.execSQL("DROP TRIGGER IF EXISTS `${table}_owner_on_insert`")
                db.execSQL("DROP TRIGGER IF EXISTS `${table}_stamp_on_update`")
                val columns = DataColumns.getValue(table).joinToString(", ") { "`$it`" }
                val keep = when (table) {
                    "users" -> ""
                    "attendance" -> ", userId = CASE WHEN NEW.userId = '' THEN OLD.userId ELSE NEW.userId END, id = OLD.id"
                    else -> ", userId = CASE WHEN NEW.userId = '' THEN OLD.userId ELSE NEW.userId END"
                }
                db.execSQL(
                    "CREATE TRIGGER `${table}_stamp_on_update` AFTER UPDATE OF $columns ON `$table` BEGIN " +
                        "UPDATE `$table` SET createdAt = OLD.createdAt, updatedAt = $SqlNow$keep " +
                        "WHERE rowid = NEW.rowid; END"
                )
                if (table != "users") {
                    db.execSQL(
                        "CREATE TRIGGER `${table}_owner_on_insert` AFTER INSERT ON `$table` " +
                            "WHEN NEW.userId = '' BEGIN " +
                            "UPDATE `$table` SET userId = COALESCE((SELECT userId FROM session WHERE slot = 0), '') " +
                            "WHERE rowid = NEW.rowid; END"
                    )
                }
            }
        }

        /** The account as it was kept before it moved into the database. */
        class LegacyAccount(
            val name: String,
            val mobile: String,
            val email: String,
            val passwordSalt: String,
            val passwordHash: String,
            val businessName: String,
            val trade: String,
            val businessAddress: String,
            val gstNumber: String,
            val photoPath: String,
            val createdAt: Long,
            val loggedIn: Boolean
        )

        /**
         * Ids become UUIDs, and the account moves in from SharedPreferences ([legacyAccount] reads it).
         * Every table is rebuilt: each row gets a new id, and the ids it points at (an employee, a site,
         * a note, a table) are swapped for their new ones. All records go to the existing account.
         */
        fun migration11To12(legacyAccount: () -> LegacyAccount?) = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // A new id beside the old one in every table, so links can be followed across
                listOf("employees", "advances", "sites", "site_contacts", "site_notes", "note_tables", "note_items", "bill_adjustments")
                    .forEach { table ->
                        db.execSQL("ALTER TABLE `$table` ADD COLUMN `uid` TEXT")
                        db.execSQL("UPDATE `$table` SET `uid` = $SqlUuid")
                    }

                db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `mobile` TEXT NOT NULL, `email` TEXT NOT NULL, `passwordSalt` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, `businessName` TEXT NOT NULL, `trade` TEXT NOT NULL, `businessAddress` TEXT NOT NULL, `gstNumber` TEXT NOT NULL, `photoPath` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_mobile` ON `users` (`mobile`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `session` (`slot` INTEGER NOT NULL, `userId` TEXT NOT NULL, `loggedInAt` INTEGER NOT NULL, PRIMARY KEY(`slot`))")

                val account = legacyAccount()
                val owner = if (account != null) newId() else ""
                if (account != null) {
                    db.execSQL(
                        "INSERT INTO `users` (`id`, `name`, `mobile`, `email`, `passwordSalt`, `passwordHash`, `businessName`, " +
                            "`trade`, `businessAddress`, `gstNumber`, `photoPath`, `createdAt`, `updatedAt`) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        arrayOf<Any>(
                            owner, account.name, account.mobile, account.email, account.passwordSalt, account.passwordHash,
                            account.businessName, account.trade, account.businessAddress, account.gstNumber, account.photoPath,
                            account.createdAt, nowMillis()
                        )
                    )
                    if (account.loggedIn) {
                        db.execSQL("INSERT INTO `session` (`slot`, `userId`, `loggedInAt`) VALUES (0, ?, ?)", arrayOf<Any>(owner, nowMillis()))
                    }
                }
                val o = "'" + owner + "'"

                db.execSQL("CREATE TABLE `employees_new` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `name` TEXT NOT NULL, `mobile` TEXT NOT NULL, `designation` TEXT NOT NULL, `department` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO `employees_new` SELECT `uid`, $o, `name`, `mobile`, `designation`, `department`, `createdAt`, `createdAt` FROM `employees`")

                db.execSQL("CREATE TABLE `attendance_new` (`employeeId` TEXT NOT NULL, `date` TEXT NOT NULL, `status` TEXT NOT NULL, `inTime` TEXT, `outTime` TEXT, `overtimeHours` REAL NOT NULL, `absentReason` TEXT NOT NULL, `remarks` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, `id` TEXT NOT NULL, `userId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`employeeId`, `date`))")
                db.execSQL(
                    "INSERT INTO `attendance_new` SELECT COALESCE(e.`uid`, 'employee-' || a.`employeeId`), a.`date`, a.`status`, a.`inTime`, a.`outTime`, " +
                        "a.`overtimeHours`, a.`absentReason`, a.`remarks`, a.`updatedAt`, $SqlUuid, $o, a.`updatedAt` " +
                        "FROM `attendance` a LEFT JOIN `employees` e ON e.`id` = a.`employeeId`"
                )

                db.execSQL("CREATE TABLE `advances_new` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `employeeId` TEXT NOT NULL, `date` TEXT NOT NULL, `amount` REAL NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL(
                    "INSERT INTO `advances_new` SELECT v.`uid`, $o, COALESCE(e.`uid`, 'employee-' || v.`employeeId`), v.`date`, v.`amount`, v.`createdAt`, v.`createdAt` " +
                        "FROM `advances` v LEFT JOIN `employees` e ON e.`id` = v.`employeeId`"
                )

                db.execSQL("CREATE TABLE `sites_new` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO `sites_new` SELECT `uid`, $o, `name`, `createdAt`, `createdAt` FROM `sites`")

                // Contacts had no time of their own; they take their site's
                db.execSQL("CREATE TABLE `site_contacts_new` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `siteId` TEXT NOT NULL, `name` TEXT NOT NULL, `mobile` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL(
                    "INSERT INTO `site_contacts_new` SELECT c.`uid`, $o, COALESCE(s.`uid`, ''), c.`name`, c.`mobile`, " +
                        "COALESCE(s.`createdAt`, $SqlNow), COALESCE(s.`createdAt`, $SqlNow) " +
                        "FROM `site_contacts` c LEFT JOIN `sites` s ON s.`id` = c.`siteId`"
                )

                db.execSQL("CREATE TABLE `site_notes_new` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `siteId` TEXT NOT NULL, `title` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isBill` INTEGER NOT NULL DEFAULT 0, `showSignature` INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(`id`))")
                db.execSQL(
                    "INSERT INTO `site_notes_new` SELECT n.`uid`, $o, COALESCE(s.`uid`, ''), n.`title`, n.`createdAt`, n.`createdAt`, n.`isBill`, n.`showSignature` " +
                        "FROM `site_notes` n LEFT JOIN `sites` s ON s.`id` = n.`siteId`"
                )

                db.execSQL("CREATE TABLE `note_tables_new` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `noteId` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `hideInPdf` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))")
                db.execSQL(
                    "INSERT INTO `note_tables_new` SELECT t.`uid`, $o, COALESCE(n.`uid`, ''), t.`name`, t.`createdAt`, t.`createdAt`, t.`hideInPdf` " +
                        "FROM `note_tables` t LEFT JOIN `site_notes` n ON n.`id` = t.`noteId`"
                )

                // An item outside any table (tableId 0) keeps an empty tableId
                db.execSQL("CREATE TABLE `note_items_new` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `noteId` TEXT NOT NULL, `tableId` TEXT NOT NULL, `item` TEXT NOT NULL, `thickness` TEXT NOT NULL, `thicknessUnit` TEXT NOT NULL, `width` TEXT NOT NULL, `widthUnit` TEXT NOT NULL, `length` TEXT NOT NULL, `lengthUnit` TEXT NOT NULL, `quantity` INTEGER, `rate` REAL, `remarks` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL(
                    "INSERT INTO `note_items_new` SELECT i.`uid`, $o, COALESCE(n.`uid`, ''), COALESCE(t.`uid`, ''), i.`item`, " +
                        "i.`thickness`, i.`thicknessUnit`, i.`width`, i.`widthUnit`, i.`length`, i.`lengthUnit`, i.`quantity`, i.`rate`, " +
                        "i.`remarks`, i.`createdAt`, i.`createdAt` " +
                        "FROM `note_items` i LEFT JOIN `site_notes` n ON n.`id` = i.`noteId` LEFT JOIN `note_tables` t ON t.`id` = i.`tableId`"
                )

                db.execSQL("CREATE TABLE `bill_adjustments_new` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `noteId` TEXT NOT NULL, `type` TEXT NOT NULL, `amount` REAL NOT NULL, `hideInPdf` INTEGER NOT NULL DEFAULT 0, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL(
                    "INSERT INTO `bill_adjustments_new` SELECT b.`uid`, $o, COALESCE(n.`uid`, ''), b.`type`, b.`amount`, b.`hideInPdf`, b.`createdAt`, b.`createdAt` " +
                        "FROM `bill_adjustments` b LEFT JOIN `site_notes` n ON n.`id` = b.`noteId`"
                )

                RecordTables.forEach { table ->
                    db.execSQL("DROP TABLE `$table`")
                    db.execSQL("ALTER TABLE `${table}_new` RENAME TO `$table`")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_${table}_userId` ON `$table` (`userId`)")
                }
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_date` ON `attendance` (`date`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_attendance_id` ON `attendance` (`id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_advances_employeeId` ON `advances` (`employeeId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_advances_date` ON `advances` (`date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_site_contacts_siteId` ON `site_contacts` (`siteId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_site_notes_siteId` ON `site_notes` (`siteId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_tables_noteId` ON `note_tables` (`noteId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_items_noteId` ON `note_items` (`noteId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_items_tableId` ON `note_items` (`tableId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bill_adjustments_noteId` ON `bill_adjustments` (`noteId`)")

                createRecordTriggers(db)
            }
        }

        /** Removes the unused advances table. */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `advances`")
            }
        }
    }
}
