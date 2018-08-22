package com.zgr.runnzzer;

import android.app.Activity;
import android.support.annotation.NonNull;

public class Transitions {


    public static void openTransition (@NonNull Activity activity){
        activity.overridePendingTransition(R.anim.open_enter, R.anim.open_exit);
    }

    public static void closeTransition (@NonNull Activity activity){
        activity.overridePendingTransition(R.anim.close_enter, R.anim.close_exit);
    }

}
