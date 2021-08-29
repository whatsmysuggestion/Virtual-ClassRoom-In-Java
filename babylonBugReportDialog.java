
import java.io.*;
import java.awt.*;
import java.awt.event.*;


public class babylonBugReportDialog
    extends babylonTextDialog
{
    // An extension of the text dialog which asks the users to send
    // a bug report

    public static String APPTYPE_CLIENT = "CLIENT";
    public static String APPTYPE_SERVER = "SERVER";

    private Frame parentWindow;
    protected babylonStringManager strings;
    private Class thisClass = babylonBugReportDialog.class;
    private String appType;
    private String stackTrace;

    private Panel p2;
    private Label commentsLabel;
    private TextArea commentsArea;
    private Button sendButton;

    public babylonBugReportDialog(babylonPanel parent, Exception e)
    {
	// Called by the client

	super(parent.parentWindow,
	      parent.strings.get(babylonBugReportDialog.class, "bug"),
	      "", 40, 10, TextArea.SCROLLBARS_VERTICAL_ONLY, false,
	      parent.strings.get("dismiss"));

	appType = APPTYPE_CLIENT;
	parentWindow = parent.parentWindow;
	strings = parent.strings;

	init(e);
    }

    public babylonBugReportDialog(babylonServerWindow parent, Exception e)
    {
	// Called by the server

	super(parent,
	      parent.server.strings.get(babylonBugReportDialog.class,
					"bug"),
	      "", 40, 10, TextArea.SCROLLBARS_VERTICAL_ONLY, false,
	      parent.server.strings.get("dismiss"));

	appType = APPTYPE_SERVER;
	parentWindow = parent;
	strings = parent.server.strings;
	init(e);
    }

    private void init(Exception e)
    {
	// Turn the exception stack trace into a string
	StringWriter sw = new StringWriter();
	e.printStackTrace(new PrintWriter(sw));
	stackTrace = sw.toString();

	// Remove the dismiss button temporarily
	p.remove(dismissButton);

	// Make a second panel
	p2 = new Panel();
	p2.setLayout(myLayout);

	commentsLabel = new Label(strings.get(thisClass, "comment"));
	commentsLabel.setFont(babylonPanel.smallFont);
	p2.add(commentsLabel,
	       new babylonConstraints(0, 0, 2, 1, 1.0, 1.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.NONE,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	commentsArea =
	    new TextArea("", 2, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
	commentsArea.addKeyListener(this);
	commentsArea.setEditable(true);
	commentsArea.setFont(babylonPanel.smallFont);
	p2.add(commentsArea,
	       new babylonConstraints(0, 1, 2, 1, 1.0, 1.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.NONE,
				      new Insets(0, 0, 0, 0),
				      0, 0));
	
	sendButton = new Button(strings.get(thisClass, "send"));
	sendButton.setFont(babylonPanel.smallFont);
	sendButton.addActionListener(this);
	sendButton.addKeyListener(this);
	p2.add(sendButton, new babylonConstraints(0, 2, 1, 1, 1.0, 1.0,
						  GridBagConstraints.EAST,
						  GridBagConstraints.NONE,
						  new Insets(0, 0, 0, 0),
						  0, 0));

	// Put the dismiss button into our new panel
	p2.add(dismissButton, new babylonConstraints(1, 2, 1, 1, 1.0, 1.0,
					       GridBagConstraints.WEST,
					       GridBagConstraints.NONE,
					       new Insets(0, 0, 0, 0),
					       0, 0));

	add(p2, new babylonConstraints(0, 1, 1, 1, 1.0, 1.0,
				       GridBagConstraints.NORTHWEST,
				       GridBagConstraints.BOTH,
				       new Insets(5, 5, 5, 5),
				       0, 0));

	// Put the text of the exception into the text area
	textArea.setText(strings.get(thisClass, "bugreport") +
			 " " + babylonBugReporter.sendTo + ":\n\n" +
			 stackTrace);
	pack();
    }

    public void send()
    {
	babylonInfoDialog sending =
	    new babylonInfoDialog(parentWindow,
				  strings.get(thisClass, "sending"), false,
				  strings.get(thisClass, "onemoment"),
				  strings.get("ok"));

	try {
	    new babylonBugReporter("Babylon Chat " + appType + " version " +
				   babylon.VERSION + "\n\n" + stackTrace +
				   "\nAdditional:\n" +
				   commentsArea.getText());
	}
	catch (Exception g) {
	    // Argh.  Couldn't send.  Make a message
	    sending.dispose();
	    new babylonTextDialog(parentWindow,
				  strings.get(thisClass, "nosend"),
				  strings.get(thisClass, "errorsending") +
				  " " + g.toString() + "\n\n" +
				  strings.get(thisClass, "pleaseemail") +
				  " " + babylonBugReporter.sendTo, 40, 10,
				  TextArea.SCROLLBARS_VERTICAL_ONLY, false,
				  strings.get("ok"));
	    return;
	}
	sending.dispose();
	dispose();
    }

    public void actionPerformed(ActionEvent E)
    {
	if (E.getSource() == sendButton)
	    {
		send();
		return;
	    }
	else
	    // Pass to superclass
	    super.actionPerformed(E);
    }

    public void keyReleased(KeyEvent E)
    {
	if ((E.getKeyCode() == E.VK_ENTER) &&
	    ((E.getSource() == sendButton) ||
	     (E.getSource() == commentsArea)))
	    {
		send();
		return;
	    }
	else if ((E.getKeyCode() == E.VK_TAB) &&
		 (E.getSource() == commentsArea))
	    {
		// Tab out of the reply area
		commentsArea.transferFocus();
		return;
	    }
	else
	    // Pass to superclass
	    super.keyReleased(E);
    }
}
