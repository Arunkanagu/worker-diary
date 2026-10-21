package dev.nura.app.thozhil.diary.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Employee::class, Attendance::class, Advance::class, Site::class, SiteContact::class, SiteNote::class, NoteItem::class, NoteTable::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun advanceDao(): AdvanceDao
    abstract fun siteDao(): SiteDao
    abstract fun noteItemDao(): NoteItemDao
    abstract fun noteTableDao(): NoteTableDao

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
    }
}
