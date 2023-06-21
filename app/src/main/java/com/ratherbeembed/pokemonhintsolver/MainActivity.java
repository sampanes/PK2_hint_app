package com.ratherbeembed.pokemonhintsolver;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private EditText textInput;
    private Button searchButton;
    private RecyclerView adapter;
    private TrieNode pokemonTrie;
    private TextView howManyResults;
    private boolean trieLoaded = false;
    private static final String TRIE_FILE_NAME = "trie.ser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pokemonTrie = new TrieNode();

        textInput = findViewById(R.id.textInput);
        searchButton = findViewById(R.id.searchButton);
        adapter = findViewById(R.id.recyclerView);
        howManyResults = findViewById(R.id.howManyResults);

        adapter.setLayoutManager(new GridLayoutManager(this, 2)); // Set GridLayoutManager with 2 columns

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SearchResult> searchResults = new ArrayList<>();

                // Add some example search results
                SearchResult result1 = new SearchResult("Mufikin Pikka", "https://cdn.poketwo.net/images/50032.png?v=26");
                SearchResult result2 = new SearchResult("dedass sanshrw B", "https://cdn.poketwo.net/images/50023.png?v=26");
                SearchResult result3 = new SearchResult("a cute lil bug", "https://cdn.poketwo.net/images/50094.png?v=26");

                searchResults.add(result1);
                searchResults.add(result2);
                searchResults.add(result3);
                searchResults.add(result1);
                searchResults.add(result2);
                searchResults.add(result3);
                searchResults.add(result1);
                searchResults.add(result2);
                searchResults.add(result3);

                String searchQuery = textInput.getText().toString(); // Assuming you have an EditText called textInput for entering the search query
                String resultText;
                int resultCount = searchResults.size();
                if (resultCount == 1) {
                    resultText = resultCount + " result for \"" + searchQuery + "\"";
                } else {
                    resultText = resultCount + " results for \"" + searchQuery + "\"";
                }
                howManyResults.setText(resultText);


                SearchResultAdapter resultAdapter = new SearchResultAdapter(searchResults);
                adapter.setAdapter(resultAdapter); // Set the adapter to the RecyclerView
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "ON RESUME start " );
        pokemonTrie = loadTrieFileFromInternalStorage();
        if (pokemonTrie == null) {
            Log.d("MainActivity", "ON RESUME trie null, download Trie" );
            downloadTrieFile();
        }
        // Add the following line to ensure the trie is initialized before performing the search
        trieLoaded = (pokemonTrie != null);
        Log.d("MainActivity", "ON RESUME end: trieLoaded = " + trieLoaded );
    }

    public static List<SearchResult> searchInTrie(TrieNode root, String prefix) {
        prefix = prefix.toLowerCase(); // Convert prefix to lowercase

        // Perform wildcard search if the prefix contains underscore character
        if (prefix.contains("*")) {
            prefix.replace("*", "_");
        }
        return root.searchTrie(root, prefix);
    }

    private TrieNode loadTrieFileFromInternalStorage() {
        try {
            File file = new File(getFilesDir(), TRIE_FILE_NAME);
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                TrieNode trie = TrieDeserializer.deserializeTrie(fileInputStream);
                Log.d("MainActivity", "Trie file loaded from internal storage.");
                return trie;
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to load trie file: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void downloadTrieFile() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://github.com/sampanes/poketwo_json_to_trie/raw/master/trie.ser")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "Failed to download trie file: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try (InputStream inputStream = response.body().byteStream();
                         FileOutputStream fileOutputStream = openFileOutput(TRIE_FILE_NAME, Context.MODE_PRIVATE)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                        fileOutputStream.flush();

                        // Load trie file into pokemonTrie object
                        FileInputStream fileInputStream = openFileInput(TRIE_FILE_NAME);
                        pokemonTrie = TrieDeserializer.deserializeTrie(fileInputStream);
                        trieLoaded = true;
                        Log.d("MainActivity", "Trie file downloaded and saved.");
                    } catch (IOException e) {
                        Log.e("MainActivity", "Failed to save trie file: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Log.e("MainActivity", "Failed to download trie file: " + response.code() + " - " + response.message());
                }
                response.close();
            }
        });
    }
}

