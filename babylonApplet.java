
import java.applet.*;
import java.awt.*;
import java.net.*;
import java.io.*;

/*
 *<applet code="babylonApplet.class" width="600" height="800"> </applet>
 */
public class babylonApplet
    extends Applet
{
    // This is just a simple applet wrapper class to allow Babylon Chat
    // clients to be embedded in HTML documents.

    public static String usernameParam     = "username";
    public static String passwordParam     = "password";
    public static String servernameParam   = "servername";
    public static String portnumberParam   = "portnumber";
    public static String chatroomParam     = "chatroom";
    public static String widthParam        = "xsize";
    public static String heightParam       = "ysize";
    public static String usepasswordsParam = "usepasswords";
    public static String locksettingsParam = "locksettings";
    public static String autoconnectParam  = "autoconnect";
    public static String hidecanvasParam   = "hidecanvas";
    public static String embedParam        = "embed";

    private Label titleLabel;
    private babylonWindow window;
    private babylonPanel panel;
    private String name = "";
    private String password = "";
    private String host = "";
    private String port = "";
    private String room = "";
    private int windowWidth = 0;
    private int windowHeight = 0;
    private boolean requirePasswords = true;
    private boolean lockSettings = false;
    private boolean autoConnect = false;
    private boolean showCanvas = true;
    protected boolean embed = false;


    public String getAppletInfo()
    {
	return ("Babylon Chat version " + babylon.VERSION
		+ " by Andy McLaughlin");
    }
    

    public String[][] getParameterInfo()
    {
	String[][] args = {
	    { usernameParam, "string", "User login handle" },
	    { passwordParam, "string", "User password" },
	    { servernameParam, "string",
	      "Internet hostname/address of Babylon Chat server" },
	    { portnumberParam, "integer 1-65535",
	      "TCP port number to use (default: 12468)" },
	    { chatroomParam, "string",
	      "Name of initial chat room" },
	    { widthParam, "integer > 0",
	      "Initial width of the client window" },
	    { heightParam, "integer > 0",
	      "Initial height of the client window" },
	    { usepasswordsParam, "no",
	      "Do not require the user to enter a password to connect" },
	    { locksettingsParam, "yes",
	      "Don't allow the user to change user name, server, or port" },
	    { autoconnectParam, "yes",
	      "Tells client to connect automatically on startup" },
	    { hidecanvasParam, "yes",
	      "Tells client to hide the drawing canvas on startup" },
	    { embedParam, "yes",
	      "Tells the client to embed itself in a WWW page instead " +
	      "of popping up in a seperate window" }
	};
	
	return (args);
    }

    
    public void init()
    {
	// Get the user name, password, host name, and port from the launching
	// document, if specified.

	if (getParameter(usernameParam) != null)
	    name = getParameter(usernameParam);

	if (getParameter(passwordParam) != null)
	    password = getParameter(passwordParam);

	if (getParameter(servernameParam) != null)
	    host = getParameter(servernameParam);

	if (getParameter(portnumberParam) != null)
	    port = getParameter(portnumberParam);

	if (getParameter(chatroomParam) != null)
	    room = getParameter(chatroomParam);

	if (getParameter(widthParam) != null)
	    windowWidth = Integer.parseInt(getParameter(widthParam));

	if (getParameter(heightParam) != null)
	    windowHeight = Integer.parseInt(getParameter(heightParam));

	// Get additional behavior parameters

	if (getParameter(usepasswordsParam) != null)
	    {
		if (getParameter(usepasswordsParam).equals("no"))
		    requirePasswords = false;
	    }

	if (getParameter(locksettingsParam) != null)
	    {
		if (getParameter(locksettingsParam).equals("yes"))
		    lockSettings = true;
	    }

	if (getParameter(autoconnectParam) != null)
	    {
		if (getParameter(autoconnectParam).equals("yes"))
		    autoConnect = true;
	    }

	if (getParameter(hidecanvasParam) != null)
	    {
		if (getParameter(hidecanvasParam).equals("yes"))
		    showCanvas = false;
	    }

	if (getParameter(embedParam) != null)
	    {
		if (getParameter(embedParam).equals("yes"))
		    embed = true;
	    }

	// If usernameParam is blank, that's OK -- it's probably a deliberate
	// attempt to get the user to enter a unique one later.  However,
	// if the host and port values are blank, we'll supply some default
	// ones here
	if ((host == null) || host.equals(""))
	    host = "visopsys.org";
	if ((port == null) || port.equals(""))
	    port = "12468";

	// Done
	return;
    }


    public void start()
    {
	// Launch a window.  It will show up as an unsigned applet window.
	panel = new babylonPanel(name, password, host, port, showCanvas,
				 getCodeBase());

	// Set the window width and height, if applicable
	Dimension tmpSize = panel.getSize();
	if (windowWidth > 0)
	    tmpSize.width = windowWidth;
	if (windowHeight > 0)
	    tmpSize.height = windowHeight;
	panel.setSize(tmpSize);

	// Give the window a reference to this applet
	panel.setApplet(this);

	// Should the window prompt users for passwords automatically?
	panel.requirePassword = requirePasswords;

	// Should the user name, server name, and port name be locked
	// against user changes?
	panel.lockSettings = lockSettings;

	// Embed or not?
	if (embed)
	    {
		// Show the panel
		panel.setParent(this);
		setLayout(new GridBagLayout());

		titleLabel = new Label();
		titleLabel.setFont(babylonPanel.largeBoldFont);
		titleLabel.setForeground(Color.white);
		titleLabel.setBackground(Color.blue);
		add(titleLabel, new babylonConstraints(0, 0, 1, 1, 1.0, 0.0,
						  GridBagConstraints.CENTER,
						  GridBagConstraints.BOTH,
						  new Insets(0, 0, 0, 0),
						  0, 0));

		add(panel, new babylonConstraints(0, 1, 1, 1, 1.0, 1.0,
						  GridBagConstraints.CENTER,
						  GridBagConstraints.BOTH,
						  new Insets(0, 0, 0, 0),
						  0, 0));
	    }
	else
	    {
		// Add the panel to a window and pop it up
		window = new babylonWindow(panel);
		panel.setParent(window);
		window.show();
	    }

	// Are we supposed to attempt an automatic connection?  If so,
	// make a little thread to do this separately from the applet
	// initiaization, since in many cases the applet will not be
	// showing on the screen until after initialization.  This can
	// cause problems with the connecting bit
	(new Thread() {
	    public void run()
		{
		    // Wait until the panel is showing on the screen
		    while (!panel.isVisible());

		    // Go
		    panel.offline();

		    if (autoConnect) // !embed && 
			panel.connect();

		    // Is the user supposed to be placed in an initial
		    // chat room?
		    if (!room.equals(""))
			if (panel.client != null)
			    {
				try {
				    panel.client.sendEnterRoom(room, false,
							       "");
				}
				catch (IOException e) {
				    panel.client.lostConnection();
				    return;
				}
			    }
		}
	    }
	 ).start();

	// Done
	return;
    }


    public void stop()
    {
	return;
    }

    public void destroy()
    {
	// Disconnect from the server if connected
	if (panel.connected == true)
	    panel.disconnect();
	if (!embed)
	    window.dispose();
	return;
    }

    public void setTitle(String newTitle)
    {
	// Sets the text of our simulated title bar at the top
	
	if (embed)
	    titleLabel.setText(newTitle);
	else if (window != null)
	    window.setTitle(newTitle);
    }
}
