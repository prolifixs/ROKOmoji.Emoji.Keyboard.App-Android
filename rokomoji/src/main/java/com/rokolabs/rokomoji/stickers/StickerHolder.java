package com.rokolabs.rokomoji.stickers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.rokolabs.rokomoji.R;

/**
 * Created by mist on 12.12.16.
 */

public class StickerHolder  extends RecyclerView.ViewHolder {
    public ImageView imageView;

    public StickerHolder(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.imageItem);
    }
}
