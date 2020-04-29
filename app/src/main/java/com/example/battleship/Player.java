package com.example.battleship;

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
    }

    public boolean shoot(int x, int y) {
        boolean successfulHit = false;
        for (int i = 0; i < 5; i++) {
            successfulHit = (successfulHit || playerShips[i].shoot(x, y));
        }
        shotHistory[shotsTaken][0]=x;
        shotHistory[shotsTaken][1]=y;
        shotsTaken++;
        return successfulHit;
    }

    public boolean isSunk(int x, int y){
        for (int i = 0; i < 5; i++) {
            if (playerShips[i].isInShip(x,y)){
                if (playerShips[i].isSunk()){
                    return true;
                }
            }
        }
        return false;
    }

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
}