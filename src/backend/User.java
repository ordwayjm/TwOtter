package backend;

import backend.DBPortal;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {

	private static final String 
		PICTURE_RE 		= "%picture%",
		USERNAME_RE 	= "%username%",
		NAME_RE 		= "%name%",
		DESC_RE 		= "%description%";
	
	protected String name, username, email, description, picture;
	
	protected User(ResultSet rs) throws SQLException
	{
		this.username 		= rs.getString(1);
		this.email 			= rs.getString(2);
		this.description	= rs.getString(3);
		this.picture 		= rs.getString(4);
		this.name 			= rs.getString(5);
	}
	
	public String toString()
	{
		return this.name + "\n\t" + this.username + ", " + this.email + ", " + this.description + ", " + this.picture;
	}
	
	public String toHTML() throws FileNotFoundException
	{

		String userTempF = DBPortal.readFile("src" + DBPortal.SEP + "backend" + DBPortal.SEP + "HTMLTemplates" + DBPortal.SEP + "user_template.html");

		String html = userTempF.replaceAll(USERNAME_RE, username);
		html = html.replaceFirst(DESC_RE, description);
		html = html.replaceFirst(NAME_RE, name);
		html = html.replaceFirst(PICTURE_RE, picture);
		return html;		
	}
}
