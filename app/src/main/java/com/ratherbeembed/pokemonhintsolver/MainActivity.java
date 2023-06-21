package com.ratherbeembed.pokemonhintsolver;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private EditText textInput;
    private Button searchButton;
    private TextView outputText;
    private TrieNode pokemonTrie;
    private boolean trieLoaded = false;
    private static final String TRIE_FILE_NAME = "trie.ser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pokemonTrie = new TrieNode();

        textInput = findViewById(R.id.textInput);
        searchButton = findViewById(R.id.searchButton);
        outputText = findViewById(R.id.outputText);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!trieLoaded) {
                    // If trie is not loaded, show an error message
                    Log.e("MainActivity", "Trie is not loaded. Cannot perform search.");
                    return;
                }
                String searchTerm = textInput.getText().toString();
                List<SearchResult> names_urls = searchInTrie(pokemonTrie, searchTerm);
                Log.d("MainActivity", "Button clicked!");

                if (names_urls != null && !names_urls.isEmpty()) {
                    Log.d("MainActivity", "Results for Pokemon: " + searchTerm);
                    for (SearchResult result : names_urls) {
                        Log.d("MainActivity", "Prefix: " + result.getPrefix());
                        Log.d("MainActivity", "URL: " + result.getUrl());
                        Log.d("MainActivity", "------------------------");
                        loadImageFromUrl(result.getUrl());
                    }
                } else {
                    System.out.println("Pokemon not found: " + searchTerm);
                }
//                outputText.setText("Result: " + names_urls);
//                loadImageFromUrl("https://via.placeholder.com/475x475.png");
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

    private void loadImageFromUrl(String imageUrl) {
        ImageView imageView = findViewById(R.id.imageView);
        Picasso.get().load(imageUrl).into(imageView);
    }
}

