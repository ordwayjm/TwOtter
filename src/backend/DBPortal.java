package backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;

/**
 * Database Portal that acts as a portal to the program database. Methods are provided for generating HTML documents
 * for the newsfeed and profile for any given user.
 * @author Kyle Rogers
 * Requires SQLite-JDBC
 */
public class DBPortal {

	public static final char SEP = File.separatorChar;

	private Connection conn;
	
	// SQL statement for retrieving all of the posts that a user has posted, including reposts
	private static final String GET_USER_POSTS_STATEMENT = 
			"SELECT DISTINCT(POSTED.postID),POSTED.username,POST.username,POST.timestamp,POST.message,USER.picture " + 
					"FROM USER JOIN POSTED ON USER.username=POST.username JOIN POST ON POST.postid=POSTED.postid WHERE " + 
					"POSTED.username= ? ORDER BY timestamp DESC";

	// SQL statement for retrieving all of the posts for the users that a given user follows
	// This statement returns both an original post and each repost. It should only return the oldest post
	private static final String GET_FEED_STATEMENT = 
			"SELECT DISTINCT(POSTED.postID),POSTED.username,POST.username,POST.timestamp,POST.message,USER.picture " + 
					"FROM POST JOIN POSTED ON POST.postid=POSTED.postid JOIN FOLLOWING ON FOLLOWING.followee=POSTED.username " + 
					"JOIN USER ON POST.username=USER.username WHERE FOLLOWING.follower=? ORDER BY timestamp DESC";

	// SQL statement for retrieving getting all of the information for a given user
	private static final String GET_USER_INFO_STATEMENT = 
			"SELECT username,email,description,picture,name FROM USER WHERE username=?";
	
	/**
	 * Creates a portal to src/backend/twotter.db
	 */
	public DBPortal()
	{
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:src" + SEP + "backend" + SEP + "twotter.db");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e)	{
			e.printStackTrace();
		}
	}

	/**
	 * Used for testing DBPortal.java
	 * @param args
	 * @throws SQLException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws SQLException, FileNotFoundException
	{
		DBPortal portal = new DBPortal();
		System.out.println(portal.getHTML("JohnSmith", true));
	}
	
	/**
	 * Dynamically generates an HTML page for username's newsfeed
	 * @param username The user requesting a newsfeed
	 * @return HTML page for the newsfeed
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
	 * Dynamically generates an HTML page for either a newsfeed or a profile
	 * @param username The user who's information is to be retrieved
	 * @param newsfeed If true - retrieves a newsfeed for username, else - retrieves the profile for username
	 * @return String of HTML 
	 * @throws FileNotFoundException One of the HTML template files is missing
	 * @throws SQLException twotter.db has an error or an error in SQL inputs
	 */
	private String getHTML(String username, boolean newsfeed) throws FileNotFoundException, SQLException
	{
		String postHTML = "";
		ArrayList<Post> posts = getPosts(username,newsfeed);
		for (Post p : posts) postHTML += p.toHTML();
		String userHTML = getUser(username).toHTML();
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
		return posts;
	}

	private User getUser(String username) throws SQLException
	{
		PreparedStatement prepStmt = conn.prepareStatement(GET_USER_INFO_STATEMENT);
		prepStmt.setString(1, username);
		ResultSet rs = prepStmt.executeQuery();
		return new User(rs);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

}
