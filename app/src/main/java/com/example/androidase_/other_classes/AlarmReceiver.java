package com.example.androidase_.other_classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.androidase_.mqtt.MqttMessageService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myService = new Intent(context, MqttMessageService.class);
        context.startService(myService);
    }
}