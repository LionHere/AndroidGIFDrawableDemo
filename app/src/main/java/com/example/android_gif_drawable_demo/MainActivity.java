package com.example.android_gif_drawable_demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    private List<GifImageView> views = new ArrayList<>();
    private Bitmap thumbnail;
    private ScheduledFuture scheduledFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get references from view
        ViewGroup root = findViewById(R.id.root);
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            views.add((GifImageView) root.getChildAt(i));
        }

        // a thumbnail bitmap
        thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.thumbnail);

        // repeat runnable
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        scheduledFuture = executor.scheduleWithFixedDelay(
                getUpdateViewRunnable(),
                500,
                250,
                TimeUnit.MILLISECONDS);
    }

    private Runnable getUpdateViewRunnable() {
        return () -> {
            // randomly grab a GifImageView to update
            ThreadLocalRandom random = ThreadLocalRandom.current();
            GifImageView gifImageView = views.get(random.nextInt(views.size()));

            // set thumbnail
            runOnUiThread(() -> gifImageView.setImageBitmap(thumbnail));

            new Thread(() -> {
                // mock downloading a gif from server
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException canceled) {
                    return;
                }
                // GifImageView#setImageDrawable
                runOnUiThread(() -> {
                    try {
                        GifDrawable gifDrawable = new GifDrawable(getResources(), R.raw.sample);
                        gifImageView.setImageDrawable(gifDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }).start();
        };
    }

    /**
     * irrelevant: stop the executor
     */
    @Override
    protected void onPause() {
        super.onPause();
        scheduledFuture.cancel(true);
        while (!scheduledFuture.isDone()) {
            synchronized(this) {
                try {
                    wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
