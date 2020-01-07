package yang.weiwei.tools;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.preprocess.Tokenizer;

public class ToolTokenizer extends ToolInterface
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
		
		Tokenizer tokenizer;
		if (modelFileName!=null && modelFileName.length()>0)
		{
			tokenizer=new Tokenizer(modelFileName);
		}
		else
		{
			tokenizer=new Tokenizer();
		}
		tokenizer.tokenizeFile(corpusFileName, outputFileName);
	}

	public void printHelp()
	{
		println("Arguments for Tokenizer:");
		println("\thelp [optional]: Print help information.");
		println("\tmodel [optional]: Use user's model to tokenize documents.");
		println("\tcorpus: Untokenized corpus file.");
		println("\toutput: Tokenized corpus file.");
	}
	
	public ToolTokenizer(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolTokenizer(Properties props)
	{
		super(props);
	}
}
