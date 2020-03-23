package jmol.jasper.MonopolyGame.Logic;

import jmol.jasper.MonopolyBoard.BoardSpaces.Boardspace;
import jmol.jasper.MonopolyBoard.BoardSpaces.Jail;
import jmol.jasper.MonopolyBoard.Data.MonopolyBoardData;
import jmol.jasper.MonopolyGame.Actions.PlayerAction;
import jmol.jasper.MonopolyGame.Actions.PlayerActionFactory;
import jmol.jasper.MonopolyGame.Actions.PlayerActionType;
import jmol.jasper.Player.Logic.Player;
import jmol.jasper.UserInterface.Logic.KeuzeMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Game {
    private List<Player> players;
    private Map<Player, Boardspace> playerBoardspaceMap;
    private int nrOfRounds;
    private Bank bank;
    private KeuzeMenu keuzeMenu;
    private Jail jail;

    public Game(GameSetup gameSetup){
        players = makeArrayList(gameSetup.getPlayers());
        playerBoardspaceMap = gameSetup.getPlayerBoardspaceMap();
        bank = Bank.getInstance();
        bank.fillPlayerlistMap(players);
        keuzeMenu = new KeuzeMenu();
        jail = (Jail) MonopolyBoardData.getBoardspace(MonopolyBoardData.SPACENR_GEVANGENIS);
    }

    public void startGame(){
        while (!gameIsOver()) {
            playTurn();
            nrOfRounds ++;
        }
        System.out.println(players.get(0) + " heeft gewonnen!");
        printGameStatus();
    }

    private boolean gameIsOver() {
        return players.size() == 1;
    }

    private void playTurn() {
        for (Player player : players) {
            System.out.println(player + " is aan de beurt en staat op " + playerBoardspaceMap.get(player).getName());
            playTurn(player);
            if (player.isGameOver()) {
                handleGameOver(player);
            }
        }
    }

    private void handleBoardActions(PlayerActionType playerActionType, Player player) {
        if (playerActionType == PlayerActionType.NO_PLAYER_ACTION) {
            return;
        }
        PlayerAction playerAction = PlayerActionFactory.getPlayerAction(
                playerActionType,
                player,
                null
                );
        playerAction.handleAction();
    }

    private void handlePlayerActions(Player player, boolean isBeginTurn) {
        PlayerActionType playerActionType;
        do {
            if (isBeginTurn) {
                playerActionType = keuzeMenu.getChoiceBeforeTurn();
            }
            else {
                playerActionType = keuzeMenu.getChoiceAfterTurn();
            }
            PlayerAction playerAction = PlayerActionFactory.getPlayerAction(
                    playerActionType,
                    player,
                    playerBoardspaceMap.get(player));
            playerAction.handleAction();
        }
        while (playerActionType != PlayerActionType.NO_PLAYER_ACTION);
    }

    private void handleGameOver(Player player) {
        playerBoardspaceMap.remove(player);
        players.remove(player);
        System.out.println(player + " is failliet en doet niet meer mee!");
    }

    private void playTurnWhileInPrison(Player player) {
        RoundOfPlay roundOfPlay = new RoundOfPlay(player);
        handlePlayerActions(player, true);
        roundOfPlay.play();
        if (roundOfPlay.canBeReleasedFromPrison()) {
            jail.releasePlayer(player);
            putPlayerOnNewBoardSpace(player, roundOfPlay.getTotalThrow(), true);
            performBoardspaceActions(player, roundOfPlay.getTotalThrow());
        }
        else {
            jail.addRound(player);
        }

        handlePlayerActions(player, false);
    }

    private void playTurn(Player player) {
        if (checkForJail(player)) {
            playTurnWhileInPrison(player);
            return;
        }
        RoundOfPlay roundOfPlay = new RoundOfPlay(player);
        do {
            performTurnActions(player, roundOfPlay);
        }
        while (roundOfPlay.canThrowAgain() && !jail.isPlayerImprisoned(player));
        handlePlayerActions(player, false);
    }

    private void performTurnActions(Player player, RoundOfPlay roundOfPlay) {
        handlePlayerActions(player, true);
        roundOfPlay.play();
        if (roundOfPlay.shouldBePrisoned()) {
            putPlayerInJail(player);
        }
        else {
            putPlayerOnNewBoardSpace(player, roundOfPlay.getTotalThrow(), false);
            performBoardspaceActions(player, roundOfPlay.getTotalThrow());
        }
    }

    private void putPlayerInJail(Player player) {
        jail.imprisonPlayer(player);
        player.moveToBoardspace(MonopolyBoardData.SPACENR_GEVANGENIS);
        playerBoardspaceMap.put(player, MonopolyBoardData.getBoardspace(MonopolyBoardData.SPACENR_GEVANGENIS));
    }

    private boolean checkForJail(Player player) {
        if (!jail.isPlayerImprisoned(player)) {
            return false;
        }

        if (jail.hasServedSentence(player)) {
            return false;
        }
        return true;
    }

    private void putPlayerOnNewBoardSpace(Player player, int diceThrow, boolean releasedFromJail) {
        int previousBoardSpaceNr = player.getBoardspaceNr();
        int newBoardSpaceNr = determineNewBoardspaceNr(previousBoardSpaceNr, diceThrow);

        if (newBoardSpaceNr != 0 &&
                !releasedFromJail &&
                newBoardSpaceNr < previousBoardSpaceNr) {
            System.out.println(player + " is voorbij start gekomen en ontvangt 200 euro!");
            player.receiveMoney(200);
        }
        player.moveToBoardspace(newBoardSpaceNr);
        playerBoardspaceMap.put(player, MonopolyBoardData.getBoardspace(newBoardSpaceNr));
    }

    private int determineNewBoardspaceNr(int previousBoardspaceNr, int diceThrow) {
        int newBoardspaceNr = previousBoardspaceNr + diceThrow;
        if (newBoardspaceNr > MonopolyBoardData.MAX_BOARDSPACE_NR) {
            newBoardspaceNr -= MonopolyBoardData.MAX_BOARDSPACE_NR - 1;
        }
        return newBoardspaceNr;
    }

    private Boardspace determineNewBoardSpace(Player player, int diceThrow) {
        int newBoardSpaceNr = determineNewBoardspaceNr(player.getBoardspaceNr(), diceThrow);
        return MonopolyBoardData.getBoardspace(newBoardSpaceNr);
    }

    private void performBoardspaceActions(Player player, int diceThrow) {
        Boardspace boardspace = playerBoardspaceMap.get(player);
        boardspace.prepareAction(player, diceThrow);
        handleBoardActions(boardspace.performAction(), player);
    }

    private void printGameStatus(){
        for (Player player : players){
            PlayerActionFactory.getPlayerAction(PlayerActionType.PRINT_STATUS,player, null);
        }
        System.out.println("Er zijn " + nrOfRounds + " ronden gespeeld.");
    }

    private List<Player> makeArrayList(Player[] playerArray) {
        List<Player> players = new ArrayList<>();
        for (Player player : playerArray) {
            players.add(player);
        }
        return players;
    }
}
