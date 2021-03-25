package com.example.parisbistro.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.parisbistro.R;
import com.example.parisbistro.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseServe extends FirebaseMessagingService {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0){
            String titulo = remoteMessage.getData().get("titulo");
            String mensagem = remoteMessage.getData().get("mensagem");

            sendNotification(titulo, mensagem);

        }

        else if(remoteMessage.getNotification() != null){
            String titulo = remoteMessage.getNotification().getTitle();
            String mensagem = remoteMessage.getNotification().getBody();

            sendNotification(titulo, mensagem);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification(String titulo, String mensagem){

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String canal = getString(R.string.default_notification_channel_id);

        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(this, canal)
                .setSmallIcon(R.drawable.logo_branca)
                .setContentTitle(titulo).setContentText(mensagem)
                .setSound(som)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(canal,"canal",NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0,notificacao.build());

    }



    @Override
    public void onNewToken(@NonNull String s) {

    }
}
