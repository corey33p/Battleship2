package com.example.battleship;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
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

import java.util.Arrays;
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
    int waiting = 0;
    int shipsPlaced = 0;
    int[] lastShipDemoLocation = {-1,-1};
    float[] lastTouchLocation;
    boolean placingShipHorizontal = true;
    String[] shipNames = {"Cruiser","Submarine","Destroyer","Battleship","Carrier"};
    int[] shipLengths = {2,3,4,4,5};
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
        resetGame();

        // change the height of the ImageView containing the grid image
        ImageView gridImage = findViewById(R.id.gridImage);
        gridImage.getLayoutParams().height = getScreenWidth();

        // setup the switch grid button
        Button leftButton = (Button) findViewById(R.id.leftButton);
        leftButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftButtonAction();
            }
        });
        leftButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (placingShips) {
                    autoPlaceShips();
                }
                return false;
            }

//            public boolean setOnLongClickListener(View v) {
//                autoPlaceShips();
//                return false;
//            }
        });

        // setup the settings button
        Button rightButton = (Button) findViewById(R.id.rightButton);
        rightButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rightButtonAction();
            }
        });

    }
    private void autoPlaceShips(){
        updateTextView("Placing ships randomly...");
        resetGame();
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
                    setGridImage(this.shipPreview);
                    this.validTentativeShipLocation = true;
                }
                this.player.addShip(shipNames[shipsPlaced],previewLocations);
                playerOneGrid = shipPreview;
                this.shipsPlaced++;
                if (this.shipsPlaced == 5) {
                    this.placingShips = false;
                    updateLeftButtonText("VIEW ENEMY\nGRID");
                    updateRightButtonText("NEW\nGAME");
                }
            }
        }
        waiting++;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTextView("Your turn to shoot!");
                justMadeRandomShips=true;
                if (!otherGrid) {toggleGrid();}
                waiting--;
            }
        },1000);
    }
    private void placeAIShips(){
        Random random = new Random();
        boolean placingAIShips = true;
        int aiShipsPlaced = 0;
        while (placingAIShips){
            int randomOrientation = random.nextInt(2);
            int randomX = random.nextInt(9);
            int randomY = random.nextInt(9);
            int[][] locationsCheck = getShipLocations(randomX,randomY,this.shipLengths[aiShipsPlaced]);
            if (validLocations(locationsCheck,false)) {
                if (randomOrientation==1){
                    this.placingShipHorizontal=!this.placingShipHorizontal;
                }
                this.ai.addShip(shipNames[aiShipsPlaced],locationsCheck);
                aiShipsPlaced++;
                if (aiShipsPlaced == 5) {
                    placingAIShips = false;
                }
            }
        }
    }
    private void rightButtonAction(){
        if (waiting>0) { return; }
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
            onTouchEvent(event);
        } else {
            resetGame();
        }
    }
    private void leftButtonAction(){
        if (waiting>0) { return; }
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
                    updateLeftButtonText("VIEW ENEMY\nGRID");
                    this.placingShips = false;
                    updateRightButtonText("NEW\nGAME");
                    waiting++;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toggleGrid();
                            updateTextView("Your turn to shoot!");
                            waiting--;
                        }
                    },1000);
                }
            }
        } else {
            toggleGrid();
        }
    }
    private void resetGame(){
        player.resetGame();
        ai.resetGame();
        placeAIShips();
        this.playerOneGrid = Bitmap.createScaledBitmap(this.originalGridImg, getScreenWidth(), getScreenWidth(), true);
        this.playerTwoGrid = Bitmap.createScaledBitmap(this.originalGridImg, getScreenWidth(), getScreenWidth(), true);
        this.shipPreview = Bitmap.createScaledBitmap(this.originalGridImg, getScreenWidth(), getScreenWidth(), true);
        this.playerOnesTurn = true;
        updateGrid();
        this.placingShips = true;
        updateRightButtonText("ROTATE\nSHIP");
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
        updateLeftButtonText("AUTO PLACE\nSHIPS");
        updateTextView("Game On!");
        waiting++;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTextView("Place your Cruiser");
                waiting--;
            }
        },1000);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (waiting>0) { return false; }

        //
        // placing ships at the beginning of the game
        //
        if (this.placingShips){
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
                            setGridImage(this.shipPreview);
                            this.validTentativeShipLocation = true;
                        }
                        updateLeftButtonText("CONFIRM SHIP");
                        buttonAutoShipFlag = false;
                    }
                }
            }
        }

        //
        // regular game play
        //
        else {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (playerOnesTurn) {
                    if (!this.otherGrid) {
                        toggleGrid();
                    } else {
                        // get location of touch in pixels
                        int xPixel = (int) event.getX();
                        int yPixel = (int) event.getY();

                        // convert pixel location to grid cell location
                        int[] cellXY = getCell(xPixel, yPixel);
                        int xCell = cellXY[0];
                        int yCell = cellXY[1];

                        // shoot the shot
                        try {
                            shoot(xCell, yCell, this.playerOnesTurn);
                        } catch (RuntimeException e) {
                            Log.wtf("WARNING ",e);
                        }
                    }
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
            updateLeftButtonText("VIEW YOUR\nGRID");
        } else {
            updateLeftButtonText("VIEW ENEMY\nGRID");
        }
        updateGrid();
    }
    private void updateTextView(String toThis) {
        TextView textView = findViewById(R.id.topText);
        textView.setText(toThis);
        Log.wtf("in-game message: ",toThis);
    }
    private void updateLeftButtonText(String toThis){
        Button button = (Button) findViewById(R.id.leftButton);
        button.setText(toThis);
    }
    private void updateRightButtonText(String toThis){
        Button button = (Button) findViewById(R.id.rightButton);
        button.setText(toThis);
    }

    // this function is used for finding the position of the grid relative to the screen pixels
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

    // this function is used for finding the position of the grid relative to the screen pixels
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // this function is used for evaluating which grid cell a pixel occupies
    private static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private void shoot(int cellX, int cellY,boolean playerOnesTurn){
        boolean hit;
        boolean sunk = false;
        String shipName = "";
        if (!playerOnesTurn) { // enemy AI's turn
            hit = player.shoot(cellX, cellY);
            ai.shotReport(cellX,cellY,hit);
            if (hit) {
                playerOneGrid = mergeBitmaps(this.playerOneGrid, this.hitImg, cellX, cellY);
                sunk = player.isSunk(cellX,cellY);
                if (sunk) {
                    shipName = player.getName(cellX,cellY);
                    ai.informSunk(shipName);
                }
            } else {
                playerOneGrid = mergeBitmaps(this.playerOneGrid, this.missImg, cellX, cellY);
            }
        } else { // players turn
            try {
                hit = ai.shoot(cellX, cellY);
            } catch (RuntimeException e){
                throw new RuntimeException(e);
            }
            if (hit) {
                playerTwoGrid = mergeBitmaps(this.playerTwoGrid, this.shipImg, cellX, cellY);
                playerTwoGrid = mergeBitmaps(this.playerTwoGrid, this.hitImg, cellX, cellY);
                sunk = ai.isSunk(cellX,cellY);
                if (sunk) {
                    shipName = ai.getName(cellX,cellY);
                    // showSunkShip(cellX,cellY);
                }
            } else {
                playerTwoGrid = mergeBitmaps(this.playerTwoGrid, this.missImg, cellX, cellY);
            }
        }

        // update text display
        String player1Name,player2Name;
        if (playerOnesTurn) {
            player1Name = "You";
            player2Name = "enemy's ";
        }
        else {
            player1Name = "Enemy";
            player2Name = "your ";
        }
        String message;
        if (sunk){
            message = player1Name+" sank "+player2Name+shipName+"!";
        } else {
            String hitOrMiss;
            if (hit) {
                hitOrMiss = " hit at position ";
            } else {
                hitOrMiss = " missed at position ";
            }
            String loc = (cellX+1) + ", " + (cellY+1);
            message = player1Name+hitOrMiss+loc;
        }
        updateTextView(message);
        // change the turn
        changeTurn();
    }
    private void setGridImage(Bitmap gridImg){
        ImageView mImg = findViewById(R.id.gridImage);
        mImg.setImageBitmap(gridImg);
    }
    // this function is used for overlaying icons (like sprites) over the grid background
    private Bitmap mergeBitmaps(Bitmap firstImage, Bitmap secondImage, int cellX, int cellY){
        int _cellSize = getScreenWidth() / 10;
        int pixX = cellX * _cellSize;
        int pixY = cellY * _cellSize;
        Rect gridOverlayBounds = new Rect(0,0,getScreenWidth(),getScreenWidth());
        Rect shotOverlayBounds = new Rect(pixX,pixY,pixX+_cellSize,pixY+_cellSize);
        Bitmap result = Bitmap.createBitmap(getScreenWidth(), getScreenWidth(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImage, null, gridOverlayBounds, null);
        canvas.drawBitmap(secondImage, null, shotOverlayBounds, null);
        return result;
    }
    // when enemy ship is sunk, this will show the ship on the map
    private void showSunkShip(int x, int y){
        int[][] locations = ai.getShipLocations(x,y);
        String locationsString = "";
        for (int i = 0;i<locations.length;i++){
            locationsString+=Arrays.toString(locations[i]);
        }
        for (int i = 0;i<locations.length;i++){
            int cellX = locations[i][0];
            int cellY = locations[i][1];
            playerTwoGrid = mergeBitmaps(playerTwoGrid, shipImg, cellX, cellY);
            playerTwoGrid = mergeBitmaps(playerTwoGrid, hitImg, cellX, cellY);
        }
        updateGrid();
    }
    private void updateGrid(){
        if (this.playerOnesTurn!=this.otherGrid) {
            setGridImage(this.playerOneGrid);
        } else {
            setGridImage(this.playerTwoGrid);
        }
    }
    private void changeTurn(){
        updateGrid();
        this.playerOnesTurn = !this.playerOnesTurn;
        if (!playerOnesTurn){ waiting++; }
        else { waiting--; }
        this.otherGrid = false;
        waiting++;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String turnMessage;
                if (playerOnesTurn) { turnMessage = "Your turn to shoot!"; }
                else { turnMessage = "Enemy's turn to shoot!"; }
                updateTextView(turnMessage);
                toggleGrid();
                waiting--;
            }
        },1000);
        if (!playerOnesTurn){
            waiting++;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int[] enemyShot = ai.doTurn();
                    shoot(enemyShot[0],enemyShot[1],false);
                    waiting--;
                }
            },2500); // delay should be at least 1000 due to previous delay
        }

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
