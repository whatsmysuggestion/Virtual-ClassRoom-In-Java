
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageFilter;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.*;
import java.util.*;
import java.net.*;


public class babylonCanvas
    extends Canvas
    implements MouseListener, MouseMotionListener
{
    public static final int FREEHAND = 0;
    public static final int LINE = 1;
    public static final int OVAL = 2;
    public static final int RECTANGLE = 3;
    public static final int TEXT = 4;

    public static final int MODE_PAINT = 0;
    public static final int MODE_XOR = 1;

    public Color drawColor;
    public int drawThickness = 1;

    public int drawType = 0;
    public boolean fill = false;

    private boolean dragging = false;
    public int oldx = 0;
    public int oldy = 0;

    private int ovalWidth;
    private int ovalHeight;
    private int rectangleWidth;
    private int rectangleHeight;

    // For the draft stuff
    private babylonLine draftLine;
    private babylonRectangle draftRect;
    private babylonOval draftOval;
    protected babylonText draftText;
    protected babylonPicture draftPicture;
    protected boolean floatingText = false;
    protected boolean floatingPicture = false;

    private babylonPanel mainPanel;

    boolean showingPicture;
    protected Image offScreen;

    protected static Color[] colourArray = 
    {
	// This is an easy way to index colours with their positions in
	// the colorChoice list.  Basically, it enumerates them, which helps
	// us to send them across the network indexed by number.

	Color.black, Color.blue, Color.cyan, Color.darkGray,
	Color.gray, Color.green, Color.lightGray, Color.magenta,
	Color.orange, Color.pink, Color.red, Color.white, Color.yellow
    };

    protected static String[] fontArray = 
    {
	// Like the colourArray above, this helps to enumerate font names
	// in a way we can index
	"Helvetica", "TimesRoman", "Courier", "Dialog", "DialogInput"
    };

    public babylonCanvas(babylonPanel parent)
    {
	super();
	mainPanel = parent;
	setBackground(Color.white);
	setForeground(Color.black);
	drawColor = Color.black;
	setSize(400, 125);
	setVisible(true);
	addMouseListener(this);
	addMouseMotionListener(this);

	offScreen = null;
	try {
	    URL imageUrl =
		new URL(parent.babylonURL.getProtocol(),
			parent.babylonURL.getHost(),
			parent.babylonURL.getFile() + "Nature Bkgrd.jpg");
	    setImage(getToolkit().getImage(imageUrl));
	}
	catch (Exception e) { 
	    e.printStackTrace();
	}
    }

    public void setImage(Image theImage)
    {
	// Fit it to the canvas
	offScreen = theImage;
	repaint();
	showingPicture = true;
    }

    public Image getContents()
    {
	// Returns the contents of the canvas as an Image.  Only returns
	// the portion which is actually showing on the screen

	// If the thing showing on the canvas is a picture, just send back
	// the picture
	if (showingPicture)
	    return (offScreen);

	ImageFilter filter =
	    new CropImageFilter(0, 0, getWidth(), getHeight());
	Image newImage =
	    createImage(new FilteredImageSource(offScreen.getSource(),
						filter));
	return(newImage);
    }

    public void floatPicture(File pictureFile)
    {
	// The user wants to paste a picture onto the canvas.  The user needs
	// to specify the location of the picture on the canvas, so we draw
	// an empty draft rectangle which follows the mouse cursor until
	// a click is entered to place it.

	Image theImage = null;
	
	mainPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	try {
	    theImage = getToolkit().getImage(pictureFile.getAbsolutePath());
	    getToolkit().prepareImage(theImage, -1, -1, this);

	    // We have to wait until the image is ready
	    while ((getToolkit().checkImage(theImage, -1, -1, this) &
		    this.ALLBITS) == 0)
		{}
	}
	catch (Exception e) {
	    mainPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    new babylonInfoDialog(mainPanel.parentWindow,
				  mainPanel.strings.get(babylonCanvas.class,
							"failed"),
				  true, e.toString(),
				  mainPanel.strings.get("ok"));
	    return;
	}

	// If the thing showing on the canvas is a picture, clear it
	if (showingPicture)
	    clear();

	// I think this is a java bug... for some reason, the first calls
	// to getWidth() and getHeight() return -1
	theImage.getWidth(this);
	theImage.getHeight(this);

	draftPicture =
	    new babylonPicture(10, 10, theImage.getWidth(this),
			       theImage.getHeight(this), pictureFile,
			       theImage);

	// Draw the initial draft rectangle
	drawRect(Color.black, draftPicture.x, draftPicture.y,
		 draftPicture.width, draftPicture.height, false,
		 2, this.MODE_XOR);

	mainPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
	floatingPicture = true;
    }

    synchronized public void paint(Graphics g)
    {
	int width = 0;
	int height = 0;

	if (offScreen != null)
	    {
		if (!showingPicture)
		    {
			width = offScreen.getWidth(this);
			height = offScreen.getHeight(this);
		    }
		else
		    {
			width = getSize().width;
			height = getSize().height;
		    }

		g.drawImage(offScreen, 0, 0, width, height, this);
		g.dispose();
	    }
    }

    synchronized public void clear()
    {
	// The maximum size of the usable drawing canvas, for now, will be
	// 1024x768
	offScreen = createImage(1024, 768);
	setBackground(Color.white);
	showingPicture = false;
	repaint();
    }

    synchronized public void drawLine(Color color, int startx, int starty,
				      int endx, int endy, int thickness,
				      int mode)
    {
	int dx, dy;
	Graphics g1 = getGraphics();
	Graphics g2;
	if (offScreen != null)
	    g2 = offScreen.getGraphics();
	else
	    g2 = g1;

	if (mode == this.MODE_XOR)
	    {
		g1.setXORMode(Color.white);
		g2.setXORMode(Color.white);
	    }
	else
	    {
		g1.setColor(color);
		g2.setColor(color);
	    }

	if (endx > startx)
	    dx = (endx - startx);
	else
	    dx = (startx - endx);
	if (endy > starty)
	    dy = (endy - starty);
	else
	    dy = (starty - endy);

	if (dx >= dy)
	    {
		starty -= (thickness / 2);
		endy -= (thickness / 2);
	    }
	else
	    {
		startx -= (thickness / 2);
		endx -= (thickness / 2);
	    }

	for (int count = 0; count < thickness; count ++)
	    {
		g1.drawLine(startx, starty, endx, endy);
		g2.drawLine(startx, starty, endx, endy);
		if (dx >= dy)
		    { starty++; endy++; }
		else
		    { startx++; endx++; }
	    }

	g1.dispose();
	g2.dispose();
    }

    synchronized public void drawOval(Color color, int x, int y, int width,
		      int height, boolean filled, int thickness, int mode)
    {
	Graphics g1 = getGraphics();
	Graphics g2;
	if (offScreen != null)
	    g2 = offScreen.getGraphics();
	else
	    g2 = g1;

	if (mode == this.MODE_XOR)
	    {
		g1.setXORMode(Color.white);
		g2.setXORMode(Color.white);
	    }
	else
	    {
		g1.setColor(color);
		g2.setColor(color);
	    }

        if (filled)
	    {
		g1.fillOval(x, y, width, height);
		g2.fillOval(x, y, width, height);
	    }
        else
	    for (int count = 0; count < thickness; count ++)
		{
		    g1.drawOval(x, y, width, height);
		    g2.drawOval(x, y, width, height);
		    x++; y++;
		    width -= 2; height -= 2;
		}
 
	g1.dispose();
	g2.dispose();
   }

    synchronized public void drawRect(Color color, int x, int y, int width,
		      int height, boolean filled, int thickness, int mode)
    {
	Graphics g1 = getGraphics();
	Graphics g2;
	if (offScreen != null)
	    g2 = offScreen.getGraphics();
	else
	    g2 = g1;

	if (mode == this.MODE_XOR)
	    {
		g1.setXORMode(Color.white);
		g2.setXORMode(Color.white);
	    }
	else
	    {
		g1.setColor(color);
		g2.setColor(color);
	    }

	if (filled)
	    {
		g1.fillRect(x, y, width, height);
		g2.fillRect(x, y, width, height);
	    }
	else
	    {
		for (int count = 0; count < thickness; count ++)
		    {
			g1.drawRect(x, y, width, height);
			g2.drawRect(x, y, width, height);
			x++; y++;
			width -= 2; height -= 2;
		    }
	    }
 
	g1.dispose();
	g2.dispose();
    }

    synchronized public void drawText(Color color, int x, int y,
	      int fontnumber, int attribs, int size, String text, int mode)
    {
	Graphics g1 = getGraphics();
	Graphics g2;
	if (offScreen != null)
	    g2 = offScreen.getGraphics();
	else
	    g2 = g1;

	if (mode == this.MODE_XOR)
	    {
		g1.setXORMode(Color.white);
		g2.setXORMode(Color.white);
	    }
	else
	    {
		g1.setColor(color);
		g2.setColor(color);
	    }

	g1.setFont(new Font (babylonCanvas.fontArray[fontnumber],
				    attribs, size));
	g2.setFont(new Font (babylonCanvas.fontArray[fontnumber],
				    attribs, size));
	g1.drawString(text, x, y);
	g2.drawString(text, x, y);
 
	g1.dispose();
	g2.dispose();
    }                         

    synchronized public void drawPicture(int x, int y, Image picture)
    {
	Graphics g1 = getGraphics();
	Graphics g2;
	if (offScreen != null)
	    g2 = offScreen.getGraphics();
	else
	    g2 = g1;

	picture.getWidth(this);
	picture.getHeight(this); 

	// Place the picture on the canvas at the given coordinate
	g1.drawImage(picture, x, y, this);
	g2.drawImage(picture, x, y, this);
    }                         

    protected void floatText(String text)
    {
	draftText = new babylonText(mainPanel.canvas.drawColor,
				    mainPanel.canvas.oldx,
				    mainPanel.canvas.oldy,
				    mainPanel.drawFontNumber,
				    mainPanel.drawStyle,
				    mainPanel.drawSize, text);
	mainPanel.canvas.floatingText = true;
    }

    public void mouseClicked(MouseEvent E)
    {
    }

    public void mouseEntered(MouseEvent E)
    {
	if (!floatingPicture)
	    mainPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }   

    public void mouseExited(MouseEvent E)
    {
	if (!floatingPicture)
	    mainPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }   

    public void mousePressed(MouseEvent E)
    {
	// Save the coordinates of the initial click
	oldx = E.getX();
	oldy = E.getY();

	// If the thing showing on the canvas is a picture, clear it
	if (showingPicture)
	    clear();

	// If we are doing lines, rectangles, or ovals, we will show
	// draft lines to suggest the final shape of the object
	if (drawType == this.LINE)
	    {
		draftLine = new babylonLine(drawColor, oldx, oldy, oldx,
					    oldy, drawThickness);
		
		// Set the draw mode to XOR and draw it.
		drawLine(draftLine.color, draftLine.startx,
			 draftLine.starty, draftLine.endx,
			 draftLine.endy, drawThickness, this.MODE_XOR);
	    }

	else if (drawType == this.OVAL)
	    {
		draftOval = new babylonOval(drawColor, oldx, oldy,
					    1, 1, false, drawThickness);
		
		// Set the draw mode to XOR and draw it.
		drawOval(draftOval.color, draftOval.x, draftOval.y,
			 draftOval.width, draftOval.height, false,
			 drawThickness, this.MODE_XOR);
	    }

	else if (drawType == this.RECTANGLE)
	    {
		draftRect = new babylonRectangle(drawColor, oldx, oldy,
						 1, 1, false, drawThickness);
		
		// Set the draw mode to XOR and draw it.
		drawRect(draftRect.color, draftRect.x, draftRect.y,
			 draftRect.width, draftRect.height, false,
			 drawThickness, this.MODE_XOR);
	    }

	// Set the activity message
	String drawingString = mainPanel.strings.get(babylonCanvas.class,
						     "drawing") +
	    " " + mainPanel.name;
	if (!mainPanel.activityField.getText().equals(drawingString))
	    mainPanel.activityField.setText(drawingString);

	if (mainPanel.connected)
	    try {
		mainPanel.client
		    .sendActivity(babylonCommand.ACTIVITY_DRAWING);
	    }
	    catch (IOException e) {
		mainPanel.client.lostConnection();
		return;
	    }
    }
	
    public void mouseReleased(MouseEvent E)
    {
	if (floatingPicture)
	    {
		// The user wants to place the picture (s)he is pasting

		// Erase the old draft image rectangle
		drawRect(Color.black, draftPicture.x, draftPicture.y,
			 draftPicture.width, draftPicture.height, false,
			 2, this.MODE_XOR);

		// Set the new coordinates
		draftPicture.x = E.getX();
		draftPicture.y = E.getY();
		
		// Draw the new floating picture rectangle
		drawPicture(draftPicture.x, draftPicture.y, 
			    draftPicture.picture);

		// Send the image to the socket
		if (mainPanel.connected)
		    try {
			mainPanel.client
			    .sendDrawPicture((short) draftPicture.x,
					     (short) draftPicture.y,
					     draftPicture.file);
		    }
		    catch (IOException e) {
			mainPanel.client.lostConnection();
			return;
		    }
		
		mainPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		floatingPicture = false;
		dragging = false;
	    }
	
	else if (drawType == this.LINE)
	    {
		// Erase the draft line
		drawLine(draftLine.color, draftLine.startx, draftLine.starty,
			 draftLine.endx, draftLine.endy, drawThickness,
			 this.MODE_XOR);

		// Add the real line to the canvas
		drawLine(drawColor, oldx, oldy, E.getX(), E.getY(),
			 drawThickness, this.MODE_PAINT);
		
		// send the draw command to the socket
		if (mainPanel.connected)
		    try {
			mainPanel.client.sendLine((short)
				mainPanel.colorChoice.getSelectedIndex(),
				(short) oldx, (short) oldy, (short) E.getX(),
				(short) E.getY(), (short) drawThickness);
		    }
		    catch (IOException e) {
			mainPanel.client.lostConnection();
			return;
		    }

		dragging = false;
	    }

	else if (drawType == this.OVAL)
	    {
		// Erase the draft oval
		drawOval(draftOval.color, draftOval.x, draftOval.y,
			 draftOval.width, draftOval.height, false,
			 drawThickness, this.MODE_XOR);

		if (oldx <= E.getX())
		    ovalWidth = E.getX() - oldx;
		else
		    ovalWidth = oldx - E.getX();
		if (oldy <= E.getY())
		    ovalHeight = E.getY() - oldy;
		else
		    ovalHeight = oldy - E.getY();

		if ((oldx <= E.getX()) && (E.getY() >= oldy))
		    { oldx = oldx; oldy = oldy; }
		else if ((oldx <= E.getX()) && (E.getY() <= oldy))
		    { oldx = oldx; oldy = E.getY(); }
		else if ((oldx >= E.getX()) && (E.getY() >= oldy))
		    { oldx = E.getX(); oldy = oldy; }
		else if ((oldx >= E.getX()) && (E.getY() <= oldy))
		    { oldx = E.getX(); oldy = E.getY(); }

		// Add the real oval to the canvas
		drawOval(drawColor, oldx, oldy, ovalWidth, ovalHeight,
			 fill, drawThickness, this.MODE_PAINT);

		if (mainPanel.connected)
		    try {
			mainPanel.client.sendPoly((short)
				mainPanel.colorChoice.getSelectedIndex(),
				(short) oldx, (short) oldy, (short) ovalWidth,
				(short) ovalHeight, (short) drawThickness,
				fill, babylonCommand.OVAL);
		    }
		    catch (IOException e) {
			mainPanel.client.lostConnection();
			return;
		    }

		dragging = false;
	    }

	else if (drawType == this.RECTANGLE)
	    {
		// Erase the draft recatngle
		drawRect(draftRect.color, draftRect.x, draftRect.y,
			 draftRect.width, draftRect.height, false,
			 drawThickness, this.MODE_XOR);

		if (oldx <= E.getX())
		    rectangleWidth = E.getX() - oldx;
		else
		    rectangleWidth = oldx - E.getX();

		if (oldy <= E.getY())
		    rectangleHeight = E.getY() - oldy;
		else
		    rectangleHeight = oldy - E.getY();

		if ((oldx <= E.getX()) && (E.getY() >= oldy))
		    { oldx = oldx; oldy = oldy; }
		else if ((oldx <= E.getX()) && (E.getY() <= oldy))
		    { oldx = oldx; oldy = E.getY(); }
		else if ((oldx >= E.getX()) && (E.getY() >= oldy))
		    { oldx = E.getX(); oldy = oldy; }
		else if ((oldx >= E.getX()) && (E.getY() <= oldy))
		    { oldx = E.getX(); oldy = E.getY(); }

		// Add the real rectangle to the vector
		drawRect(drawColor, oldx, oldy, rectangleWidth,
			 rectangleHeight, fill, drawThickness,
			 this.MODE_PAINT);

		// Send it out to the other clients
		if (mainPanel.connected)
		    try {
			mainPanel.client.sendPoly((short)
				mainPanel.colorChoice.getSelectedIndex(),
				(short) oldx, (short) oldy,
				(short) rectangleWidth,
				(short) rectangleHeight,
				(short) drawThickness, fill,
				babylonCommand.RECT);
		    }
		    catch (IOException e) {
			mainPanel.client.lostConnection();
			return;
		    }

		dragging = false;
	    }

	else if (drawType == this.TEXT)
	    {
		if (floatingText)
		    {
			// The user wants to place the text (s)he created.
	
			// Erase the old draft text
			drawText(drawColor, draftText.x, draftText.y,
				 draftText.fontnumber, draftText.attribs,
				 draftText.size, draftText.text,
				 this.MODE_XOR);

			// Set the new coordinates
			draftText.x = E.getX();
			draftText.y = E.getY();
		
			// Draw the permanent text
			drawText(drawColor, draftText.x, draftText.y,
				 draftText.fontnumber, draftText.attribs,
				 draftText.size, draftText.text,
				 this.MODE_PAINT);

			// Output to the other clients
			if (mainPanel.connected)
			    try {
				mainPanel.client
				    .sendDrawText((short) mainPanel
					  .colorChoice.getSelectedIndex(),
					  (short) draftText.x,
					  (short) draftText.y,
					  (short) draftText.fontnumber,
					  (short) draftText.attribs, 
					  (short) draftText.size,
					  draftText.text);
			    }
			    catch (IOException e) {
				mainPanel.client.lostConnection();
				return;
			    }

			floatingText = false;
		    }

		else
		    {
			new babylonFontSelectDialog(mainPanel);

			if (floatingText)
			    // Draw a draft version
			    drawText(drawColor, draftText.x, draftText.y,
				     draftText.fontnumber, draftText.attribs,
				     draftText.size, draftText.text,
				     this.MODE_XOR);
		    }
	    }
    }   

    public void mouseDragged(MouseEvent E)
    {
	// If the thing showing on the canvas is a picture, clear it
	if (showingPicture)
	    clear();

	if (drawType == this.FREEHAND)
	    {
		drawLine(drawColor, oldx, oldy, E.getX(), E.getY(),
			 drawThickness, this.MODE_PAINT);
	
		// send the draw command to the socket
		if (mainPanel.connected)
		    try {
			mainPanel.client.sendLine((short)
				mainPanel.colorChoice.getSelectedIndex(),
				(short) oldx, (short) oldy, (short) E.getX(),
				(short) E.getY(), (short) drawThickness);
		    }
		    catch (IOException e) {
			mainPanel.client.lostConnection();
			return;
		    }

		oldx = E.getX();
		oldy = E.getY();
	    }

	else
	    dragging = true;

	if (drawType == this.LINE)
	    {
		// Erase the old draft line
		drawLine(draftLine.color, draftLine.startx, draftLine.starty,
			 draftLine.endx, draftLine.endy, drawThickness,
			 this.MODE_XOR);

		// Draw the new draft line
		draftLine.endx = E.getX();
		draftLine.endy = E.getY();
		drawLine(draftLine.color, draftLine.startx, draftLine.starty,
			 draftLine.endx, draftLine.endy, drawThickness,
			 this.MODE_XOR);
	    }

	else if (drawType == this.OVAL)
	    {
		// Erase the old draft oval
		drawOval(draftOval.color, draftOval.x, draftOval.y,
			 draftOval.width, draftOval.height, false, 
			 drawThickness, this.MODE_XOR);

		if (oldx <= E.getX())
		    draftOval.width = E.getX() - oldx;
		else
		    draftOval.width = oldx - E.getX();

		if (oldy <= E.getY())
		    draftOval.height = E.getY() - oldy;
		else
		    draftOval.height = oldy - E.getY();

		if ((oldx <= E.getX()) && (E.getY() >= oldy))
		    { draftOval.x = oldx; draftOval.y = oldy; }
		else if ((oldx <= E.getX()) && (E.getY() <= oldy))
		    { draftOval.x = oldx; draftOval.y = E.getY(); }
		else if ((oldx >= E.getX()) && (E.getY() >= oldy))
		    { draftOval.x = E.getX(); draftOval.y = oldy; }
		else if ((oldx >= E.getX()) && (E.getY() <= oldy))
		    { draftOval.x = E.getX(); draftOval.y = E.getY(); }

		// Draw the new draft oval
		drawOval(draftOval.color, draftOval.x, draftOval.y,
			 draftOval.width, draftOval.height, false, 
			 drawThickness, this.MODE_XOR);
	    }

	else if (drawType == this.RECTANGLE)
	    {
		// Erase the old draft rectangle
		drawRect(draftRect.color, draftRect.x, draftRect.y,
			 draftRect.width, draftRect.height, false, 
			 drawThickness, this.MODE_XOR);

		if (oldx <= E.getX())
		    draftRect.width = E.getX() - oldx;
		else
		    draftRect.width = oldx - E.getX();

		if (oldy <= E.getY())
		    draftRect.height = E.getY() - oldy;
		else
		    draftRect.height = oldy - E.getY();

		if ((oldx <= E.getX()) && (E.getY() >= oldy))
		    { draftRect.x = oldx; draftRect.y = oldy; }
		else if ((oldx <= E.getX()) && (E.getY() <= oldy))
		    { draftRect.x = oldx; draftRect.y = E.getY(); }
		else if ((oldx >= E.getX()) && (E.getY() >= oldy))
		    { draftRect.x = E.getX(); draftRect.y = oldy; }
		else if ((oldx >= E.getX()) && (E.getY() <= oldy))
		    { draftRect.x = E.getX(); draftRect.y = E.getY(); }

		// Draw the new draft rectangle
		drawRect(draftRect.color, draftRect.x, draftRect.y,
			 draftRect.width, draftRect.height, false, 
			 drawThickness, this.MODE_XOR);
	    }
    }

    public void mouseMoved(MouseEvent E)
    {
	if (floatingText)
	    {
		// When the user has entered some text to place on the
		// canvas, it remains sticky with the cursor until another
		// click is entered to place it.

		// Erase the old draft text
		drawText(drawColor, draftText.x, draftText.y,
			 draftText.fontnumber, draftText.attribs,
			 draftText.size, draftText.text, this.MODE_XOR);

		// Set the new coordinates
		draftText.x = E.getX();
		draftText.y = E.getY();
		
		// Draw the new floating text
		drawText(drawColor, draftText.x, draftText.y,
			 draftText.fontnumber, draftText.attribs, 
			 draftText.size, draftText.text, this.MODE_XOR);
	    }

	if (floatingPicture)
	    {
		// When the user has opted to paste a picture to the canvas,
		// it remains sticky with the cursor until another click
		// is entered to place it

		// Erase the old draft image rectangle
		drawRect(Color.black, draftPicture.x, draftPicture.y,
			 draftPicture.width, draftPicture.height, false,
			 2, this.MODE_XOR);

		// Set the new coordinates
		draftPicture.x = E.getX();
		draftPicture.y = E.getY();
		
		// Draw the new floating picture rectangle
		drawRect(drawColor, draftPicture.x, draftPicture.y,
			 draftPicture.width, draftPicture.height, false,
			 2, this.MODE_XOR);
	    }
    }   


    class babylonLine
    {
	public Color color;
	public int startx;
	public int starty;
	public int endx;
	public int endy;
	public int thickness;

	public babylonLine(Color mycolor, int mystartx, int mystarty,
			   int myendx, int myendy, int mythickness)
	{
	    color = mycolor;
	    startx = mystartx;
	    starty = mystarty;
	    endx = myendx;
	    endy = myendy;
	    thickness = mythickness;
	}
    }

    class babylonRectangle
    {
	public Color color;
	public int x;
	public int y;
	public int width;
	public int height;
	public boolean fill;
	public int thickness;

	public babylonRectangle(Color mycolor, int myx, int myy, int mywidth,
				int myheight, boolean myfill, int mythickness)
	{
	    color = mycolor;
	    x = myx;
	    y = myy;
	    width = mywidth;
	    height = myheight;
	    fill = myfill;
	    thickness = mythickness;
	}
    }

    class babylonOval
    {
	public Color color;
	public int x;
	public int y;
	public int width;
	public int height;
	public boolean fill;
	public int thickness;

	public babylonOval(Color mycolor, int myx, int myy, int mywidth,
			   int myheight, boolean myfill, int mythickness)
	{
	    color = mycolor;
	    x = myx;
	    y = myy;
	    width = mywidth;
	    height = myheight;
	    fill = myfill;
	    thickness = mythickness;
	}
    }

    class babylonText
    {
	public Color color;
	public int x;
	public int y;
	public int fontnumber;
	public int attribs;
	public int size;
	public String text;

	public babylonText(Color mycolor, int myx, int myy, int myfontnumber,
			   int myattribs, int mysize, String mytext)
	{
	    color = mycolor;
	    x = myx;
	    y = myy;
	    fontnumber = myfontnumber;
	    attribs = myattribs;
	    size = mysize;
	    text = mytext;
	}
    }

    class babylonPicture
    {
	public int x;
	public int y;
	public int width;
	public int height;
	public File file;
	public Image picture;

	public babylonPicture(int myX, int myY, int myWidth, int myHeight,
			      File myFile, Image myPicture)
	{
	    x = myX;
	    y = myY;
	    width = myWidth;
	    height = myHeight;
	    file = myFile;
	    picture = myPicture;
	}
    }
}


class babylonFontSelectDialog
    extends Dialog
    implements ActionListener, ItemListener, KeyListener, WindowListener
{
    private Class thisClass = babylonCanvas.class;
    
    private GridBagLayout myLayout;
    private babylonPanel mainPanel;

    private Panel p1;
    private Label typeLabel;
    private Choice type;
    private Label sizeLabel;
    private Choice size;
    private Label styleLabel;
    private Checkbox bold;
    private Checkbox italics;

    private Panel p2;
    private Label sampleLabel;
    private babylonSampleCanvas sample;
    private Label textLabel;
    private TextField text;
    private Panel p3;
    private Button ok;
    private Button cancel;


    public babylonFontSelectDialog(babylonPanel panel)
    {
	super(panel.parentWindow,
	      panel.strings.get(babylonCanvas.class, "choosefont"),
	      true);
	mainPanel = panel;

	myLayout = new GridBagLayout();
	setLayout(myLayout);

	p1 = new Panel();
	p1.setLayout(myLayout);

	typeLabel = new Label(mainPanel.strings.get(thisClass, "fonttype"));
	p1.add(typeLabel,
	       new babylonConstraints(0, 0, 1, 1, 0.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.NONE,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	type = new Choice();
	type.addItemListener(this);
	type.addItem("Arial / Helvetica");
	type.addItem("Times New Roman / Adobe-Times");
	type.addItem("Courier New / Courier");
	type.addItem("MS Sans Serif / Lucida");
	type.addItem("MS Sans Serif II / Lucida Typewriter");
	type.select(mainPanel.drawFontNumber);
	p1.add(type,
	       new babylonConstraints(1, 0, 2, 1, 1.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.HORIZONTAL,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	sizeLabel = new Label(mainPanel.strings.get(thisClass, "fontsize"));
	p1.add(sizeLabel,
	       new babylonConstraints(0, 1, 1, 1, 0.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.NONE,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	size = new Choice();
	size.addItemListener(this);
	size.addItem("10");
	size.addItem("12");
	size.addItem("14");
	size.addItem("18");
	size.addItem("20");
	size.addItem("24");
	size.addItem("36");
	size.addItem("48");
	size.addItem("60");
	size.addItem("72");
	size.addItem("120");
	size.select("" + mainPanel.drawSize);
	p1.add(size,
	       new babylonConstraints(1, 1, 2, 1, 1.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.HORIZONTAL,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	styleLabel = new Label(mainPanel.strings.get(thisClass, "fontstyle"));
	p1.add(styleLabel,
	       new babylonConstraints(0, 2, 1, 1, 0.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.NONE,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	bold = new Checkbox(mainPanel.strings.get(thisClass, "bold"));
	bold.addItemListener(this);
	bold.setState((mainPanel.drawStyle == Font.BOLD) ||
		      (mainPanel.drawStyle == (Font.BOLD + Font.ITALIC)));
	p1.add(bold,
	       new babylonConstraints(1, 2, 1, 1, 0.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.NONE,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	italics = new Checkbox(mainPanel.strings.get(thisClass, "italics"));
	italics.addItemListener(this);
	italics.setState((mainPanel.drawStyle == Font.ITALIC) ||
			 (mainPanel.drawStyle == (Font.BOLD + Font.ITALIC)));
	p1.add(italics,
	       new babylonConstraints(2, 2, 1, 1, 0.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.NONE,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	add(p1, new babylonConstraints(0, 0, 1, 1, 0.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.BOTH,
				      new Insets(5, 5, 0, 5),
				      0, 0));

	p2 = new Panel();
	p2.setLayout(myLayout);

	sampleLabel =
	    new Label(mainPanel.strings.get(thisClass, "fontsample"));
	p2.add(sampleLabel,
	       new babylonConstraints(0, 0, 1, 1, 0.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.NONE,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	sample = new babylonSampleCanvas(mainPanel);
	p2.add(sample,
	       new babylonConstraints(0, 1, 1, 1, 1.0, 1.0,
				      GridBagConstraints.CENTER,
				      GridBagConstraints.BOTH,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	textLabel = new Label(mainPanel.strings.get(thisClass, "text"));
	p2.add(textLabel,
	       new babylonConstraints(0, 2, 1, 1, 0.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.NONE,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	text = new TextField(40);
	text.addKeyListener(this);
	p2.add(text,
	       new babylonConstraints(0, 3, 1, 1, 0.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.HORIZONTAL,
				      new Insets(0, 0, 0, 0),
				      0, 0));

	add(p2, new babylonConstraints(0, 1, 1, 1, 0.0, 0.0,
				      GridBagConstraints.NORTHWEST,
				      GridBagConstraints.BOTH,
				      new Insets(5, 5, 0, 5),
				      0, 0));

	p3 = new Panel();
	p3.setLayout(myLayout);


	ok = new Button(mainPanel.strings.get("ok"));
	ok.addActionListener(this);
	ok.addKeyListener(this);
	p3.add(ok, new babylonConstraints(0, 0, 1, 1, 0.0, 0.0,
					  GridBagConstraints.EAST,
					  GridBagConstraints.NONE,
					  new Insets(0, 0, 0, 0),
					  0, 0));

	cancel = new Button(mainPanel.strings.get("cancel"));
	cancel.addActionListener(this);
	cancel.addKeyListener(this);
	p3.add(cancel, new babylonConstraints(1, 0, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.NONE,
					  new Insets(0, 0, 0, 0),
					  0, 0));

	add(p3, new babylonConstraints(0, 2, 1, 1, 0.0, 0.0,
					  GridBagConstraints.NORTHWEST,
					  GridBagConstraints.BOTH,
					  new Insets(5, 5, 5, 5),
					  0, 0));

	setSize(600, 600);
	pack();

	Dimension parentSize = mainPanel.getSize();
	Point parentLocation = mainPanel.getLocationOnScreen();
	Dimension mySize = getSize();
	if ((parentSize.width <= mySize.width) ||
	    (parentSize.height <= mySize.height))
	    // If this window is bigger than the parent window, place it at
	    // the same coordinates as the parent.
	    setLocation(parentLocation.x, parentLocation.y);
	else
	    // Otherwise, place it centered within the parent window.
	    setLocation((((parentSize.width - mySize.width) / 2)
			 + parentLocation.x),
			(((parentSize.height - mySize.height) / 2)
			 + parentLocation.y));

	sample.repaint();
	addKeyListener(this);
	addWindowListener(this);
	setResizable(false);
	setVisible(true);
	text.requestFocus();
    }
    
    public void actionPerformed(ActionEvent E)
    {
	if (E.getSource() == ok)
	    {
		mainPanel.canvas.floatText(text.getText());
		dispose();
		return;
	    }
	
	else if (E.getSource() == cancel)
	    {
		dispose();
		return;
	    }
    }

    public void itemStateChanged(ItemEvent E)
    {
	if (E.getSource() == bold)
	    {
		if (bold.getState())
		    mainPanel.drawStyle = Font.BOLD;
		else
		    mainPanel.drawStyle = Font.PLAIN;

		if (italics.getState())
		    mainPanel.drawStyle += Font.ITALIC;

		sample.repaint();
		return;
	    }
	
	else if (E.getSource() == italics)
	    {
		if (italics.getState())
		    mainPanel.drawStyle = Font.ITALIC;
		else
		    mainPanel.drawStyle = Font.PLAIN;

		if (bold.getState())
		    mainPanel.drawStyle += Font.BOLD;

		sample.repaint();
		return;
	    }

	else if (E.getSource() == type)
	    {
		mainPanel.drawFontNumber = type.getSelectedIndex();
		sample.repaint();
		return;
	    }

	else if (E.getSource() == size)
	    {
		mainPanel.drawSize =
		    Integer.parseInt(size.getSelectedItem());
		sample.repaint();
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
		if ((E.getSource() == ok) ||
		    (E.getSource() == text))
		    {
			mainPanel.canvas.floatText(text.getText());
			dispose();
			return;
		    }

		else if (E.getSource() == cancel)
		    {
			dispose();
			return;
		    }
	    }

	else if (E.getKeyCode() == E.VK_TAB)
	    {
		text.transferFocus();
		return;
	    }

	else if (E.getSource() == text)
	    {
		// We need to do the sample update after the key is released
		// or else the keystroke won't show up in the text.getText()
		// call below.
		sample.text = text.getText();
		sample.repaint();
		return;
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


    class babylonSampleCanvas
	extends Canvas
    {
	private static final int X = 300;
	private static final int Y = 75;
    
	public String text;
	public babylonPanel mainPanel;

	public babylonSampleCanvas(babylonPanel mainWindow)
	{
	    super();
	    mainPanel = mainWindow;
	    setBackground(Color.lightGray);
	    setSize(X, Y);
	    repaint();
	    setVisible(true);
	    text = mainPanel.strings.get(babylonCanvas.class,
					 "babylonandon");
	}

	public void paint(Graphics g)
	{
	    g.setColor(Color.black);
	    g.setFont(new Font(babylonCanvas
			       .fontArray[mainPanel.drawFontNumber],
			       mainPanel.drawStyle, mainPanel.drawSize));
	    if (mainPanel.drawSize > Y)
		g.drawString(text, 0, (Y - (Y / 10)));
	    else
		g.drawString(text, 0, mainPanel.drawSize);

	    g.dispose();
	}
    }
}
