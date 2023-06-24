package com.ratherbeembed.pokemonhintsolver;

public class TrieUpdater {
    public static void updateTrie(TrieNode root, String name, String url) {
        TrieNode currentNode = root;

        // Insert each character of the name into the trie
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '\ufe0f') {
                continue; // Skip the "\ufe0f" character
            }
            char lowerC = Character.toLowerCase(c);
            // System.out.println("Inserting character: " + lowerC); // Print the lowercase character

            int index = CharUtils.getIndex(lowerC);

            if (currentNode.getChildren()[index] == null) {
                currentNode.getChildren()[index] = new TrieNode();
            }

            currentNode = currentNode.getChildren()[index];
        }


        // Check if the node is already marked as the end of the word
        if (currentNode.isEndOfWord()) {
            // Node already exists with the same name, check if the URL conflicts
            if (!currentNode.getURL().equals(url)) {
                throw new RuntimeException("URL conflict for name: " + name);
            }
        } else {
            // Mark the node as the end of the word and associate the URL with it
            currentNode.setEndOfWord(true);
            currentNode.setURL(url);
        }
    }
}
