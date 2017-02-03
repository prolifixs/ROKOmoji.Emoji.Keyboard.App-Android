package com.rokolabs.keyboard.packs;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rokolabs.keyboard.R;

public class PackHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public TextView textView;

    public PackHolder(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.rv_item_image);
        textView = (TextView) itemView.findViewById(R.id.rv_item_text);
    }
}