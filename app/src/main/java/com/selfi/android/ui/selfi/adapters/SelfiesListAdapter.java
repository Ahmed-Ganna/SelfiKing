package com.selfi.android.ui.selfi.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.selfi.android.R;
import com.selfi.android.data.model.SelfiImage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelfiesListAdapter extends BaseAdapter {
    private static final String TAG = SelfiesListAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<SelfiImage> images ;

     static class ViewHolder {
        @BindView(R.id.image)ImageView image;
        @BindView(R.id.like_btn)ImageView likeBtn;
        @BindView(R.id.likes_count)TextView likesCountTv;
        @BindView(R.id.hash_tag_label)TextView labelTv;
        @BindView(R.id.position_label)TextView positionLabel;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public SelfiesListAdapter(Context context) {
        this.context = context;
        images = new ArrayList();
    }

    public int getCount() {
        return this.images.size();
    }

    public SelfiImage getItem(int position) {
        return this.images.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_selfie,null,false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        SelfiImage selfiImage = getItem(position);
        viewHolder.image.setBackgroundColor(selfiImage.getColor()!=0?(int) selfiImage.getColor()
                :context.getResources().getColor(R.color.colorAccent));
        viewHolder.likesCountTv.setText(String.valueOf((int) selfiImage.getLikes()));
        viewHolder.labelTv.setText(TextUtils.isEmpty(selfiImage.getTag())?"#Selfi_king":"#"+selfiImage.getTag());
        viewHolder.positionLabel.setText(String.valueOf(position+1));
        Picasso.with(this.context)
                .load(selfiImage.getImage())
                .into(viewHolder.image);

        return view;
    }

    public void clear() {
        this.images.clear();
        notifyDataSetChanged();
    }

    public void update(ArrayList<SelfiImage> images) {
        this.images.clear();
        this.images = images;
        notifyDataSetChanged();
    }

    public void addMore(ArrayList<SelfiImage> objects) {
        this.images.addAll(objects);
        notifyDataSetChanged();
    }

}


