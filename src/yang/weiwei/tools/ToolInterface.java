package yang.weiwei.tools;

import java.io.IOException;
import java.io.FileReader;
import java.util.Properties;

import yang.weiwei.util.IOUtil;

public abstract class ToolInterface
{	
	protected boolean help=false;
	protected Properties props;
	
	public abstract void parseCommand();
	
	protected abstract boolean checkCommand();
	
	public abstract void execute() throws IOException;
	
	public abstract void printHelp();
	
	protected static void println(Object obj)
	{
		IOUtil.println(obj);
	}
	
	public ToolInterface(String cfgFileName) throws IOException
	{
		props=new Properties();
		props.load(new FileReader(cfgFileName));
		help=Boolean.valueOf(props.getProperty("help", "false"));
	}
	
	public ToolInterface(Properties props)
	{
		this.props=new Properties(props);
		help=Boolean.valueOf(this.props.getProperty("help", "false"));
	}
}
