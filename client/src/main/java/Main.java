import serverfacade.ServerFacade;
import ui.PregameUI;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("♕ Welcome to Jamison's 240 Chess Client! Type Help to get started!");

        try {
            String serverUrl = "http://localhost:8080";
            ServerFacade serverFacade = new ServerFacade(serverUrl);
            PregameUI pregameUI = new PregameUI(serverFacade);
            pregameUI.run();
        } catch (Exception ex) {
            System.out.printf("Unable to open server: %s%n", ex.getMessage());
        }
    }
}