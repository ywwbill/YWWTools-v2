package yang.weiwei.tools.lda;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.lda.LDA;
import yang.weiwei.lda.LDAParam;
import yang.weiwei.tools.ToolLDAInterface;

public class ToolLDA extends ToolLDAInterface
{
	//general
	protected boolean test;
	protected boolean verbose;
	
	//basic parameter
	protected double alpha;
	protected double beta;
	protected int numTopics;
	protected boolean updateAlpha;
	protected int updateAlphaInterval;
	protected int numIters;
	
	//basic configure
	protected String vocabFileName;
	protected String corpusFileName;
	protected String modelFileName;
	
	//basic optional configure
	protected String thetaFileName;
	protected String topicFileName;
	protected int numTopWords;
	protected String topicCountFileName;
	
	public void parseCommand()
	{
		super.parseCommand();
		test=Boolean.valueOf(props.getProperty("test", "false"));
		verbose=Boolean.valueOf(props.getProperty("verbose", "true"));
		
		alpha=Double.valueOf(props.getProperty("alpha", "1.0"));
		beta=Double.valueOf(props.getProperty("beta", "0.1"));
		numTopics=Integer.valueOf(props.getProperty("topics", "10"));
		updateAlpha=Boolean.valueOf(props.getProperty("update", "false"));
		updateAlphaInterval=Integer.valueOf(props.getProperty("update_interval", "10"));
		numIters=Integer.valueOf(props.getProperty("iters", "100"));
		
		vocabFileName=props.getProperty("vocab");
		corpusFileName=props.getProperty("corpus");
		modelFileName=props.getProperty("trained_model");
		
		thetaFileName=props.getProperty("theta");
		topicFileName=props.getProperty("output_topic");
		numTopWords=Integer.valueOf(props.getProperty("top_word", "10"));
		topicCountFileName=props.getProperty("topic_count");
	}
	
	protected boolean checkCommand()
	{
		if (!super.checkCommand()) return false;
		
		if (help) return false;
		
		if (model==null || model.length()==0)
		{
			model="lda";
		}
		
		if (!ldaNames.contains(model))
		{
			println("Model is not supported.");
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
	
	protected LDAParam createParam() throws IOException
	{
		LDAParam param=new LDAParam(vocabFileName);
		param.alpha=alpha;
		param.beta=beta;
		param.numTopics=numTopics;
		param.verbose=verbose;
		param.updateAlpha=updateAlpha;
		param.updateAlphaInterval=updateAlphaInterval;
		return param;
	}
	
	public void execute() throws IOException
	{
		if (!checkCommand())
		{
			printHelp();
			return;
		}
		
		LDAParam param=createParam();
		LDA lda=null;
		if (!test)
		{
			lda=new LDA(param);
			lda.readCorpus(corpusFileName);
			lda.initialize();
			lda.sample(numIters);
			lda.writeModel(modelFileName);
		}
		else
		{
			lda=new LDA(modelFileName, param);
			lda.readCorpus(corpusFileName);
			lda.initialize();
			lda.sample(numIters);
		}
		writeFiles(lda);
	}
	
	protected void writeFiles(LDA lda) throws IOException
	{
		if (!test && topicFileName!=null && topicFileName.length()>0)
		{
			lda.writeResult(topicFileName, numTopWords);
		}
		if (thetaFileName!=null && thetaFileName.length()>0)
		{
			lda.writeDocTopicDist(thetaFileName);
		}
		if (topicCountFileName!=null && topicCountFileName.length()>0)
		{
			lda.writeDocTopicCounts(topicCountFileName);
		}
	}

	public void printHelp()
	{
		println("Arguments for LDA:");
		println("Basic arguments:");
		println("\thelp [optional]: Print help information.");
		println("\tmodel [optional]: The topic model you want to use (default: LDA). Supported models are");
		println("\t\tLDA: Vanilla LDA");
		println("\t\tBP-LDA: LDA with block priors. Blocks are pre-computed.");
		println("\t\tST-LDA: Single topic LDA. Each document can only be assigned to one topic.");
		println("\t\tWSB-TM: LDA with block priors. Blocks are computed by WSBM.");
		println("\t\tRTM: Relational topic model.");
		println("\t\t\tLex-WSB-RTM: RTM with WSB-computed block priors and lexical weights.");
		println("\t\t\tLex-WSB-Med-RTM: Lex-WSB-RTM with hinge loss.");
		println("\t\tSLDA: Supervised LDA. Support multi-class classification.");
		println("\t\t\tBS-LDA: Binary SLDA.");
		println("\t\t\tLex-WSB-BS-LDA: BS-LDA with WSB-computed block priors and lexical weights.");
		println("\t\t\tLex-WSB-Med-LDA: Lex-WSB-BS-LDA with hinge loss.");
		println("\ttest [optional]: Use the model for test (default: false).");
		println("\tverbose [optional]: Print log to console (default: true).");
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
	
	public ToolLDA(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolLDA(Properties props)
	{
		super(props);
	}
}
