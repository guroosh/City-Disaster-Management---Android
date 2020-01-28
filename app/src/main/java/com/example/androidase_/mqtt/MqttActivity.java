package com.example.androidase_.mqtt;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidase_.R;
import com.example.androidase_.other_classes.AlarmReceiver;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Calendar;

public class MqttActivity extends AppCompatActivity {
    public static String IP_ADDRESS = "10.6.51.149";
    public static String MQTT_BROKER_URL = "tcp://" + IP_ADDRESS + ":1883";
    @SuppressLint("StaticFieldLeak")
    public static TextView mqttInfo;
    @SuppressLint("StaticFieldLeak")
    public static TextView mqttMetaInfo;
    public int mJobId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);
        mqttInfo = findViewById(R.id.mqtt_info);
        mqttMetaInfo = findViewById(R.id.mqtt_meta_info);
        final EditText sendMessage = findViewById(R.id.publishMessage);
        Button sendMessageButton = findViewById(R.id.publishMessageButton);

        final PahoMqttClient pahoMqttClient = new PahoMqttClient();
        final MqttAndroidClient client = pahoMqttClient.getMqttClient(getApplicationContext(), MQTT_BROKER_URL, -1);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = sendMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    try {
                        pahoMqttClient.publishMessage(client, message, 1, "default");
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                sendMessage.setText("");
            }
        });


        //way 1
//        // wrap your stuff in a componentName
//        ComponentName mServiceComponent = new ComponentName(getApplicationContext(), MqttMessageService.class);
//        // set up conditions for the job
//        JobInfo task = new JobInfo.Builder(mJobId++, mServiceComponent)
//                .setPeriodic(500)
////                .setRequiresCharging(true) // default is "false"
////                .setRequiredNetworkCapabilities(JobInfo.NetworkType.UNMETERED) // Parameter may be "ANY", "NONE" (=default) or "UNMETERED"
//                .build();
//        // inform the system of the job
//        JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        jobScheduler.schedule(task);


        //way 2
//        Intent intent = new Intent(MqttActivity.this, MqttMessageService.class);
//        startService(intent);


        //way 3
        Intent myAlarm = new Intent(getApplicationContext(), AlarmReceiver.class);
        //myAlarm.putExtra("project_id", project_id); //Put Extra if needed
        PendingIntent recurringAlarm = PendingIntent.getBroadcast(getApplicationContext(), 0, myAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Calendar updateTime = Calendar.getInstance();
        //updateTime.setWhatever(0);    //set time to start first occurrence of alarm
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), 500, recurringAlarm); //you can modify the interval of course

    }
}