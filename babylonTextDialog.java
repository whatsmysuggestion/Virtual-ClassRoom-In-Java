
import java.awt.*;
import java.awt.event.*;


public class babylonTextDialog
    extends Dialog
    implements ActionListener, KeyListener, WindowListener
{
    private Frame parentFrame;
    protected GridBagLayout myLayout;

    protected Panel p;
    protected Button dismissButton;
    protected TextArea textArea;


    public babylonTextDialog(Frame parent, String myLabel,
			     String contents, int columns, int rows,
			     int scrollbars, boolean IsModal,
			     String dismissString)
    {
	super(parent, myLabel, IsModal);

	parentFrame = parent;
	
	myLayout = new GridBagLayout();
	setLayout(myLayout);

	p = new Panel();
	p.setLayout(myLayout);

	textArea = new TextArea(contents, rows, columns, scrollbars);
	textArea.addKeyListener(this);
	textArea.setEditable(false);
	textArea.setFont(babylonPanel.smallFont);
	p.add(textArea, new babylonConstraints(0, 0, 1, 1, 1.0, 1.0,
					      GridBagConstraints.NORTHWEST,
					      GridBagConstraints.BOTH,
					      new Insets(0, 0, 5, 0),
					      0, 0));

	dismissButton = new Button(dismissString);
	dismissButton.setFont(babylonPanel.smallFont);
	dismissButton.addActionListener(this);
	dismissButton.addKeyListener(this);
	p.add(dismissButton, new babylonConstraints(0, 1, 1, 1, 1.0, 1.0,
					      GridBagConstraints.CENTER,
					      GridBagConstraints.NONE,
					      new Insets(0, 0, 0, 0),
					      0, 0));
	
	add(p, new babylonConstraints(0, 0, 1, 1, 1.0, 1.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.BOTH,
				      new Insets(5, 5, 5, 5),
				      0, 0));

	pack();

	if ((parentFrame instanceof babylonWindow) ||
	    (parentFrame instanceof babylonServerWindow))
	    babylonPanel.centerDialog(parentFrame, this);
	else
	    babylonPanel.centerDialogOnScreen(this);

	addKeyListener(this);
	addWindowListener(this);
	setResizable(false);
	setVisible(true);
	dismissButton.requestFocus();
    }

    public void actionPerformed(ActionEvent E)
    {
	if (E.getSource() == dismissButton)
	    {
		dispose();
		return;
	    }
    }

    public void keyPressed(KeyEvent E)
    {
    }

    public void keyReleased(KeyEvent E)
    {
	if (E.getKeyCode() == E.VK_ENTER)
	    {
		if (E.getSource() == dismissButton)
		    {
			dispose();
			return;
		    }
	    }

	if (E.getKeyCode() == E.VK_TAB)
	    {
		if (E.getSource() == textArea)
		    {
			// Tab out of the text area
			textArea.transferFocus();
			return;
		    }
	    }
    }

    public void keyTyped(KeyEvent E)
    {
    }   

    public void windowActivated(WindowEvent E)
    {
    }

    public void windowClosed(WindowEvent E)
    {
    }

    public void windowClosing(WindowEvent E)
    {
	dispose();
    }

    public void windowDeactivated(WindowEvent E)
    {
    }

    public void windowDeiconified(WindowEvent E)
    {
    }

    public void windowIconified(WindowEvent E)
    {
    }

    public void windowOpened(WindowEvent E)
    {
    }
}
