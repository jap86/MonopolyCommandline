package jmol.jasper.Player.Logic;

import jmol.jasper.MonopolyBoard.Logic.Property;

import java.util.*;

public class Player {
    private String name;
    private int amountOfMoney;
    private int boardspaceNr;
    private Random dice;
    private List<Property> properties;
    private Map<String, Integer> propertyMap;
    private boolean isGameOver;

    public Player(String name){
        this.name = name;
        this.amountOfMoney = 1500;
        this.boardspaceNr = 0;
        properties = new ArrayList<>();
        dice = new Random();
        propertyMap = new HashMap<>();
    }


    public int throwDice() {
        return dice.nextInt(5) +1;
    }

    public void buyProperty(Property property){
        properties.add(property);
    }

    public void moveToBoardspace(int boardspaceNr) {
        this.boardspaceNr = boardspaceNr;
    }

    public boolean hasAllInstances(String type, int nrOfInstances) {
        Integer instancesOwned = propertyMap.get(type);
        if (instancesOwned == null) {
            return false;
        }
        return instancesOwned == nrOfInstances;
    }

    public int getOwnedInstances(String type) {
        if (!(propertyMap.containsKey(type))) {
            return 0;
        }
        return propertyMap.get(type);
    }

    public boolean receiveMoney(int amount) {
        if (amount < 0) {
            return false;
        }
        amountOfMoney += amount;
        return true;
    }

    public boolean payMoney(int amount){
        if (amount < 0) {
            return false;
        }
        if (!canAffordPayment(amount)) {
            amountOfMoney = 0;
            isGameOver = true;
            return false;
        }
        amountOfMoney -= amount;
        return true;
    }

    public void printAmountOfMoney() {
        System.out.println("Je hebt nog " + amountOfMoney + " euro.");
    }

    public boolean canAffordPayment(int amtToPay) {
        return amountOfMoney >= amtToPay;
    }
    @Override
    public String toString(){
        return name;
    }

    public String getName() {
        return name;
    }

    public int getAmountOfMoney() {
        return amountOfMoney;
    }

    public int getBoardspaceNr() {
        return boardspaceNr;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public boolean isGameOver() {
        return isGameOver;
    }
}

