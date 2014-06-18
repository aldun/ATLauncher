/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.atlauncher.App;

public final class Timestamper {
    private static final SimpleDateFormat format = new SimpleDateFormat(
            App.settings.getDateFormat() + " HH:mm:ss a");

    public static String now() {
        return format.format(new Date());
    }

    public static String was(Date date) {
        return format.format(date);
    }

    public static void updateDateFormat() {
        format.applyLocalizedPattern(App.settings.getDateFormat() + " HH:mm:ss a");
    }
}