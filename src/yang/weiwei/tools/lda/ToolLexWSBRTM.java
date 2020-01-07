package yang.weiwei.tools.lda;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.lda.LDAParam;
import yang.weiwei.lda.rtm.lex_wsb_rtm.LexWSBRTM;

public class ToolLexWSBRTM extends ToolRTM
{
	protected double _alpha;
	protected double a;
	protected double b;
	protected double gamma;
	protected int numBlocks;
	protected boolean blockFeat;
	
	protected String wsbmGraphFileName;
	protected String outputWSBMFileName; 
	
	public void parseCommand()
	{
		super.parseCommand();
		
		_alpha=Double.valueOf(props.getProperty("alpha_prime", "1.0"));
		a=Double.valueOf(props.getProperty("a", "1.0"));
		b=Double.valueOf(props.getProperty("b", "1.0"));
		gamma=Double.valueOf(props.getProperty("gamma", "1.0"));
		numBlocks=Integer.valueOf(props.getProperty("blocks", "10"));
		blockFeat=Boolean.valueOf(props.getProperty("block_feature", "false"));
		
		wsbmGraphFileName=props.getProperty("wsbm_graph");
		outputWSBMFileName=props.getProperty("output_wsbm");
	}
	
	public boolean checkCommand()
	{
		if (!super.checkCommand()) return false;
		
		if (_alpha<=0.0)
		{
			println("Hyperparameter alpha' must be a positive real number.");
			return false;
		}
		
		if (a<=0.0)
		{
			println("Hyperparameter a must be a positive real number.");
			return false;
		}
		
		if (b<=0.0)
		{
			println("Hyperparameter b must be a positive real number.");
			return false;
		}
		
		if (gamma<=0.0)
		{
			println("Hyperparameter gamma must be a positive real number.");
			return false;
		}
		
		if (numBlocks<=0)
		{
			println("Number of blocks must be a positive integer.");
			return false;
		}
		
		return true;
	}
	
	protected LDAParam createParam() throws IOException
	{
		LDAParam param=super.createParam();
		param._alpha=_alpha;
		param.a=a;
		param.b=b;
		param.gamma=gamma;
		param.numBlocks=numBlocks;
		param.blockFeat=blockFeat;
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
		LexWSBRTM lda=null;
		if (!test)
		{
			lda=new LexWSBRTM(param);
			lda.readCorpus(corpusFileName);
			lda.readGraph(rtmTrainGraphFileName, LexWSBRTM.TRAIN_GRAPH);
			lda.readGraph(rtmTestGraphFileName, LexWSBRTM.TEST_GRAPH);
			if (wsbmGraphFileName!=null && wsbmGraphFileName.length()>0)
			{
				lda.readBlockGraph(wsbmGraphFileName);
			}
			lda.initialize();
			lda.sample(numIters);
			lda.writeModel(modelFileName);
		}
		else
		{
			lda=new LexWSBRTM(modelFileName, param);
			lda.readCorpus(corpusFileName);
			if (rtmTrainGraphFileName!=null && rtmTrainGraphFileName.length()>0)
			{
				lda.readGraph(rtmTrainGraphFileName, LexWSBRTM.TRAIN_GRAPH);
			}
			lda.readGraph(rtmTestGraphFileName, LexWSBRTM.TEST_GRAPH);
			if (wsbmGraphFileName!=null && wsbmGraphFileName.length()>0)
			{
				lda.readBlockGraph(wsbmGraphFileName);
			}
			lda.initialize();
			lda.sample(numIters);
		}
		writeFiles(lda);
	}
	
	protected void writeFiles(LexWSBRTM lda) throws IOException
	{
		super.writeFiles(lda);
		if (wsbmGraphFileName!=null && wsbmGraphFileName.length()>0 &&
				outputWSBMFileName!=null && outputWSBMFileName.length()>0)
		{
			lda.writeBlocks(outputWSBMFileName);
		}
	}
	
	public void printHelp()
	{
		super.printHelp();
		println("Lex-WSB-RTM arguments:");
		println("\twsbm_graph [optional]: Link file for WSBM to find blocks.");
		println("\talpha_prime [optional]: Parameter of Dirichlet prior of block distribution over topics (default: 1.0).");
		println("\ta [optional]: Parameter of Gamma prior for block link rates (default: 1.0).");
		println("\tb [optional]: Parameter of Gamma prior for block link rates (default: 1.0).");
		println("\tgamma [optional]: Parameter of Dirichlet prior for block distribution (default: 1.0).");
		println("\tblocks [optional]: Number of blocks (default: 10).");
		println("\toutput_wsbm [optional]: File for WSBM-identified blocks.");
	}
	
	public ToolLexWSBRTM(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolLexWSBRTM(Properties props)
	{
		super(props);
	}
}
