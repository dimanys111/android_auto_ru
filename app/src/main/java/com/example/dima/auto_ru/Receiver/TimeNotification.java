package com.example.dima.auto_ru.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.dima.auto_ru.WalkingIconService;


public class TimeNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (WalkingIconService.Ser!=null) {
            WalkingIconService.Ser.otprSoket_();
        }
        else
        {
            //context.startService(new Intent(context, WalkingIconService.class));
        }
    }
}