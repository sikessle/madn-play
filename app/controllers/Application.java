package controllers;

import java.util.Map;

import models.BoardControllerObserver;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.htwg.madn.controller.IBoardControllerPort;
import de.htwg.madn.model.Dice;
import de.htwg.madn.view.tui.TUIView;

public class Application extends Controller {

	private static IBoardControllerPort boardController = GameTuiSingleton
			.getInstance().getBoardController();
	private static TUIView tui = GameTuiSingleton.getInstance().getTUIView();

	public static Result index() {
		return getResult();
	}

	public static Result command(String id) {
		tui.handleInput(id);
		return getResult();
	}

	private static Result getResult() {
		return ok(views.html.index.render(boardController));
	}

	public static Result apiTuiCommand(String id) {
		tui.handleInput(id);
		return ok();
	}

	public static Result apiJsonCommand(String cmd) {
		ObjectNode result = Json.newObject();
		final Map<String, String[]> req = request().body().asFormUrlEncoded();

		if ("dice".equals(cmd)) {
			boardController.rollDice();
			Dice dice = boardController.getModelPort().getDice();
			if (dice.getThrowsCount() > 0) {
				int diceNumber = dice.getLastNumber();
				result.put("number", diceNumber);
			}
		}

		return ok(result);
	}

	public static WebSocket<JsonNode> connectWebSocket() {
		return new WebSocket<JsonNode>() {

			@Override
			public void onReady(WebSocket.In<JsonNode> in,
					WebSocket.Out<JsonNode> out) {
				new BoardControllerObserver(boardController, out);
			}

		};
	}
}
