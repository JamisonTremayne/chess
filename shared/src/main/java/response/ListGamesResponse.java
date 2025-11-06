package response;

import datamodel.GameData;
import java.util.ArrayList;

public record ListGamesResponse(ArrayList<GameData> games) {
}
