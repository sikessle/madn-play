package models;

import play.mvc.WebSocket.Out;

import com.fasterxml.jackson.databind.JsonNode;

import de.htwg.madn.controller.IBoardControllerPort;
import de.htwg.madn.util.observer.IObserver;

public class BoardControllerObserver implements IObserver {

	private final Out<JsonNode> out;
	private final GameBoardToJson jsonifier;

	public BoardControllerObserver(IBoardControllerPort boardController,
			Out<JsonNode> out) {
		this.out = out;
		jsonifier = new GameBoardToJson(boardController);
		boardController.addObserver(this);
		update();
	}

	@Override
	public void update() {
		out.write(jsonifier.getGameAsJson());
	}

}
