package com.example.smartattendance.api

import kotlinx.serialization.Serializable
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

object MfaApi {

    @Serializable
    data class MfaSettings(
        val mfa_id: Int? = null,
        val user_id: String,
        val mfa_enabled: Boolean = false,
        val mfa_secret: String? = null,
        val backup_codes: List<String>? = null,
        val created_at: String? = null,
        val updated_at: String? = null
    )

    // Generate a random secret key for TOTP (Base32 encoded)
    fun generateSecret(): String {
        val random = SecureRandom()
        val bytes = ByteArray(20)
        random.nextBytes(bytes)
        return encodeBase32(bytes)
    }

    // Generate backup codes (8 codes, 8 characters each)
    fun generateBackupCodes(): List<String> {
        val codes = mutableListOf<String>()
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = SecureRandom()

        repeat(8) {
            val code = (1..8)
                .map { chars[random.nextInt(chars.length)] }
                .joinToString("")
                .chunked(4)
                .joinToString("-")
            codes.add(code)
        }
        return codes
    }

    // Verify TOTP code
    fun verifyTOTP(secret: String, code: String, timeWindow: Int = 1): Boolean {
        val currentTime = System.currentTimeMillis() / 1000 / 30

        // Check current time window and adjacent windows
        for (i in -timeWindow..timeWindow) {
            val timeSlice = currentTime + i
            val generatedCode = generateTOTP(secret, timeSlice)
            if (generatedCode == code) {
                return true
            }
        }
        return false
    }

    // Generate TOTP code for a given time slice
    private fun generateTOTP(secret: String, timeSlice: Long): String {
        val decodedSecret = decodeBase32(secret)
        val data = ByteArray(8)
        var value = timeSlice
        for (i in 7 downTo 0) {
            data[i] = value.toByte()
            value = value shr 8
        }

        val signKey = SecretKeySpec(decodedSecret, "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(signKey)
        val hash = mac.doFinal(data)

        val offset = hash[hash.size - 1].toInt() and 0xf
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                ((hash[offset + 1].toInt() and 0xff) shl 16) or
                ((hash[offset + 2].toInt() and 0xff) shl 8) or
                (hash[offset + 3].toInt() and 0xff)

        val otp = binary % 1000000
        return String.format("%06d", otp)
    }

    // Get TOTP provisioning URI for QR code
    fun getTOTPUri(secret: String, email: String, issuer: String = "SmartAttendance"): String {
        return "otpauth://totp/$issuer:$email?secret=$secret&issuer=$issuer"
    }

    // Base32 encoding
    private fun encodeBase32(data: ByteArray): String {
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        var result = ""
        var bits = 0
        var value = 0

        for (b in data) {
            value = (value shl 8) or (b.toInt() and 0xFF)
            bits += 8
            while (bits >= 5) {
                result += base32Chars[(value shr (bits - 5)) and 0x1F]
                bits -= 5
            }
        }

        if (bits > 0) {
            result += base32Chars[(value shl (5 - bits)) and 0x1F]
        }

        return result
    }

    // Base32 decoding
    private fun decodeBase32(encoded: String): ByteArray {
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val result = mutableListOf<Byte>()
        var bits = 0
        var value = 0

        for (c in encoded.uppercase()) {
            if (c == '=') break
            val index = base32Chars.indexOf(c)
            if (index == -1) continue

            value = (value shl 5) or index
            bits += 5

            if (bits >= 8) {
                result.add(((value shr (bits - 8)) and 0xFF).toByte())
                bits -= 8
            }
        }

        return result.toByteArray()
    }

    // Save MFA settings to database
    suspend fun enableMfa(userId: String, secret: String, backupCodes: List<String>): Result<Unit> {
        return try {
            // Check if settings already exist
            val existing = AuthApi.supabase.from("user_mfa_settings")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<MfaSettings>()

            if (existing != null) {
                // Update existing settings
                AuthApi.supabase.from("user_mfa_settings")
                    .update({
                        MfaSettings::mfa_enabled setTo true
                        MfaSettings::mfa_secret setTo secret
                        MfaSettings::backup_codes setTo backupCodes
                    }) {
                        filter {
                            eq("user_id", userId)
                        }
                    }
            } else {
                // Insert new settings
                val settings = MfaSettings(
                    user_id = userId,
                    mfa_enabled = true,
                    mfa_secret = secret,
                    backup_codes = backupCodes
                )
                AuthApi.supabase.from("user_mfa_settings").insert(settings)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to enable MFA: ${e.message}"))
        }
    }

    // Disable MFA
    suspend fun disableMfa(userId: String): Result<Unit> {
        return try {
            AuthApi.supabase.from("user_mfa_settings")
                .update({
                    MfaSettings::mfa_enabled setTo false
                    MfaSettings::mfa_secret setTo null
                    MfaSettings::backup_codes setTo null
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to disable MFA: ${e.message}"))
        }
    }

    // Get MFA settings for a user
    suspend fun getMfaSettings(userId: String): Result<MfaSettings?> {
        return try {
            val settings = AuthApi.supabase.from("user_mfa_settings")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<MfaSettings>()
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get MFA settings: ${e.message}"))
        }
    }

    // Verify MFA code (TOTP or backup code)
    suspend fun verifyMfaCode(userId: String, code: String): Result<Boolean> {
        return try {
            val settings = getMfaSettings(userId).getOrNull()

            if (settings == null || !settings.mfa_enabled) {
                return Result.success(false)
            }

            // Try TOTP verification first
            if (settings.mfa_secret != null && verifyTOTP(settings.mfa_secret, code)) {
                return Result.success(true)
            }

            // Try backup codes
            if (settings.backup_codes != null && code.replace("-", "") in settings.backup_codes.map { it.replace("-", "") }) {
                // Remove used backup code
                val updatedCodes = settings.backup_codes.filter { it.replace("-", "") != code.replace("-", "") }
                AuthApi.supabase.from("user_mfa_settings")
                    .update({
                        MfaSettings::backup_codes setTo updatedCodes
                    }) {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                return Result.success(true)
            }

            Result.success(false)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to verify MFA code: ${e.message}"))
        }
    }
}

