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
        val id: Int? = null,
        val email: String,
        val password: String,
        val name: String? = null
    )

    suspend fun signUp(email: String, password: String, name: String? = null): Result<Unit> {
        // --- VALIDASI INPUT ---
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email dan password tidak boleh kosong."))
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
            val user = User(email = email, password = hashedPassword, name = name)
            supabase.postgrest.from("users").insert(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mendaftar. Silakan periksa koneksi internet dan coba lagi."))
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
                if (user.password == hashedPassword) {
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
