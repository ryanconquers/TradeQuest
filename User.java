package model;

import java.util.HashMap;

public class User {

    private String name;
    private double cash;

    private HashMap<String, Integer> portfolio = new HashMap<>();

    public User(String name, double cash) {
        this.name = name;
        this.cash = cash;
    }

    public String getName() {
        return name;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public HashMap<String, Integer> getPortfolio() {
        return portfolio;
    }
}