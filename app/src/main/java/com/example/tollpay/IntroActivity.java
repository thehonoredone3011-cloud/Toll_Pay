package com.example.tollpay;;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

public class IntroActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private IntroPagerAdapter adapter;
    private Button prevButton, nextButton;
    private LinearLayout dotsLayout;
    private int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3}; // Replace with actual images
    private String[] texts = {"Register your vehicle easily in our app and enjoy the facility of paying toll online.",
            "The secure payment feature in the Toll pay app ensures that users can conveniently and safely complete their toll transactions.",
            "The Toll pay app is equipped with FASTag integration, a convenient and efficient way for users to make toll payments."};
    private ImageView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        // Optional: Make status bar icons dark for better visibility on light backgrounds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        viewPager = findViewById(R.id.viewPager);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        dotsLayout = findViewById(R.id.dotsLayout);

        adapter = new IntroPagerAdapter(this, images, texts);
        viewPager.setAdapter(adapter);

        // Initialize dots and set them to the initial position
        initDots(images.length);
        updateDots(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateButtons(position);
                updateDots(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        nextButton.setOnClickListener(view -> {
            int nextPosition = viewPager.getCurrentItem() + 1;
            if (nextPosition < adapter.getCount()) {
                viewPager.setCurrentItem(nextPosition);
            } else {
                navigateToLogin(); // Final page shows "Login"
            }
        });

        prevButton.setOnClickListener(view -> {
            int prevPosition = viewPager.getCurrentItem() - 1;
            if (prevPosition >= 0) {
                viewPager.setCurrentItem(prevPosition);
            }
        });
    }

    private void initDots(int count) {
        dots = new ImageView[count];
        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(R.drawable.dot_inactive); // Set inactive dot drawable
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 0, 4, 0);
            dotsLayout.addView(dots[i], params);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageResource(i == position ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void updateButtons(int position) {
        prevButton.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        nextButton.setText(position == adapter.getCount() - 1 ? "Login" : "Next");
    }

    private void navigateToLogin() {
        Intent intent = new Intent(IntroActivity.this, LoginActivity.class); // Replace with LoginActivity
        startActivity(intent);
        finish();
    }
}

