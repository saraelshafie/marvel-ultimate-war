

package views;


import engine.Game;
import engine.Player;
import engine.PriorityQueue;
import exceptions.*;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.abilities.*;
import model.effects.Effect;
import model.effects.EffectType;
import model.world.*;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static engine.Game.getAvailableChampions;

public class Controller {

    static MainFrame mainFrame;

    static MediaPlayer mediaPlayer;
    static Alert alert = new Alert(Alert.AlertType.ERROR);

    private static final String RED_BAR = "shiny-red";
    private static final String ORANGE_BAR = "shiny-orange";
    private static final String GREEN_BAR  = "shiny-green";
    private static final String[] barColorStyleClasses = { RED_BAR, ORANGE_BAR, GREEN_BAR };

    public Controller(MainFrame mainFrame) throws IOException {
        Controller.mainFrame = mainFrame;
    }

    public static void initializePlayers() {
        String firstName = mainFrame.startingRoot.text1.getText();
        String secondName = mainFrame.startingRoot.text2.getText();

        mainFrame.firstPlayer = new Player(firstName);
        mainFrame.secondPlayer = new Player(secondName);
    }

    public static void onStartButton() {
        initializePlayers();
        try {
            mainFrame.selectionRoot = new SelectionRoot();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        fadeOut(mainFrame.startingRoot, mainFrame.selectionRoot);
    }

    public static void onStatsButton(int i) {

        Champion c = getAvailableChampions().get(i);

//        disable all buttons except current
        mainFrame.selectionRoot.hboxStats.setStyle("-fx-background-image: url('views/champ_imgs/" + c.getName().substring(0, 2) + ".png');");


        disableAllButtonsExcept(i);
        mainFrame.selectionRoot.select.setDisable(false);


        if (mainFrame.selectionRoot.vboxSelect.getChildren().contains(mainFrame.selectionRoot.select)) {
            mainFrame.selectionRoot.vboxSelect.getChildren().remove(mainFrame.selectionRoot.select);
        }


        mainFrame.selectionRoot.vboxSelect.getChildren().add(mainFrame.selectionRoot.select);

        if (mainFrame.selectionRoot.vboxBack.getChildren().contains(mainFrame.selectionRoot.back)) {
            mainFrame.selectionRoot.vboxBack.getChildren().remove(mainFrame.selectionRoot.back);
        }

        mainFrame.selectionRoot.vboxBack.getChildren().add(mainFrame.selectionRoot.back);


        mainFrame.selectionRoot.select.setOnAction(e -> {
            onClick();
            onSelectButton(i);
            mainFrame.selectionRoot.select.setDisable(true);
            enableAllButtonsExceptChosen();
            mainFrame.selectionRoot.buttonList.get(i).setDisable(true);
        });

        mainFrame.selectionRoot.back.setOnAction(e -> {
            onClick();
            mainFrame.selectionRoot.vboxSelect.getChildren().remove(mainFrame.selectionRoot.select);
            mainFrame.selectionRoot.vboxBack.getChildren().remove(mainFrame.selectionRoot.back);
            enableAllButtonsExceptChosen();
        });

    }


    public static void disableAllButtonsExcept(int i) {
        ArrayList<Button> buttonList = mainFrame.selectionRoot.buttonList;

        for (int j = 0; j < buttonList.size(); j++)
            if (i != j)
                buttonList.get(j).setDisable(true);

    }

    public static void enableAllButtonsExceptChosen() {
        ArrayList<Button> buttonList = mainFrame.selectionRoot.buttonList;

        for (Button button : buttonList)
            if (!mainFrame.selectionRoot.chosenList.contains(button))
                button.setDisable(false);

    }


    public static void onSelectButton(int i) {
        ArrayList<Champion> availableChampions = getAvailableChampions();
        if (mainFrame.firstPlayer.getTeam().size() < 3) {
            mainFrame.firstPlayer.getTeam().add(availableChampions.get(i));
            if(mainFrame.firstPlayer.getTeam().size()==3)
                selectionPopup(mainFrame.secondPlayer.getName(),mainFrame.selectionRoot);
        }
        else if (mainFrame.secondPlayer.getTeam().size() < 3)
            mainFrame.secondPlayer.getTeam().add(availableChampions.get(i));

        if (mainFrame.secondPlayer.getTeam().size() == 3) {
            onSwitchToLeader();
        }


        mainFrame.selectionRoot.chosenList.add(mainFrame.selectionRoot.buttonList.get(i));
        mainFrame.selectionRoot.firstClick = true;

        System.out.print("first team = ");
        displayArrayC(mainFrame.firstPlayer.getTeam());

        System.out.println();
        System.out.print("second team = ");
        displayArrayC(mainFrame.secondPlayer.getTeam());

        System.out.println();
    }

    public static void onSwitchToBoard(String mapChosen) {
//        mainFrame.game = new Game(mainFrame.firstPlayer, mainFrame.secondPlayer);
        mainFrame.boardRoot = new BoardRoot(mainFrame.game, mapChosen);

        fadeOut(mainFrame.mapSelectionRoot, mainFrame.boardRoot);

        update();
    }


    public static void onSwitchToMap() {
//        mainFrame.game = new Game(mainFrame.firstPlayer, mainFrame.secondPlayer);
        mainFrame.mapSelectionRoot = new MapSelectionRoot(mainFrame.game);

        fadeOut(mainFrame.instructionsRoot, mainFrame.mapSelectionRoot);
    }


    public static void onSwitchToLeader() {
        mainFrame.game = new Game(mainFrame.firstPlayer, mainFrame.secondPlayer);
        mainFrame.leaderRoot = new LeaderRoot(mainFrame.game, 10);

        fadeOut(mainFrame.selectionRoot, mainFrame.leaderRoot);

    }

    public static String getChampionType(Champion c){
        if(c instanceof Hero)
            return "hero";
        else if(c instanceof AntiHero)
            return "antihero";
        else
            return "villain";
    }


    public static void onSelectLeader(boolean firstPlayer, int i) {

        if (firstPlayer) {
            Champion c = mainFrame.game.getFirstPlayer().getTeam().get(i);
            System.out.println("Champ name: " + c.getName());

            mainFrame.game.getFirstPlayer().setLeader(c);

            for (int j = 0; j < mainFrame.leaderRoot.firstTeamButtons.size(); j++) {
                Button button = mainFrame.leaderRoot.firstTeamButtons.get(j);
                if (j != i)
                    button.setDisable(true);
            }

            System.out.println("First leader: " + mainFrame.game.getFirstPlayer().getLeader().getName());
        } else {
            Champion c = mainFrame.game.getSecondPlayer().getTeam().get(i);
            mainFrame.game.getSecondPlayer().setLeader(c);

            for (int j = 0; j < mainFrame.leaderRoot.secondTeamButtons.size(); j++) {
                Button button = mainFrame.leaderRoot.secondTeamButtons.get(j);
                if (j != i)
                    button.setDisable(true);
            }
            System.out.println("Second leader: " + mainFrame.game.getSecondPlayer().getLeader().getName());
        }

        if (mainFrame.firstPlayer.getLeader() != null && mainFrame.secondPlayer.getLeader() != null)
            onSwitchToInstructions();

    }

    public static void onSwitchToInstructions() {

        mainFrame.instructionsRoot = new InstructionsRoot();
        fadeOut(mainFrame.leaderRoot, mainFrame.instructionsRoot);
    }


    public static void updateTurnOrder() {

        mainFrame.boardRoot.turnOrder.getChildren().clear();

        ArrayList<Champion> a = new ArrayList<>();
        PriorityQueue t = mainFrame.game.getTurnOrder();
        while (!t.isEmpty())
            a.add((Champion) t.remove());

        for (Champion c : a) {
            Button b = new Button();
            b.getStylesheets().add(mainFrame.boardRoot.getClass().getResource("css/turn-order.css").toExternalForm());
            b.getStyleClass().add(c.getName().substring(0, 2));


            mainFrame.boardRoot.turnOrder.getChildren().add(b);
            t.insert(c);
        }
    }

    public static void addRings(ArrayList<Damageable> targets){
        Object[][] b = mainFrame.game.getBoard();

        mainFrame.boardRoot.gifGridpane = new GridPane();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {

                ColorAdjust colorAdjust = new ColorAdjust();
                colorAdjust.setContrast(0.4);
                colorAdjust.setBrightness(-0.2);
                colorAdjust.setSaturation(1);

                ImageView healGif = new ImageView("views/gifs/ring-gif.gif");
                healGif.setEffect(colorAdjust);

                HBox hbox = new HBox();
                hbox.setPrefSize(500, 500);
                hbox.setAlignment(Pos.BOTTOM_CENTER);
                hbox.setBlendMode(BlendMode.SCREEN);


                mainFrame.boardRoot.gifGridpane.add(hbox, j, 4 - i);

                if (targets.contains(b[i][j]))
                    hbox.getChildren().add(healGif);

            }
        }

        mainFrame.boardRoot.center.getChildren().add(mainFrame.boardRoot.gifGridpane);
        mainFrame.boardRoot.borderPane.setCenter(mainFrame.boardRoot.center);
    }


    public static void addGifsLeader(Champion c, ArrayList<Damageable> targets){

        String gifType = "";

        //attack
        if (c instanceof Hero)
            gifType = "buff";

        else if (c instanceof Villain)
            gifType = "dmg";

        else if (c instanceof AntiHero)
            gifType = "debuff";


        Object[][] b = mainFrame.game.getBoard();

        GridPane gridPane = new GridPane();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {


                ImageView healGif = new ImageView("views/gifs/" + gifType + "-gif.gif");
//                healGif.setEffect(colorAdjust);

                HBox hbox = new HBox();
                hbox.setPrefSize(500, 500);
                hbox.setAlignment(Pos.BOTTOM_CENTER);
                hbox.setBlendMode(BlendMode.SCREEN);


                gridPane.add(hbox, j, 4 - i);

                if (targets.contains(b[i][j])) {

                    hbox.getChildren().add(healGif);

                    FadeTransition fadeTransition = new FadeTransition(
                            Duration.millis(3000),
                            hbox);

                    fadeTransition.setToValue(0);
                    fadeTransition.play();


                    fadeTransition.setOnFinished(e -> {
                        mainFrame.boardRoot.center.getChildren().remove(gridPane);
                        mainFrame.boardRoot.borderPane.setCenter(mainFrame.boardRoot.center);

//                        update();

                    });


                }
            }
        }

        mainFrame.boardRoot.center.getChildren().add(gridPane);
        mainFrame.boardRoot.borderPane.setCenter(mainFrame.boardRoot.center);
    }



    public static void addGifs(Ability a, ArrayList<Damageable> targets, Object[][] b){


        if(targets == null) {
            System.out.println("Targets are NULL");
            return;
        }

        String gifType;

        //attack
        if (a == null || a instanceof DamagingAbility)
            gifType = "dmg";

        else if (a instanceof HealingAbility)
            gifType = "heal";

        else if (a instanceof CrowdControlAbility &&
                ((CrowdControlAbility) a).getEffect().getType() == EffectType.BUFF)
            gifType = "buff";

        else
            gifType = "debuff";


        GridPane gridPane = new GridPane();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {

//                ColorAdjust colorAdjust = new ColorAdjust();
//                colorAdjust.setContrast(0.4);
//                colorAdjust.setBrightness(-0.2);
//                colorAdjust.setSaturation(1);

                ImageView healGif = new ImageView("views/gifs/" + gifType + "-gif.gif");
//                healGif.setEffect(colorAdjust);

                HBox hbox = new HBox();
                hbox.setPrefSize(500, 500);
                hbox.setAlignment(Pos.BOTTOM_CENTER);
                hbox.setBlendMode(BlendMode.SCREEN);


                gridPane.add(hbox, j, 4 - i);

                if (targets.contains(b[i][j])) {

                    hbox.getChildren().add(healGif);

                    FadeTransition fadeTransition = new FadeTransition(
                            Duration.millis(3000),
                            hbox);

                    fadeTransition.setToValue(0);
                    fadeTransition.play();


                    fadeTransition.setOnFinished(e -> {
                        mainFrame.boardRoot.center.getChildren().remove(gridPane);
                        mainFrame.boardRoot.borderPane.setCenter(mainFrame.boardRoot.center);

                    });


                }
            }
        }

        mainFrame.boardRoot.center.getChildren().add(gridPane);
        mainFrame.boardRoot.borderPane.setCenter(mainFrame.boardRoot.center);
    }

    public static void startGrid() {
        Object[][] b = mainFrame.game.getBoard();

        GridPane gridPane = new GridPane();

        gridPane.setStyle("-fx-background-image: url('views/bg_imgs/" + mainFrame.boardRoot.mapChosen + "Map.png');");

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {

                DamageableBtn damageableBtn;

                if (b[i][j] instanceof Cover) {
                    Cover cover = (Cover) b[i][j];

                    damageableBtn = new DamageableBtn(cover.getCurrentHP(), cover.getMaxHP(), "cover", mainFrame.boardRoot.mapChosen);

                } else if (b[i][j] instanceof Champion) {

                    Champion champion = (Champion) b[i][j];

                    damageableBtn = new DamageableBtn(champion.getCurrentHP(), champion.getMaxHP(), "icons", champion.getName().substring(0, 2));

                } else {
                    damageableBtn = new DamageableBtn(0,0,"cover","snow");
                    damageableBtn.setOpacity(0);
                    damageableBtn.setStyle("-fx-background-color: transparent;");
                }

                gridPane.add(damageableBtn, j, 4 - i);
                mainFrame.boardRoot.buttonBoard[i][j] = damageableBtn;


            }
        }


        mainFrame.boardRoot.center.getChildren().clear();
        mainFrame.boardRoot.center.getChildren().add(gridPane);
    }

    public static void displayArrayC(ArrayList<Champion> a) {
        for (Champion c : a)
            System.out.print(c.getName() + " ");
    }

    public static String appendSpaces(int a) {
        String s = "(" + a + ")";
        while (s.length() != 6)
            s = " " + s;
        return " " + s + " ";
    }

    public static void fadeOut(Parent oldRoot, Parent newRoot) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(500));
        fadeTransition.setNode(oldRoot);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);

        fadeTransition.setOnFinished(e -> {
            newRoot.setOpacity(0);
            oldRoot.getScene().setRoot(newRoot);
            fadeIn(newRoot);
//            mainFrame.window.setMaximized(true);
//            mainFrame.window.setFullScreen(true);
        });

        fadeTransition.play();
    }

    public static void fadeIn(Parent newRoot) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(500));
        fadeTransition.setNode(newRoot);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    public static void exceptionMessage(String header, String content){

        ImageView blackImg = mainFrame.boardRoot.blackImg;
//        ImageView alertBox = new ImageView(new Image("views/popup_imgs/alert4.png"));
        StackPane stackPane = new MyAlert(header, content, "red-alert");

        mainFrame.boardRoot.getChildren().add(blackImg);
        mainFrame.boardRoot.getChildren().add(stackPane);

        FadeTransition fadeTransition1 = new FadeTransition(Duration.millis(500), blackImg);
        fadeTransition1.setFromValue(0);
        fadeTransition1.setToValue(1);

        FadeTransition fadeTransition2 = new FadeTransition(Duration.millis(500), blackImg);
        fadeTransition2.setFromValue(1);
        fadeTransition2.setToValue(0);


        FadeTransition fadeTransition3 = new FadeTransition(Duration.millis(500), stackPane);
        fadeTransition3.setFromValue(0);
        fadeTransition3.setToValue(1);

        FadeTransition fadeTransition4 = new FadeTransition(Duration.millis(500), stackPane);
        fadeTransition4.setFromValue(1);
        fadeTransition4.setToValue(0);


        fadeTransition2.setOnFinished(e -> {
            mainFrame.boardRoot.getChildren().clear();
            mainFrame.boardRoot.getChildren().add(mainFrame.boardRoot.borderPane);
        });

        SequentialTransition seqTransition1 = new SequentialTransition(fadeTransition1, new PauseTransition(Duration.millis(2900)), fadeTransition2);
        seqTransition1.play();

        SequentialTransition seqTransition2 = new SequentialTransition(fadeTransition3, new PauseTransition(Duration.millis(2900)), fadeTransition4);
        seqTransition2.play();

    }


    public static void selectionPopup(String playerName, SelectionRoot root){

        ImageView blackImg = new ImageView(new Image("views/gen_imgs/black-img.png"));
        StackPane stackPane = new MyAlert("Alert", playerName + ", choose three champions for your team", "selection-alert");


        root.getChildren().add(blackImg);
        root.getChildren().add(stackPane);

        FadeTransition fadeTransition1 = new FadeTransition(Duration.millis(500), blackImg);
        fadeTransition1.setFromValue(0);
        fadeTransition1.setToValue(1);

        FadeTransition fadeTransition2 = new FadeTransition(Duration.millis(500), blackImg);
        fadeTransition2.setFromValue(1);
        fadeTransition2.setToValue(0);


        FadeTransition fadeTransition3 = new FadeTransition(Duration.millis(500), stackPane);
        fadeTransition3.setFromValue(0);
        fadeTransition3.setToValue(1);

        FadeTransition fadeTransition4 = new FadeTransition(Duration.millis(500), stackPane);
        fadeTransition4.setFromValue(1);
        fadeTransition4.setToValue(0);


        fadeTransition2.setOnFinished(e -> {
            mainFrame.selectionRoot.getChildren().clear();
            mainFrame.selectionRoot.getChildren().add(mainFrame.selectionRoot.selectionRootVbox);
        });

        SequentialTransition seqTransition1 = new SequentialTransition(fadeTransition1, new PauseTransition(Duration.millis(2900)), fadeTransition2);
        seqTransition1.play();

        SequentialTransition seqTransition2 = new SequentialTransition(fadeTransition3, new PauseTransition(Duration.millis(2900)), fadeTransition4);
        seqTransition2.play();

    }

    public static Direction codeToDirection(KeyCode code) {
        switch (code) {
            case W:
                return Direction.UP;
            case S:
                return Direction.DOWN;
            case A:
                return Direction.LEFT;
            case D:
                return Direction.RIGHT;
            default:
                return null;
        }
    }

    public static void onMoveButton(KeyCode code) {
        Direction dir = codeToDirection(code);

        Point oldLocation = mainFrame.game.getCurrentChampion().getLocation();
        int oldX = oldLocation.x;
        int oldY = oldLocation.y;

        try {
            mainFrame.game.move(dir);
        } catch (UnallowedMovementException movement) {
            exceptionMessage("Unallowed Movement!", movement.getMessage());
        } catch (NotEnoughResourcesException resource) {
            exceptionMessage("Not Enough Resources!", resource.getMessage());
        }

        Point newLocation = mainFrame.game.getCurrentChampion().getLocation();

        int newX = newLocation.x;
        int newY = newLocation.y;

        startGrid();
        updateBars();
    }

    public static void onAttackButton() {

        mainFrame.boardRoot.setOnKeyPressed(e -> {
            Object[][] board = clone(mainFrame.game.getBoard());
            AtomicReference<ArrayList<Damageable>> targets = new AtomicReference<>();

            Direction dir = codeToDirection(e.getCode());
            targets.set(mainFrame.game.getTargetOfAttack(dir));

            try {
                mainFrame.game.attack(dir);

                update();
                addGifs(null, targets.get(), board);

            } catch (ChampionDisarmedException disarm) {
                exceptionMessage("Champion Disarmed!", disarm.getMessage());
            } catch (NotEnoughResourcesException resource) {
                exceptionMessage("Not Enough Resources!", resource.getMessage());
            }
        });
    }

    public static Object[][] clone(Object[][] b){

        Object[][] res = new Object[b.length][b[0].length];

        for(int i=0;i<b.length;i++)
            for(int j=0;j<b[i].length;j++)
                res[i][j] = b[i][j];

        return res;
    }

    public static void onCastAbility(String abilityName) {

        Ability ability = findAbility(abilityName);
        AreaOfEffect castArea = ability.getCastArea();

        Object[][] board = clone(mainFrame.game.getBoard());
        AtomicReference<ArrayList<Damageable>> targets = new AtomicReference<>(new ArrayList<>());

        try {
            if (castArea == AreaOfEffect.TEAMTARGET || castArea == AreaOfEffect.SELFTARGET || castArea == AreaOfEffect.SURROUND) {

                targets.set(mainFrame.game.getTargetsOfCast(ability));

                mainFrame.game.castAbility(ability);
                update();
                addGifs(ability, targets.get(), board);
                gameOver();

            } else if (castArea == AreaOfEffect.DIRECTIONAL) {
                mainFrame.boardRoot.setOnKeyPressed(f -> {

                    Direction dir = codeToDirection(f.getCode());

                    targets.set(mainFrame.game.getTargetsOfCastDir(ability, dir));

                    castAbilityDirectional(ability, dir);

                });
            } else {
                onSingleTarget(ability);



            }


        } catch (AbilityUseException abilityUseException) {
            exceptionMessage("Unallowed Use!", abilityUseException.getMessage());
        } catch (NotEnoughResourcesException notEnoughResourcesException) {
            exceptionMessage("Not Enough Resources!", notEnoughResourcesException.getMessage());
        } catch (CloneNotSupportedException cloneNotSupportedException) {

        } catch (InvalidTargetException invalidTargetException) {
            exceptionMessage("Invalid Target!", invalidTargetException.getMessage());
        }
    }

    public static void castAbilityDirectional(Ability ability, Direction dir) {
        try {

            Object[][] board = clone(mainFrame.game.getBoard());
            ArrayList<Damageable> targets = mainFrame.game.getTargetsOfCastDir(ability, dir);

            mainFrame.game.castAbility(ability, dir);
            update();

            addGifs(ability,targets , board);

        } catch (AbilityUseException abilityUseException) {
            exceptionMessage("Ability Use Exception", abilityUseException.getMessage());
        } catch (NotEnoughResourcesException notEnoughResourcesException) {
            exceptionMessage("Not EnoughResources", notEnoughResourcesException.getMessage());
        } catch (CloneNotSupportedException cloneNotSupportedException) {

        }
    }

    public static void onSingleTarget(Ability ability) throws InvalidTargetException, NotEnoughResourcesException, AbilityUseException, CloneNotSupportedException {
        DamageableBtn[][] buttonBoard = mainFrame.boardRoot.buttonBoard;

        Object[][] board = clone(mainFrame.game.getBoard());
        ArrayList<Damageable> targets = new ArrayList<>();

        for (int i = 0; i < buttonBoard.length; i++)
            for (int j = 0; j < buttonBoard[i].length; j++) {
                DamageableBtn damageableBtn = buttonBoard[i][j];

                int finalI = i;
                int finalJ = j;

                // INTELLIJ MADE ME DO IT :-(
                damageableBtn.button.setOnAction(e -> {
                    try {
                        targets.add((Damageable) mainFrame.game.getBoard()[finalI][finalJ]);

                        mainFrame.game.castAbility(ability, finalI, finalJ);
//                        addGifs(mainFrame.game.getTargetsOfCastSin(ability, finalI, finalJ));

                        update();

                        addGifs(ability, targets, board);

                        damageableBtn.button.setOnAction(null);
                    } catch (Exception ex) {
                        exceptionMessage("ERROR", ex.getMessage());
                    }
                });
            }
    }

    private static Ability findAbility(String name) {
        for (Ability a : mainFrame.game.getCurrentChampion().getAbilities()) {
            if (a.getName().equals(name))
                return a;
        }
        return null;
    }


    public static void update() {
//        updateGrid();
        startGrid();
        updateHP();
        updateTurnOrder();
        updateBars();
        updateAbilities();
        updateSideIcons();
        disableInactiveBars();
        updateLeaderTooltip();
        gameOver();
    }

    public static void onEndTurn() {
        mainFrame.game.endTurn();

        update();
    }


    public static void updateHP() {

        mainFrame.boardRoot.hp.getChildren().clear();

        for (Champion c : mainFrame.game.getFirstPlayer().getTeam()) {
            Button button = new Button(c.getName() + " " + c.getCurrentHP());
            mainFrame.boardRoot.hp.getChildren().add(button);
        }

        for (Champion c : mainFrame.game.getSecondPlayer().getTeam()) {
            Button button = new Button(c.getName() + " " + c.getCurrentHP());
            mainFrame.boardRoot.hp.getChildren().add(button);
        }
    }

    public static void onLeaderAbility() {
        try {
            ArrayList<Damageable> targets = mainFrame.game.getTargetsOfLeader();
            Champion c = mainFrame.game.getCurrentChampion();

            mainFrame.game.useLeaderAbility();

            update();
            gameOver();

            addGifsLeader(c, targets);

        } catch (Exception e) {
            exceptionMessage("Invalid action", e.getMessage());
        }
    }

    public static void gameOver() {
        if (mainFrame.game.checkGameOver() != null)
            onSwitchToGameOver(mainFrame.game.checkGameOver());
    }

    public static void onSwitchToGameOver(Player player) {

        mainFrame.gameOverRoot = new GameOverRoot(player);
        fadeOut(mainFrame.boardRoot, mainFrame.gameOverRoot);
    }

    public static void onClick() {

        String s = "rclick.mp3";
        Media h = new Media(Paths.get(s).toUri().toString());
        mediaPlayer = new MediaPlayer(h);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setVolume(5000);

        //mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
        mediaPlayer.play();

    }

    public static void setBarStyleClass(ProgressBar bar, String barStyleClass) {
        bar.getStyleClass().clear();
        bar.getStylesheets().clear();

        bar.getStylesheets().add(mainFrame.boardRoot.getClass().getResource("css/bar-colors.css").toExternalForm());
        bar.getStyleClass().add(barStyleClass);

    }

    public static void disableInactiveBars(){

        Champion c = mainFrame.game.getCurrentChampion();

        if(mainFrame.game.getFirstPlayer().getTeam().contains(c)){
            mainFrame.boardRoot.firstPlayerVBox.setEffect(mainFrame.boardRoot.normalEffect);
            mainFrame.boardRoot.secondPlayerVBox.setEffect(mainFrame.boardRoot.darknessEffect);
        }else{
            mainFrame.boardRoot.firstPlayerVBox.setEffect(mainFrame.boardRoot.darknessEffect);
            mainFrame.boardRoot.secondPlayerVBox.setEffect(mainFrame.boardRoot.normalEffect);
        }

    }

    public static void updateBars(){
        Champion c = mainFrame.game.getCurrentChampion();

        if(mainFrame.game.getFirstPlayer().getTeam().contains(c)){

            double progress = (double) c.getCurrentHP()/c.getMaxHP();

            Label firstHPLabel = mainFrame.boardRoot.firstHPLabel;

            mainFrame.boardRoot.firstPlayerIcon.getStyleClass().clear();
            mainFrame.boardRoot.firstPlayerIcon.getStyleClass().add(c.getName().substring(0, 2));

            mainFrame.boardRoot.firstPlayerBar1.setProgress((double) c.getCurrentHP()/c.getMaxHP());
            mainFrame.boardRoot.firstPlayerBar2.setProgress((double) c.getMana()/c.getMaxMana());

            mainFrame.boardRoot.firstAPRow.getChildren().remove(mainFrame.boardRoot.firstAPRow.getChildren().size() - 1);
            mainFrame.boardRoot.firstAPRow.getChildren().add(createAPBar(c.getCurrentActionPoints(), c.getMaxActionPointsPerTurn(), "L"));


            mainFrame.boardRoot.firstHPLabel.setText(c.getCurrentHP() + "/" + c.getMaxHP());
            mainFrame.boardRoot.firstManaLabel.setText(c.getMana() + "/" + c.getMaxMana());
        }
        else{

            double progress = (double) c.getCurrentHP()/c.getMaxHP();

            mainFrame.boardRoot.secondPlayerIcon.getStyleClass().clear();
            mainFrame.boardRoot.secondPlayerIcon.getStyleClass().add(c.getName().substring(0, 2));

            mainFrame.boardRoot.secondPlayerBar1.setProgress((double) c.getCurrentHP()/c.getMaxHP());
            mainFrame.boardRoot.secondPlayerBar2.setProgress((double) c.getMana()/c.getMaxMana());

            mainFrame.boardRoot.secondAPRow.getChildren().remove(0);
            mainFrame.boardRoot.secondAPRow.getChildren().add(0, createAPBar(c.getCurrentActionPoints(), c.getMaxActionPointsPerTurn(), "R"));

            mainFrame.boardRoot.secondHPLabel.setText(c.getCurrentHP() + "/" + c.getMaxHP());
            mainFrame.boardRoot.secondManaLabel.setText(c.getMana() + "/" + c.getMaxMana());
        }

    }

    public static String getChampColor(Champion c){
        String name = c.getName();

        return switch (name) {
            case "Captain America", "Iceman", "Quicksilver", "Thor", "Venom" -> "blue";
            case "Deadpool", "Dr Strange", "Ironman", "Spiderman" -> "red";
            case "Hela", "Hulk" -> "green";
            default -> "yellow";
        };
    }

    public static HBox getStatsPopUp(Champion c){

        MyProgressBar hpBar = new MyProgressBar("HP" , c.getCurrentHP(),c.getMaxHP());

        //SPEED
        MyProgressBar speedBar = new MyProgressBar("SPEED" , c.getSpeed(),c.getMaxSpeed());

        //RANGE
        MyProgressBar rangeBar = new MyProgressBar("RANGE" , c.getAttackRange(),c.getMaxAttackRange());

        //DAMAGE
        MyProgressBar damageBar = new MyProgressBar("DAMAGE" , c.getAttackDamage(),c.getMaxAttackDamage());

        VBox bars1 = new VBox(40);
        bars1.setAlignment(Pos.CENTER);

        VBox bars2 = new VBox(40);
        bars2.setAlignment(Pos.CENTER);

        bars1.getChildren().addAll(hpBar,damageBar);
        bars2.getChildren().addAll(speedBar,rangeBar);

        Button closeBtn = new Button();
        closeBtn.setPrefSize(50,50);
        closeBtn.getStylesheets().add(mainFrame.boardRoot.getClass().getResource("css/popup.css").toExternalForm());
        closeBtn.getStyleClass().add("closeBtn");

        HBox root = new HBox(50);
        root.setMaxSize(1100,260);

        root.setStyle("-fx-background-image: url('views/bg_imgs/" + getChampColor(c) + "-selection.png');");

        Button champ = new Button();
        champ.setPrefSize(100,100);
        champ.getStylesheets().add(mainFrame.boardRoot.getClass().getResource("css/popup.css").toExternalForm());
        champ.getStyleClass().add(c.getName().substring(0,2));
        champ.setPadding(new Insets(0,0,7,50));

        HBox champHbox = new HBox();
        champHbox.setAlignment(Pos.BOTTOM_LEFT);
        champHbox.getChildren().add(champ);

        HBox closeBtnHbox = new HBox();
        closeBtnHbox.setPadding(new Insets(10,0,0,0));
        closeBtnHbox.getChildren().add(closeBtn);
        closeBtnHbox.setAlignment(Pos.TOP_RIGHT);
        HBox bars = new HBox(15);
        bars.getChildren().addAll(bars1,bars2);

        VBox effects_bars = new VBox(30);
        effects_bars.setAlignment(Pos.CENTER);


        HBox effectsHbox = new HBox(15);

        effects_bars.getChildren().addAll(bars, effectsHbox);


        if(!c.getAppliedEffects().isEmpty()){
            Label title = new Label("EFFECTS: ");
            title.getStylesheets().add(Controller.class.getResource("css/game-font.css").toExternalForm());
            title.getStyleClass().add("effects-font");

            effectsHbox.getChildren().clear();
            effectsHbox.getChildren().add(title);

        }
        else
            effectsHbox.getChildren().clear();

        for (Effect e: c.getAppliedEffects()) {
            Label effect = new Label(e.getName() + " (" + e.getDuration() + ")" );
            effect.getStylesheets().add(Controller.class.getResource("css/game-font.css").toExternalForm());
            effect.getStyleClass().add("effects-font");

            effectsHbox.getChildren().add(effect);
        }

        root.getChildren().addAll(champHbox,effects_bars, closeBtnHbox);

        closeBtn.setOnAction(e -> {

            FadeTransition fadeTransition1 = new FadeTransition(Duration.millis(500), mainFrame.boardRoot.blackImg);
            FadeTransition fadeTransition2 = new FadeTransition(Duration.millis(500), mainFrame.boardRoot.popUpStats);

            fadeTransition1.setFromValue(1);
            fadeTransition1.setToValue(0);
            fadeTransition1.play();

            fadeTransition2.setFromValue(1);
            fadeTransition2.setToValue(0);
            fadeTransition2.play();

            fadeTransition1.setOnFinished(f -> {
                mainFrame.boardRoot.getChildren().clear();
                mainFrame.boardRoot.getChildren().add(mainFrame.boardRoot.borderPane);
            });


        });

        return root;
    }

    public static HBox createAPBar(int curr, int max, String dir){
        HBox root = new HBox();

        for(int i=1; i<=max; i++){
            ProgressBar bar = new ProgressBar();
            bar.setPrefSize(15, 15);
            bar.getStylesheets().add(BigTest.class.getResource("css/bar-colors.css").toExternalForm());
            bar.getStyleClass().add("shiny-orange");

            if(dir.equals("L")){
                if(i <= curr)
                    bar.setProgress(1);
                else
                    bar.setProgress(0);
            }

            if(dir.equals("R")){
                if(i <= max-curr)
                    bar.setProgress(0);
                else
                    bar.setProgress(1);
            }

            root.getChildren().add(bar);
        }

        return  root;
    }

    public static void createAbilitiesStyleSheet() throws IOException {

        Game.loadAbilities("Abilities.csv");
        Game.loadChampions("Champions.csv");

        String stylesheet = "";

        for(Champion c : Game.getAvailableChampions()){
            for(int i=1; i<=c.getAbilities().size(); i++){
                Ability a = c.getAbilities().get(i-1);

                String abilityName = a.getName().toLowerCase().replace(" ","-");

                String s =
                        "." + c.getName().substring(0,2) + "-Ability-" + i + "{\n" +
                                "   -fx-background-color: transparent;\n" +
                                "   -fx-graphic: url('ability_imgs/" + abilityName + ".png');\n" +
                                "   -fx-cursor: hand;\n" +
                                "}\n" +

                                "."  + c.getName().substring(0,2) + "-Ability-" + i + ":hover {\n" +
                                "   -fx-background-color: transparent;\n" +
                                "   -fx-graphic: url('ability_imgs/" + abilityName + "-hover.png');\n" +
                                "   -fx-cursor: hand;\n" +
                                "}\n" +

                                "."  + c.getName().substring(0,2) + "-Ability-" + i + ":pressed {\n" +
                                "   -fx-background-color: transparent;\n" +
                                "   -fx-graphic: url('ability_imgs/" + abilityName + ".png');\n" +
                                "   -fx-cursor: hand;\n" +
                                "}\n\n";


                stylesheet += s + "\n";
            }
        }

        System.out.println(stylesheet);
    }


    public static String getAbilityStatsToolTip(Ability a) {
        String text = a.getName() + "\n";

        if(a instanceof DamagingAbility)
            text += "DAMAGING - " + a.getCastArea() + "\n" + "DAMAGE: " + ((DamagingAbility) a).getDamageAmount() + "\n";
        else if(a instanceof HealingAbility)
            text += "HEALING - " + a.getCastArea() + "\n" + "HEAL: " + ((HealingAbility) a).getHealAmount() + "\n";
        else
            text += "CROWDCONTROL - " + a.getCastArea() + "\n" + "EFFECT: " + ((CrowdControlAbility)a).getEffect().getName() + "(" +
                    ((CrowdControlAbility)a).getEffect().getDuration() + ") " + ((CrowdControlAbility)a).getEffect().getType() + "\n";

        text += "MANA: "+ a.getManaCost() +"\n" + "AP: "+ a.getRequiredActionPoints() + "\n" +
                "RANGE: " + a.getCastRange() + "\n" + "BASE COOLDOWN: " + a.getBaseCooldown() + "\n" +
                "CURRENT COOLDOWN: " + a.getCurrentCooldown();
        return text;

    }

    public static void updateAbilities(){

        Champion c = mainFrame.game.getCurrentChampion();

        for(int i=0; i<c.getAbilities().size(); i++){
            Ability a = c.getAbilities().get(i);
            Tooltip t = new Tooltip();

            Button firstBtn = mainFrame.boardRoot.firstAbilityButtons.get(i);
            Button secondBtn = mainFrame.boardRoot.secondAbilityButtons.get(i);

            if(mainFrame.game.getFirstPlayer().getTeam().contains(c)) {
                firstBtn.setTooltip(t);
                String text = getAbilityStatsToolTip(a);
                t.setText(text);
                firstBtn.setOnAction(e -> onCastAbility(a.getName()));

                firstBtn.setOnMouseEntered(e -> {
                    addRings(mainFrame.game.getPotentialTargets(a));
                });

                firstBtn.setOnMouseExited(e -> {

                    mainFrame.boardRoot.gifGridpane.getChildren().clear();
                    mainFrame.boardRoot.center.getChildren().remove(mainFrame.boardRoot.gifGridpane);
                    mainFrame.boardRoot.borderPane.setCenter(mainFrame.boardRoot.center);

                });

//                firstBtn.setOnMouseExited(e -> {
//                    FadeTransition fadeTransition = new FadeTransition(
//                            Duration.millis(3000),
//                            mainFrame.boardRoot.gifGridpane);
//
//                    fadeTransition.setToValue(0);
//                    fadeTransition.play();
//
//
//                    fadeTransition.setOnFinished(f -> {
//                        mainFrame.boardRoot.gifGridpane.getChildren().clear();
//                        mainFrame.boardRoot.center.getChildren().remove(mainFrame.boardRoot.gifGridpane);
//                        mainFrame.boardRoot.borderPane.setCenter(mainFrame.boardRoot.center);
//
//                    });
//                });

                firstBtn.getStyleClass().clear();
                firstBtn.getStyleClass().add(c.getName().substring(0,2) + "-Ability-" + (i+1));
                mainFrame.boardRoot.firstPlayerAbilities.setSpacing(7);
            }
            else{
                secondBtn.setTooltip(t);
                String text = getAbilityStatsToolTip(a);
                t.setText(text);

                secondBtn.setOnAction(e -> onCastAbility(a.getName()));

                secondBtn.setOnMouseEntered(e -> {
                    addRings(mainFrame.game.getPotentialTargets(a));
                });

                secondBtn.setOnMouseExited(e -> {

                    mainFrame.boardRoot.gifGridpane.getChildren().clear();
                    mainFrame.boardRoot.center.getChildren().remove(mainFrame.boardRoot.gifGridpane);
                    mainFrame.boardRoot.borderPane.setCenter(mainFrame.boardRoot.center);

                });

                secondBtn.getStyleClass().clear();
                secondBtn.getStyleClass().add(c.getName().substring(0,2) + "-Ability-" + (i+1) );

                mainFrame.boardRoot.secondPlayerAbilities.setSpacing(7);
            }
        }
    }

    public static void updateSideIcons(){

        for(MySideIcon s : mainFrame.boardRoot.firstSideIcons)
            if(s.champion.getCurrentHP()==0)
                s.addRedCross();

        for(MySideIcon s : mainFrame.boardRoot.secondSideIcons)
            if(s.champion.getCurrentHP()==0)
                s.addRedCross();


    }

    public static void updateLeaderTooltip(){

        Tooltip t = new Tooltip();

        String text;

        if (mainFrame.game.getCurrentChampion() == mainFrame.game.getFirstPlayer().getLeader()) {
            if (mainFrame.game.isFirstLeaderAbilityUsed())
                text = "Leader Ability used";
            else
                text = "Leader Ability not used";

        } else if (mainFrame.game.getCurrentChampion() == mainFrame.game.getSecondPlayer().getLeader()) {
            if (mainFrame.game.isSecondLeaderAbilityUsed())
                text = "Leader Ability used";
            else
                text = "Leader Ability not used";

        } else
            text = "Current champion is not a leader";


        t.setText(text);
//        t.setStyle("-fx-font: normal bold 11 Langdon;" + "-fx-base: #AE3522;" + "-fx-text-fill: orange;");
        mainFrame.boardRoot.leaderAbility.setTooltip(t);

    }

}


