package com.example.battleship;

import android.util.Log;

import java.util.Arrays;
import java.util.Random;

public class AI extends Player {
    String[] shipNames = {"Cruiser","Submarine","Destroyer","Battleship","Carrier"};
    int[] shipLengths = {3,3,4,4,5};
    private int[][] enemyGrid = new int[10][10];
    int hitCellCount = 0;
    int sunkShipSum = 0;
    int[] lastHit;
    boolean shipFound = false;
    boolean multipleShipsFound = false;
    int shootingDirectionIndex = 0; // up, down, left, right
    boolean lastWasAHit;
    String[] shootingDirection = new String[]{"north","south","east","west"};
    boolean[] sunkShips = new boolean[] {false,false,false,false,false};

    // constructor
    public AI(){
        super();
        for (int row = 0;row<10;row++){
            enemyGrid[row] = new int[]{0,0,0,0,0,0,0,0,0,0};
        }
    }

    public int[] doTurn(){
        int[] shot = new int[]{-1, -1};
        boolean validShotFound = false;
        while (!validShotFound) {
            if (multipleShipsFound) {
                shot[0]=-1;shot[1]=-1;
            } else if (shipFound) {
                int direction = shootingDirectionIndex % 4;
                if (direction == 0){
                    shot = new int[]{lastHit[0],lastHit[1]+1};
                } else if (direction == 1){
                    shot = new int[]{lastHit[0],lastHit[1]-1};
                } else if (direction == 2){
                    shot = new int[]{lastHit[0]+1,lastHit[1]};
                } else {
                    shot = new int[]{lastHit[0]-1,lastHit[1]};
                }
            } else { // pick a random spot
                Random random = new Random();
                int randomX = random.nextInt(9);
                int randomY = random.nextInt(9);
                shot[0]=randomX;shot[1]=randomY;
            }
            if (enemyGrid[shot[0]][shot[1]]==0){
                validShotFound = true;
            }
        }
        return shot;
    }

    // let the AI know which ship it has sunk
    public void informSunk(String name){
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
        for (int i = 0;i<5;i++){
            if (sunkShips[i]){
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
        if (sunkShipSum!=hitCellCount){
            multipleShipsFound=true;
        }
    }

    // let the AI know whether it has hit a ship
    public void shotReport(int x,int y,boolean hit){
        if (hit) {
            shipFound = true;
            lastWasAHit = true;
            enemyGrid[x][y]=1;
            lastHit = new int[]{x, y};
        } else {
            enemyGrid[x][y]=-1;
            lastWasAHit = false;
            shootingDirectionIndex++;
        }
    }
}
