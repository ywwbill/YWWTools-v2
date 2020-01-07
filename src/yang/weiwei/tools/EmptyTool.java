package yang.weiwei.tools;

import java.io.IOException;
import java.util.Properties;

public class EmptyTool extends ToolInterface
{	
	public void parseCommand()
	{
		
	}

	protected boolean checkCommand()
	{
		return true;
	}

	public void execute() throws IOException
	{
		
	}

	public void printHelp()
	{
		println("Arguments for Tools:");
		println("\t--help [optional]: Print help information.");
	}
	
	public EmptyTool(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public EmptyTool(Properties props)
	{
		super(props);
	}
}
