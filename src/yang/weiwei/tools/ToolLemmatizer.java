package yang.weiwei.tools;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.preprocess.Lemmatizer;

public class ToolLemmatizer extends ToolInterface
{
	private String dictFileName;
	private String corpusFileName;
	private String outputFileName;
	
	public void parseCommand()
	{
		dictFileName=props.getProperty("dict");
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
		
		Lemmatizer lemmatizer;
		if (dictFileName!=null && dictFileName.length()>0)
		{
			lemmatizer=new Lemmatizer(dictFileName);
		}
		else
		{
			lemmatizer=new Lemmatizer();
		}
		lemmatizer.lemmatizeFile(corpusFileName, outputFileName);
	}

	public void printHelp()
	{
		println("Arguments for Lemmatizer:");
		println("\thelp [optional]: Print help information.");
		println("\tdict [optional]: Use user's model to lemmatize documents.");
		println("\tcorpus: Unlemmatized, tokenized, and POS-tagged corpus file.");
		println("\toutput: Lemmatized corpus file.");
	}
	
	public ToolLemmatizer(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolLemmatizer(Properties props)
	{
		super(props);
	}
}
