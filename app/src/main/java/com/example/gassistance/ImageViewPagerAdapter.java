package com.example.gassistance;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class ImageViewPagerAdapter extends PagerAdapter {
    private List<Integer> imageList;
    private Context context;

    public ImageViewPagerAdapter(List<Integer> imageList, MainActivity mainActivity) {
        this.imageList = imageList;
        this.context = mainActivity;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (context == null) {
            Log.e("ImageViewPagerAdapter", "Context is null");
            return null;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_image_view_pager, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setImageResource(imageList.get(position));

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
