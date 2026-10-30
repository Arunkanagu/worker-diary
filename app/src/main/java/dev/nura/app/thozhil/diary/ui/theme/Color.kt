package dev.nura.app.thozhil.diary.ui.theme

import androidx.compose.ui.graphics.Color

// Site Diary palette: a deep green cloth cover, cool ruled paper inside, and a
// measuring-tape amber used sparingly for the ribbon and "today" moments.

/** The diary cover: splash, Home hero and the phone dock. */
val Cover = Color(0xFF163528)
/** Raised surfaces sitting on the cover (pills, chips). */
val CoverHigh = Color(0xFF24493A)
/** Text and icons on the cover that aren't the main white text. */
val OnCoverMuted = Color(0xFFB9CDBF)

/** Measuring-tape amber: ribbon, today markers. Dark text on it, never white. */
val Amber = Color(0xFFE5A13A)
val AmberSoft = Color(0xFFFBEBCF)
val AmberInk = Color(0xFF6B4204)

// The names below are the original kit's; screens use them, so they keep their roles.

/** Field green: progress fills, "present" dots. */
val Sage = Color(0xFF2E6A4F)
/** Soft green borders and tracks. */
val Moss = Color(0xFF9DBBA8)
/** Green tint for avatars, icon wells and selected rows. */
val Mist = Color(0xFFDCE8DF)
/** Cool ruled paper: the page behind everything. */
val Ivory = Color(0xFFF3F5F0)
/** Strong rules and disabled strokes. */
val Sand = Color(0xFFC6CDC4)

/** Primary actions; white text on it is 9:1, and it is 8:1 as text on paper. */
val SageDeep = Color(0xFF1F4D3A)
/** Darkest green, for icons and text on Mist. */
val SageInk = Color(0xFF143326)
/** Sheets and cards: white paper on the cooler page. */
val IvoryCard = Color(0xFFFFFFFF)
/** Hairline rules, table headers and tracks. */
val SandLight = Color(0xFFE5E9E2)
/** Primary text: green-black ink. */
val Ink = Color(0xFF15201A)
/** Secondary text (5.6:1 on paper). */
val InkMuted = Color(0xFF59635B)
/** Input borders (3.3:1 on white, meets the 3:1 component minimum). */
val Line = Color(0xFF8B958D)
val ErrorRed = Color(0xFFB42318)

/** Absence on the dark cover, where ErrorRed is too dark to read (6:1 on Cover). */
val Coral = Color(0xFFF2A196)
/** Presence on the dark cover: a pale field green that reads at a glance. */
val CoverMint = Color(0xFFA8D5B8)

/** Every other table row is tinted with this, like the lines of a register book. */
val TableStripe = Color(0x59E5E9E2)
