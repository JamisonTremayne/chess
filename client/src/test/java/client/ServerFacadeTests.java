package client;

import chess.ChessGame;
import datamodel.UserData;
import org.junit.jupiter.api.*;
import response.*;
import request.*;
import server.Server;
import serverfacade.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private static String serverUrl = "http://localhost:0";
    private static UserData goodUser = new UserData("bill", "w", "a");
    private static UserData badUser = new UserData(null, null, null);

    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade(serverUrl);
    }

    @BeforeEach
    public void resetDatabase() throws Exception {
        serverFacade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerSuccess() throws Exception {
        LoginResponse response = serverFacade.register(goodUser);
        Assertions.assertEquals("bill", response.username());
        Assertions.assertNotNull(response.authToken());
    }

    @Test
    public void registerFailure() throws Exception {
        Assertions.assertThrows(Exception.class, () -> serverFacade.register(badUser));
        serverFacade.register(goodUser);
        Assertions.assertThrows(Exception.class, () -> serverFacade.register(goodUser));
    }

    @Test
    public void loginSuccess() throws Exception {
        LoginResponse response = serverFacade.register(goodUser);
        serverFacade.logout(new LogoutRequest(response.authToken()));
        LoginResponse loginResponse = serverFacade.login(new LoginRequest("bill", "w"));
        Assertions.assertEquals(response.username(), loginResponse.username());
        Assertions.assertNotEquals(response.authToken(), loginResponse.authToken());
    }

    @Test
    public void loginFailure() throws Exception {
        LoginResponse response = serverFacade.register(goodUser);
        serverFacade.logout(new LogoutRequest(response.authToken()));
        Assertions.assertThrows(Exception.class, () -> serverFacade.login(new LoginRequest("bill", "a")));
        Assertions.assertThrows(Exception.class, () -> serverFacade.login(new LoginRequest("what", "w")));
    }

    @Test
    public void logoutSuccess() throws Exception {
        LoginResponse response = serverFacade.register(goodUser);
        Assertions.assertDoesNotThrow(() -> serverFacade.logout(new LogoutRequest(response.authToken())));
    }

    @Test
    public void logoutFailure() throws Exception {
        serverFacade.register(goodUser);
        Assertions.assertThrows(Exception.class, () -> serverFacade.logout(new LogoutRequest("what?")));
    }

    @Test
    public void createGameSuccess() throws Exception {
        LoginResponse response = serverFacade.register(goodUser);
        CreateGameResponse gameResponse = serverFacade.createGame(new CreateGameRequest("game", response.authToken()));
        Assertions.assertTrue(gameResponse.gameID() >= 1000);
    }

    @Test
    public void createGameFailure() throws Exception {
        LoginResponse response = serverFacade.register(goodUser);
        Assertions.assertThrows(Exception.class, () -> serverFacade.createGame(new CreateGameRequest("game", "what?")));
        Assertions.assertThrows(Exception.class, () -> serverFacade.createGame(new CreateGameRequest(null, response.authToken())));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        LoginResponse response = serverFacade.register(goodUser);
        serverFacade.createGame(new CreateGameRequest("game", response.authToken()));
        ListGamesResponse listResponse = serverFacade.listGames(new ListGamesRequest(response.authToken()));
        Assertions.assertEquals(1, listResponse.games().size());
    }

    @Test
    public void listGamesFailure() throws Exception {
        LoginResponse response = serverFacade.register(goodUser);
        serverFacade.createGame(new CreateGameRequest("game", response.authToken()));
        Assertions.assertThrows(Exception.class, () -> serverFacade.listGames(new ListGamesRequest("what?")));
    }

    @Test
    public void joinGameSuccess() throws Exception {
        LoginResponse response = serverFacade.register(goodUser);
        CreateGameResponse gameResponse = serverFacade.createGame(new CreateGameRequest("game", response.authToken()));
        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.WHITE, gameResponse.gameID(), response.authToken());
        Assertions.assertDoesNotThrow(() -> serverFacade.joinGame(request));
    }

    @Test
    public void joinGameFailure() throws Exception {
        LoginResponse response = serverFacade.register(goodUser);
        CreateGameResponse gameResponse = serverFacade.createGame(new CreateGameRequest("game", response.authToken()));
        Assertions.assertThrows(Exception.class, () -> serverFacade.joinGame(null));
        JoinGameRequest requestOne = new JoinGameRequest(ChessGame.TeamColor.WHITE, -10, response.authToken());
        Assertions.assertThrows(Exception.class, () -> serverFacade.joinGame(requestOne));
        JoinGameRequest requestTwo = new JoinGameRequest(ChessGame.TeamColor.WHITE, gameResponse.gameID(), "what?");
        Assertions.assertThrows(Exception.class, () -> serverFacade.joinGame(requestTwo));

    }
}
