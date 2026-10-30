package dev.nura.app.thozhil.diary.data

import android.util.Base64
import androidx.room.withTransaction
import dev.nura.app.thozhil.diary.util.SharedPrefHelper
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Signing up, logging in and out, and the profile, kept in the users and session tables. The diary
 * keeps one account per phone. Passwords are stored only as a salted PBKDF2 hash.
 */
@Singleton
class AccountRepository @Inject constructor(
    private val db: AppDatabase,
    private val prefs: SharedPrefHelper
) {
    private val users get() = db.userDao()

    /** The logged-in user, or null while nobody is. */
    val currentUser: Flow<User?> get() = users.observeCurrent()

    suspend fun current(): User? = users.current()

    /**
     * Whether someone is logged in. Called first thing at start-up, so it also finishes moving the
     * account out of SharedPreferences once the database has taken it in.
     */
    suspend fun isLoggedIn(): Boolean {
        val user = users.current()
        prefs.clearLegacyAccount()
        return user != null
    }

    /** True once someone has signed up with a password on this phone. */
    suspend fun hasAccount(): Boolean = users.findAnyWithPassword() != null

    /** The account whose mobile number or email is [identifier]. */
    suspend fun find(identifier: String): User? {
        val id = identifier.trim()
        if (id.isEmpty()) return null
        val user = if (id.contains('@')) users.findByEmail(id.lowercase()) else users.findByMobile(normalizeMobile(id))
        return user?.takeIf { it.passwordHash.isNotEmpty() }
    }

    /**
     * Signs up and logs in. Someone kept from before there were passwords becomes this account, so
     * their records stay theirs; any records without an owner on this phone become theirs too.
     */
    suspend fun createAccount(name: String, mobile: String, email: String, password: String): User {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return db.withTransaction {
            val earlier = users.current()?.takeIf { it.passwordHash.isEmpty() }
            val user = (earlier ?: User(name = name, mobile = "")).copy(
                name = name.trim(),
                mobile = normalizeMobile(mobile),
                email = email.trim().lowercase(),
                passwordSalt = salt.encode(),
                passwordHash = hash(password, salt).encode()
            )
            if (earlier != null) users.update(user) else users.insert(user)
            claimUnownedRecords(user.id)
            users.setSession(Session(userId = user.id))
            user
        }
    }

    /** Logs in when the mobile number or email and password match. */
    suspend fun login(identifier: String, password: String): Boolean {
        val user = find(identifier) ?: return false
        val salt = user.passwordSalt.decode()
        if (!MessageDigest.isEqual(user.passwordHash.decode(), hash(password, salt))) return false
        users.setSession(Session(userId = user.id))
        return true
    }

    /** Sets a new password for the account found by [identifier]. */
    suspend fun resetPassword(identifier: String, newPassword: String): Boolean {
        val user = find(identifier) ?: return false
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        users.update(user.copy(passwordSalt = salt.encode(), passwordHash = hash(newPassword, salt).encode()))
        return true
    }

    /** Saves the edited profile. The mobile number and email also log in, so they change that too. */
    suspend fun saveProfile(user: User) {
        users.update(
            user.copy(
                name = user.name.trim(),
                mobile = normalizeMobile(user.mobile),
                email = user.email.trim().lowercase(),
                businessName = user.businessName.trim(),
                trade = user.trade.trim(),
                businessAddress = user.businessAddress.trim(),
                gstNumber = user.gstNumber.trim().uppercase()
            )
        )
    }

    /** Logs out; the account and its records stay, so the same details log back in. */
    suspend fun logout() {
        users.clearSession()
    }

    private suspend fun claimUnownedRecords(userId: String) {
        users.claimEmployees(userId)
        users.claimAttendance(userId)
        users.claimSites(userId)
        users.claimSiteContacts(userId)
        users.claimSiteNotes(userId)
        users.claimNoteTables(userId)
        users.claimNoteItems(userId)
        users.claimBillAdjustments(userId)
    }

    private fun normalizeMobile(mobile: String): String = mobile.filter { it.isDigit() }.takeLast(10)

    private fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, HASH_ITERATIONS, HASH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.encode(): String = Base64.encodeToString(this, Base64.NO_WRAP)
    private fun String.decode(): ByteArray = Base64.decode(this, Base64.NO_WRAP)

    private companion object {
        const val HASH_ITERATIONS = 10_000
        const val HASH_BITS = 256
    }
}
