
import java.awt.*;
import java.awt.event.*;


public class babylonInfoDialog
    extends Dialog
    implements ActionListener, KeyListener, WindowListener
{
    protected Frame parentFrame;
    protected Panel p;
    protected Label message;
    protected Button ok;
    protected GridBagLayout myLayout;
    

    public babylonInfoDialog(Frame parent, String TheTitle,
			     boolean isModal, String theMessage,
			     String okString)
    {
	super(parent, TheTitle, isModal);

	parentFrame = parent;

	myLayout = new GridBagLayout();
	setLayout(myLayout);

	p = new Panel();
	p.setLayout(myLayout);

	message = new Label(theMessage);
	p.add(message, new babylonConstraints(0, 0, 1, 1, 1.0, 1.0,
					      GridBagConstraints.CENTER,
					      GridBagConstraints.NONE,
					      new Insets(0, 0, 0, 0),
					      0, 0));

	ok = new Button(okString);
	ok.addActionListener(this);
	ok.addKeyListener(this);
	p.add(ok, new babylonConstraints(0, 1, 1, 1, 1.0, 1.0,
					 GridBagConstraints.CENTER,
					 GridBagConstraints.NONE,
					 new Insets(0, 0, 0, 0),
					 0, 0));

	add(p, new babylonConstraints(0, 0, 1, 1, 1.0, 1.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.BOTH,
				      new Insets(5, 5, 5, 5),
				      0, 0));

	setBackground(Color.lightGray);
	pack();
	setResizable(false);

	if ((parentFrame instanceof babylonWindow) ||
	    (parentFrame instanceof babylonServerWindow))
	    babylonPanel.centerDialog(parentFrame, this);
	else
	    babylonPanel.centerDialogOnScreen(this);

	addKeyListener(this);
	addWindowListener(this);
	ok.requestFocus();
	show();
	return;
    }

    public void actionPerformed(ActionEvent E)
    {
	if (E.getSource() == ok)
	    {
		dispose();
		return;
	    }
	return;
    }

    public void keyPressed(KeyEvent E)
    {
    }

    public void keyReleased(KeyEvent E)
    {
	if (E.getKeyCode() == E.VK_ENTER)
	    {
		if (E.getSource() == ok)
		    {
			dispose();
			return;
		    }
	    }
	return;
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
	return;
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
