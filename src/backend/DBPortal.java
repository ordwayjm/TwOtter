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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DBPortal {


	public static final char SEP = File.separatorChar;

	private Connection conn;
	private Scanner scn;
	private static final String GET_USER_POSTS_STATEMENT = 
			"SELECT DISTINCT(POSTED.postID),POSTED.username,POST.username,POST.timestamp,POST.message,USER.picture " + 
					"FROM USER JOIN POSTED ON USER.username=POST.username JOIN POST ON POST.postid=POSTED.postid WHERE " + 
					"POSTED.username= ? ORDER BY timestamp DESC";

	private static final String GET_FEED_STATEMENT = 
			"SELECT DISTINCT(POSTED.postID),POSTED.username,POST.username,POST.timestamp,POST.message,USER.picture " + 
					"FROM POST JOIN POSTED ON POST.postid=POSTED.postid JOIN FOLLOWING ON FOLLOWING.followee=POSTED.username " + 
					"JOIN USER ON POST.username=USER.username WHERE FOLLOWING.follower=? ORDER BY timestamp DESC";

	private static final String GET_USER_INFO_STATEMENT = 
			"SELECT username,email,description,picture,name FROM USER WHERE username=?";
	
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

	public static void main(String[] args) throws SQLException, FileNotFoundException
	{
		DBPortal portal = new DBPortal();
		System.out.println(portal.getHTML("JohnSmith", true));
	}
	
	public String getHTML(String username, boolean newsfeed) throws FileNotFoundException, SQLException
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

	public String genUserNewsfeed(String username) throws SQLException
	{
		ArrayList<Post> posts = getPosts(username,true);
		User u = getUser(username);
		try {
			String templateF = readFile("src" + SEP + "backend" + SEP + "template.html");
			String postTempF = readFile("src" + SEP + "backend" + SEP + "post_template.html");
			String userTempF = readFile("src" + SEP + "backend" + SEP + "user_template.html");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

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
