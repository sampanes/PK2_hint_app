package com.ratherbeembed.pokemonhintsolver;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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
    private RecyclerView adapter;
    private TrieNode pokemonTrie;
    private TextView howManyResults;
    private TextView toggleText;
    private boolean toggleEnabled;
    private Singleton mySingleton;
    private static final String TRIE_FILE_NAME = "trie.ser";
    private static final String JSON_FILE_NAME = "pokemon_dict.json";
    private static final String JSON_FILE_FULL_PATH_NAME = "/data/user/0/com.ratherbeembed.pokemonhintsolver/files/" + JSON_FILE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pokemonTrie = new TrieNode();

        textInput = findViewById(R.id.textInput);
        searchButton = findViewById(R.id.searchButton);
        adapter = findViewById(R.id.recyclerView);
        howManyResults = findViewById(R.id.howManyResults);
        toggleText = findViewById(R.id.toggleText);
        mySingleton = Singleton.getInstance();

        SwitchCompat toggleSwitch = findViewById(R.id.toggleSwitch);
        toggleSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> toggleClicked());

        adapter.setLayoutManager(new GridLayoutManager(this, 2));

        searchButton.setOnClickListener(v -> {
            String searchQuery = textInput.getText().toString();
            Log.d("MainActivity", "Search Pressed: " + searchQuery);

            List<SearchResult> searchResults = pokemonTrie.searchTrie(searchQuery);
            Log.d("MainActivity", "Searched trie, searchResults: " + searchResults);

            String resultText = getResultText(searchResults.size(), searchQuery);
            howManyResults.setText(resultText);

            SearchResultAdapter resultAdapter = new SearchResultAdapter(searchResults, toggleEnabled);
            adapter.setAdapter(resultAdapter);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pokemonTrie == null) {
            Log.e("MainActivity", "onResume, pokemonTrie is null");
            pokemonTrie = loadTrieFileFromInternalStorage();
        }
        if (pokemonTrie == null || pokemonTrie.isEmpty()) {
            Log.e("MainActivity", "onResume, pokemonTrie is " + (pokemonTrie == null ? "null" : "empty"));
            if (jsonFileExists()) {
                Log.e("MainActivity", "onResume, json file exists, create trie and serialize");
                createTrieFromJsonFile();
                serializeTrieToFile();
            } else {
                Log.e("MainActivity", "onResume, json file does not exist, download, create trie, serialize");
                downloadJsonAndCreateTrie();
                serializeTrieToFile();
            }
        }
    }

    private void downloadJsonAndCreateTrie() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://github.com/sampanes/poketwo_json_to_trie/raw/master/src/pokemon_dict.json")
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
                    // Get the JSON string from the response
                    String json = response.body().string();

                    // Save the JSON string to a file
                    saveJsonToFile(json);

                    Log.d("MainActivity", "JSON file downloaded and saved.");
                } else {
                    Log.e("MainActivity", "Failed to download JSON file: " + response.code() + " - " + response.message());
                }
                response.close();
            }

        });
    }

    private void saveJsonToFile(String json) {
        try {
            // Create a file object with the desired file name
            File file = new File(getFilesDir(), JSON_FILE_NAME);

            // Write the JSON string to the file
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            Log.e("MainActivity", "Failed to save JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void serializeTrieToFile() {
        try {
            File file = new File(getFilesDir(), "trie.ser");
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(pokemonTrie);
            out.close();
            fileOut.close();
            Log.d("MainActivity", "Trie serialized.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createTrieFromJsonFile() {
        Gson gson = new Gson();

        try (FileReader fileReader = new FileReader(JSON_FILE_FULL_PATH_NAME)) {
            JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
            int skipped_pks = 0;

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String pokemonName = entry.getKey();
                JsonElement jsonValue = entry.getValue();

                if (jsonValue.isJsonObject()) {
                    JsonObject pokemonDataJson = jsonValue.getAsJsonObject();
                    JsonElement nameElement = pokemonDataJson.get("Name");
                    JsonElement urlElement = pokemonDataJson.get("URL");

                    if (skipped_pks == 0) {
                        Log.d("MainTrieFromJson", "First Skipped Pokemon Data: " + pokemonDataJson);
                    }

                    if (nameElement != null && nameElement.isJsonPrimitive() && urlElement != null && urlElement.isJsonPrimitive()) {
                        String name = nameElement.getAsString();
                        String url = urlElement.getAsString();

                        // Replace the symbol if present in the Pokemon name
                        if (name.contains("\u2728")) {
                            name = name.replace("\u2728", "shiny");
                        }
                        // Replace the symbol if present in the Pokemon name
                        if (name.contains("&")) {
                            name = name.replace("&", "and");
                        }

                        Log.d("MainTrieFromJson", "Name: " + name);
                        Log.d("MainTrieFromJson", "URL: " + url);

                        TrieUpdater.updateTrie(pokemonTrie, name, url);
                    } else {
                        skipped_pks += 1;
                        if (skipped_pks < 5) {
                            Log.d("MainTrieFromJson", "Skipping Pokemon: " + pokemonName);
                            Log.d("MainTrieFromJson", "nameElement: " + nameElement);
                            Log.d("MainTrieFromJson", "urlElement: " + urlElement);
                        }
                    }
                } else {
                    Log.d("MainTrieFromJson", "Not Object.. Invalid JSON entry for Pokemon: " + pokemonName);
                }
            }

            if (skipped_pks > 0) {
                Log.d("MainTrieFromJson", "Lots of non-pokemon, probably shinys with funky star: " + skipped_pks);
            }
        } catch (IOException e) {
            Log.e("MainTrieFromJson", "error calling new FileReader " + JSON_FILE_FULL_PATH_NAME + "\nerror: " + e);
            e.printStackTrace();
        }
    }


    private boolean jsonFileExists() {
        File file = new File(getFilesDir(), JSON_FILE_NAME);
        boolean exists = file.exists();
        Log.d("MainActivity", "JSON file exists: " + exists);
        Log.d("MainActivity", "JSON file path: " + file.getAbsolutePath());
        return exists;
    }

    private List<SearchResult> createExampleSearchResults() {
        List<SearchResult> searchResults = new ArrayList<>();
        searchResults.add(new SearchResult("Mufikin Pikka", "https://cdn.poketwo.net/images/50032.png?v=26"));
        searchResults.add(new SearchResult("dedass sanshrw B", "https://cdn.poketwo.net/images/50023.png?v=26"));
        searchResults.add(new SearchResult("a cute lil bug", "https://cdn.poketwo.net/images/50094.png?v=26"));
        return searchResults;
    }

    private String getResultText(int resultCount, String searchQuery) {
        String resultText = resultCount + " result" + (resultCount == 1 ? "" : "s") + " for \"" + searchQuery + "\"";
        return resultText;
    }

    private TrieNode loadTrieFileFromInternalStorage() {
        TrieNode trie = null;
        try {
            File file = new File(getFilesDir(), TRIE_FILE_NAME);
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                trie = TrieDeserializer.deserializeTrie(fileInputStream);
                Log.d("MainActivity", "Trie file loaded from internal storage.");
            } else {
                Log.d("MainActivity", "Trie file does not exist in internal storage.");
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to load trie file: " + e.getMessage());
            e.printStackTrace();
        }
        return trie;
    }

    public void toggleClicked() {
        // Handle the toggle click event here
        SwitchCompat toggleSwitch = findViewById(R.id.toggleSwitch);
        if (toggleSwitch.isChecked()) {
            toggleEnabled = true;
            mySingleton.setData(true);
            toggleText.setText("Full Command");
        } else {
            toggleEnabled = false;
            mySingleton.setData(false);
            toggleText.setText("Name Only");
        }
    }

}


