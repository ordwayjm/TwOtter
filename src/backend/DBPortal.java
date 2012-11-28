package backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.sql.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Database Portal that acts as a portal to the program database. Methods are provided for generating HTML documents
 * for the newsfeed and profile for any given user.
 * @author Kyle Rogers
 * Requires SQLite-JDBC
 */
public class DBPortal {

	/**
	 * Used for testing DBPortal.java
	 * @param args
	 * @throws SQLException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws SQLException, FileNotFoundException
	{
		DBPortal portal = new DBPortal();
		System.out.println(portal.getHTML("KyleRogers", true));
	}
	
	public static final char SEP = File.separatorChar;

	private Connection conn;
	
	// SQL statement for retrieving all of the posts that a user has posted, including reposts
	private static final String GET_USER_POSTS_STATEMENT = 
			"SELECT DISTINCT(POSTED.postID),POSTED.username,POST.username,POSTED.timestamp,POST.message,USER.picture " + 
					"FROM USER JOIN POSTED ON USER.username=POST.username JOIN POST ON POST.postid=POSTED.postid WHERE " + 
					"POSTED.username= ? ORDER BY timestamp DESC";

	// SQL statement for retrieving all of the posts for the users that a given user follows
	// This statement returns both an original post and each repost. It should only return the oldest post
	private static final String GET_FEED_STATEMENT = 
			"SELECT DISTINCT(POSTED.postID),POSTED.username,POST.username,POSTED.timestamp,POST.message,USER.picture " + 
					"FROM POST JOIN POSTED ON POST.postid=POSTED.postid JOIN FOLLOWING ON FOLLOWING.followee=POSTED.username " + 
					"JOIN USER ON POST.username=USER.username WHERE FOLLOWING.follower=? ORDER BY timestamp DESC";

	// SQL statement for retrieving getting all of the information for a given user
	private static final String GET_USER_INFO_STATEMENT = 
			"SELECT username,email,description,picture,name FROM USER WHERE username=?";
	
	private static final String CREATE_POST_STATEMENT = 
			"INSERT INTO POST VALUES(null,?,?); INSERT INTO POSTED VALUES( ?,(select last_insert_rowid()),null,?);";
	
	
	/**
	 * Creates a portal to src/backend/twotter.db
	 */
	public DBPortal()
	{
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:src" + SEP + "backend" + SEP + "twotter.db");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e)	{
			e.printStackTrace();
		}
	}
	
	public boolean createPost(String message, long sessionID)
	{
		PreparedStatement prepStmt;
		try {
			String username = getUsernameByID(sessionID);
			Date now = new Date();
			prepStmt = conn.prepareStatement(CREATE_POST_STATEMENT);
			prepStmt.setString(1, message);
			prepStmt.setString(2, username);
			prepStmt.setString(3, username);
			prepStmt.setString(4, DateFormat.getDateTimeInstance(
		            DateFormat.SHORT, DateFormat.SHORT).format(now));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private String getUsernameByID(long sessionID) throws SQLException
	{
		PreparedStatement prepStmt = conn.prepareStatement("SELECT username FROM USER WHERE sessionID = ?");
		prepStmt.setLong(1, sessionID);
		ResultSet rs = prepStmt.executeQuery();
		return rs.getString("username");
	}
	
	/**
	 * Dynamically generates an HTML page for username's news feed
	 * @param username The user requesting a news feed
	 * @return HTML page for the news feed
	 * @throws FileNotFoundException one of the template HTML files is missing
	 * @throws SQLException There was an error in twotter.db or with the username
	 */
	public String getNewsFeedHTML(String username) throws FileNotFoundException, SQLException
	{
		return getHTML(username,true);
	}

	/**
	 * Dynamically generates an HTML page for username's profile
	 * @param username The user who's profile is being requested
	 * @return HTML page for the profile
	 * @throws FileNotFoundException one of the template HTML files is missing
	 * @throws SQLException There was an error in twotter.db or with the username
	 */	
	public String getProfileHTML(String username) throws FileNotFoundException, SQLException
	{
		return getHTML(username,false);
	}
	
	/**
	 * Retrieves the profile of the user based on their session ID
	 * @param sessionID
	 * @return String of HTML. If the user does not exist, returns null
	 * @throws FileNotFoundException One of the HTML template files is missing
	 * @throws SQLException The session ID is invalid, twotter.db has an error, or the SQL inputs have an error
	 */
	public String getProfileHTML_SessionID(long sessionID) throws FileNotFoundException, SQLException
	{
		return getHTML(getUsernameByID(sessionID),false);
	}
	
	/**
	 * Dynamically generates an HTML page for either a newsfeed or a profile
	 * @param username The user who's information is to be retrieved
	 * @param newsfeed If true - retrieves a newsfeed for username, else - retrieves the profile for username
	 * @return String of HTML. If the requested user does not exist, returns null
	 * @throws FileNotFoundException One of the HTML template files is missing
	 * @throws SQLException twotter.db has an error or an error in SQL inputs
	 */
	private String getHTML(String username, boolean newsfeed) throws FileNotFoundException, SQLException
	{
		User u = getUser(username);
		if (u == null) return null;
		String userHTML = getUser(username).toHTML();
		String postHTML = "";
		ArrayList<Post> posts = getPosts(username,newsfeed);
		if (posts.size() == 0) postHTML = readFile("src/backend/HTMLTemplates/nothing_here.html");
		for (Post p : posts) postHTML += p.toHTML();
		String tempF = DBPortal.readFile("src" + DBPortal.SEP + "backend" + DBPortal.SEP + "HTMLTemplates" + DBPortal.SEP + "template.html");
		String page = tempF.replaceFirst("%userInformation%", userHTML);
		page = page.replaceFirst("%posts%", postHTML);
		return page;
	}

	
	private ArrayList<Post> getPosts(String username, boolean newsfeed) throws SQLException
	{
		ArrayList<Post> posts = new ArrayList<Post>();
		PreparedStatement prepStmt = conn.prepareStatement(newsfeed ? GET_FEED_STATEMENT : GET_USER_POSTS_STATEMENT);
		prepStmt.setString(1, username);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next()) posts.add(new Post(rs));
		rs.close();
		return posts;
	}

	private User getUser(String username) throws SQLException
	{
		User u = null;
		PreparedStatement prepStmt = conn.prepareStatement(GET_USER_INFO_STATEMENT);
		prepStmt.setString(1, username);
		ResultSet rs = prepStmt.executeQuery();
		rs.next();
		try{
			u = new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5));
		}catch(Exception e)	{
			return null;
		}
		finally{
			rs.close();
		}
		return u;
	}


	/**
	 * Opens a file and outputs a string
	 * @param pathname Location of the file to be opened
	 * @return A string of the text in the file
	 * @throws FileNotFoundException The file does not exist
	 */
	public static String readFile(String pathname) throws FileNotFoundException
	{
		FileInputStream stream = new FileInputStream(new File(pathname));
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
