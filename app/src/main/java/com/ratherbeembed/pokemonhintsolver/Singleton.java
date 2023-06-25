package com.ratherbeembed.pokemonhintsolver;

public class Singleton {
    private static Singleton instance;
    private boolean isToggled;

    private Singleton() {
        // Private constructor to prevent instantiation from outside the class
    }

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

    public boolean getData() {
        return isToggled;
    }

    public void setData(boolean data) {
        this.isToggled = data;
    }
}
