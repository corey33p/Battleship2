package com.example.battleship;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.widget.ImageView;
import android.view.MotionEvent;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
import android.content.res.Resources;
import android.graphics.Canvas;

public class MainActivity extends AppCompatActivity {
    int cellSize = getScreenWidth()/10;
 //   Drawable waves = getResources().getDrawable( -700055 );
    Drawable waves = getResources().getDrawable(R.drawable.waves_grid);
//    Drawable waves = ResourcesCompat.getDrawable(this.context.getResources(), R.drawable.waves_grid, null);
    Bitmap gridImg = drawableToBitmap(waves);
    Bitmap _hitImg = drawableToBitmap(getResources().getDrawable(R.drawable.hit));
    Bitmap hitImg = Bitmap.createScaledBitmap(_hitImg, cellSize, cellSize, true);
    Bitmap _missImg = drawableToBitmap(getResources().getDrawable(R.drawable.miss));
    Bitmap missImg = Bitmap.createScaledBitmap(_missImg, cellSize, cellSize, true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // get location of touch in pixels
        int x = (int)event.getX();
        int y = (int)event.getY();

        // get location and size of the grid image
        ImageView gridImage = findViewById(R.id.gridImage);
        int[] imageLoc = getImagePositionInsideImageView(gridImage);
        int imageLeft = imageLoc[0];
        int imageTop = imageLoc[1];
        int imageSize = gridImage.getWidth();

        // convert location to grid position
        x-=imageLeft;
        y-=(imageTop+getStatusBarHeight()+142);
        float cellSize = imageSize / 10;
        int xCell = (int) (x / cellSize);
        int yCell = (int) (y / cellSize);
        String loc = xCell + ", " + yCell;
        updateTextView(loc);

        // update the grid image
        shoot(xCell,yCell,true);
        return false;
    }
    public void updateTextView(String toThis) {
        TextView textView = findViewById(R.id.topText);
        textView.setText(toThis);
    }
    public static int[] getImagePositionInsideImageView(ImageView imageView) {
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - actH)/2;
        int left = (int) (imgViewW - actW)/2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private Bitmap mergeBitmaps(Bitmap firstImage, Bitmap secondImage, int x, int y){
        Bitmap result = Bitmap.createBitmap(firstImage.getWidth(), firstImage.getHeight(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImage, 0f, 0f, null);
        canvas.drawBitmap(secondImage, x, y, null);
        return result;
    }
    private void shoot(int cellX, int cellY, boolean hit){
        int pixX = cellX * cellSize;
        int pixY = cellY * cellSize;
        Bitmap newGrid;
        if (hit) {
            newGrid = mergeBitmaps(gridImg,hitImg,pixX,pixY);
        } else {
            newGrid = mergeBitmaps(gridImg,missImg,pixX,pixY);
        }
        ImageView mImg = findViewById(R.id.gridImage);
        mImg.setImageBitmap(newGrid);
    }
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

//    BufferedImage img = null;
//    try {
//        img = ImageIO.read(new File("strawberry.jpg"));
//    } catch (IOException e) {
//    }
}
