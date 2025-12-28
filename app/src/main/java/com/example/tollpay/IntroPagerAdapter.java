package com.example.tollpay;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class IntroPagerAdapter extends PagerAdapter {

    private Context context;
    private int[] images;       // Array of images for each page
    private String[] texts;     // Array of descriptive texts for each page
    private LayoutInflater inflater;

    public IntroPagerAdapter(Context context, int[] images, String[] texts) {
        this.context = context;
        this.images = images;
        this.texts = texts;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return images.length; // Number of pages
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.intro_page, container, false);

        ImageView imageView = view.findViewById(R.id.introImage);
        TextView textView = view.findViewById(R.id.introText);

        imageView.setImageResource(images[position]);  // Set the image for the current position
        textView.setText(texts[position]);             // Set the text for the current position

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
