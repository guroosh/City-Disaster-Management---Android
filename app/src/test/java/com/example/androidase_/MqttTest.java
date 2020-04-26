package com.example.androidase_;

import com.example.androidase_.chatbox.ChatActivity;

import junit.framework.TestCase;

import org.json.JSONException;
import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MqttTest {
    @Test
    public void connectToMqttTest() {
        ChatActivity activity = new ChatActivity();
        activity.connectMQTT();
        TestCase.assertEquals("", "");
    }
}