package ui;

import serverfacade.ServerFacade;

public class PostloginUI extends ClientUI {

    private final String authToken;

    public PostloginUI(ServerFacade serverFacade, String authToken) {
        super(serverFacade, "LOGGED IN");

        this.authToken = authToken;
    }

    @Override
    public String parseCommand(String command) throws Exception {
        return "";
    }
}
