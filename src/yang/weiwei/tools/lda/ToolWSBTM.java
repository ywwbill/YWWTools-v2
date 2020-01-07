package yang.weiwei.tools.lda;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.lda.wsb_tm.WSBTM;
import yang.weiwei.lda.LDAParam;

public class ToolWSBTM extends ToolLDA
{
	protected double _alpha;
	protected double a;
	protected double b;
	protected double gamma;
	protected int numBlocks;
	protected boolean directed;
	
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
		directed=Boolean.valueOf(props.getProperty("directed", "false"));
		
		wsbmGraphFileName=props.getProperty("wsbm_graph");
		outputWSBMFileName=props.getProperty("output_wsbm");
	}
	
	protected boolean checkCommand()
	{
		if (!super.checkCommand()) return false;
		
		if ((wsbmGraphFileName==null || wsbmGraphFileName.length()==0) && !test)
		{
			println("Graph file for WSBM is not specified.");
			return false;
		}
		
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
		param.directed=directed;
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
		WSBTM lda=null;
		if (!test)
		{
			lda=new WSBTM(param);
			lda.readCorpus(corpusFileName);
			lda.readGraph(wsbmGraphFileName);
			lda.initialize();
			lda.sample(numIters);
			lda.writeModel(modelFileName);
		}
		else
		{
			lda=new WSBTM(modelFileName, param);
			lda.readCorpus(corpusFileName);
			if (wsbmGraphFileName!=null && wsbmGraphFileName.length()>0)
			{
				lda.readGraph(wsbmGraphFileName);
			}
			lda.initialize();
			lda.sample(numIters);
		}
		writeFiles(lda);
	}
	
	protected void writeFiles(WSBTM lda) throws IOException
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
		println("WSB-TM arguments:");
		println("\twsbm_graph [optional in test]: Link file for WSBM to find blocks.");
		println("\talpha_prime [optional]: Parameter of Dirichlet prior of block distribution over topics (default: 1.0).");
		println("\ta [optional]: Parameter of Gamma prior for block link rates (default: 1.0).");
		println("\tb [optional]: Parameter of Gamma prior for block link rates (default: 1.0).");
		println("\tgamma [optional]: Parameter of Dirichlet prior for block distribution (default: 1.0).");
		println("\tblocks [optional]: Number of blocks (default: 10).");
		println("\tdirected [optional]: Set all edges directed or undirected (default: false).");
		println("\toutput_wsbm [optional]: File for WSBM-identified blocks.");
	}
	
	public ToolWSBTM(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolWSBTM(Properties props)
	{
		super(props);
	}
}
