package com.example.androidase_;

import android.util.Log;

import com.example.androidase_.activities.LoginActivity;

import junit.framework.TestCase;

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
        TestCase.assertTrue(b);
        b = activity.checkUsernameAndPassword( "username","username", "username", "username");
        TestCase.assertTrue(b);
        b = activity.checkUsernameAndPassword( "username","username", "username", "password");
        TestCase.assertFalse(b);
        b = activity.checkUsernameAndPassword( "username","username", "password", "username");
        TestCase.assertFalse(b);
        b = activity.checkUsernameAndPassword( "username","password", "username", "username");
        TestCase.assertFalse(b);
        b = activity.checkUsernameAndPassword( "password","username", "username", "username");
        TestCase.assertFalse(b);

        b = activity.checkUsernameAndPassword( "username","username", "password", "password");
        TestCase.assertFalse(b);
        b = activity.checkUsernameAndPassword( "username","password", "password", "username");
        TestCase.assertFalse(b);
        b = activity.checkUsernameAndPassword( "password","password", "username", "username");
        TestCase.assertFalse(b);
        b = activity.checkUsernameAndPassword( "password","username", "username", "password");
        TestCase.assertFalse(b);
        b = activity.checkUsernameAndPassword( "username","password", "username", "password");
        TestCase.assertTrue(b);
        b = activity.checkUsernameAndPassword( "password","username", "password", "username");
        TestCase.assertTrue(b);
    }
}