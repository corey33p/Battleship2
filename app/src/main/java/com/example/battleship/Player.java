package com.example.battleship;

import android.util.Log;

import java.util.Arrays;

public class Player {
    protected Ship[] playerShips = new Ship[5];
    private int shipsAdded = 0;
    private int shotsTaken = 0;
    private int[][] shotHistory = new int[100][2];

    public Player() {
        for (int i = 0; i<100; i++){
            shotHistory[i][0] = -1;
            shotHistory[i][1] = -1;
        }
    }

    void addShip(String shipName, int[][] shipLocations) {
        playerShips[shipsAdded] = new Ship(shipName,shipLocations);
        shipsAdded++;
        Log.wtf("ship "+shipsAdded+" "+shipName," added");
    }

    public boolean shoot(int x, int y) {
        Log.wtf("shot: ","x:"+x+"y:"+y);
        for (int i = 0;i<shotHistory.length;i++){
            int[] shot = new int[]{x,y};
            if (Arrays.equals(shot,shotHistory[i])){
                throw new RuntimeException("This shot has already been taken! ");
            }
        }
        if ((x<0)||(x>9)||(y<0)||(y>9)){
            throw new RuntimeException("Shot was off the game grid! ");
        }
        boolean successfulHit = false;
        for (int i = 0; i < 5; i++) {
            successfulHit = (successfulHit || playerShips[i].shoot(x, y));
        }
        shotHistory[shotsTaken][0]=x;
        shotHistory[shotsTaken][1]=y;
        shotsTaken++;
        return successfulHit;
    }

    // checks if the ship located at location x,y is sunk
    public boolean isSunk(int x, int y){
        for (int i = 0; i < 5; i++) {
            if (playerShips[i].isInShip(x,y)){
                return playerShips[i].isSunk();
            }
        }
        return false;
    }

    // while placing ships in game setup, function will check if it's a valid place for the ship
    public boolean validShipLocation(int[][] locCheck){
        // iterate over the input locations
        for (int inLocIndex = 0;inLocIndex<locCheck.length;inLocIndex++) {
            int[] individualLocation = locCheck[inLocIndex];

            // iterate over the existing ships
            for (int shipIndex = 0; shipIndex < shipsAdded; shipIndex++) {
                int[][] theShipLocations = playerShips[shipIndex].getLocations();

                // iterate over the locations within the ship
                for (int locationIndex = 0; locationIndex < theShipLocations.length; locationIndex++) {
                    if (Arrays.equals(individualLocation,theShipLocations[locationIndex])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String getName(int x, int y){
        for (int i = 0; i < 5; i++) {
            if (playerShips[i].isInShip(x,y)){
                if (playerShips[i].isSunk()){
                    return playerShips[i].getName();
                }
            }
        }
        return "";
    }

    public int[][] getShipLocations(int x, int y){
        for (int i = 0; i < 5; i++) {
            if (playerShips[i].isInShip(x,y)){
                if (playerShips[i].isSunk()){
                    return playerShips[i].getLocations();
                }
            }
        }
        throw new RuntimeException("This is not a valid ship location. ");
    }

    public void resetGame(){
        playerShips = null;
        playerShips = new Ship[5];
        shipsAdded = 0;
        shotsTaken = 0;
        shotHistory = null;
        shotHistory = new int[100][2];
        for (int i = 0; i<100; i++){
            shotHistory[i][0] = -1;
            shotHistory[i][1] = -1;
        }
    }
}