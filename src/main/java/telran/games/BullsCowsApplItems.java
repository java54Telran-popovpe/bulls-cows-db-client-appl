package telran.games;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import telran.games.model.MoveData;
import telran.view.*;

public class BullsCowsApplItems {

	private static final String MESS_EMPTY_LIST = "List of possible games you could use is empty";
	private static final String ITEM_GUESS = "Guess a sequence of " + BullsCowsService.N_DIGITS + " digits";
	private static final String ITEM_JOIN_GAME = "Join a game as user %s";
	private static final String ITEM_CONTINUE_GAME = "Continue a game as user %s";
	private static final String ITEM_START_GAME = "Start and play game as user %s";
	private static final String MESSAGE_USER_PLAYS_THE_GAME = "Now user %s playing game with Id %d";
	static BullsCowsService bullsCows;
	static long gameId;
	static String username;

	public static List<Item> getItems(BullsCowsService bullsCows) {
		BullsCowsApplItems.bullsCows = bullsCows;

		Item[] items = { Item.of("Login", BullsCowsApplItems::loginGamer),
				Item.of("Register", BullsCowsApplItems::registerGamer) };

		return new ArrayList<>(List.of(items));
	}

	static void loginGamer(InputOutput io) {
		String usernameToLogin = io.readString("Enter existing username");
		username = bullsCows.loginGamer(usernameToLogin);
		new Menu(String.format("Now you are %s", username), getMenuAfterGettingUsername()).perform(io);
	}

	static void registerGamer(InputOutput io) {
		String usernameToLogin = io.readString("Enter new username");
		LocalDate birthdate = io.readIsoDate("Enter birthdate (YYYY-MM-DD)", "Error parsing date, please enter again");
		bullsCows.registerGamer(usernameToLogin, birthdate);
		username = usernameToLogin;
		new Menu(String.format("Now you are %s", username), getMenuAfterGettingUsername()).perform(io);
	}

	private static Item[] getMenuAfterGettingUsername() {
		Item[] items = { 	Item.of("Create a new game",BullsCowsApplItems::createGame),
							Item.of(String.format(ITEM_START_GAME,username), BullsCowsApplItems::startGameAndPlay),
							Item.of(String.format(ITEM_CONTINUE_GAME, username), BullsCowsApplItems::chooseStartedGameAndPlay),
							Item.of(String.format(ITEM_JOIN_GAME, username), BullsCowsApplItems::joinGame),
							Item.ofExit()};
		return items;
	}
	
	private static void createGame(InputOutput io) {
		long newGameId = bullsCows.createGame();
		io.writeLine(String.format("Game with id %d created", newGameId));
		gameId = newGameId;
	}

	private static void startGameAndPlay(InputOutput io) {

		HashSet<String> optionsSet = bullsCows.getNotStartedGamesWithGamer(username).stream().map(n -> Long.toString(n))
				.collect(Collectors.toCollection(HashSet<String>::new));

		if (optionsSet.isEmpty()) {
			io.writeLine(MESS_EMPTY_LIST);
		} else {
			gameId = getGameIdFromOptions(io, optionsSet);
			bullsCows.startGame(gameId);
			Menu makingMoveMenu = new Menu(String.format(MESSAGE_USER_PLAYS_THE_GAME, username, gameId),
					getItemForPlayingGame());
			makingMoveMenu.perform(io);
		}
	}

	private static Item[] getItemForPlayingGame() {
		return new Item[] {
				Item.of(ITEM_GUESS, BullsCowsApplItems::guessItem),
				Item.ofExit() };
	}

	private static void chooseStartedGameAndPlay(InputOutput io) {
		HashSet<String> optionsSet = bullsCows.getStartedGamesWithGamer(username).stream().map(n -> Long.toString(n))
				.collect(Collectors.toCollection(HashSet<String>::new));
		if (optionsSet.isEmpty()) {
			io.writeLine(MESS_EMPTY_LIST);
		} else {
			gameId = getGameIdFromOptions(io, optionsSet);
			Menu makingMoveMenu = new Menu(String.format(MESSAGE_USER_PLAYS_THE_GAME, username, gameId),
					getItemForPlayingGame());
			makingMoveMenu.perform(io);
		}

	}

	private static void joinGame(InputOutput io) {
		HashSet<String> optionsSet = bullsCows.getNotStartedGamesWithNoGamer(username).stream()
				.map(n -> Long.toString(n)).collect(Collectors.toCollection(HashSet<String>::new));
		if (optionsSet.isEmpty()) {
			io.writeLine(MESS_EMPTY_LIST);
		} else {
			gameId = getGameIdFromOptions(io, optionsSet);
			bullsCows.gamerJoinGame(gameId, username);
			io.writeLine(String.format("You have joined to the game Id %d", gameId));
		}
	}

	private static long getGameIdFromOptions(InputOutput io, HashSet<String> optionsSet) {
		String optionsList = optionsSet.stream().collect(Collectors.joining("\n"));
		String userInput = io.readStringOptions("Enter one of following game Ids to complete action\n" + optionsList,
				"Unknown game Id", optionsSet);
		return Long.parseLong(userInput);
	}

	private static void guessItem(InputOutput io) {
		String promt = "enter " + BullsCowsService.N_DIGITS + " non-repeated digits";
		String guess = io.readString(promt);
		List<MoveData> history = bullsCows.moveProcessing(guess, gameId, username);
		history.forEach(io::writeLine);
		if (bullsCows.gameOver(gameId)) {
			io.writeLine("Congratulations: you are winner");
		}
	}
}
