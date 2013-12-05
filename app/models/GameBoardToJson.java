package models;

import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.htwg.madn.controller.IBoardControllerPort;
import de.htwg.madn.model.AbstractSpecialField;
import de.htwg.madn.model.Dice;
import de.htwg.madn.model.Figure;
import de.htwg.madn.model.IGameSettings;
import de.htwg.madn.model.Player;
import de.htwg.madn.model.PublicField;

public class GameBoardToJson {

	private final IBoardControllerPort controller;
	private ObjectNode result;

	public GameBoardToJson(IBoardControllerPort boardController) {
		this.controller = boardController;
	}

	public String getGameAsJson() {
		result = Json.newObject();
		fillResultWithGameData();
		return result.toString();
	}

	private void fillResultWithGameData() {
		insertNonFieldData();
		insertSpecialFields();
		insertPublicFields();
	}

	private void insertNonFieldData() {
		result.put("status", controller.getStatusString());
		result.put("activePlayer", activePlayerObject());
		result.put("canQuitGame", canQuitGame());
		result.put("canStartGame", canStartGame());
		result.put("canAddPlayer", canAddPlayer());
		result.put("dice", diceNumber());
	}

	private boolean canQuitGame() {
		return controller.gameIsRunning();
	}

	private boolean canStartGame() {
		IGameSettings settings = controller.getSettings();
		return !controller.gameIsRunning()
				&& controller.getPlayers().size() >= settings.getMinPlayers();
	}

	private JsonNode activePlayerObject() {
		Player activePlayer = controller.getActivePlayer();
		if (activePlayer == null) {
			return null;
		}
		String name = activePlayer.getName();
		int id = activePlayer.getId();
		ObjectNode player = Json.newObject();
		player.put("name", name);
		player.put("id", id);
		return player;
	}

	private boolean canAddPlayer() {
		return !controller.gameIsRunning()
				&& controller.getPlayers().size() < controller.getSettings()
						.getMaxPlayers();
	}

	private Integer diceNumber() {
		Dice dice = controller.getModelPort().getDice();
		if (dice.getThrowsCount() == 0) {
			return null;
		}
		return dice.getLastNumber();
	}

	private void insertSpecialFields() {
		ArrayNode homeFields = Json.newObject().arrayNode();
		ArrayNode finishFields = Json.newObject().arrayNode();

		for (Player player : controller.getPlayers()) {
			addSpecialFields(homeFields, player.getHomeField());
			addSpecialFields(finishFields, player.getFinishField());
		}

		result.put("homeFields", homeFields);
		result.put("finishFields", finishFields);
	}

	private void addSpecialFields(ArrayNode node, AbstractSpecialField field) {
		JsonNode fields = specialFields(field);

		if (fields.size() > 0) {
			ObjectNode item = Json.newObject();

			item.put("playerName", field.getOwner().getName());
			item.put("playerId", field.getOwner().getId());
			item.put("fields", fields);

			node.add(item);
		}
	}

	private JsonNode specialFields(AbstractSpecialField specialField) {
		ArrayNode fields = Json.newObject().arrayNode();

		for (int i = 0; i < specialField.getSize(); i++) {
			Figure figure = specialField.getFigure(i);
			addFigureObject(fields, figure, i);
		}

		return fields;
	}

	private void insertPublicFields() {
		ArrayNode fields = Json.newObject().arrayNode();
		PublicField publicField = controller.getModelPort().getPublicField();

		for (int i = 0; i < publicField.getSize(); i++) {
			Figure figure = publicField.getFigure(i);
			addFigureObject(fields, figure, i);
		}

		result.put("publicFields", fields);
	}

	private void addFigureObject(ArrayNode fields, Figure figure, int index) {
		if (figure != null) {
			ObjectNode node = Json.newObject();
			node.put("index", index);
			node.put("figure", String.valueOf(figure.getLetter()));
			node.put("playerId", figure.getOwner().getId());
			fields.add(node);
		}
	}

}
