package yang.weiwei.tools;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.preprocess.POSTagger;

public class ToolPOSTagger extends ToolInterface
{
	private String modelFileName;
	private String corpusFileName;
	private String outputFileName;
	
	public void parseCommand()
	{
		modelFileName=props.getProperty("model");
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
		
		POSTagger tagger;
		if (modelFileName!=null && modelFileName.length()>0)
		{
			tagger=new POSTagger(modelFileName);
		}
		else
		{
			tagger=new POSTagger();
		}
		tagger.tagFile(corpusFileName, outputFileName);
	}

	public void printHelp()
	{
		println("Arguments for POS-Tagger:");
		println("\thelp [optional]: Print help information.");
		println("\tmodel [optional]: Use user's model to tag documents.");
		println("\tcorpus: Untagged tokenized corpus file.");
		println("\toutput: Tagged corpus file.");
	}
	
	public ToolPOSTagger(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolPOSTagger(Properties props)
	{
		super(props);
	}
}
