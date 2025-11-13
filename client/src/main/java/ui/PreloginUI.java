package ui;

import datamodel.UserData;
import request.LoginRequest;
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

    public String help() {
        String helpString = formatHelp("help", "List available commands.");
        helpString += formatHelp("register <USERNAME> <PASSWORD> <EMAIL>", "Register a new user.");
        helpString += formatHelp("login <USERNAME> <PASSWORD>", "Login to an existing account.");
        helpString += formatHelp("quit", "Quit the program.");
        return helpString;
    }

    private String register(String[] args) throws Exception {
        if (args.length > 4) {
            String errorString = EscapeSequences.SET_TEXT_COLOR_RED + "ERROR: ";
            errorString += EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " You gave too many arguments. \n";
            errorString += "To register, please give <USERNAME> <PASSWORD> <EMAIL>, each separated by spaces.";
            return errorString;
        } else if (args.length < 4) {
            String errorString = EscapeSequences.SET_TEXT_COLOR_RED + "ERROR: ";
            errorString += EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " You did not give enough arguments. \n";
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
        PostloginUI postloginUI = new PostloginUI(serverFacade, response.authToken());
        changeUITo(postloginUI);
        return returnString;
    }

    private String login(String[] args) throws Exception {
        if (args.length > 3) {
            String errorString = formatError("You gave too many arguments. \n");
            errorString += "To login, please provide your registered <USERNAME> <PASSWORD>, each separated by spaces.";
            return errorString;
        } else if (args.length < 3) {
            String errorString = formatError("You did not give enough arguments. \n");
            errorString += "To register, please provide your registered <USERNAME> <PASSWORD>, each separated by spaces.";
            return errorString;
        }
        String username = args[1];
        String password = args[2];
        LoginRequest loginRequest = new LoginRequest(username, password);
        LoginResponse response = serverFacade.login(loginRequest);
        String returnString = EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully logged in! Welcome ";
        returnString += EscapeSequences.SET_TEXT_BOLD + response.username();
        returnString += EscapeSequences.RESET_TEXT_BOLD_FAINT + "!";
        PostloginUI postloginUI = new PostloginUI(serverFacade, response.authToken());
        changeUITo(postloginUI);
        return returnString;
    }

    private String quit() {
        quit = true;
        int randomIndex = new Random().nextInt(7);
        return quitStrings[randomIndex];
    }
}
