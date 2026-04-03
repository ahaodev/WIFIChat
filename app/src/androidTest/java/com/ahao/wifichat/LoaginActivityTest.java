package com.ahao.wifichat;

import android.support.test.rule.ActivityTestRule;

import com.ahao.wifichat.ui.me.LoginActivity;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import  android.support.test.espresso.matcher.ViewMatchers;

/**
 * Created by hao.ruan on 2017/11/20.
 */

public class LoaginActivityTest{
    @Rule public ActivityTestRule<LoginActivity> activityTestRule =
            new ActivityTestRule<>(LoginActivity.class);
    @Test
    public void  login(){
        onView(ViewMatchers.withHint("用户名")).check(matches(isDisplayed()));
    }
}
