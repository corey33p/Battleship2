package com.example.battleship;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class AI extends Player {
    private String[] shipNames = {"Cruiser","Submarine","Destroyer","Battleship","Carrier"};
    private int[] shipLengths = {2,3,4,4,5};
    private int[][] enemyGrid = new int[10][10]; // 0=empty;1=hit;-1=miss;2=sunk
    private int hitCellCount = 0;
    private int sunkShipSum = 0;
    private char axisLockDirection = 'z';
    private int axisLockNumber = -1;
    private int[][] lastTwoHits = new int[][]{{-1,-1},{-1,-1}};
    private boolean[] sunkShips = new boolean[] {false,false,false,false,false};
    private ArrayList<String> sunkShipsNotAccountedFor = new ArrayList<>();

    // constructor
    public AI(){
        super();
        for (int row = 0;row<10;row++){
            enemyGrid[row] = new int[]{0,0,0,0,0,0,0,0,0,0};
        }
    }
    private int[] hunt(){
        int[][] statBoard = new int[10][10];
        boolean fits;
        int[] ret = {0,0};
        for(int size : shipLengths){
            for(int i = 0; i < 10; i++){
                for(int j = 0; j < (10 - size); j++){
                    fits = true;
                    for(int z = j; z < (j + size); z++){
                        if(enemyGrid[i][z] != 0)
                            fits = false;
                    }
                    if(fits){
                        for(int z = j; z < (j + size); z++)
                            statBoard[i][z]++;
                    }
                }
            }
            for(int i = 0; i < 10; i++){
                for(int j = 0; j < (10 - size); j++){
                    fits = true;
                    for(int z = j; z < (j + size); z++){
                        if(enemyGrid[z][i] != 0)
                            fits = false;
                    }
                    if(fits){
                        for(int z = j; z < (j + size); z++)
                            statBoard[z][i]++;
                    }
                }
            }
        }
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                if(statBoard[i][j] > statBoard[ret[0]][ret[1]]){
                    ret[0] = i;
                    ret[1] = j;
                }
            }
        }
        return ret;
    }

    private int[] target(ArrayList<Integer[]> hits){
        int[][] statsBoard = new int[10][10];
        boolean fits;
        for (int hitIndex = 0;hitIndex<hits.size();hitIndex++){
            int minX = 0, minY = 0, maxX = 9, maxY = 9;
            int axisX = hits.get(hitIndex)[0];
            int axisY = hits.get(hitIndex)[1];

            // iterate through possible sizes of opponent ships,
            // get possible distances away the ships may reach in all directions
            for(int size : shipLengths){
                if(axisX - size + 1>= 0)
                    minX = axisX - size + 1;
                if(axisY - size + 1 >= 0)
                    minY = axisY - size + 1;
                if(axisX + size - 1 < 10)
                    maxX = axisX + size - 1;
                if(axisY + size - 1 < 10)
                    maxY = axisY + size - 1;

                if ((axisLockDirection=='y')||(axisLockDirection=='z')) {
                    // check possible X values for given X axis location
                    // find out if the ship would fit in given direction
                    for (int i = minX; i <= maxX - size + 1; i++) {
                        fits = true;
                        for (int j = i; j < i + size; j++) {
                            if ((enemyGrid[j][axisY] == -1) || (enemyGrid[j][axisY] == 2))
                                fits = false;
                        }
                        if (fits) { // if ship would fit, increase score of current grid location by 1
                            for (int j = i; j < i + size; j++) {
                                if (enemyGrid[j][axisY] == 0)
                                    statsBoard[j][axisY] += 1;
                            }
                        }
                    }
                }

                if ((axisLockDirection=='x')||(axisLockDirection=='z')) {
                    // check possible Y values for given Y axis location
                    // find out if the ship would fit in given direction
                    for (int i = minY; i <= maxY - size + 1; i++) {
                        fits = true;
                        for (int j = i; j < i + size; j++) {
                            if ((enemyGrid[axisX][j] == -1) || (enemyGrid[axisX][j] == 2))
                                fits = false;
                        }
                        if (fits) { // if ship would fit, increase score of current grid location by 1
                            for (int j = i; j < i + size; j++) {
                                if (enemyGrid[axisX][j] == 0)
                                    statsBoard[axisX][j] += 1;
                            }
                        }
                    }
                }
            }
        }

        // find the location of the maximum value in the statsboard
        int[] ret = {0,0};
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                if((statsBoard[i][j] > statsBoard[ret[0]][ret[1]])){
                    ret[0] = i;
                    ret[1] = j;
                }
            }
        }
        for(int i = 0; i < 10; i++){
            Log.wtf("statsBoard "+i,Arrays.toString(statsBoard[i]));
        }
        Log.wtf("target ret:" ,""+Arrays.toString(ret));
        return ret;
    }
    public int[] doTurn(){
        int[] shot = new int[]{0, 0};
        ArrayList<Integer[]> hits = getHits();
        for (int hitIndex = 0;hitIndex<hits.size();hitIndex++){
            Log.wtf("hits: ",Arrays.toString(hits.get(hitIndex)));
        }
        if (hits.isEmpty()){
            shot = hunt();
        } else {
            int tries = 0;
            while (Arrays.equals(shot,new int[]{0,0})) {
                shot = target(hits);
                tries++;
                axisLockDirection='z';
                if (tries==2){
                    throw new RuntimeException("Failed to target ship. ");
                }
            }
        }
        return shot;
    }
    ArrayList<Integer[]> getHits(){
        for (int i = 0;i<enemyGrid.length;i++){
            Log.wtf("enemy grid ",Arrays.toString(enemyGrid[i]));
        }
        ArrayList<Integer[]> hits = new ArrayList<Integer[]>();
        for(int i = 0; i < 10; i++){ // gets a list of previous hits
            for(int j = 0; j < 10; j++){
                if(enemyGrid[i][j] == 1){
                    hits.add(new Integer[]{i,j});
                }
            }
        }
        return hits;
    }
    // let the AI know which ship it has sunk
    public void informSunk(String name){
        ArrayList<Integer[]> hits = getHits();
        boolean nameFound = false;
        for (int i = 0;i<5;i++){
            if (name==shipNames[i]){
                sunkShips[i]=true;
                nameFound = true;
            }
        }
        if (!nameFound){
            throw new RuntimeException("Ship name "+name+" is not recognized. ");
        }
        sunkShipSum = 0;
        for (int i = 0;i<sunkShipsNotAccountedFor.size();i++){
            for (int j = 0;j<shipNames.length;j++){
                if (shipNames[j]==sunkShipsNotAccountedFor.get(i)){
                    sunkShipSum+=shipLengths[j];
                }
            }
        }
        for (int i = 0;i<shipNames.length;i++){
            if (shipNames[i] == name){
                sunkShipSum+=shipLengths[i];
            }
        }
        hitCellCount = 0;
        for (int row = 0;row<10;row++){
            for (int col = 0;col<10;col++){
                if (enemyGrid[row][col]==1){
                    hitCellCount++;
                }
            }
        }
        if (sunkShipSum<hitCellCount){
            sunkShipsNotAccountedFor.add(name);
        } else {
            for (int row = 0;row<10;row++){
                for (int col = 0;col<10;col++){
                    if (enemyGrid[row][col]==1){
                        enemyGrid[row][col]=2;
                    }
                }
            }
        }
    }

    // let the AI know whether it has hit a ship
    public void shotReport(int x,int y,boolean hit){
        if (hit) {
            enemyGrid[x][y]=1;
            lastTwoHits[0]=new int[]{lastTwoHits[1][0],lastTwoHits[1][1]};
            lastTwoHits[1]=new int[]{x,y};
            if (lastTwoHits[0][0]==lastTwoHits[1][0]){
                axisLockDirection = 'x';
                axisLockNumber = lastTwoHits[0][0];
            } else if (lastTwoHits[0][1]==lastTwoHits[1][1]) {
                axisLockDirection = 'y';
                axisLockNumber = lastTwoHits[0][1];
            } else {
                axisLockDirection = 'z';
            }
        } else {
            enemyGrid[x][y]=-1;
        }
    }

    @Override
    public void resetGame(){
        super.resetGame();
        enemyGrid = new int[10][10];
        hitCellCount = 0;
        sunkShipSum = 0;
        sunkShips = new boolean[] {false,false,false,false,false};
        for (int row = 0;row<10;row++){
            enemyGrid[row] = new int[]{0,0,0,0,0,0,0,0,0,0};
        }
    }
}
