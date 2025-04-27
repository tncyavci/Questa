package com.example.questa.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.questa.MainActivity
import com.example.questa.R
import com.example.questa.util.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class FirebaseMessagingService : FirebaseMessagingService() {

    /**
     * FCM token yenilendiğinde çağrılır
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("Refreshed FCM token: $token")
        // Token'ı sunucuya göndermek için burada işlem yapılabilir
        sendRegistrationToServer(token)
    }
    
    /**
     * Uzak bir mesaj alındığında çağrılır
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Timber.d("From: ${remoteMessage.from}")
        
        // Veri mesajı kontrolü
        remoteMessage.data.isNotEmpty().let {
            Timber.d("Message data payload: ${remoteMessage.data}")
        }
        
        // Bildirim mesajı kontrolü
        remoteMessage.notification?.let {
            Timber.d("Message Notification Body: ${it.body}")
            it.body?.let { body ->
                sendNotification(it.title ?: getString(R.string.app_name), body)
            }
        }
    }
    
    /**
     * FCM token'ını sunucuya kaydetme
     */
    private fun sendRegistrationToServer(token: String) {
        // TODO: Token'ı Firebase veritabanına veya kendi sunucunuza kaydedin
        // Örnek: kullanıcıya ait bir token alanını güncelleyin
    }
    
    /**
     * Bildirim oluşturma ve gösterme
     */
    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = Constants.NOTIFICATION_CHANNEL_ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Android O ve üzeri için bildirim kanalı oluşturma
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        notificationManager.notify(0, notificationBuilder.build())
    }
} 