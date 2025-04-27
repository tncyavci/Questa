package com.example.questa.util

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Long tipindeki zaman değerini formatlanmış bir tarih string'ine dönüştürür
 */
fun Long.formatToDate(pattern: String = "dd.MM.yyyy HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Bir string'in null veya boş olup olmadığını kontrol eder
 */
fun String?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

/**
 * Bir string'in geçerli bir email olup olmadığını kontrol eder
 */
fun String.isValidEmail(): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    return matches(emailRegex)
}

/**
 * Context üzerinden Toast mesajı gösterir
 */
fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

/**
 * Compose için Toast gösterme composable'ı
 */
@Composable
fun ShowToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    val context = LocalContext.current
    LaunchedEffect(message) {
        Toast.makeText(context, message, length).show()
    }
}

/**
 * Belirli bir gecikmeyle bir işlemi gerçekleştirir
 */
suspend fun withDelay(delayMillis: Long, action: () -> Unit) {
    delay(delayMillis)
    action()
} 