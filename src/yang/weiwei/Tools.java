package yang.weiwei;

import java.io.IOException;
import java.util.HashSet;

import yang.weiwei.tools.ToolLDAInterface;
import yang.weiwei.tools.ToolWSBM;
import yang.weiwei.tools.ToolSCC;
import yang.weiwei.tools.ToolStoplist;
import yang.weiwei.tools.ToolTLDA;
import yang.weiwei.tools.ToolMTM;
import yang.weiwei.tools.ToolLemmatizer;
import yang.weiwei.tools.ToolPOSTagger;
import yang.weiwei.tools.ToolStemmer;
import yang.weiwei.tools.ToolTokenizer;
import yang.weiwei.tools.ToolTreeBuilder;
import yang.weiwei.tools.ToolCorpusConverter;
import yang.weiwei.tools.ToolInterface;

public class Tools extends ToolInterface
{	
	private static HashSet<String> toolSet;
	
	private String tool;
	
	public void parseCommand()
	{
		tool=props.getProperty("tool").toLowerCase();
	}
	
	protected boolean checkCommand()
	{
		if (tool==null || tool.length()==0 || !toolSet.contains(tool))
		{
			if (!help || (tool!=null && tool.length()>0))
			{
				println("Tool name is not specified or does not exist.");
			}
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
		
		ToolInterface toolImpl=null;
		switch (tool)
		{
		case "lda": toolImpl=new ToolLDAInterface(props); break;
		case "tlda": toolImpl=new ToolTLDA(props); break;
		case "tree-builder": toolImpl=new ToolTreeBuilder(props); break;
		case "mtm": toolImpl=new ToolMTM(props); break;
		case "wsbm": toolImpl=new ToolWSBM(props); break;
		case "scc": toolImpl=new ToolSCC(props); break;
		case "stoplist": toolImpl=new ToolStoplist(props); break;
		case "lemmatizer": toolImpl=new ToolLemmatizer(props); break;
		case "pos-tagger": toolImpl=new ToolPOSTagger(props); break;
		case "stemmer": toolImpl=new ToolStemmer(props); break;
		case "tokenizer": toolImpl=new ToolTokenizer(props); break;
		case "corpus-converter": toolImpl=new ToolCorpusConverter(props); break;
		}
		if (toolImpl!=null)
		{
			toolImpl.parseCommand();
			toolImpl.execute();
		}
	}
	
	public void printHelp()
	{
		println("Arguments for Tools:");
		println("\thelp [optional]: Print help information.");
		println("\ttool: Name of tool you want to use. Supported tools are");
		println("\t\tLemmatizer: Lemmatize POS-tagged corpus. Support English only, but can support other languages given dictionary.");
		println("\t\tPOS-Tagger: Tag words' POS. Support English only, but can support other languages given trained models.");
		println("\t\tStemmer: Stem words. Support English only, but can support other languages given trained models.");
		println("\t\tStoplist: Remove stop words. Support English only, but can support other languages given dictionary.");
		println("\t\tTokenizer: Tokenize corpus. Support English only, but can support other languages given trained models.");
		println("\t\tCorpus-Converter: Convert word corpus into indexed corpus (for LDA) and vice versa.");
		println("\t\tSCC: Strongly connected components.");
		println("\t\tWSBM: Weighted stochastic block model. Find blocks in a network.");
		println("\t\tLDA: Latent Dirichlet allocation. Include a variety of extensions.");
		println("\t\ttLDA: tree LDA.");
		println("\t\tTree-Builder: Tool for building two-level, HAC, and HAC-LD trees.");
		println("\t\tMTM: Multilingual topic model.");
	}
	
	public Tools(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	static
	{
		toolSet=new HashSet<String>();
		toolSet.add("lemmatizer");
		toolSet.add("pos-tagger");
		toolSet.add("stemmer");
		toolSet.add("stoplist");
		toolSet.add("tokenizer");
		toolSet.add("corpus-converter");
		toolSet.add("scc");
		toolSet.add("wsbm");
		toolSet.add("lda");
		toolSet.add("tlda");
		toolSet.add("tree-builder");
		toolSet.add("mtm");
	}
	
	public static void main(String args[]) throws IOException
	{
		Tools tool=new Tools(args[0]);
		tool.parseCommand();
		tool.execute();
	}
}
