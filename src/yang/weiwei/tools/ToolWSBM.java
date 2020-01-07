package yang.weiwei.tools;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.wsbm.WSBM;
import yang.weiwei.wsbm.WSBMParam;

public class ToolWSBM extends ToolInterface
{
	private boolean directed;
	private boolean verbose;
	
	private int numNodes;
	private int numBlocks;
	private int numIters;
	private double a;
	private double b;
	private double gamma;
	
	private String graphFileName;
	private String outputFileName;
	
	public void parseCommand()
	{
		directed=Boolean.valueOf(props.getProperty("directed", "false"));
		verbose=Boolean.valueOf(props.getProperty("verbose", "true"));
		
		numNodes=Integer.valueOf(props.getProperty("nodes", "-1"));
		numBlocks=Integer.valueOf(props.getProperty("blocks", "-1"));
		numIters=Integer.valueOf(props.getProperty("iters", "100"));
		a=Double.valueOf(props.getProperty("a", "1.0"));
		b=Double.valueOf(props.getProperty("b", "1.0"));
		gamma=Double.valueOf(props.getProperty("gamma", "1.0"));
		
		graphFileName=props.getProperty("graph");
		outputFileName=props.getProperty("output");
	}

	protected boolean checkCommand()
	{
		if (help) return false;
		
		if (numNodes<=0)
		{
			println("Number of nodes is non-positive or not specified.");
			return false;
		}
		
		if (numBlocks<=0)
		{
			println("Number of blocks is non-positive or not specified.");
			return false;
		}
		
		if (graphFileName==null || graphFileName.length()==0)
		{
			println("Graph file is not specified.");
			return false;
		}
		
		if (outputFileName==null || outputFileName.length()==0)
		{
			println("Output file is not specified.");
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
		
		if (numIters<=0)
		{
			println("Numer of iterations must be a positive integer.");
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
		
		WSBMParam param=new WSBMParam();
		param.directed=directed;
		param.numNodes=numNodes;
		param.numBlocks=numBlocks;
		param.a=a;
		param.b=b;
		param.gamma=gamma;
		param.verbose=verbose;
		WSBM wsbm=new WSBM(param);
		wsbm.readGraph(graphFileName);
		wsbm.init();
		wsbm.sample(numIters);
		wsbm.writeBlockAssign(outputFileName);
	}

	public void printHelp()
	{
		println("Arguments for WSBM:");
		println("\thelp [optional]: Print help information.");
		println("\tnodes: Number of nodes.");
		println("\tblocks: Number of blocks.");
		println("\tgraph: Graph file.");
		println("\toutput: Output file.");
		println("\tdirected [optional]: Set edges directed or undirected (default: false).");
		println("\ta [optional]: Parameter for edge rates' Gamma prior (default: 1.0).");
		println("\tb [optional]: Parameter for edge rates' Gamma prior (default: 1.0).");
		println("\tgamma [optional]: Parameter for block distribution's Dirichlet prior (default 1.0).");
		println("\titers [optional]: Number of iterations (default: 100).");
		println("\tverbose [optional]: Print log to console (default: true).");
	}
	
	public ToolWSBM(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolWSBM(Properties props)
	{
		super(props);
	}
}
