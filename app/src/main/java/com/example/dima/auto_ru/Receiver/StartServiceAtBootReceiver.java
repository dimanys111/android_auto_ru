package com.example.dima.auto_ru.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.dima.auto_ru.WalkingIconService;


public class StartServiceAtBootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, WalkingIconService.class));
    }
}