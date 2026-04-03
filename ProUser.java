package model;

public class ProUser extends User {

    private double leverage = 2.0;

    public ProUser() {
        super("ProUser", 10000);
    }

    public double getLeverage() {
        return leverage;
    }

    public void setLeverage(double leverage) {
        this.leverage = leverage;
    }
}