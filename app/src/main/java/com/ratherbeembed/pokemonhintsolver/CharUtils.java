package com.ratherbeembed.pokemonhintsolver;

public class CharUtils {
    private static final char[] characters = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-', '♂', '♀', ' ', '\'', '’', 'é', ':', '%'
    };

    static int getIndex(char ch) {
        if (ch >= 'a' && ch <= 'z') {
            return ch - 'a';
        } else if (ch >= '0' && ch <= '9') {
            return ch - '0' + 26; // Handle numeric characters
        } else if (ch == '.') {
            return 36;
        } else if (ch == '-') {
            return 37;
        } else if (ch == '♂') {
            return 38;
        } else if (ch == '♀') {
            return 39;
        } else if (ch == ' ') {
            return 40;
        } else if (ch == '\'' || ch == '’') {
            return 41;
        } else if (ch == 'é') {
            return 42;
        } else if (ch == ':') {
            return 43;
        } else if (ch == '%') {
            return 44;
        } else {
            throw new IllegalArgumentException("Invalid character: " + ch);
        }
    }

    static char getCharacter(int index) {
        if (index >= 0 && index < characters.length) {
            return characters[index];
        } else {
            throw new IllegalArgumentException("Invalid index: " + index);
        }
    }
}
