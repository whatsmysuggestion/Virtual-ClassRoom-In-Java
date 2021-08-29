
import java.util.*;


public class babylonUser
{
    // This object keeps all the relevant information about a user for either
    // the server or the client

    protected int id;
    protected String name;
    protected String password;
    protected String additional;
    protected String language;
    protected String chatroomName;      // Used by the client
    protected babylonChatRoom chatroom; // Used by the server

    babylonUser(babylonServer server)
    {
	// This constructor will be used by the server, since it automatically
	// assigns a new user Id

	id = server.getUserId();
	name = "newuser" + id;
	password = "";
	additional = "";
	language = "en"; // English by default
	chatroomName = "";
	chatroom = null;
    }

    babylonUser(int i, String nm, String pw, String add, String lang)
    {
	// This constructor will be used by the client, with information
	// supplied by the server

	id = i;
	name = nm;
	password = pw;
	additional = add;
	language = lang;
	chatroomName = "";
	chatroom = null;
    }
}
