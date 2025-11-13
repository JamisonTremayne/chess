package ui;

import datamodel.UserData;
import response.LoginResponse;
import serverfacade.ServerFacade;

import java.util.Random;

public class PreloginUI extends ClientUI {

    private final String[] quitStrings = new String[7];

    public PreloginUI(ServerFacade serverFacade) {
        super(serverFacade, "LOGGED OUT");

        quitStrings[0] = "I am so sad rn :(";
        quitStrings[1] = "I can' believe youv' done this";
        quitStrings[2] = "WAIT NO NO NONONO";
        quitStrings[3] = "Aww shucks *cries to self*";
        quitStrings[4] = "Whatever I didn't wanna play with you anyway >:(";
        quitStrings[5] = "bruh";
        quitStrings[6] = "But whyy?!";
    }

    @Override
    public String parseCommand(String command) throws Exception {
        String[] commandWords = command.split(" ");
        if (commandWords.length == 0) {
            throw new Exception();
        }
        String commandHead = commandWords[0].toLowerCase(); //NOT CASE SENSITIVE
        switch (commandHead) {
            case "help" -> {
                return help();
            } case "register" -> {
                return register(commandWords);
            } case "login" -> {
                return login(commandWords);
            } case "quit" -> {
                return quit();
            } default -> {
                return invalidCommand(commandHead);
            }
        }
    }

    private String help() {
        String helpString = "";
        helpString += EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.SET_TEXT_BOLD;
        helpString += "     help" + EscapeSequences.RESET_TEXT_BOLD_FAINT;
        helpString += EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        helpString += " - List available commands.\n";
        helpString += EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.SET_TEXT_BOLD;
        helpString += "     register <USERNAME> <PASSWORD> <EMAIL>" + EscapeSequences.RESET_TEXT_BOLD_FAINT;
        helpString += EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        helpString += " - Register as a new user.\n";
        helpString += EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.SET_TEXT_BOLD;
        helpString += "     login <USERNAME> <PASSWORD>" + EscapeSequences.RESET_TEXT_BOLD_FAINT;
        helpString += EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        helpString += " - Login to an existing account.\n";
        helpString += EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.SET_TEXT_BOLD;
        helpString += "     quit" + EscapeSequences.RESET_TEXT_BOLD_FAINT;
        helpString += EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        helpString += " - Quit the program :(";
        helpString += EscapeSequences.RESET_TEXT_COLOR;
        return helpString;
    }

    private String register(String[] args) throws Exception {
        if (args.length > 4) {
            String errorString = EscapeSequences.SET_TEXT_COLOR_RED + "ERROR: ";
            errorString += EscapeSequences.RESET_TEXT_COLOR + " You gave too many arguments. \n";
            errorString += "To register, please give <USERNAME> <PASSWORD> <EMAIL>, each separated by spaces.";
            return errorString;
        } else if (args.length < 4) {
            String errorString = EscapeSequences.SET_TEXT_COLOR_RED + "ERROR: ";
            errorString += EscapeSequences.RESET_TEXT_COLOR + " You did not give enough arguments. \n";
            errorString += "To register, please give <USERNAME> <PASSWORD> <EMAIL>, each separated by spaces.";
            return errorString;
        }
        String username = args[1];
        String password = args[2];
        String email = args[3];
        UserData userData = new UserData(username, password, email);
        LoginResponse response = serverFacade.register(userData);
        String returnString = EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully registered and logged in as ";
        returnString += EscapeSequences.SET_TEXT_BOLD + response.username();
        returnString += EscapeSequences.RESET_TEXT_BOLD_FAINT + "!";
        PostloginUI postloginUI = new PostloginUI(serverFacade);
        changeUITo(postloginUI);
        return returnString;
    }

    private String login(String[] args) {
        String returnString = "";
        return returnString;
    }

    private String quit() {
        quit = true;
        int randomIndex = new Random().nextInt(7);
        return quitStrings[randomIndex];
    }

    private String invalidCommand(String head) {
        String invalidString = EscapeSequences.SET_TEXT_COLOR_RED + EscapeSequences.SET_TEXT_ITALIC;
        invalidString += head + EscapeSequences.RESET_TEXT_ITALIC;
        invalidString += " is not a valid command." + EscapeSequences.RESET_TEXT_COLOR;
        invalidString += " Try typing ";
        invalidString += EscapeSequences.SET_TEXT_BOLD + EscapeSequences.SET_TEXT_COLOR_GREEN;
        invalidString += "help" + EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.RESET_TEXT_COLOR;
        invalidString += " to get a list of valid commands :)";
        return invalidString;
    }
}
