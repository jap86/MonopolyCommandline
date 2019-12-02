package jmol.jasper.MonopolyBoard.Logic;

import jmol.jasper.Player.Logic.Player;
import jmol.jasper.Utility.Logic.ExpressionValidator;
import jmol.jasper.Utility.Logic.UserInputReader;

public abstract class Property extends Boardspace {
    protected int value;
    protected Player owner;
    protected String type;
    protected int nrOfInstances;
    protected static PropertyBuyer propertyBuyer;

    public Property(UserInputReader userInputReader, String name, int spaceNr, String type, int nrOfInstances, int[] values) {
        super(userInputReader, name, spaceNr);
        this.type = type;
        this.nrOfInstances = nrOfInstances;
        value = values[0];
    }

    @Override
    public int performAction (){
        if (owner == null) {
            buyProperty();
        }
        if (owner != null) {
            payRent();
        }
        return spaceNr;
    }

    public void payRent() {
        if (owner == visitor) {
            return;
        }
        int rent = calculateRent();
        System.out.println(visitor + " betaald " + rent + " euro rente aan " + owner);
        visitor.payMoney(rent);
        owner.receiveMoney(rent);
    }

    private boolean wantToBuyProperty() {
       if (!visitor.canAffordPayment(value)) {
           System.out.println(visitor + " heeft niet genoeg geld om " + name + " te kopen.");
           return false;
       }
       System.out.println("Wil je " + name + " kopen? De prijs is " + value + " euro.");
       visitor.printAmountOfMoney();
       Boolean wantToBuyProperty = userInputReader.getBoolean();
       while (!ExpressionValidator.getInstance().isValidBoolean(wantToBuyProperty)) {
           System.out.println("Voer ja, j, yes, y voor ja en nee, no, n voor nee");
           wantToBuyProperty = userInputReader.getBoolean();
       }
       return wantToBuyProperty;
    }

    public boolean buyProperty(Player player) {
        if (owner != null) {
            return false;
        }
        owner = player;
        return true;
    }


    private boolean buyProperty() {
        if (!wantToBuyProperty()) {
            return false;
        }
        if (!visitor.buyProperty(this, value)) {
            return false;
        }
        return true;
    }

    public abstract int calculateRent();

    public String getType() {
        return type;
    }

    public int getValue() {return value;}
}
