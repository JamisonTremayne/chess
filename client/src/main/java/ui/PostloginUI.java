package ui;

import serverfacade.ServerFacade;

public class PostloginUI extends ClientUI {

    public PostloginUI(ServerFacade serverFacade) {
        super(serverFacade, "LOGGED IN");
    }

    @Override
    public String parseCommand(String command) throws Exception {
        return "";
    }
}
