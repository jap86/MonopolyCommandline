package jmol.jasper.MonopolyBoard.Logic;

import jmol.jasper.Utility.Logic.UserInputReader;

public class Start extends Boardspace{

    public Start(UserInputReader userInputReader, String name, int spaceNr) {
        super(userInputReader, name, spaceNr);
    }

    @Override
    public int performAction() {
        System.out.println(visitor + " staat op start en ontvangt 400 euro!");
        visitor.receiveMoney(400);
        return spaceNr;
    }
}