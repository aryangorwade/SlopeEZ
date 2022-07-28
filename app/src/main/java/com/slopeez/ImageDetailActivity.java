package com.slopeez;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.chrisbanes.photoview.PhotoView;
import com.slopeez.flying.stickerview.util.FileUtil;
import com.squareup.picasso.Picasso;

import com.xiaopo.flying.sticker.BitmapStickerIcon;
import com.xiaopo.flying.sticker.DeleteIconEvent;
import com.xiaopo.flying.sticker.DrawableSticker;
import com.xiaopo.flying.sticker.FlipHorizontallyEvent;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;
import com.xiaopo.flying.sticker.ZoomIconEvent;
import com.slopeez.R;

import java.io.File;
import java.util.Arrays;

public class ImageDetailActivity extends AppCompatActivity {

 // initializing everything
    String imgPath;
    private PhotoView photoView;
    private boolean orientationIsLocked = false;
    private StickerView stickerView;
    private Context context;
    public Bitmap photobmp;
    int photobmpwidth, photobmpheight;
    private DrawableSticker sticker;
    public int count = 0;
    final int NUM_PROTRACTORS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up everything ---------------------------------------------------------------------------
        setContentView(R.layout.activity_image_detail);
        stickerView = (StickerView) findViewById(R.id.sticker_view);
        photoView = findViewById(R.id.photo_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //currently you can config your own icons and icon event
        //the event you can customx
        BitmapStickerIcon deleteIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                com.xiaopo.flying.sticker.R.drawable.sticker_ic_close_white_18dp),
                BitmapStickerIcon.LEFT_TOP);
        deleteIcon.setIconEvent(new DeleteIconEvent());

        BitmapStickerIcon zoomIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                com.xiaopo.flying.sticker.R.drawable.sticker_ic_scale_white_18dp),
                BitmapStickerIcon.RIGHT_BOTOM);
        zoomIcon.setIconEvent(new ZoomIconEvent());

        BitmapStickerIcon flipIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                com.xiaopo.flying.sticker.R.drawable.sticker_ic_flip_white_18dp),
                BitmapStickerIcon.RIGHT_TOP);
        flipIcon.setIconEvent(new FlipHorizontallyEvent());

        BitmapStickerIcon heartIcon =
                new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_white_24dp),
                        BitmapStickerIcon.LEFT_BOTTOM);
        // TODO: change hello icon event to "Glad you're loving it!" or smth
        heartIcon.setIconEvent(new HelloIconEvent());

        stickerView.setIcons(Arrays.asList(deleteIcon, zoomIcon, flipIcon, heartIcon));

        //default icon layout
        //stickerView.configDefaultIcons();

        stickerView.setBackgroundColor(Color.WHITE);
        stickerView.setLocked(false);
        stickerView.setConstrained(true);

        stickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            @Override
            public void onStickerAdded(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerAdded");
            }

            @Override
            public void onStickerClicked(@NonNull Sticker sticker) {
                //stickerView.removeAllSticker();
                if (sticker instanceof TextSticker) {
                    ((TextSticker) sticker).setTextColor(Color.RED);
                    stickerView.replace(sticker);
                    stickerView.invalidate();
                }
                Log.d(TAG, "onStickerClicked");
            }

            @Override
            public void onStickerDeleted(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerDeleted");
            }

            @Override
            public void onStickerDragFinished(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerDragFinished");
            }

            @Override
            public void onStickerTouchedDown(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerTouchedDown");
            }

            @Override
            public void onStickerZoomFinished(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerZoomFinished");
            }

            @Override
            public void onStickerFlipped(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerFlipped");
            }

            @Override
            public void onStickerDoubleTapped(@NonNull Sticker sticker) {
                Log.d(TAG, "onDoubleTapped: double tap will be with two click");
            }
        });

        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            toolbar.inflateMenu(R.menu.menu_save);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.item_save) {
                        File file = FileUtil.getNewFile(ImageDetailActivity.this, "Sticker");
                        if (file != null) {
                            stickerView.save(file);
                            Toast.makeText(ImageDetailActivity.this, "saved in " + file.getAbsolutePath(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ImageDetailActivity.this, "the file is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                    //                    stickerView.replace(new DrawableSticker(
                    //                            ContextCompat.getDrawable(MainActivity.this, R.drawable.haizewang_90)
                    //                    ));
                    return false;
                }
            });

            if(savedInstanceState != null)
            {
                count = (savedInstanceState.getInt("count_value"));
            }

            loadSticker(count);
        }

        // ---------------------------------------------------------------------------------------

        // on below line getting data which we have passed from our adapter class.
        imgPath = getIntent().getStringExtra("imgPath");
        // on below line we are getting our image file from its path.
        File imgFile = new File(imgPath);

        if (imgFile.exists()) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 3;
            photobmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), bmOptions);
            photobmpwidth = photobmp.getWidth();
            photobmpheight = photobmp.getHeight();

        }

// TODO: make multiple protractor images: like one white marking and one grey and one black. make bolder markings
// TODO: allow user to select between protractor colors
         // if the file exists then we are loading that image in our image view.
        if (imgFile.exists()) {
           Picasso.get().load(imgFile).placeholder(R.drawable.ic_launcher_background).into(photoView);
       }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("count_value", count);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    // TODO: store the image thumbnails in an array instead of remaking them each time: will make app faster. check if u can move image

    private int getDrawableId(ImageView iv) {
        return (Integer) iv.getTag();
    }

    private void loadSticker(int num) {
        Drawable drawable = null;
        if (num==0)
        {
            drawable = ContextCompat.getDrawable(this, R.drawable.protractor_img_1);
        }
        if (num==1)
        {
            drawable = ContextCompat.getDrawable(this, R.drawable.protractor_img);
        }

        if (num==2)
        {
            drawable = ContextCompat.getDrawable(this, R.drawable.protractor_img_2);
        }
        stickerView.addSticker(new DrawableSticker(drawable));
    }

    public void testReplace(View view) {
        if (stickerView.replace(sticker)) {
            Toast.makeText(ImageDetailActivity.this, "Replace Sticker successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ImageDetailActivity.this, "Replace Sticker failed!", Toast.LENGTH_SHORT).show();
        }
    }

    public void testLock(View view) {
        stickerView.setLocked(!stickerView.isLocked());

        if (!orientationIsLocked) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                Toast.makeText(this, "Landscape and Protractor Locked",
                        Toast.LENGTH_LONG).show();
                orientationIsLocked = true;
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Toast.makeText(this, "Portrait and Protractor Locked",
                        Toast.LENGTH_LONG).show();
                orientationIsLocked = true;
            }
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    Toast.makeText(this, "Landscape and Protractor Unlocked", Toast.LENGTH_LONG).show();
                }
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    Toast.makeText(this, "Portrait and Protractor Unlocked", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void testRemove(View view) {
        if (stickerView.removeCurrentSticker()) {
            Toast.makeText(ImageDetailActivity.this, "Removed current Sticker successfully!", Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(ImageDetailActivity.this, "Removing current Sticker failed!", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void testRemoveAll(View view) {
        stickerView.removeAllStickers();
    }

    public void reset(View view) {
        stickerView.removeAllStickers();
        loadSticker(0);
    }

    public void testAdd(View view) {
        loadSticker(count);
    }

    public void chngProtractor(View view) {
        count++;
        if (count>=NUM_PROTRACTORS) {
            count = 0;
        }
        loadSticker(count);
    }
}



