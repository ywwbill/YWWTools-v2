package yang.weiwei.tools;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.util.format.CorpusConverter;

public class ToolCorpusConverter extends ToolInterface
{
	private boolean getVocab=false;
	private boolean toIndex=false;
	private boolean toWord=false;
	
	private String wordCorpusFileName;
	private String indexCorpusFileName;
	private String vocabFileName;
	
	public void parseCommand()
	{
		getVocab=Boolean.valueOf(props.getProperty("get_vocab", "false"));
		toIndex=Boolean.valueOf(props.getProperty("to_index", "false"));
		toWord=Boolean.valueOf(props.getProperty("to_word", "false"));
		
		wordCorpusFileName=props.getProperty("word_corpus");
		indexCorpusFileName=props.getProperty("index_corpus");
		vocabFileName=props.getProperty("vocab");
	}
	
	protected boolean checkCommand()
	{
		if (help) return false;
		
		int numTrue=0;
		if (getVocab) numTrue++;
		if (toIndex) numTrue++;
		if (toWord) numTrue++;
		if (numTrue!=1)
		{
			println("No option or multiple options are selected.");
			return false;
		}
		
		if (wordCorpusFileName==null || wordCorpusFileName.length()==0)
		{
			println("Word corpus file is not specified.");
			return false;
		}
		
		if (vocabFileName==null || vocabFileName.length()==0)
		{
			println("Vocabulary file is not specified.");
			return false;
		}
		
		if ((toIndex || toWord) && (indexCorpusFileName==null || indexCorpusFileName.length()==0))
		{
			println("Index corpus file is not specified.");
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
		
		if (getVocab)
		{
			CorpusConverter.collectVocab(wordCorpusFileName, vocabFileName);
		}
		
		if (toIndex)
		{
			CorpusConverter.word2Index(wordCorpusFileName, indexCorpusFileName, vocabFileName);
		}
		
		if (toWord)
		{
			CorpusConverter.index2Word(vocabFileName, indexCorpusFileName, wordCorpusFileName);
		}
	}
	
	public void printHelp()
	{
		println("Arguments for corpus converter:");
		println("\thelp [optional]: Print help information.");
		println("\tConvert options (only one can be selected):");
		println("\t\tget_vocab: Collect vocabulary from a given word corpus file.");
		println("\t\tto_index: Convert a word corpus file into an indexed corpus file and collect vocabulary.");
		println("\t\tto_word: Convert an indexed corpus file into a word corpus file given vocabulary.");
		println("\tword_corpus: Corpus file that contains words.");
		println("\tindex_corpus: Indexed corpus file. Not required when only collecting vocabulary (i.e. --get-vocab).");
		println("\tvocab: Vocabulary file.");
	}
	
	public ToolCorpusConverter(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolCorpusConverter(Properties props)
	{
		super(props);
	}
}
