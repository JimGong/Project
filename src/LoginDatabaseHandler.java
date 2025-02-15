import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles all database-related actions. Uses singleton design pattern.
 *
 * @see LoginServer
 */
public class LoginDatabaseHandler {

	/** A {@link org.apache.log4j.Logger log4j} logger for debugging. */
	private static Logger log = LogManager.getLogger();

	/** Makes sure only one database handler is instantiated. */
	private static LoginDatabaseHandler singleton = new LoginDatabaseHandler();

	/** Used to determine if necessary tables are provided. */
	private static final String LOGIN_USER_TABLES_SQL = "SHOW TABLES LIKE 'login_users';";
	private static final String SEARCH_HISTORY_TABLES_SQL = "SHOW TABLES LIKE 'search_history';";
	private static final String URL_TABLES_SQL = "SHOW TABLES LIKE 'URL';";
	private static final String FAVOURITE_TABLES_SQL = "SHOW TABLES LIKE 'favourite';";

	/** Used to create necessary tables for this example. */
	private static final String CREATE_LOGIN_USERS_SQL = "CREATE TABLE login_users ("
			+ "userid INTEGER AUTO_INCREMENT PRIMARY KEY, "
			+ "username VARCHAR(32) NOT NULL UNIQUE, "
			+ "password CHAR(64) NOT NULL, " + "usersalt CHAR(32) NOT NULL"
			+ "lastlogin CHAR(64) NOT NULL UNIQUE);";

	/** Used to create search history tables for this example. */
	private static final String CREATE_SEARCH_HISTORY_SQL = "CREATE TABLE search_history (userid INTEGER AUTO_INCREMENT PRIMARY KEY, username VARCHAR(32) NOT NULL, query CHAR(64) NOT NULL, time CHAR(64) NOT NULL);";

	private static final String CREATE_URL_SQL = "CREATE TABLE URL (id INTEGER AUTO_INCREMENT PRIMARY KEY,"
			+ " url CHAR(250) NOT NULL UNIQUE, title CHAR(64) NOT NULL, snippet CHAR(250), lastvisit CHAR(64) NOT NULL);";

	private static final String CREATE_FAVOURITE_SQL = "CREATE TABLE favourite (id INTEGER AUTO_INCREMENT PRIMARY KEY, username CHAR(64) NOT NULL, url CHAR(250) NOT NULL, time CHAR(64) NOT NULL);";

	/** Used to insert a new user into the database. */
	private static final String REGISTER_SQL = "INSERT INTO login_users (username, password, usersalt, lastlogin) "
			+ "VALUES (?, ?, ?, NOW());";

	/** Used to determine if a username already exists. */
	private static final String USER_SQL = "SELECT username FROM login_users WHERE username = ?";

	/***/
	private static final String ADD_QUERY_SQL = "INSERT INTO search_history (username, query, time) "
			+ "VALUES (?, ?, NOW());";

	private static final String ADD_URL_SQL = "INSERT INTO URL (url, title, snippet, lastvisit) VALUES(?, ?, ?, ?);";

	private static final String ADD_FAVOURIATE_SQL = "INSERT INTO favourite (username, url, time) VALUES(?, ?, NOW());";

	/** Used to change the passward for the user */
	private static final String UPDATE_PASSWORD_SQL = "UPDATE login_users SET password = ?, usersalt = ? "
			+ "WHERE username = ?;";

	private static final String UPDATE_FAVOURITE_SQL = "UPDATE favourite SET url = ? WHERE username = ?;";

	private static final String UPDATE_LOGIN_TIME_SQL = "UPDATE login_users SET lastlogin = NOW() "
			+ "WHERE username = ?;";

	private static final String UPDATE_VISITED_TIME_SQL = "UPDATE URL SET lastvisit = NOW() WHERE url = ?;";

	private static final String UPDATE_SNIPPET_SQL = "UPDATE URL SET snippet = ? WHERE url = ?;";

	private static final String GET_LOGIN_TIME_SQL = "SELECT DISTINCT(lastlogin) FROM login_users "
			+ "WHERE username= ?;";

	private static final String GET_LOGGED_IN_USERS_SQL = "SELECT username FROM  login_users ORDER BY lastlogin DESC LIMIT 5;";

	private static final String GET_FAVOURITE_SQL = "SELECT url FROM favourite WHERE username = ?;";

	private static final String GET_FAVOURITE_RESULT = "SELECT CONCAT(time,'&nbsp;&nbsp;&nbsp;', url) AS full_favourite FROM favourite WHERE username = ? ORDER BY time DESC;";

	private static final String GET_URL_VISITED_TIME_SQL = "SELECT lastvisit FROM URL WHERE url = ?;";

	private static final String GET_SNIPPET_SQL = "SELECT snippet FROM URL WHERE url = ?;";

	private static final String GET_TITLE_SQL = "SELECT title FROM URL WHERE url=?;";

	private static final String GET_SUGGESTED_QUERY_SQL = "SELECT DISTINCT(query) AS query FROM search_history WHERE username <> ? ORDER BY time DESC LIMIT 5;";

	/** Used to retrieve the salt associated with a specific user. */
	private static final String SALT_SQL = "SELECT usersalt FROM login_users WHERE username = ?";

	/** Used to authenticate a user. */
	private static final String AUTH_SQL = "SELECT username FROM login_users "
			+ "WHERE username = ? AND password = ?";

	private static final String CHECK_URL_SQL = "SELECT url FROM URL WHERE url = ?;";

	private static final String CHECK_USER_EXIST_SQL = "SELECT username FROM login_users WHERE username=?;";

	/** Used to remove a user from the database. */
	private static final String DELETE_SQL = "DELETE FROM login_users WHERE username = ?";

	private static final String GET_HISTORY_SQL = "SELECT CONCAT(time, '&nbsp;&nbsp;&nbsp;', query)"
			+ "AS full_history FROM search_history WHERE username= ? ORDER BY time DESC;";

	private static final String CLEAN_HISTORY_SQL = "DELETE FROM search_history WHERE username = ?;";

	private static final String CLEAN_FAVOURITE_SQL = "DELETE FROM favourite WHERE username = ?;";

	/** Used to configure connection to database. */
	private DatabaseConnector db;

	/** Used to generate password hash salt for user. */
	private Random random;

	/**
	 * Initializes a database handler for the Login example. Private constructor
	 * forces all other classes to use singleton.
	 */
	private LoginDatabaseHandler() {
		Status status = Status.OK;
		random = new Random(System.currentTimeMillis());

		try {
			db = new DatabaseConnector("database.properties");

			status = db.testConnection() ? setupLoginTables()
					: Status.CONNECTION_FAILED;

		} catch (FileNotFoundException e) {
			status = Status.MISSING_CONFIG;
		} catch (IOException e) {
			status = Status.MISSING_VALUES;
		}

		if (status != Status.OK) {
			log.fatal(status.message());
		}
	}

	/**
	 * Gets the single instance of the database handler.
	 *
	 * @return instance of the database handler
	 */
	public static LoginDatabaseHandler getInstance() {
		return singleton;
	}

	/**
	 * Checks to see if a String is null or empty.
	 *
	 * @param text
	 *            - String to check
	 * @return true if non-null and non-empty
	 */
	public static boolean isBlank(String text) {
		return (text == null) || text.trim().isEmpty();
	}

	/**
	 * Checks if necessary table exists in database, and if not tries to create
	 * it.
	 *
	 * @return {@link Status.OK} if table exists or create is successful
	 */
	private Status setupLoginTables() {
		Status status = Status.ERROR;

		try (Connection connection = db.getConnection();
				Statement statement = connection.createStatement();) {
			if (!statement.executeQuery(LOGIN_USER_TABLES_SQL).next()) {
				// Table missing, must create
				log.debug("Creating login user tables...");
				statement.executeUpdate(CREATE_LOGIN_USERS_SQL);

				// Check if create was successful
				if (!statement.executeQuery(LOGIN_USER_TABLES_SQL).next()) {
					status = Status.CREATE_FAILED;
				}
				else {
					status = Status.OK;
				}
			}
			else {
				log.debug("Login Users Table found.");
				status = Status.OK;
			}
			if (!statement.executeQuery(SEARCH_HISTORY_TABLES_SQL).next()) {
				// Table missing, must create
				log.debug("Creating search history tables...");
				statement.executeUpdate(CREATE_SEARCH_HISTORY_SQL);

				// Check if create was successful
				if (!statement.executeQuery(SEARCH_HISTORY_TABLES_SQL).next()) {
					status = Status.CREATE_FAILED;
				}
				else {
					status = Status.OK;
				}
			}
			else {
				log.debug("Search History Tables found.");
				status = Status.OK;
			}

			if (!statement.executeQuery(URL_TABLES_SQL).next()) {
				// Table missing, must create
				log.debug("Creating url tables...");
				statement.executeUpdate(CREATE_URL_SQL);

				// Check if create was successful
				if (!statement.executeQuery(URL_TABLES_SQL).next()) {
					status = Status.CREATE_FAILED;
				}
				else {
					status = Status.OK;
				}
			}
			else {
				log.debug("URL Tables found.");
				status = Status.OK;
			}

			if (!statement.executeQuery(FAVOURITE_TABLES_SQL).next()) {
				// Table missing, must create
				log.debug("Creating favourite url tables...");
				statement.executeUpdate(CREATE_FAVOURITE_SQL);

				// Check if create was successful
				if (!statement.executeQuery(FAVOURITE_TABLES_SQL).next()) {
					status = Status.CREATE_FAILED;
				}
				else {
					status = Status.OK;
				}
			}
			else {
				log.debug("URL Tables found.");
				status = Status.OK;
			}
		} catch (Exception ex) {
			status = Status.CREATE_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	/**
	 * Tests if a user already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection
	 *            - active database connection
	 * @param user
	 *            - username to check
	 * @return Status.OK if user does not exist in database
	 * @throws SQLException
	 */
	private Status duplicateUser(Connection connection, String user) {

		assert connection != null;
		assert user != null;

		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(USER_SQL);) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.DUPLICATE_USER : Status.OK;
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
			status = Status.SQL_EXCEPTION;
		}

		return status;
	}

	/**
	 * Tests if a user already exists in the database.
	 *
	 * @see #duplicateUser(Connection, String)
	 * @param user
	 *            - username to check
	 * @return Status.OK if user does not exist in database
	 */
	public Status duplicateUser(String user) {
		Status status = Status.ERROR;

		try (Connection connection = db.getConnection();) {
			status = duplicateUser(connection, user);
		} catch (SQLException e) {
			status = Status.CONNECTION_FAILED;
			log.debug(e.getMessage(), e);
		}

		return status;
	}

	private Status checkUserExit(Connection connection, String user) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(CHECK_USER_EXIST_SQL);) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();
			int size = 0;
			while ((results != null) && results.next()) {
				size++;
			}
			if (size == 0) {
				status = Status.INVALID_USER;
			}
			else {
				status = Status.OK;
			}
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
			status = Status.SQL_EXCEPTION;
		}

		return status;
	}

	public Status checkUserExit(String user) {
		Status status = Status.ERROR;

		try (Connection connection = db.getConnection();) {
			status = checkUserExit(connection, user);
		} catch (SQLException e) {
			status = Status.CONNECTION_FAILED;
			log.debug(e.getMessage(), e);
		}

		return status;
	}

	/**
	 * Returns the hex encoding of a byte array.
	 *
	 * @param bytes
	 *            - byte array to encode
	 * @param length
	 *            - desired length of encoding
	 * @return hex encoded byte array
	 */
	public static String encodeHex(byte[] bytes, int length) {
		BigInteger bigint = new BigInteger(1, bytes);
		String hex = String.format("%0" + length + "X", bigint);

		assert hex.length() == length;
		return hex;
	}

	/**
	 * Calculates the hash of a password and salt using SHA-256.
	 *
	 * @param password
	 *            - password to hash
	 * @param salt
	 *            - salt associated with user
	 * @return hashed password
	 */
	public static String getHash(String password, String salt) {
		String salted = salt + password;
		String hashed = salted;

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salted.getBytes());
			hashed = encodeHex(md.digest(), 64);
		} catch (Exception ex) {
			log.debug("Unable to properly hash password.", ex);
		}

		return hashed;
	}

	/**
	 * Registers a new user, placing the username, password hash, and salt into
	 * the database if the username does not already exist.
	 *
	 * @param newuser
	 *            - username of new user
	 * @param newpass
	 *            - password of new user
	 * @return {@link Status.OK} if registration successful
	 */
	private Status registerUser(Connection connection, String newuser,
			String newpass) {

		Status status = Status.ERROR;

		byte[] saltBytes = new byte[16];
		random.nextBytes(saltBytes);

		String usersalt = encodeHex(saltBytes, 32);
		String passhash = getHash(newpass, usersalt);

		try (PreparedStatement statement = connection
				.prepareStatement(REGISTER_SQL);) {
			statement.setString(1, newuser);
			statement.setString(2, passhash);
			statement.setString(3, usersalt);
			statement.executeUpdate();

			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	/**
	 * Registers a new user, placing the username, password hash, and salt into
	 * the database if the username does not already exist.
	 *
	 * @param newuser
	 *            - username of new user
	 * @param newpass
	 *            - password of new user
	 * @return {@link Status.OK} if registration successful
	 */
	public Status registerUser(String newuser, String newpass) {
		Status status = Status.ERROR;
		log.debug("Registering " + newuser + ".");

		// make sure we have non-null and non-emtpy values for login
		if (isBlank(newuser) || isBlank(newpass)) {
			status = Status.INVALID_LOGIN;
			log.debug(status);
			return status;
		}

		// try to connect to database and test for duplicate user
		try (Connection connection = db.getConnection();) {
			status = duplicateUser(connection, newuser);

			// if okay so far, try to insert new user
			if (status == Status.OK) {
				status = registerUser(connection, newuser, newpass);
			}
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Gets the salt for a specific user.
	 *
	 * @param connection
	 *            - active database connection
	 * @param user
	 *            - which user to retrieve salt for
	 * @return salt for the specified user or null if user does not exist
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private String getSalt(Connection connection, String user)
			throws SQLException {
		assert connection != null;
		assert user != null;

		String salt = null;

		try (PreparedStatement statement = connection
				.prepareStatement(SALT_SQL);) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();

			if (results.next()) {
				salt = results.getString("usersalt");
			}
		}

		return salt;
	}

	/**
	 * Checks if the provided username and password match what is stored in the
	 * database. Requires an active database connection.
	 *
	 * @param username
	 *            - username to authenticate
	 * @param password
	 *            - password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 * @throws SQLException
	 */
	private Status authenticateUser(Connection connection, String username,
			String password) throws SQLException {

		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(AUTH_SQL);) {
			String usersalt = getSalt(connection, username);
			String passhash = getHash(password, usersalt);

			statement.setString(1, username);
			statement.setString(2, passhash);

			ResultSet results = statement.executeQuery();
			status = results.next() ? status = Status.OK : Status.INVALID_LOGIN;
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
			status = Status.SQL_EXCEPTION;
		}

		return status;
	}

	/**
	 * Checks if the provided username and password match what is stored in the
	 * database. Must retrieve the salt and hash the password to do the
	 * comparison.
	 *
	 * @param username
	 *            - username to authenticate
	 * @param password
	 *            - password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 */
	public Status authenticateUser(String username, String password) {
		Status status = Status.ERROR;

		log.debug("Authenticating user " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUser(connection, username, password);
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	private boolean urlExisted(Connection connection, String url) {

		boolean isExistent = false;

		try (PreparedStatement statement = connection
				.prepareStatement(CHECK_URL_SQL);) {

			statement.setString(1, url);

			ResultSet results = statement.executeQuery();
			isExistent = results.next() ? true : false;
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);

		}
		return isExistent;
	}

	public boolean urlExisted(String url) {

		log.debug("Checking if url " + url + "existed.");
		boolean isExistent = false;
		try (Connection connection = db.getConnection();) {
			isExistent = urlExisted(connection, url);
		} catch (SQLException ex) {

			log.debug(false, ex);
		}

		return isExistent;
	}

	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 *
	 * @param username
	 *            - username to remove
	 * @param password
	 *            - password of user
	 * @return {@link Status.OK} if removal successful
	 */
	private Status removeUser(Connection connection, String username,
			String password) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(DELETE_SQL);) {
			statement.setString(1, username);

			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 *
	 * @param username
	 *            - username to remove
	 * @param password
	 *            - password of user
	 * @return {@link Status.OK} if removal successful
	 */
	public Status removeUser(String username, String password) {
		Status status = Status.ERROR;

		log.debug("Removing user " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUser(connection, username, password);

			if (status == Status.OK) {
				status = removeUser(connection, username, password);
			}
		} catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	private Status updatePassword(Connection connection, String user,
			String newpass) {

		Status status = Status.ERROR;
		byte[] saltBytes = new byte[16];
		random.nextBytes(saltBytes);

		String usersalt = encodeHex(saltBytes, 32);
		String passhash = getHash(newpass, usersalt);

		try (PreparedStatement statement = connection
				.prepareStatement(UPDATE_PASSWORD_SQL);) {
			statement.setString(1, passhash);
			statement.setString(2, usersalt);
			statement.setString(3, user);
			statement.executeUpdate();

			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	public Status updatePassword(String user, String newpass) {
		Status status = Status.ERROR;
		log.debug("Updating password for " + user + ".");

		// // make sure we have non-null and non-emtpy values for login
		if (isBlank(user) || isBlank(newpass)) {
			status = Status.INVALID_LOGIN;
			log.debug(status);
			return status;
		}
		if (checkUserExit(user) == Status.INVALID_USER) {
			status = Status.INVALID_USER;
			log.debug(status);
			return status;
		}

		// try to connect to database
		try (Connection connection = db.getConnection();) {

			status = updatePassword(connection, user, newpass);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	private Status updateLastLoginTime(Connection connection, String user) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(UPDATE_LOGIN_TIME_SQL);) {

			statement.setString(1, user);
			statement.executeUpdate();

			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	public Status updateLastLoginTime(String user) {
		Status status = Status.ERROR;

		log.debug("Updating last login time for " + user + ".");

		// // make sure we have non-null and non-emtpy values for login
		if (isBlank(user)) {
			status = Status.INVALID_LOGIN;
			log.debug(status);
			return status;
		}

		// try to connect to database
		try (Connection connection = db.getConnection();) {

			status = updateLastLoginTime(connection, user);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	private Status updateFavourite(Connection connection, String url,
			String username) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(UPDATE_FAVOURITE_SQL);) {

			statement.setString(1, url);
			statement.setString(2, username);
			statement.executeUpdate();

			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	public Status updateFavourite(String url, String username) {
		Status status = Status.ERROR;

		log.debug("Updating isFavourite for " + url + ".");

		// // make sure we have non-null and non-emtpy values for login
		if (isBlank(url)) {
			status = Status.INVALID_LOGIN;
			log.debug(status);
			return status;
		}

		// try to connect to database
		try (Connection connection = db.getConnection();) {

			status = updateFavourite(connection, url, username);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	private Status addSearchHistory(Connection connection, String username,
			String query) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(ADD_QUERY_SQL);) {
			statement.setString(1, username);
			statement.setString(2, query);
			statement.executeUpdate();

			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}
		return status;
	}

	public Status addSearchHistory(String username, String query) {
		Status status = Status.ERROR;

		if (isBlank(query) || isBlank(username)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}

		log.debug("add " + query + " for " + username);

		try (Connection connection = db.getConnection();) {

			status = addSearchHistory(connection, username, query);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	private Status addFavourite(Connection connection, String username,
			String url) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(ADD_FAVOURIATE_SQL);) {
			statement.setString(1, username);
			statement.setString(2, url);
			statement.executeUpdate();

			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}
		return status;
	}

	public Status addFavourite(String username, String url) {
		Status status = Status.ERROR;

		if (isBlank(url) || isBlank(username)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}

		log.debug("add to favourite for " + username + " " + url);

		try (Connection connection = db.getConnection();) {

			status = addFavourite(connection, username, url);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	private Status cleanFavourite(Connection connection, String username) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(CLEAN_FAVOURITE_SQL);) {
			statement.setString(1, username);
			statement.executeUpdate();

			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}
		return status;
	}

	public Status cleanFavourite(String username) {
		Status status = Status.ERROR;

		if (isBlank(username)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}

		log.debug("clean to favourite for " + username);

		try (Connection connection = db.getConnection();) {

			status = cleanFavourite(connection, username);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;

	}

	private Status getSearchHistory(Connection connection, String username,
			PrintWriter out) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(GET_HISTORY_SQL);) {

			statement.setString(1, username);

			status = Status.OK;

			ResultSet searchHistory = statement.executeQuery();

			int size = 0;

			while ((searchHistory != null) && searchHistory.next()) {

				out.printf("\t<p>%s </p>",
						searchHistory.getString("full_history"));
				size++;
			}
			if (size == 0) {
				out.printf("%n<p>You have no search history<p>");
			}

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	public Status getSearchHistory(String username, PrintWriter out) {

		Status status = Status.ERROR;

		if (isBlank(username)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try (Connection connection = db.getConnection();) {

			status = getSearchHistory(connection, username, out);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	private boolean getFavrouite(Connection connection, String username,
			String url) {

		try (PreparedStatement statement = connection
				.prepareStatement(GET_FAVOURITE_SQL);) {

			statement.setString(1, username);

			ResultSet searchHistory = statement.executeQuery();

			while ((searchHistory != null) && searchHistory.next()) {

				if (searchHistory.getString("url").equals(url)) {
					return true;
				}
			}

		} catch (SQLException ex) {
			log.debug(ex.getMessage(), ex);
		}

		return false;
	}

	public boolean getFavourite(String username, String url) {

		boolean isFavourite = false;
		if (isBlank(username) || isBlank(url)) {
			return false;
		}
		try (Connection connection = db.getConnection();) {

			isFavourite = getFavrouite(connection, username, url);

		} catch (SQLException ex) {
			log.debug(false, ex);
		}

		return isFavourite;
	}

	private Status getFavouriteResult(Connection connection, String username,
			PrintWriter out) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(GET_FAVOURITE_RESULT);) {
			statement.setString(1, username);

			status = Status.OK;

			ResultSet favResults = statement.executeQuery();

			int size = 0;
			while ((favResults != null) && favResults.next()) {
				out.printf("<p>" + favResults.getString("full_favourite")
						+ "<p>%n");
				size++;
			}
			if (size == 0) {
				out.printf("<p>You dont have any favourite result.<p>%n");
			}

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	public Status getFavouriteResult(String username, PrintWriter out) {
		Status status = Status.ERROR;

		if (isBlank(username)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try (Connection connection = db.getConnection();) {

			status = getFavouriteResult(connection, username, out);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	private Status getLastLoginTime(Connection connection, String username,
			PrintWriter out) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(GET_LOGIN_TIME_SQL);) {
			statement.setString(1, username);

			status = Status.OK;

			ResultSet time = statement.executeQuery();

			while ((time != null) && time.next()) {
				out.printf("<p>" + username + ", your last visit was on "
						+ time.getString("lastlogin") + "<p>%n");
			}

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	public Status getLastLoginTime(String username, PrintWriter out) {
		Status status = Status.ERROR;

		if (isBlank(username)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try (Connection connection = db.getConnection();) {

			status = getLastLoginTime(connection, username, out);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	private Status getLoggedInUser(Connection connection, PrintWriter out) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection
				.prepareStatement(GET_LOGGED_IN_USERS_SQL);) {

			status = Status.OK;

			ResultSet users = statement.executeQuery();
			out.printf("<p>The last 5 logged in users:<p>");
			while ((users != null) && users.next()) {
				out.printf("<p>%s<p>", users.getString("username"));
			}

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	public Status getLoggedInUser(PrintWriter out) {
		Status status = Status.ERROR;

		try (Connection connection = db.getConnection();) {

			status = getLoggedInUser(connection, out);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	private Status cleanSearchHistory(Connection connection, String username) {
		Status status = Status.ERROR;
		try (PreparedStatement statement = connection
				.prepareStatement(CLEAN_HISTORY_SQL);) {

			statement.setString(1, username);

			statement.executeUpdate();

			status = Status.OK;

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	public Status cleanSearchHistory(String username) {
		Status status = Status.ERROR;

		if (isBlank(username)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try (Connection connection = db.getConnection();) {

			status = cleanSearchHistory(connection, username);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	private Status addURL(Connection connection, String url, String title,
			String snippet) {
		Status status = Status.ERROR;
		try (PreparedStatement statement = connection
				.prepareStatement(ADD_URL_SQL);) {
			statement.setString(1, url);
			statement.setString(2, title);
			statement.setString(3, snippet);
			statement.setString(4, "NotVisited");

			statement.executeUpdate();

			status = Status.OK;

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	public Status addURL(String url, String title, String snippet) {
		Status status = Status.ERROR;

		if (isBlank(url)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try (Connection connection = db.getConnection();) {

			status = addURL(connection, url, title, snippet);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	private Status updateURLVisitedTime(Connection connection, String url) {
		Status status = Status.ERROR;
		try (PreparedStatement statement = connection
				.prepareStatement(UPDATE_VISITED_TIME_SQL);) {

			statement.setString(1, url);

			statement.executeUpdate();

			status = Status.OK;

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	public Status updateURLVisitedTime(String url) {
		Status status = Status.ERROR;

		if (isBlank(url)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try (Connection connection = db.getConnection();) {

			status = updateURLVisitedTime(connection, url);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	private Status updateSnippet(Connection connection, String url,
			String snippet) {
		Status status = Status.ERROR;
		try (PreparedStatement statement = connection
				.prepareStatement(UPDATE_SNIPPET_SQL);) {
			statement.setString(1, snippet);
			statement.setString(2, url);
			statement.executeUpdate();

			status = Status.OK;

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	public Status updateSnippet(String url, String snippet) {
		Status status = Status.ERROR;
		if (isBlank(url)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try (Connection connection = db.getConnection();) {

			status = updateSnippet(connection, url, snippet);

		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	private Status getURLVisitedTime(Connection connection, String url,
			PrintWriter out) {
		Status status = Status.ERROR;
		try (PreparedStatement statement = connection
				.prepareStatement(GET_URL_VISITED_TIME_SQL);) {
			statement.setString(1, url);
			ResultSet time = statement.executeQuery();
			out.printf("<font size='3' color='gray'>");
			while ((time != null) && time.next()) {

				String lastVisitTime = time.getString("lastvisit");

				if (lastVisitTime.equals("NotVisited")
						|| lastVisitTime.contains("NotVisited")) {
					lastVisitTime = "You have never visited this website.";
				}
				else {
					lastVisitTime = "Your last visit was on " + lastVisitTime;
				}
				out.printf("<p style='line-height:3px';>" + lastVisitTime
						+ "<p>%n");

			}
			out.printf("</font>");

			status = Status.OK;

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}
		return status;
	}

	public Status getURLVisitedTime(String url, PrintWriter out) {
		Status status = Status.ERROR;

		if (isBlank(url)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try (Connection connection = db.getConnection();) {

			status = getURLVisitedTime(connection, url, out);
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	private Status getSnippet(Connection connection, String url,
			PrintWriter out) {
		Status status = Status.ERROR;
		try (PreparedStatement statement = connection
				.prepareStatement(GET_SNIPPET_SQL);) {

			statement.setString(1, url);
			ResultSet snippet = statement.executeQuery();
			out.printf("<font size='3' color='dimgray'>");
			while ((snippet != null) && snippet.next()) {
				out.printf("<p style='line-height:18px';>"
						+ snippet.getString("snippet") + "<p>");
			}
			out.printf("</font>%n");

			status = Status.OK;

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}
		return status;
	}

	public Status getSnippet(String url, PrintWriter out) {
		Status status = Status.ERROR;

		if (isBlank(url)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try (Connection connection = db.getConnection();) {

			status = getSnippet(connection, url, out);
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	private Status getTitle(Connection connection, String url,
			PrintWriter out) {
		Status status = Status.ERROR;
		try (PreparedStatement statement = connection
				.prepareStatement(GET_TITLE_SQL);) {
			statement.setString(1, url);
			ResultSet title = statement.executeQuery();

			while ((title != null) && title.next()) {
				out.printf("<h1>" + title.getString("title") + "</h1>%n");
			}

			status = Status.OK;

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}
		return status;
	}

	public Status getTitle(String url, PrintWriter out) {
		Status status = Status.ERROR;

		if (isBlank(url)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try (Connection connection = db.getConnection();) {

			status = getTitle(connection, url, out);
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}

	private Status getSuggestedQuery(Connection connection, String user,
			PrintWriter out) {
		Status status = Status.ERROR;
		try (PreparedStatement statement = connection
				.prepareStatement(GET_SUGGESTED_QUERY_SQL);) {
			statement.setString(1, user);

			ResultSet queries = statement.executeQuery();
			int size = 0;
			while ((queries != null) && queries.next()) {
				out.printf("<p>" + queries.getString("query") + "</p>%n");
				size++;
			}

			if (size == 0) {
				out.printf("<p>You have no suggestion<p>%n");
			}

			status = Status.OK;

		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}
		return status;
	}

	public Status getSuggestedQuery(String user, PrintWriter out) {
		Status status = Status.ERROR;

		if (isBlank(user)) {
			status = Status.MISSING_VALUES;
			log.debug(status);
			return status;
		}
		try (Connection connection = db.getConnection();) {

			status = getSuggestedQuery(connection, user, out);
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		return status;
	}
}
