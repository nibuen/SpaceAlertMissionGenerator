package com.boarbeard.generator.beimax;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

import static org.junit.Assert.fail;

/**
 * An incredibly dumb SharedPreferences implementation which probably didn't
 * need to be written.  Make that <i>definitely</i> didn't need to be written.
 */
public class BogoSharedPreferences implements SharedPreferences {
    private HashMap<String, Object> map = new HashMap<>();

    @Override
    public Map<String, ?> getAll() {
        return map;
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return map.containsKey(key) ? (String)(map.get(key)) : defValue;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return map.containsKey(key) ? (Set<String>)(map.get(key)) : defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        return map.containsKey(key) ? (Integer)(map.get(key)) : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        return map.containsKey(key) ? (Long)(map.get(key)) : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        return map.containsKey(key) ? (Float)(map.get(key)) : defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return map.containsKey(key) ? (Boolean)(map.get(key)) : defValue;
    }

    @Override
    public boolean contains(String key) {
        return map.containsKey(key);
    }

    @Override
    public Editor edit() {
        fail("not implemented");
        return null;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        fail("not implemented");
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        fail("not implemented");
    }

    /**
     * Not part of the SharedPreferences API; added so that we can set
     * preference values in unit tests.
     */
    public void set(String key, String val) {
        if (val != null) map.put(key, val);
        else map.remove(key);
    }
    /**
     * Not part of the SharedPreferences API; added so that we can set
     * preference values in unit tests.
     */
    public void set(String key, boolean val) {
        map.put(key, val);
    }
    /**
     * Not part of the SharedPreferences API; added so that we can set
     * preference values in unit tests.
     */
    public void set(String key, int val) {
        map.put(key, val);
    }
}
