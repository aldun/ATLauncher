/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.atlauncher.App;
import com.atlauncher.LogManager;

public enum Language {
    INSTANCE;

    private final Map<String, Properties> langs = new HashMap<String, Properties>();
    private volatile String current;

    private Language() {
        try {
            this.load("English");
        } catch (Exception ex) {
            App.settings.logStackTrace(ex);
            ex.printStackTrace(System.err);
        }
    }

    public synchronized void load(String lang) throws IOException {
        if (!this.langs.containsKey(lang)) {
            Properties props = new Properties();
            props.load(new FileInputStream(new File(App.settings.getLanguagesDir(), lang
                    .toLowerCase() + ".lang")));
            this.langs.put(lang, props);
            LogManager.info("Loading Language: " + lang);
        }

        this.current = lang;
    }

    public synchronized String localize(String lang, String tag) {
        if (this.langs.containsKey(lang)) {
            Properties props = this.langs.get(lang);
            if (props.containsKey(tag)) {
                return props.getProperty(tag, tag);
            } else {
                if (lang == "English") {
                    return "Unknown language key " + tag;
                } else {
                    return this.localize("English", tag);
                }
            }
        } else {
            return this.localize("English", tag);
        }
    }

    public synchronized String localize(String tag) {
        return this.localize(this.current, tag);
    }

    public synchronized String getCurrent() {
        return this.current;
    }

    public static String[] available() {
        File[] files = App.settings.getLanguagesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".lang");
            }
        });
        String[] langs = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            langs[i] = files[i].getName().substring(0, 1).toUpperCase()
                    + files[i].getName().substring(1, files[i].getName().lastIndexOf("."));
        }
        return langs;
    }

    public static synchronized String current() {
        return INSTANCE.current;
    }
}