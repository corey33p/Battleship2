package com.example.battleship;

import android.util.Log;

import java.util.Arrays;

public class Ship {
    private String name;
    private int[][] locations;
    private boolean[] hits;
    private int length;

    Ship(String name, int[][] locationsIn){
        this.name = name;
        this.locations = locationsIn;
        this.length = locationsIn.length;
        hits = new boolean[length];
        for (int i = 0;i<hits.length;i++){
            hits[i] = false;
        }
    }
    public boolean[] getHits(){
        return this.hits;
    }
    public String getName(){
        return this.name;
    }
    /* shot will be fired at a location
    if the location contains the ship,
    the location will be added to the hits array
     */
    public boolean shoot(int x, int y){
        int[] location = new int[2];
        location[0]=x;location[1]=y;

        // iterate over the locations occupied by this ship
        for (int i = 0; i < locations.length; i++) {
            if (Arrays.equals(location, locations[i])){
                if (hits[i]) {
                    throw new RuntimeException("This location has already been hit. ");
                } else {
                    hits[i] = true;
                    return true;
                }
            }
        }
        return false;
    }
    // checks if a location is part of the ship
    public boolean isInShip(int x, int y){
        int[] loc = new int[]{x, y};
        for (int i = 0; i < locations.length; i++) {
            if (Arrays.equals(loc, locations[i])) {
                return true;
            }
        }
        return false;
    }
    public int[][] getLocations(){
        return this.locations;
    }
    public boolean isSunk(){
        boolean foundUnhitPart = false;
        for (int i = 0;i<hits.length;i++){
            if (hits[i]==false){
                foundUnhitPart = true;
            }
        }
        return !foundUnhitPart;
    }
}