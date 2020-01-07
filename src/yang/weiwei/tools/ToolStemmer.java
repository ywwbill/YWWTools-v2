package yang.weiwei.tools;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.preprocess.Stemmer;

public class ToolStemmer extends ToolInterface
{
	private String corpusFileName;
	private String outputFileName;
	
	public void parseCommand()
	{
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
		
		Stemmer stemmer=new Stemmer();
		stemmer.stemFile(corpusFileName, outputFileName);
	}

	public void printHelp()
	{
		println("Arguments for Stemmer:");
		println("\thelp [optional]: Print help information.");
		println("\tcorpus: Unstemmed corpus file.");
		println("\toutput: Stemmed corpus file.");
	}
	
	public ToolStemmer(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolStemmer(Properties props)
	{
		super(props);
	}
}
