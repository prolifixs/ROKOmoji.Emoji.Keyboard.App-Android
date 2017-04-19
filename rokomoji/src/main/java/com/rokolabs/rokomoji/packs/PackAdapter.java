package com.rokolabs.rokomoji.packs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rokolabs.rokomoji.KeyboardService;
import com.rokolabs.rokomoji.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mist on 21.12.16.
 */

public class PackAdapter extends RecyclerView.Adapter<PackHolder> {
    private List<PackData> packDataList = new ArrayList<PackData>();
    private KeyboardService keyboardService;
    private int selectedTab = 0;

    public PackAdapter(KeyboardService kbs, List<PackData> pdl) {
        this.packDataList = pdl;
        this.keyboardService = kbs;
    }

    @Override
    public PackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pack_rv_item, parent, false);
        return new PackHolder(view);
    }

    @Override
    public void onBindViewHolder(final PackHolder holder, final int position) {
        final PackData pack = packDataList.get(position);
        if(pack == null) {
            holder.progressBar.setVisibility(View.VISIBLE);
            return;
        }

        holder.progressBar.setVisibility(View.GONE);
        try {
            Bitmap bm;
            if (selectedTab == position){
                bm = BitmapFactory.decodeFile(pack.iconOn.getPath());
            } else {
                bm = BitmapFactory.decodeFile(pack.iconOff.getPath());
            }
            holder.imageView.setImageBitmap(bm);
        } catch (Exception e) {
            e.printStackTrace();
            holder.textView.setText(pack.name);
        }
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedTab = holder.getAdapterPosition();
                keyboardService.switchBoard(holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return packDataList.size();
    }
}
