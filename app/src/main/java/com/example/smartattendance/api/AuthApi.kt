package com.example.smartattendance.api

import android.util.Patterns
import com.example.smartattendance.utils.SessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import java.security.MessageDigest
import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.postgrest

object AuthApi {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://cioxpsttwaltmiblakrg.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNpb3hwc3R0d2FsdG1pYmxha3JnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjAxNzU1MzIsImV4cCI6MjA3NTc1MTUzMn0.nvvRU_WCG_uC36EtFr-5aqGOlIRRBiwqC_7sYRMPL84"
    ) {
        install(Postgrest)
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    @Serializable
    data class User(
        val user_id: String? = null,
        val full_name: String? = null,
        val nim: String? = null,
        val email: String,
        val password_hash: String,
        val role: String = "student",
        val created_at: String? = null,
        val updated_at: String? = null
    )

    suspend fun signUp(email: String, password: String, fullName: String? = null, nim: String? = null): Result<Unit> {
        // --- VALIDASI INPUT ---
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email dan password tidak boleh kosong."))
        }
        if (fullName.isNullOrBlank()) {
            return Result.failure(Exception("Nama lengkap tidak boleh kosong."))
        }
        if (nim.isNullOrBlank()) {
            return Result.failure(Exception("NIM tidak boleh kosong."))
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Format email tidak valid."))
        }
        if (password.length < 6) {
            return Result.failure(Exception("Password minimal harus 6 karakter."))
        }
        // --- AKHIR VALIDASI ---

        return try {
            val hashedPassword = hashPassword(password)
            val user = User(
                user_id = nim, // Use NIM as user_id
                full_name = fullName,
                nim = nim,
                email = email,
                password_hash = hashedPassword,
                role = "student"
            )
            supabase.postgrest.from("users").insert(user)
            Result.success(Unit)
        } catch (e: Exception) {
            // Log the actual error for debugging
            println("SignUp Error: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()

            val errorMessage = when {
                e.message?.contains("duplicate", ignoreCase = true) == true ||
                e.message?.contains("unique", ignoreCase = true) == true ||
                e.message?.contains("already exists", ignoreCase = true) == true ->
                    "Email atau NIM sudah terdaftar. Silakan gunakan yang lain."

                e.message?.contains("UnknownHostException", ignoreCase = true) == true ||
                e.message?.contains("ConnectException", ignoreCase = true) == true ||
                e.message?.contains("SocketTimeoutException", ignoreCase = true) == true ||
                e.message?.contains("network", ignoreCase = true) == true ||
                e.message?.contains("internet", ignoreCase = true) == true ->
                    "Tidak ada koneksi internet. Silakan periksa koneksi Anda dan coba lagi."

                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Koneksi timeout. Silakan coba lagi."

                e.message?.contains("unauthorized", ignoreCase = true) == true ||
                e.message?.contains("401", ignoreCase = true) == true ->
                    "Tidak diizinkan. Periksa konfigurasi aplikasi."

                e.message?.contains("forbidden", ignoreCase = true) == true ||
                e.message?.contains("403", ignoreCase = true) == true ->
                    "Akses ditolak. Periksa konfigurasi database."

                e.message?.contains("bad request", ignoreCase = true) == true ||
                e.message?.contains("400", ignoreCase = true) == true ->
                    "Data tidak valid. Periksa format input."

                e.message?.isNotBlank() == true ->
                    "Error: ${e.message}"

                else -> "Gagal mendaftar. Error tidak diketahui."
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun login(email: String, password: String, sessionManager: SessionManager): Result<User> {
        // --- VALIDASI INPUT ---
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email dan password tidak boleh kosong."))
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Format email tidak valid."))
        }
        // --- AKHIR VALIDASI ---

        return try {
            val response = supabase.postgrest.from("users")
                .select {
                    filter {
                        eq("email", email)
                    }
                }.decodeList<User>()

            if (response.isNotEmpty()) {
                val user = response.first()
                val hashedPassword = hashPassword(password)
                if (user.password_hash == hashedPassword) {
                    sessionManager.saveUser(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Email atau password salah"))
                }
            } else {
                Result.failure(Exception("Email atau password salah"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal login: ${'$'}{e.message}"))
        }
    }
}
