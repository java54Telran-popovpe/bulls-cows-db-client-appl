package telran.games;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.json.JSONObject;

import telran.net.Request;
import telran.net.TcpClient;
import telran.net.games.model.*;
import telran.net.games.service.BullsCowsService;

public class BullsCowsProxy implements BullsCowsService {
	TcpClient tcpClient;

	public BullsCowsProxy(TcpClient tcpClient) {
		this.tcpClient = tcpClient;
	}

	@Override
	public long createGame() {
		String strRes = tcpClient.sendAndReceive(new Request("createGame", ""));
		return Long.parseLong(strRes);
	}

	@Override
	public List<String> startGame(long gameId) {
		String strRes = tcpClient.sendAndReceive(new Request("startGame", Long.toString(gameId)));
		return resultsFromJSON(strRes, String::toString);
	}

	@Override
	public void registerGamer(String username, LocalDate birthDate) {
		tcpClient
				.sendAndReceive(new Request("registerGamer", new UsernameBirthdateDto(username, birthDate).toString()));

	}

	@Override
	public void gamerJoinGame(long gameId, String username) {
		tcpClient.sendAndReceive(new Request("gamerJoinGame", new GameGamerDto(gameId, username).toString()));

	}

	@Override
	public List<Long> getNotStartedGames() {
		String strRes = tcpClient.sendAndReceive(new Request("getNotStartedGames", ""));
		return resultsFromJSON(strRes, Long::parseLong);
	}

	@Override
	public List<MoveData> moveProcessing(String sequence, long gameId, String username) {
		String strRes = tcpClient.sendAndReceive(
				new Request("moveProcessing", new SequenceGameGamerDto(sequence, gameId, username).toString()));
		return resultsFromJSON(strRes, s -> new MoveData(new JSONObject(s)));
	}

	@Override
	public boolean gameOver(long gameId) {
		String strRes = tcpClient.sendAndReceive(new Request("gameOver", Long.toString(gameId)));
		return Boolean.parseBoolean(strRes);
	}

	@Override
	public List<String> getGameGamers(long gameId) {
		String strRes = tcpClient.sendAndReceive(new Request("getGameGamers", Long.toString(gameId)));
		return resultsFromJSON(strRes, String::toString);
	}

	@Override
	public List<Long> getNotStartedGamesWithGamer(String username) {
		String strRes = tcpClient.sendAndReceive(new Request("getNotStartedGamesWithGamer", username));
		return resultsFromJSON(strRes, Long::parseLong);
	}

	@Override
	public List<Long> getNotStartedGamesWithNoGamer(String username) {
		String strRes = tcpClient.sendAndReceive(new Request("getNotStartedGamesWithNoGamer", username));
		return resultsFromJSON(strRes, Long::parseLong);
	}

	@Override
	public List<Long> getStartedGamesWithGamer(String username) {
		String strRes = tcpClient.sendAndReceive(new Request("getStartedGamesWithGamer", username));
		return resultsFromJSON(strRes, Long::parseLong);
	}

	@Override
	public String loginGamer(String username) {
		return tcpClient.sendAndReceive(new Request("loginGamer", username));
	}

	private <T> List<T> resultsFromJSON(String res, Function<String, T> map) {
		return res.length() == 0 ? List.of() : Arrays.stream(res.split(";")).map(map).toList();
	}

}
