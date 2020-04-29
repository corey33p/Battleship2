package com.example.battleship;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.MotionEvent;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Handler;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    final Handler handler = new Handler(); // used for performing delayed actions
    int cellSize = getScreenWidth()/10;
    int gridImgOriginalSize;
    boolean playerOnesTurn = true;
    boolean otherGrid = false;
    boolean placingShips = true;
    int[][] previewLocations;
    boolean validTentativeShipLocation = false;
    boolean justMadeRandomShips = false;
    boolean buttonAutoShipFlag = true;
    boolean waiting = true;
    int shipsPlaced = 0;
    int[] lastShipDemoLocation = {-1,-1};
    float[] lastTouchLocation;
    boolean placingShipHorizontal = true;
    String[] shipNames = {"Cruiser","Submarine","Destroyer","Battleship","Carrier"};
    int[] shipLengths = {3,3,4,4,5};
    Bitmap originalGridImg, playerOneGrid, playerTwoGrid, hitImg, missImg, shipImg, shipPreview;
    Player player = new Player();
    AI ai = new AI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.originalGridImg = drawableToBitmap(getResources().getDrawable(R.drawable.waves_grid));
        this.gridImgOriginalSize = this.originalGridImg.getWidth();
        Bitmap _hitImg = drawableToBitmap(getResources().getDrawable(R.drawable.hit));
        this.hitImg = Bitmap.createScaledBitmap(_hitImg, cellSize, cellSize, true);
        Bitmap _missImg = drawableToBitmap(getResources().getDrawable(R.drawable.miss));
        this.missImg = Bitmap.createScaledBitmap(_missImg, cellSize, cellSize, true);
        Bitmap _shipImg = drawableToBitmap(getResources().getDrawable(R.drawable.ship));
        this.shipImg = Bitmap.createScaledBitmap(_shipImg, cellSize, cellSize, true);
        placeAIShips();
        resetGame();

        // change the height of the ImageView containing the grid image
        ImageView gridImage = findViewById(R.id.gridImage);
        gridImage.getLayoutParams().height = getScreenWidth();

        // setup the switch grid button
        Button clickButton = (Button) findViewById(R.id.switchGridButton);
        clickButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridButtonAction();
            }
        });
        clickButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                autoPlaceShips();
                return false;
            }

            public boolean setOnLongClickListener(View v) {
                autoPlaceShips();
                return false;
            }
        });

        // setup the settings button
        ImageButton imageButton = (ImageButton) findViewById(R.id.settingsButton);
        imageButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsButtonAction();
            }
        });

    }
    private void autoPlaceShips(){
        if (waiting) { return; }
        updateTextView("Placing ships randomly...");
        Random random = new Random();
        while (placingShips){
            int randomOrientation = random.nextInt(2);
            int randomX = random.nextInt(9);
            int randomY = random.nextInt(9);
            int[][] locationsCheck = getShipLocations(randomX,randomY,this.shipLengths[this.shipsPlaced]);
            if (validLocations(locationsCheck,true)) {
                if (randomOrientation==1){
                    this.placingShipHorizontal=!this.placingShipHorizontal;
                }
                this.previewLocations = locationsCheck;
                for (int i = 0; i < previewLocations.length; i++) {
                    this.shipPreview = mergeBitmaps(this.shipPreview, this.shipImg, previewLocations[i][0], previewLocations[i][1]);
                    ImageView mImg = findViewById(R.id.gridImage);
                    mImg.setImageBitmap(this.shipPreview);
                    this.validTentativeShipLocation = true;
                }
                this.player.addShip(shipNames[shipsPlaced],previewLocations);
                playerOneGrid = shipPreview;
                this.shipsPlaced++;
                if (this.shipsPlaced == 5) {
                    this.placingShips = false;
                    updateButtonText("VIEW ENEMY GRID");
                    ImageButton btn = (ImageButton) findViewById(R.id.settingsButton);
                    btn.setImageResource(R.drawable.restart);
                }
            }
        }
        waiting = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTextView("Your turn to shoot!");
                justMadeRandomShips=true;
                if (!otherGrid) {toggleGrid();}
            }
        },1000);
        waiting = false;
    }
    private void placeAIShips(){
        Random random = new Random();
        boolean placingAIShips = true;
        int aiShipsPlaced = 0;
        while (placingAIShips){
            int randomOrientation = random.nextInt(2);
            int randomX = random.nextInt(9);
            int randomY = random.nextInt(9);
            int[][] locationsCheck = getShipLocations(randomX,randomY,this.shipLengths[this.shipsPlaced]);
            if (validLocations(locationsCheck,false)) {
                if (randomOrientation==1){
                    this.placingShipHorizontal=!this.placingShipHorizontal;
                }
                this.ai.addShip(shipNames[shipsPlaced],locationsCheck);
                aiShipsPlaced++;
                if (aiShipsPlaced == 5) {
                    placingAIShips = false;
                }
            }
        }
    }
    private void settingsButtonAction(){
        if (waiting) { return; }
        if (this.placingShips){
            this.placingShipHorizontal=!this.placingShipHorizontal;

            // trigger a screen press at the last touch location to rotate the ship
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis() + 100;
            float x = lastTouchLocation[0];
            float y = lastTouchLocation[1];
            int action = MotionEvent.ACTION_DOWN;
            int metaState = 0;
            MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, metaState);
            ImageView mImg = findViewById(R.id.gridImage);
            onTouchEvent(event);
        } else {
            resetGame();
        }
    }
    private void gridButtonAction(){
        if (waiting) { return; }
        if (this.placingShips){
            if (buttonAutoShipFlag) {
                autoPlaceShips();
                justMadeRandomShips=true;
                this.placingShips = false;
                return;
            }
            if (this.validTentativeShipLocation) {
                this.player.addShip(shipNames[shipsPlaced],previewLocations);
                playerOneGrid = shipPreview;
                this.shipsPlaced++;
                if (this.shipsPlaced < 5){
                    updateTextView("Place your " + shipNames[shipsPlaced]);
                } else {
                    updateButtonText("VIEW ENEMY GRID");
                    this.placingShips = false;
                    ImageButton btn = (ImageButton)findViewById(R.id.settingsButton);
                    btn.setImageResource(R.drawable.restart);
                    waiting = true;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toggleGrid();
                            updateTextView("Your turn to shoot!");
                        }
                    },1000);
                    waiting = false;
                }
            }
        } else {
//            if (this.justMadeRandomShips){
//                this.justMadeRandomShips=false;
//            } else {
//                toggleGrid();
//            }
            toggleGrid();
        }
    }
    private void resetGame(){
        this.playerOneGrid = Bitmap.createScaledBitmap(this.originalGridImg, getScreenWidth(), getScreenWidth(), true);
        this.playerTwoGrid = Bitmap.createScaledBitmap(this.originalGridImg, getScreenWidth(), getScreenWidth(), true);
        this.shipPreview = Bitmap.createScaledBitmap(this.originalGridImg, getScreenWidth(), getScreenWidth(), true);
        this.playerOnesTurn = true;
        updateGrid();
        this.placingShips = true;
        ImageButton btn = (ImageButton)findViewById(R.id.settingsButton);
        btn.setImageResource(R.drawable.rotate);
        player = new Player();
        playerOnesTurn = true;
        otherGrid = false;
        placingShips = true;
        validTentativeShipLocation = false;
        justMadeRandomShips = false;
        shipsPlaced = 0;
        lastShipDemoLocation[0]=-1;
        lastShipDemoLocation[1]=-1;
        buttonAutoShipFlag = true;
        placingShipHorizontal = true;
        updateButtonText("AUTO PLACE SHIPS");
        updateTextView("Game On!");
        waiting = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTextView("Place your cruiser.");
            }
        },1000);
        waiting = false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (waiting) { return false; }

        //
        // placing ships at the beginning of the game
        //
        if (this.placingShips){
            buttonAutoShipFlag = false;
            updateButtonText("CONFIRM SHIP");
            if ((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_MOVE)) {
                int xPixel = (int) event.getX();
                int yPixel = (int) event.getY();
                lastTouchLocation = new float[]{(float)xPixel,(float)yPixel};
                int[] cellXY = getCell(xPixel,yPixel);
                if (cellXY!=this.lastShipDemoLocation){
                    this.shipPreview = this.playerOneGrid;
                    int[][] locationsCheck = getShipLocations(cellXY[0],cellXY[1],this.shipLengths[this.shipsPlaced]);
                    if (validLocations(locationsCheck,true)){
                        this.previewLocations = locationsCheck;
                        for (int i = 0;i<previewLocations.length;i++){
                            this.shipPreview = mergeBitmaps(this.shipPreview, this.shipImg, previewLocations[i][0], previewLocations[i][1]);
                            ImageView mImg = findViewById(R.id.gridImage);
                            mImg.setImageBitmap(this.shipPreview);
                            this.validTentativeShipLocation = true;
                        }
                    }
                }
            }
        }

        //
        // regular game play
        //
        else {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!this.otherGrid) {
                    toggleGrid();
                } else {
                    // get location of touch in pixels
                    int xPixel = (int) event.getX();
                    int yPixel = (int) event.getY();

                    // convert pixel location to grid cell location
                    int[] cellXY = getCell(xPixel,yPixel);
                    int xCell = cellXY[0];
                    int yCell = cellXY[1];

                    // update the grid image
                    shoot(xCell, yCell, this.playerOnesTurn);
                }
            }
        }
        return false;
    }
    private int[] getCell(int pixelX,int pixelY){
        // get location and size of the grid image
        ImageView gridImage = findViewById(R.id.gridImage);
        int[] imageLoc = getImagePositionInsideImageView(gridImage);
        int imageLeft = imageLoc[0];
        int imageTop = imageLoc[1];
        int imageSize = gridImage.getWidth();

        // convert location to grid position
        pixelX -= imageLeft;
        pixelY -= (imageTop + getStatusBarHeight() + 142);
        float cellSize = imageSize / 10;
        int xCell = (int) (pixelX / cellSize);
        int yCell = (int) (pixelY / cellSize);
        int[] result = {xCell,yCell};
        return result;
    }
    private int[][] getShipLocations(int cellX,int cellY,int shipLength){
        int frontOffset = (int) (shipLength - .5)/2;
        int[][] shipLocations = new int[shipLength][2];
        for (int i = 0;i<shipLength;i++) {
            if (this.placingShipHorizontal) {
                shipLocations[i] = new int[]{cellX - frontOffset + i, cellY};
            } else {
                shipLocations[i] = new int[]{cellX,cellY-frontOffset+i};
            }
        }
        return shipLocations;
    }
    private boolean validLocations(int[][] shipLocations,boolean isPlayerOne){
        for (int i = 0;i<shipLocations.length;i++){
            for (int j = 0;j<shipLocations[i].length;j++){
                if ((shipLocations[i][j] < 0) || (shipLocations[i][j] > 9)) {
                    return false;
                }
            }
        }
        if (isPlayerOne) {
            return player.validShipLocation(shipLocations);
        } else {
            return ai.validShipLocation(shipLocations);
        }
    }
    private void toggleGrid(){
        this.otherGrid = !this.otherGrid;
        if (this.otherGrid){
            updateButtonText("VIEW MY GRID");
        } else {
            updateButtonText("VIEW ENEMY GRID");
        }
        updateGrid();
    }
    private void updateTextView(String toThis) {
        TextView textView = findViewById(R.id.topText);
        textView.setText(toThis);
    }
    private void updateButtonText(String toThis){
        Button button = (Button) findViewById(R.id.switchGridButton);
        button.setText(toThis);
    }
    private static int[] getImagePositionInsideImageView(ImageView imageView) {
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
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    private static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private Bitmap mergeBitmaps(Bitmap firstImage, Bitmap secondImage, int cellX, int cellY){
        int _cellSize = getScreenWidth() / 10;
        int pixX = cellX * _cellSize;
        int pixY = cellY * _cellSize;
        Rect gridOverlayBounds = new Rect(0,0,getScreenWidth(),getScreenWidth());
        Rect shotOverlayBounds = new Rect(pixX,pixY,pixX+cellSize,pixY+cellSize);
        Bitmap result = Bitmap.createBitmap(getScreenWidth(), getScreenWidth(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImage, null, gridOverlayBounds, null);
        canvas.drawBitmap(secondImage, null, shotOverlayBounds, null);
        return result;
    }
    private void shoot(int cellX, int cellY,boolean playerOnesTurn){
        boolean hit;
        Bitmap newGrid;
        if (!playerOnesTurn) { // enemy AI's turn
            hit = player.shoot(cellX,cellY);
            ai.shotReport(cellX,cellY,hit);
            if (hit) {
                newGrid = mergeBitmaps(this.playerOneGrid, this.hitImg, cellX, cellY);

            } else {
                newGrid = mergeBitmaps(this.playerOneGrid, this.missImg, cellX, cellY);
            }
            playerOneGrid = newGrid;
        } else { // players turn
            hit = ai.shoot(cellX,cellY);
            if (hit) {
                newGrid = mergeBitmaps(this.playerTwoGrid, this.hitImg, cellX, cellY);
            } else {
                newGrid = mergeBitmaps(this.playerTwoGrid, this.missImg, cellX, cellY);
            }
            playerTwoGrid = newGrid;
        }
        ImageView mImg = findViewById(R.id.gridImage);
        mImg.setImageBitmap(newGrid);

        // update text display
        String player;
        if (playerOnesTurn) { player = "You "; }
        else { player = "Enemy "; }
        String hitOrMiss;
        if (hit) { hitOrMiss = "hit at position "; }
        else { hitOrMiss = "missed at position "; }
        String loc = cellX + ", " + cellY;
        updateTextView(player+hitOrMiss+loc);

        // change the turn
        changeTurn();
    }
    private void updateGrid(){
        ImageView mImg = findViewById(R.id.gridImage);
        if (this.playerOnesTurn!=this.otherGrid) {
            mImg.setImageBitmap(this.playerOneGrid);
        } else {
            mImg.setImageBitmap(this.playerTwoGrid);
        }
    }
    private void changeTurn(){
        updateGrid();
        this.playerOnesTurn = !this.playerOnesTurn;
        this.otherGrid = false;
        waiting = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String turnMessage;
                if (playerOnesTurn) { turnMessage = "Your turn to shoot!"; }
                else { turnMessage = "Enemy's turn to shoot!"; }
                updateTextView(turnMessage);
                toggleGrid();
            }
        },1000);
        if (!playerOnesTurn){
            waiting = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int[] enemyShot = ai.doTurn();
                    shoot(enemyShot[0],enemyShot[1],false);
                }
            },2500); // delay should be at least 1000 due to previous delay
        }
        waiting = false;
    }
    private static Bitmap drawableToBitmap(Drawable drawable) {
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
}
