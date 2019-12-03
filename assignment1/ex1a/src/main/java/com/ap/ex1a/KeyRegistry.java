package com.ap.ex1a;

import java.util.HashMap;

public class KeyRegistry {
    private HashMap<Class, String> registry;

    public KeyRegistry() {
        this.registry = new HashMap<>();
    }

    public void add(Class c, String key) {
        registry.put(c, key);
    }

    public String get(Class c) {
        return registry.get(c);
    }
}
