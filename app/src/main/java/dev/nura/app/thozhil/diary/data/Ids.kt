package dev.nura.app.thozhil.diary.data

import java.util.UUID

/**
 * A new record id. Ids are random UUIDs rather than counting up from 1, so records made on different
 * phones never share an id when they're put online together.
 */
fun newId(): String = UUID.randomUUID().toString()

/** Milliseconds since 1970, as every createdAt and updatedAt is stored. */
fun nowMillis(): Long = System.currentTimeMillis()
