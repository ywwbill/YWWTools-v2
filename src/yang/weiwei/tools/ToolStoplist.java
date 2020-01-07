package yang.weiwei.tools;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.preprocess.StopList;

public class ToolStoplist extends ToolInterface
{
	private String dictFileName;
	private String corpusFileName;
	private String outputFileName;
	
	public void parseCommand()
	{
		dictFileName=props.getProperty("model");
		corpusFileName=props.getProperty("corpus");
		outputFileName=props.getProperty("output");
	}

	protected boolean checkCommand()
	{
		if (help) return false;
		
		if (corpusFileName==null || corpusFileName.length()==0)
		{
			println("Corpus file is not specified.");
			return false;
		}
		
		if (outputFileName==null || outputFileName.length()==0)
		{
			println("Output file is not specified.");
			return false;
		}
		
		return true;
	}

	public void execute() throws IOException
	{
		if (!checkCommand())
		{
			printHelp();
			return;
		}
		
		StopList stoplist;
		if (dictFileName!=null && dictFileName.length()>0)
		{
			stoplist=new StopList(dictFileName);
		}
		else
		{
			stoplist=new StopList();
		}
		stoplist.removeStopWords(corpusFileName, outputFileName);
	}

	public void printHelp()
	{
		println("Arguments for Stoplist:");
		println("\thelp [optional]: Print help information.");
		println("\tdict [optional]: Use user's stop word dictionary to remove stop words.");
		println("\tcorpus: Corpus file with stop words.");
		println("\toutput: Corpus file without stop words.");
	}
	
	public ToolStoplist(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolStoplist(Properties props)
	{
		super(props);
	}
}
