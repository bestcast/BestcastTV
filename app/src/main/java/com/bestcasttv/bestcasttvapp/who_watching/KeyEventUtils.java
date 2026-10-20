package com.bestcasttv.bestcasttvapp.who_watching;

import android.view.KeyEvent;

public class KeyEventUtils {
    public static boolean isConfirmButton(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER;
    }
}
