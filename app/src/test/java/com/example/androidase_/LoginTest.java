package com.example.androidase_;

import android.util.Log;

import com.example.androidase_.activities.LoginActivity;

import org.junit.Test;

import static com.example.androidase_.other_classes.MathOperations.measureDistanceInMeters;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LoginTest {
    @Test
    public void testCheckUsernameAndPassword() {
        LoginActivity activity = new LoginActivity();
        boolean b = activity.checkUsernameAndPassword( "","", "", "");
        assertEquals(b, true);
    }
}