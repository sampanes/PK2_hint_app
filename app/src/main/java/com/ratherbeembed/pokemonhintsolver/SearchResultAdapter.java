package com.ratherbeembed.pokemonhintsolver;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {
    private List<SearchResult> searchResults;


    public SearchResultAdapter(List<SearchResult> searchResults, boolean toggleEnabled) {
        this.searchResults = searchResults;
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        SearchResult searchResult = searchResults.get(position);
        holder.bind(searchResult);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public static class SearchResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nameTextView;
        private ImageView imageView;
        private String textToCopy;
        private Singleton mySingleton;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            imageView = itemView.findViewById(R.id.imageView);
            mySingleton = Singleton.getInstance();
            // Set the click listener for the item view
            itemView.setOnClickListener(this);
        }

        public void bind(SearchResult searchResult) {
            nameTextView.setText(searchResult.getPrefix());
            // Store the text to copy to the clipboard
            textToCopy = searchResult.getTextToCopy();
            // Load the image using Picasso
            Picasso.get().load(searchResult.getUrl()).fit().centerInside().into(imageView);
        }

        @Override
        public void onClick(View v) {
            // Copy the text to the clipboard
            String fullText;
            mySingleton = Singleton.getInstance();
            boolean toggleEnabled = mySingleton.getData();
            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (toggleEnabled){
                fullText = "@pokétwo c " + textToCopy;
            }
            else {
                fullText = textToCopy;
            }
            ClipData clip = ClipData.newPlainText("Copied Text", fullText);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(v.getContext(), "Clipboard: " + fullText, Toast.LENGTH_SHORT).show();
        }
    }

    public interface ToggleSwitchListener {
        void onToggleSwitchChanged(boolean isChecked);
    }

}

