package com.example.androidase_;

import com.example.androidase_.chatbox.ChatActivity;

import junit.framework.TestCase;

import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class FireBaseTest {
    @Test
    public void pushToFirebaseTest() {
        ChatActivity activity = new ChatActivity();
        int code = 0;
        try {
            code = activity.pushToFirebase("topic", "title", "message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TestCase.assertEquals(code, 200);
    }
}