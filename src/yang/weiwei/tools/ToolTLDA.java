package yang.weiwei.tools;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.tlda.TLDAParam;
import yang.weiwei.tlda.TLDA;

public class ToolTLDA extends ToolInterface
{
	private boolean test;
	private boolean verbose;
	
	private double alpha;
	private double beta;
	private int numTopics;
	private boolean updateAlpha;
	private int updateAlphaInterval;
	private int numIters;
	
	private String treePriorFileName;
	private String vocabFileName;
	private String corpusFileName;
	private String modelFileName;
	
	private String thetaFileName;
	private String topicFileName;
	private String topicCountFileName;
	private int numTopWords;
	
	public void parseCommand()
	{
		test=Boolean.valueOf(props.getProperty("test", "false"));
		verbose=Boolean.valueOf(props.getProperty("verbose", "true"));
		
		alpha=Double.valueOf(props.getProperty("alpha", "0.01"));
		beta=Double.valueOf(props.getProperty("beta", "0.01"));
		numTopics=Integer.valueOf(props.getProperty("topics", "10"));
		updateAlpha=Boolean.valueOf(props.getProperty("update", "false"));
		updateAlphaInterval=Integer.valueOf(props.getProperty("update_interval", "10"));
		numIters=Integer.valueOf(props.getProperty("iters", "100"));
		
		treePriorFileName=props.getProperty("tree");
		vocabFileName=props.getProperty("vocab");
		corpusFileName=props.getProperty("corpus");
		modelFileName=props.getProperty("trained_model");
		
		thetaFileName=props.getProperty("theta");
		topicFileName=props.getProperty("output_topic");
		topicCountFileName=props.getProperty("topic_count");
		numTopWords=Integer.valueOf(props.getProperty("top_word", "10"));
	}

	protected boolean checkCommand()
	{
		if (help) return false;
		
		if (treePriorFileName==null || treePriorFileName.length()==0)
		{
			println("Tree prior file is not specified.");
			return false;
		}
		
		if (vocabFileName==null || vocabFileName.length()==0)
		{
			println("Vocabulary file is not specified.");
			return false;
		}
		
		if (corpusFileName==null || corpusFileName.length()==0)
		{
			println("Corpus file is not specified.");
			return false;
		}
		
		if (modelFileName==null || modelFileName.length()==0)
		{
			println("Model file is not specified.");
			return false;
		}
		
		if (alpha<=0.0)
		{
			println("Hyperparameter alpha must be a positive real number.");
			return false;
		}
		
		if (beta<=0.0)
		{
			println("Hyperparameter beta must be a positive real number.");
			return false;
		}
		
		if (numTopics<=0)
		{
			println("Number of topics must be a positive integer.");
			return false;
		}
		
		if (numIters<=0)
		{
			println("Number of iterations must be a positive integer.");
			return false;
		}
		
		if (updateAlphaInterval<=0)
		{
			println("Interval of updating alpha must be a positive integer.");
			return false;
		}
		
		if (numTopWords<=0)
		{
			println("Number of top words must be a positive integer.");
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
		
		TLDAParam param=new TLDAParam(vocabFileName, treePriorFileName);
		param.alpha=alpha;
		param.beta=beta;
		param.numTopics=numTopics;
		param.verbose=verbose;
		param.updateAlpha=updateAlpha;
		param.updateAlphaInterval=updateAlphaInterval;
		TLDA tlda=null;
		if (!test)
		{
			tlda=new TLDA(param);
			tlda.readCorpus(corpusFileName);
			tlda.initialize();
			tlda.sample(numIters);
			tlda.writeModel(modelFileName);
			if (topicFileName!=null && topicFileName.length()>0)
			{
				tlda.writeWordResult(topicFileName, numTopWords);
			}
		}
		else
		{
			tlda=new TLDA(modelFileName, param);
			tlda.readCorpus(corpusFileName);
			tlda.initialize();
			tlda.sample(numIters);
		}
		if (thetaFileName!=null && thetaFileName.length()>0)
		{
			tlda.writeDocTopicDist(thetaFileName);
		}
		if (topicCountFileName!=null && topicCountFileName.length()>0)
		{
			tlda.writeDocTopicCounts(topicCountFileName);
		}
	}

	public void printHelp()
	{
		println("Arguments for tLDA:");
		println("\thelp [optional]: Print help information.");
		println("\ttest [optional]: Use the model for test (default: false).");
		println("\tverbose [optional]: Print log to console (default: true).");
		println("\ttree: Tree prior file.");
		println("\tvocab: Vocabulary file.");
		println("\tcorpus: Corpus file");
		println("\ttrained_model: Model file.");
		
		println("\talpha [optional]: Parameter of Dirichlet prior of document distribution over topics (default: 1.0).");
		println("\tbeta [optional]: Parameter of Dirichlet prior of topic distribution over words (default: 0.1).");
		println("\ttopics [optional]: Number of topics (default: 10).");
		println("\titers [optional]: Number of iterations (default: 100).");
		println("\tupdate [optional]: Update alpha while sampling (default: false).");
		println("\tupdate_interval [optional]: Interval of updating alpha (default: 10).");
		println("\ttheta [optional]: File for document distribution over topics.");
		println("\toutput_topic [optional]: File for showing topics.");
		println("\ttop_word [optional]: Number of words to give when showing topics (default: 10).");
		println("\ttopic_count [optional]: File for document-topic counts.");
	}
	
	public ToolTLDA(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolTLDA(Properties props)
	{
		super(props);
	}
}
