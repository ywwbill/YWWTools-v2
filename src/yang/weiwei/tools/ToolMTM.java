package yang.weiwei.tools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import yang.weiwei.mtm.MTM;
import yang.weiwei.mtm.MTMParam;

public class ToolMTM extends ToolInterface
{
	private boolean test;
	private boolean verbose;
	
	private int numLangs;
	private double[] alpha;
	private double[] beta;
	private int[] numTopics;
	private int[] wordTfThreshold;
	private int numIters;
	private double lambda;
	private int reg;
	private boolean tfidf;
	private boolean updateAlpha;
	private int updateAlphaInterval;
	private int numTopWords;
	
	private String[] vocabFileName;
	private String[] corpusFileName;
	private String dictFileName;
	private String modelFileName;
	
	private String[] thetaFileName;
	private String[] topicCountFileName;
	private String topicFileName;
	private String rhoFileName;
	
	public void parseCommand()
	{
		test=Boolean.valueOf(props.getProperty("test", "false"));
		verbose=Boolean.valueOf(props.getProperty("verbose", "true"));
		
		numLangs=Integer.valueOf(props.getProperty("num_langs", "0"));
		lambda=Double.valueOf(props.getProperty("lambda", "0.0"));
		reg=Integer.valueOf(props.getProperty("reg", "0"));
		tfidf=Boolean.valueOf(props.getProperty("tfidf", "false"));
		updateAlpha=Boolean.valueOf(props.getProperty("update", "false"));
		updateAlphaInterval=Integer.valueOf(props.getProperty("update_interval", "10"));
		numIters=Integer.valueOf(props.getProperty("iters", "100"));
		numTopWords=Integer.valueOf(props.getProperty("top_word", "10"));
		
		vocabFileName=props.getProperty("vocab").split(",");
		corpusFileName=props.getProperty("corpus").split(",");
		dictFileName=props.getProperty("dict");
		modelFileName=props.getProperty("trained_model");
		
		if (props.getProperty("theta")!=null)
		{
			thetaFileName=props.getProperty("theta").split(",");
		}
		if (props.getProperty("topic_count")!=null)
		{
			topicCountFileName=props.getProperty("topic_count").split(",");
		}
		topicFileName=props.getProperty("output_topic");
		rhoFileName=props.getProperty("rho");
		
		String[] seg;
		if (numLangs>1)
		{
			alpha=new double[numLangs];
			Arrays.fill(alpha, 0.01);
			if (props.getProperty("alpha")!=null)
			{
				seg=props.getProperty("alpha").split(",");
				for (int i=0; i<Math.min(numLangs, seg.length); i++)
				{
					if (seg[i].length()>0)
					{
						alpha[i]=Double.valueOf(seg[i]);
					}
				}
			}
			
			beta=new double[numLangs];
			Arrays.fill(beta, 0.01);
			if (props.getProperty("beta")!=null)
			{
				seg=props.getProperty("beta").split(",");
				for (int i=0; i<Math.min(numLangs, seg.length); i++)
				{
					if (seg[i].length()>0)
					{
						beta[i]=Double.valueOf(seg[i]);
					}
				}
			}
			
			numTopics=new int[numLangs];
			Arrays.fill(numTopics, 10);
			if (props.getProperty("topics")!=null)
			{
				seg=props.getProperty("topics").split(",");
				for (int i=0; i<Math.min(numLangs, seg.length); i++)
				{
					if (seg[i].length()>0)
					{
						numTopics[i]=Integer.valueOf(seg[i]);
					}
				}
			}
			
			wordTfThreshold=new int[numLangs];
			Arrays.fill(wordTfThreshold, 0);
			if (props.getProperty("word_tf_threshold")!=null)
			{
				seg=props.getProperty("word_tf_threshold").split(",");
				for (int i=0; i<Math.min(numLangs, seg.length); i++)
				{
					if (seg[i].length()>0)
					{
						wordTfThreshold[i]=Integer.valueOf(seg[i]);
					}
				}
			}
		}
	}
	
	public boolean checkCommand()
	{
		if (help) return false;
		
		if (numLangs<=1)
		{
			println("Number of languages must be greater than 1.");
			return false;
		}
		
		if (vocabFileName==null)
		{
			println("Vocabulary files are not specified.");
			return false;
		}
		
		if (vocabFileName.length!=numLangs)
		{
			println("Number of vocabulary files does not match the number of languages.");
			return false;
		}
		
		for (int i=0; i<numLangs; i++)
		{
			if (vocabFileName[i]==null || vocabFileName[i].length()==0)
			{
				println("The vocabulary file of language "+i+" is not specified.");
				return false;
			}
		}
		
		if (corpusFileName==null)
		{
			println("Corpus files are not specified.");
			return false;
		}
		
		if (corpusFileName.length!=numLangs)
		{
			println("Number of corpus files does not match the number of languages.");
			return false;
		}
		
		for (int i=0; i<numLangs; i++)
		{
			if (corpusFileName[i]==null || corpusFileName[i].length()==0)
			{
				println("The corpus file of language "+i+" is not specified.");
				return false;
			}
		}
		
		if (dictFileName==null || dictFileName.length()==0)
		{
			println("Dictionary file is not specified.");
			return false;
		}
		
		if (modelFileName==null || modelFileName.length()==0)
		{
			println("Model file is not specified.");
			return false;
		}
		
		for (int i=0; i<numLangs; i++)
		{
			if (alpha[i]<=0.0)
			{
				println("Hyperparameter alpha must be a positive real number.");
				return false;
			}
			
			if (beta[i]<=0.0)
			{
				println("Hyperparameter beta must be a positive real number.");
				return false;
			}
			
			if (numTopics[i]<=0)
			{
				println("Number of topics must be a positive integer.");
				return false;
			}
			
			if (wordTfThreshold[i]<0)
			{
				println("Word term frequency threshold must be a non-negative integer.");
				return false;
			}
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
		
		if (reg<0 || reg>4)
		{
			println("Regularization option must be an integer between 0 and 4.");
			return false;
		}
		
		if (lambda<=0.0 && reg!=0)
		{
			println("Regularization parameter must be a non-negative real number.");
			return false;
		}
		
		if (thetaFileName!=null && thetaFileName.length!=numLangs)
		{
			println("Number of document-topic distribution files does not match the number of languages.");
			return false;
		}
		
		if (topicCountFileName!=null && topicCountFileName.length!=numLangs)
		{
			println("Number of topic count files does not match the number of languages.");
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
		
		MTMParam param=new MTMParam(vocabFileName);
		param.numLangs=numLangs;
		param.alpha=alpha.clone();
		param.beta=beta.clone();
		param.numTopics=numTopics.clone();
		param.verbose=verbose;
		param.wordTfThreshold=wordTfThreshold.clone();
		param.numTopWords=numTopWords;
		param.lambda=lambda;
		param.reg=reg;
		param.tfidf=tfidf;
		param.updateAlpha=updateAlpha;
		param.updateAlphaInterval=updateAlphaInterval;
		
		MTM mtm=null;
		if (!test)
		{
			mtm=new MTM(param);
			mtm.readCorpus(corpusFileName);
			mtm.readWordAssociations(dictFileName);
			mtm.initialize();
			mtm.sample(numIters);
			mtm.writeModel(modelFileName);
			if (rhoFileName!=null)
			{
				mtm.writeTopicTransMatrices(rhoFileName);
			}
		}
		else
		{
			mtm=new MTM(modelFileName, param);
			mtm.readCorpus(corpusFileName);
			mtm.initialize();
			mtm.sample(numIters);
		}
		
		
		if (thetaFileName!=null && thetaFileName.length>0)
		{
			for (int i=0; i<numLangs; i++)
			{
				if (thetaFileName[i]!=null && thetaFileName[i].length()>0)
				{
					mtm.writeDocTopicDist(i, thetaFileName[i]);
				}
			}
		}
		if (topicCountFileName!=null && topicCountFileName.length>0)
		{
			for (int i=0; i<numLangs; i++)
			{
				if (topicCountFileName[i]!=null && topicCountFileName[i].length()>0)
				{
					mtm.writeDocTopicCounts(i, topicCountFileName[i]);
				}
			}
		}
		if (topicFileName!=null && topicFileName.length()>0)
		{
			mtm.writeResult(topicFileName, numTopWords);
		}
	}
	
	public void printHelp()
	{
		println("Arguments for MTM:");
		println("\thelp [optional]: Print help information.");
		println("\ttest [optional]: Use the model for test (default: false).");
		println("\tverbose [optional]: Print log to console (default: true).");
		println("\tnum_langs: Number of languages.");
		println("\tvocab: Vocabulary files, separated by commas (,).");
		println("\tcorpus: Corpus files, separated by commas (,)");
		println("\tdict: Dictionary file.");
		println("\ttrained_model: Model file.");
		
		println("\talpha [optional]: Parameters of Dirichlet priors of document distribution over topics, separated by commas (default: 0.01).");
		println("\tbeta [optional]: Parameters of Dirichlet priors of topic distribution over words, separated by commas (default: 0.01).");
		println("\ttopics [optional]: Numbers of topics, separated by commas (default: 10).");
		println("\titers [optional]: Number of iterations (default: 100).");
		println("\tupdate [optional]: Update alpha while sampling (default: false).");
		println("\tupdate_interval [optional]: Interval of updating alpha (default: 10).");
		println("\treg [optional]: Regularization when optimizing topic link weights. 0: None, 1: L1, 2: L2, 3: Entropy, 4: Identity Matrix (default: 0).");
		println("\tlambda [optional]: The coefficient of regularization. Only effective when reg is not 0 (default: 0.0).");
		println("\ttfidf [optional]: Use TF-IDF weights for word translation pairs (default: false).");
		
		println("\ttheta [optional]: Files for document distribution over topics, separated by commas (,).");
		println("\toutput_topic [optional]: File for showing topics.");
		println("\ttop_word [optional]: Number of words to give when showing topics (default: 10).");
		println("\ttopic_count [optional]: Files for document-topic counts, separated by commas (,).");
		println("\trho [optional, available in training only]: File for topic weight transformation matrices.");
	}
	
	public ToolMTM(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolMTM(Properties props)
	{
		super(props);
	}
}
