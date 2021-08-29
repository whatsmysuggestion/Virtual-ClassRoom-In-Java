
import java.awt.*;


public class babylonConstraints
    extends GridBagConstraints
{
    // The GridBagConstraints in Java versions prior to 1.2 doesn't contain
    // this very useful constructor:

    public babylonConstraints(int gridx, int gridy, int gridwidth,
			      int gridheight, double weightx, double weighty,
			      int anchor, int fill, Insets insets,
			      int ipadx, int ipady)
    {
	super();

	this.gridx = gridx;
	this.gridy = gridy;
	this.gridwidth = gridwidth;
	this.gridheight = gridheight;
	this.weightx = weightx;
	this.weighty = weighty;
	this.anchor = anchor;
	this.fill = fill;
	this.insets = insets;
	this.ipadx = ipadx;
	this.ipady = ipady;
    }
}
