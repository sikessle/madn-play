package controllers;

import java.util.HashMap;
import java.util.Map;

import models.BoardControllerObserver;
import play.core.Router;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.OpenID;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.data.*;
import static play.data.Form.*;
import play.*;
import play.mvc.*;
import play.mvc.Http.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.htwg.madn.controller.IBoardControllerPort;
import de.htwg.madn.model.Dice;
import de.htwg.madn.view.tui.TUIView;

public class Application extends Controller {

	private static IBoardControllerPort boardController = GameTuiSingleton
			.getInstance().getBoardController();
	private static TUIView tui = GameTuiSingleton.getInstance().getTUIView();

	@Security.Authenticated(Secured.class)
	public static Result index() {
		return getResult();
	}

	public static Result command(String id) {
		tui.handleInput(id);
		return getResult();
	}

	private static Result getResult() {
		String user = request().username();
		return ok(views.html.index.render(boardController, user));
	}

	public static Result apiTuiCommand(String id) {
		tui.handleInput(id);
		return ok();
	}

	public static Result apiJsonCommand(String cmd) {
		ObjectNode result = Json.newObject();

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

	public static Result openIdAuth() {
		String providerUrl = "https://www.google.com/accounts/o8/id";
		String returnToUrl = "http://" + request().host() + "/openID/verify";
		Map<String, String> attributes = new HashMap<>();
		attributes.put("Email", "http://schema.openid.net/contact/email");
		F.Promise<String> redirectUrl = OpenID.redirectURL(providerUrl,
				returnToUrl, attributes);
		return redirect(redirectUrl.get());
	}

	public Result openIdVerify() {
		try {
			F.Promise<OpenID.UserInfo> userInfoPromise = OpenID.verifiedId();
			OpenID.UserInfo userInfo = userInfoPromise.get();
			session().clear();
			session("email", userInfo.attributes.get("Email"));
			return redirect(routes.Application.index());
		} catch (Throwable x) {
			return redirect(routes.Application.login());
		}

	}

	public static Result logout() {
		session().clear();
		return redirect(routes.Application.login());
	}

	public static Result login() {
		return ok(views.html.login.render(Form.form(Login.class)));
	}

	public static Result authenticate() {
		Form<Login> loginForm = DynamicForm.form(Login.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			return badRequest(views.html.login.render(loginForm));
		} else {
			session().clear();
			session("email", loginForm.get().email);
			return redirect(routes.Application.index());
		}
	}

	public static class Login {

		public String email;
		public String password;

		private static String defaultEmail = "user@htwg.de";
		private static String defaultPassword = "root";

		public String validate() {

			if (defaultEmail.equals(email) && defaultPassword.equals(password)) {
				return null;
			}
			return "Invalid user or password";
		}
	}
}
