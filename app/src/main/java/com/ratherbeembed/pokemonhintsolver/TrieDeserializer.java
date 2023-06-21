package com.ratherbeembed.pokemonhintsolver;

import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class TrieDeserializer {
    public static TrieNode deserializeTrie(FileInputStream fileInputStream) {
        TrieNode root = null;

        try (ObjectInputStream objectIn = new ObjectInputStream(fileInputStream)) {
            Log.d("TrieDeserializer", "Start deserializing trie from file...");

            root = (TrieNode) objectIn.readObject();

            Log.d("TrieDeserializer", "Trie deserialized from file.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.d("TrieDeserializer", "Failed to deserialize trie from file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TrieDeserializer", "An error occurred during trie deserialization: " + e.getMessage());
        }

        return root;
    }
}


