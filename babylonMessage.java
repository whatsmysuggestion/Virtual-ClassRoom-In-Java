
public class babylonMessage
{
    public String messageFor;
    public String messageFrom;
    public String text;

    public babylonMessage(String whoFor, String whoFrom, String info)
    {
	messageFor = whoFor;
	messageFrom = whoFrom;
	text = info;
    }
}

