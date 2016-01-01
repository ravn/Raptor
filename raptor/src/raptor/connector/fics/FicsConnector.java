/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2016 RaptorProject (https://github.com/Raptor-Fics-Interface/Raptor)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.connector.fics;

import static raptor.chess.util.GameUtils.getChessPieceCharacter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.RaptorAction;
import raptor.action.RaptorAction.Category;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.connector.fics.pref.FicsPage;
import raptor.connector.fics.pref.FicsRightClickChannelMenu;
import raptor.connector.fics.pref.FicsRightClickGamesMenu;
import raptor.connector.fics.pref.FicsRightClickPersonMenu;
import raptor.connector.ics.IcsConnector;
import raptor.connector.ics.IcsConnectorContext;
import raptor.connector.ics.IcsParser;
import raptor.connector.ics.IcsUtils;
import raptor.connector.ics.dialog.IcsLoginDialog;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.pref.page.ActionContainerPage;
import raptor.pref.page.ConnectorMessageBlockPage;
import raptor.pref.page.ConnectorQuadrantsPage;
import raptor.service.ActionScriptService;
import raptor.service.ThreadService;
import raptor.swt.BugButtonsWindowItem;
import raptor.swt.FicsSeekDialog;
import raptor.swt.RegularExpressionEditorDialog;
import raptor.swt.SWTUtils;
import raptor.swt.chat.ChatConsole;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chat.controller.RegExController;
import raptor.swt.chess.controller.PlayingMouseAction;
import raptor.util.RaptorLogger;
import raptor.util.RaptorRunnable;
import raptor.util.RaptorStringTokenizer;

/**
 * The connector used to connect to www.freechess.org.
 */
public class FicsConnector extends IcsConnector implements PreferenceKeys,
		GameConstants {
	protected static L10n local = L10n.getInstance();
	private MenuManager actions, linksMenu;
	private static final RaptorLogger LOG = RaptorLogger
			.getLog(FicsConnector.class);
	protected static final String[][] PROBLEM_ACTIONS = {
			{ local.getString("ficsConn1"), "tell puzzlebot gettactics" },
			{ local.getString("ficsConn2"), "tell puzzlebot getmate" },
			{ "separator", "separator" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(PAWN, true) + " vs "
							+ getChessPieceCharacter(KING, false),
					"tell endgamebot play kpk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(PAWN, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(PAWN, false),
					"tell endgamebot play kpkp" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(PAWN, true)
							+ getChessPieceCharacter(PAWN, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(PAWN, false),
					"tell endgamebot play kppkp" },
			{ "separator", "separator" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(QUEEN, true) + " vs "
							+ getChessPieceCharacter(KING, false),
					"tell endgamebot play kqk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(QUEEN, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(ROOK, false),
					"tell endgamebot play kqkr" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(QUEEN, true)
							+ getChessPieceCharacter(PAWN, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(QUEEN, false),
					"tell endgamebot play kqpkq" },
			{ "separator", "separator" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(ROOK, true) + " vs "
							+ getChessPieceCharacter(KING, false),
					"tell endgamebot play krk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(ROOK, true)
							+ getChessPieceCharacter(PAWN, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(ROOK, false),
					"tell endgamebot play krpk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(ROOK, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(PAWN, false),
					"tell endgamebot play krkp" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(ROOK, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(KNIGHT, false),
					"tell endgamebot play krkn" },
			{ "separator", "separator" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(BISHOP, true)
							+ getChessPieceCharacter(BISHOP, true) + " vs "
							+ getChessPieceCharacter(KING, false),
					"tell endgamebot play kbbk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(BISHOP, true)
							+ getChessPieceCharacter(KNIGHT, true) + " vs "
							+ getChessPieceCharacter(KING, false),
					"tell endgamebot play kbnk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(KNIGHT, true)
							+ getChessPieceCharacter(KNIGHT, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(PAWN, false),
					"tell endgamebot play knnkp" } };

	protected MenuManager ficsMenu;
	protected Action autoConnectAction;
	protected Action connectAction;
	protected List<Action> onlyEnabledOnConnectActions = new ArrayList<Action>(
			20);

	protected Object extendedCensorSync = new Object();
	protected static final String EXTENDED_CENSOR_FILE_NAME = Raptor.USER_RAPTOR_HOME_PATH
			+ "/fics/extendedCensor.txt";

	protected GameBotService gameBotService = new GameBotService(this);
	protected GameBotParser gameBotParser = new GameBotParser(this);
	protected String partnerOnConnect;
	protected boolean isDisconnecting = false;

	public FicsConnector() {
		this(new IcsConnectorContext(new IcsParser(false)));
	}

	public FicsConnector(IcsConnectorContext context) {
		super(context);
		context.getParser().setConnector(this);
		createMenuActions();

	}

	protected String getInitialTimesealString() {
		return "TIMESEAL2|OpenSeal|OpenSeal|";
	}

	public GameBotService getGameBotService() {
		return gameBotService;
	}

	public void setGameBotService(GameBotService gameBotService) {
		this.gameBotService = gameBotService;
	}

	@Override
	public void disconnect() {

		synchronized (this) {
			if (!isDisconnecting) {
				isDisconnecting = true;
				try {
					if (isConnected()) {
						super.disconnect();
					}
					connectAction.setEnabled(true);
					if (autoConnectAction != null) {
						autoConnectAction.setEnabled(true);
					}
					for (Action action : onlyEnabledOnConnectActions) {
						action.setEnabled(false);
					}
				} finally {
					isDisconnecting = false;
				}
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * Returns the menu manager for this connector.
	 */
	public MenuManager getMenuManager() {
		return ficsMenu;
	}

	/**
	 * Return the preference node to add to the root preference dialog. This
	 * preference node will show up with the connectors first name. You can add
	 * secondary nodes by implementing getSecondaryPreferenceNodes. These nodes
	 * will show up below the root node.
	 */
	public PreferencePage getRootPreferencePage() {
		return new FicsPage();
	}

	/**
	 * Returns an array of the secondary preference nodes.
	 */
	public PreferenceNode[] getSecondaryPreferenceNodes() {
		return new PreferenceNode[] {
				new PreferenceNode("ficsMenuActions", new ActionContainerPage(
						local.getString("ficsConn3"),
						local.getString("ficsConn4"),
						RaptorActionContainer.FicsMenu)),
				new PreferenceNode("fics",
						new ConnectorMessageBlockPage("fics")),
				new PreferenceNode("fics", new FicsRightClickChannelMenu()),
				new PreferenceNode("fics", new FicsRightClickGamesMenu()),
				new PreferenceNode("fics", new FicsRightClickPersonMenu()),

				new PreferenceNode("fics", new ConnectorQuadrantsPage("fics")),
				new PreferenceNode("fics", new ConnectorQuadrantsPage("fics2")), };

	}

	/**
	 * Returns true if isConnected and a user is playing a game.
	 */
	public boolean isLoggedInUserPlayingAGame() {
		boolean result = false;
		if (isConnected()) {
			for (Game game : getGameService().getAllActiveGames()) {
				if (game.isInState(Game.PLAYING_STATE)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * This method is overridden to support simul bughouse.
	 */
	@Override
	public void publishEvent(final ChatEvent event) {
		if (chatService != null) { // Could have been disposed.

			// handle gamebot tab messages.
			GameBotParseResults gameBotResults = gameBotParser.parse(event);
			if (gameBotResults != null) {
				if (!gameBotResults.isIncomplete()) {
					if (gameBotResults.isPlayerInDb()) {
						gameBotService.fireGameBotPageArrived(
								gameBotResults.getRows(),
								gameBotResults.hasNextPage);
					} else {
						gameBotService
								.fireGameBotPlayerNotInDb(gameBotResults.playerName);
					}
				}
				return;
			}

			if (event.getType() == ChatType.PARTNERSHIP_CREATED) {

				if (getPreferences().getBoolean(
						PreferenceKeys.FICS_SHOW_BUGBUTTONS_ON_PARTNERSHIP)) {
					SWTUtils.openBugButtonsWindowItem(this);
				}
				if (getPreferences().getBoolean(
						PreferenceKeys.BUGHOUSE_SHOW_BUGWHO_ON_PARTNERSHIP)) {
					SWTUtils.openBugWhoWindowItem(this);
				}

			} else if (event.getType() == ChatType.PARTNERSHIP_DESTROYED) {
				isSimulBugConnector = false;
				simulBugPartnerName = null;

				if (LOG.isDebugEnabled()) {
					LOG.debug("Partnership destroyed. Resetting partnership information.");
				}

				// Remove bug buttons if up displayed you have no partner.
				RaptorWindowItem[] windowItems = Raptor.getInstance()
						.getWindow().getWindowItems(BugButtonsWindowItem.class);
				for (RaptorWindowItem item : windowItems) {
					BugButtonsWindowItem bugButtonsItem = (BugButtonsWindowItem) item;
					if (bugButtonsItem.getConnector() == this) {
						Raptor.getInstance().getWindow()
								.disposeRaptorWindowItem(bugButtonsItem);
					}
				}
			}
			super.publishEvent(event);
		}
	}

	protected void addProblemActions(MenuManager problemMenu) {
		for (int i = 0; i < PROBLEM_ACTIONS.length; i++) {
			if (PROBLEM_ACTIONS[i][0].equals("separator")) {
				problemMenu.add(new Separator());
			} else {
				final String action = PROBLEM_ACTIONS[i][1];
				Action problemAction = new Action(PROBLEM_ACTIONS[i][0]) {
					public void run() {
						sendMessage(action);
					}
				};
				problemAction.setEnabled(false);
				onlyEnabledOnConnectActions.add(problemAction);
				problemMenu.add(problemAction);
			}

		}
	}

	@Override
	protected void connect(final String profileName) {
		if (actions != null && actions.isEmpty()) {
			// Don't do this for fics2, or if menu's been created before.
			createFicsMenuActions();
		}
		synchronized (this) {
			if (!isConnected()) {
				for (Action action : onlyEnabledOnConnectActions) {
					action.setEnabled(true);
				}
				connectAction.setEnabled(false);
				if (autoConnectAction != null) {
					autoConnectAction.setChecked(getPreferences().getBoolean(
							context.getPreferencePrefix() + "auto-connect"));
					autoConnectAction.setEnabled(true);
				}

				super.connect(profileName);

				if (isConnecting) {
					if (getPreferences().getBoolean(
							context.getPreferencePrefix()
									+ "show-bugbuttons-on-connect")) {
						Raptor.getInstance()
								.getWindow()
								.addRaptorWindowItem(
										new BugButtonsWindowItem(this));
					}
				}
			}
		}
	}

	/**
	 * Creates menu with fics server actions all of which can be invoked only
	 * after connecting to the server
	 */
	private void createFicsMenuActions() {
		// create fics actions menu
		RaptorAction[] scripts = ActionScriptService.getInstance().getActions(
				Category.IcsCommands);
		for (final RaptorAction raptorAction : scripts) {
			Action action = new Action(raptorAction.getName()) {
				public void run() {
					raptorAction.setConnectorSource(FicsConnector.this);
					raptorAction.run();
				}
			};
			action.setEnabled(false);
			action.setToolTipText(raptorAction.getDescription());
			onlyEnabledOnConnectActions.add(action);
			actions.add(action);
		}

		// create links menu
		RaptorAction[] ficsMenuActions = ActionScriptService.getInstance()
				.getActions(RaptorActionContainer.FicsMenu);
		for (final RaptorAction raptorAction : ficsMenuActions) {
			if (raptorAction instanceof Separator) {
				linksMenu.add(new Separator());
			} else {
				Action action = new Action(raptorAction.getName()) {
					@Override
					public void run() {
						raptorAction.setConnectorSource(FicsConnector.this);
						raptorAction.run();
					}
				};
				action.setToolTipText(raptorAction.getDescription());
				linksMenu.add(action);
			}
		}
	}

	public void showLoginDialog() {
		Raptor.getInstance().getWindow().getShell().getDisplay()
				.syncExec(new RaptorRunnable() {
					@Override
					public void execute() {
						IcsLoginDialog dialog = new IcsLoginDialog(context
								.getPreferencePrefix(), local
								.getString("ficsConn7"));
						dialog.open();
						getPreferences().setValue(
								context.getPreferencePrefix() + "profile",
								dialog.getSelectedProfile());
						autoConnectAction.setChecked(getPreferences()
								.getBoolean(
										context.getPreferencePrefix()
												+ "auto-connect"));
						getPreferences().save();
						if (dialog.wasLoginPressed()) {
							connect();
						}
					}
				});

	}

	/**
	 * Creates the connectionsMenu and all of the actions associated with it.
	 */
	protected void createMenuActions() {
		ficsMenu = new MenuManager(local.getString("ficsConn5"));
		connectAction = new Action(local.getString("ficsConn6")) {
			@Override
			public void run() {
				showLoginDialog();
			}
		};

		Action disconnectAction = new Action(local.getString("ficsConn8")) {
			@Override
			public void run() {
				disconnect();
			}
		};

		Action reconnectAction = new Action(local.getString("ficsConn9")) {
			@Override
			public void run() {
				disconnect();
				// Sleep half a second for everything to adjust.
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
				}
				connect(currentProfileName);
			}
		};

		Action seekTableAction = new Action(local.getString("ficsConn10")) {
			@Override
			public void run() {
				SWTUtils.openSeekTableWindowItem(FicsConnector.this);
			}
		};

		Action bugwhoAction = new Action(local.getString("ficsConn11")) {
			@Override
			public void run() {
				SWTUtils.openBugWhoWindowItem(FicsConnector.this);
			}
		};

		Action bugbuttonsAction = new Action(local.getString("ficsConn12")) {
			@Override
			public void run() {
				SWTUtils.openBugButtonsWindowItem(FicsConnector.this);
			}
		};

		Action gamesAction = new Action(local.getString("ficsConn13")) {
			@Override
			public void run() {
				SWTUtils.openGamesWindowItem(FicsConnector.this);
			}
		};

		Action regexTabAction = new Action(local.getString("ficsConn14")) {
			@Override
			public void run() {
				RegularExpressionEditorDialog regExDialog = new RegularExpressionEditorDialog(
						Raptor.getInstance().getWindow().getShell(),
						getShortName() + local.getString("ficsConn15"),
						local.getString("ficsConn16"));
				String regEx = regExDialog.open();
				if (StringUtils.isNotBlank(regEx)) {
					final RegExController controller = new RegExController(
							FicsConnector.this, regEx);
					ChatConsoleWindowItem chatConsoleWindowItem = new ChatConsoleWindowItem(
							controller);
					Raptor.getInstance().getWindow()
							.addRaptorWindowItem(chatConsoleWindowItem, false);
					ChatUtils
							.appendPreviousChatsToController((ChatConsole) chatConsoleWindowItem
									.getControl());
				}
			}
		};

		autoConnectAction = new Action(local.getString("ficsConn17"),
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				getPreferences().setValue(
						context.getPreferencePrefix() + "auto-connect",
						isChecked());
				getPreferences().save();
			}
		};

		Action showSeekDialogAction = new Action(local.getString("ficsConn21")) {
			public void run() {
				FicsSeekDialog dialog = new FicsSeekDialog(Raptor.getInstance()
						.getWindow().getShell());
				String seek = dialog.open();
				if (seek != null) {
					sendMessage(seek);
				}
			}
		};

		actions = new MenuManager(local.getString("ficsConn22"));

		connectAction.setEnabled(true);
		disconnectAction.setEnabled(false);
		reconnectAction.setEnabled(false);
		autoConnectAction.setEnabled(true);
		bugwhoAction.setEnabled(false);
		seekTableAction.setEnabled(false);
		regexTabAction.setEnabled(false);
		bugbuttonsAction.setEnabled(false);
		showSeekDialogAction.setEnabled(false);
		gamesAction.setEnabled(false);

		onlyEnabledOnConnectActions.add(bugwhoAction);
		onlyEnabledOnConnectActions.add(disconnectAction);
		onlyEnabledOnConnectActions.add(reconnectAction);
		onlyEnabledOnConnectActions.add(regexTabAction);
		onlyEnabledOnConnectActions.add(seekTableAction);
		onlyEnabledOnConnectActions.add(bugbuttonsAction);
		onlyEnabledOnConnectActions.add(showSeekDialogAction);
		onlyEnabledOnConnectActions.add(gamesAction);

		autoConnectAction.setChecked(getPreferences().getBoolean(
				context.getPreferencePrefix() + "auto-connect"));

		ficsMenu.add(connectAction);
		ficsMenu.add(disconnectAction);
		ficsMenu.add(reconnectAction);
		ficsMenu.add(autoConnectAction);

		ficsMenu.add(new Separator());
		ficsMenu.add(actions);
		MenuManager tabsMenu = new MenuManager(local.getString("ficsConn23"));
		tabsMenu.add(gamesAction);
		tabsMenu.add(seekTableAction);
		tabsMenu.add(new Separator());
		tabsMenu.add(bugbuttonsAction);
		tabsMenu.add(bugwhoAction);
		tabsMenu.add(new Separator());
		tabsMenu.add(regexTabAction);
		ficsMenu.add(tabsMenu);
		linksMenu = new MenuManager(local.getString("ficsConn24"));
		ficsMenu.add(linksMenu);
		MenuManager problems = new MenuManager(local.getString("ficsConn25"));
		addProblemActions(problems);
		ficsMenu.add(problems);
		ficsMenu.add(showSeekDialogAction);
	}

	protected boolean isSmartMoveEnabled() {
		return isSmartMoveOption(getPreferences().getString(
				PreferenceKeys.PLAYING_CONTROLLER
						+ PreferenceKeys.RIGHT_MOUSE_BUTTON_ACTION))
				|| isSmartMoveOption(getPreferences()
						.getString(
								PreferenceKeys.PLAYING_CONTROLLER
										+ PreferenceKeys.LEFT_DOUBLE_CLICK_MOUSE_BUTTON_ACTION))
				|| isSmartMoveOption(getPreferences().getString(
						PreferenceKeys.PLAYING_CONTROLLER
								+ PreferenceKeys.LEFT_MOUSE_BUTTON_ACTION))
				|| isSmartMoveOption(getPreferences().getString(
						PreferenceKeys.PLAYING_CONTROLLER
								+ PreferenceKeys.MIDDLE_MOUSE_BUTTON_ACTION))
				|| isSmartMoveOption(getPreferences().getString(
						PreferenceKeys.PLAYING_CONTROLLER
								+ PreferenceKeys.MISC1_MOUSE_BUTTON_ACTION))
				|| isSmartMoveOption(getPreferences().getString(
						PreferenceKeys.PLAYING_CONTROLLER
								+ PreferenceKeys.MISC2_MOUSE_BUTTON_ACTION));
	}

	protected boolean isSmartMoveOption(String option) {
		return option != null
				&& (option.equals(PlayingMouseAction.SmartMove.toString())
						|| option.equals(PlayingMouseAction.RandomCapture
								.toString())
						|| option.equals(PlayingMouseAction.RandomMove
								.toString()) || option
							.equals(PlayingMouseAction.RandomRecapture
									.toString()));
	}

	protected void loadExtendedCensorList() {
		if (new File(EXTENDED_CENSOR_FILE_NAME).exists()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Loading " + EXTENDED_CENSOR_FILE_NAME);
			}
			synchronized (extendedCensorSync) {
				extendedCensorList.clear();
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(
							EXTENDED_CENSOR_FILE_NAME));
					String currentLine = null;
					while ((currentLine = reader.readLine()) != null) {
						String user = currentLine.trim();
						if (StringUtils.isNotBlank(user)) {
							extendedCensorList.add(IcsUtils.stripTitles(user)
									.toLowerCase());
						}
					}

				} catch (Throwable t) {
					onError("Error reading " + EXTENDED_CENSOR_FILE_NAME, t);

				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (Throwable t) {
						}
					}
				}
			}
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info("No extended censor list found.");
			}
		}
	}

	@Override
	protected void onSuccessfulLogin() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				isConnecting = false;
				fireConnected();
				hasVetoPower = false;
				sendMessage("iset defprompt 1", true);
				sendMessage("iset gameinfo 1", true);
				sendMessage("iset ms 1", true);
				sendMessage("iset allresults 1", true);
				sendMessage("iset startpos 1", true);
				sendMessage("iset pendinfo 1", true);

				if (getPreferences().getBoolean(
						PreferenceKeys.FICS_NO_WRAP_ENABLED)) {
					sendMessage("iset nowrap 1", true);
				}
				sendMessage("iset smartmove "
						+ (isSmartMoveEnabled() ? "1" : "0"), true);
				sendMessage(
						"iset premove "
								+ (getPreferences().getBoolean(
										BOARD_PREMOVE_ENABLED) ? "1" : "0"),
						true);
				sendMessage("set interface "
						+ getPreferences().getString(APP_NAME));
				sendMessage("set style 12", true);
				sendMessage("set bell 0", true);
				sendMessage("set ptime 0", true);

				String loginScript = getPreferences().getString(
						FICS_LOGIN_SCRIPT);
				if (StringUtils.isNotBlank(loginScript)) {
					RaptorStringTokenizer tok = new RaptorStringTokenizer(
							loginScript, "\n\r", true);
					while (tok.hasMoreTokens()) {
						try {
							Thread.sleep(50L);
						} catch (InterruptedException ie) {
						}
						sendMessage(tok.nextToken().trim());
					}
				}

				if (StringUtils.isNotBlank(partnerOnConnect)) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException ie) {
					}
					sendMessage("set bugopen on");
					sendMessage("partner " + partnerOnConnect);
					partnerOnConnect = null;
				}

				sendMessage("iset lock 1", true);
				hasVetoPower = true;
			}
		});
	}

	protected void writeExtendedCensorList() {
		synchronized (extendedCensorSync) {
			FileWriter writer = null;
			try {
				writer = new FileWriter(EXTENDED_CENSOR_FILE_NAME, false);
				for (String user : extendedCensorList) {
					writer.write(user + "\n");
				}
				writer.flush();

			} catch (Throwable t) {
				onError("Error writing " + EXTENDED_CENSOR_FILE_NAME, t);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (Throwable t) {
					}
				}
			}
		}
	}
}
