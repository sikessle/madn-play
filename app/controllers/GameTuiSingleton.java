package controllers;

import de.htwg.madn.controller.BoardController;
import de.htwg.madn.controller.IBoardControllerPort;
import de.htwg.madn.model.Board;
import de.htwg.madn.model.GameSettings;
import de.htwg.madn.model.IGameSettings;
import de.htwg.madn.model.IModelPort;
import de.htwg.madn.model.ModelPort;
import de.htwg.madn.view.tui.TUIView;

public class GameTuiSingleton {

	private static final int MINPLAYERS = 2;
	private static final int MAXPLAYERS = 4;
	private static final int FIGURESPERPLAYER = 4;
	private static final int PUBLICFIELDSCOUNT = 40;
	private static final int DICEMIN = 1;
	private static final int DICEMAX = 6;
	private static final int MINNUMBERTOEXITHOME = 6;
	private static final int THROWSALLOWEDINHOME = 3;
	private static final int THROWSALLOWEDINPUBLIC = 1;

	private static GameTuiSingleton instance;

	private final IGameSettings settings;
	private final IModelPort model;
	private final IBoardControllerPort boardController;
	private final TUIView tui;

	private GameTuiSingleton() {
		settings = new GameSettings(MINPLAYERS, MAXPLAYERS, FIGURESPERPLAYER,
				PUBLICFIELDSCOUNT, DICEMIN, DICEMAX, MINNUMBERTOEXITHOME,
				THROWSALLOWEDINHOME, THROWSALLOWEDINPUBLIC);
		model = new ModelPort(settings, new Board(settings));
		boardController = new BoardController(model);
		// new GUIView(boardController);
		tui = new TUIView(boardController);
	}

	public static GameTuiSingleton getInstance() {
		assureInstanceIsAvailable();
		return instance;
	}

	private static void assureInstanceIsAvailable() {
		if (instance == null) {
			instance = new GameTuiSingleton();
		}
	}

	public TUIView getTUIView() {
		return tui;
	}

	public IBoardControllerPort getBoardController() {
		return boardController;
	}

}
