import chess.*;
import ui.PregameUI;

public class Main {

    private static final PregameUI pregameUI = new PregameUI();

    public static void main(String[] args) throws Exception {
        System.out.println("♕ Welcome to Jamison's 240 Chess Client! Type Help to get started!");

        pregameUI.run();
    }
}