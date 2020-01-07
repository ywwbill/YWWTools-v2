package yang.weiwei.tools.lda;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.lda.rtm.RTM;
import yang.weiwei.lda.LDAParam;

public class ToolRTM extends ToolLDA
{
	protected double nu;
	protected boolean directed;
	protected int PLRInterval;
	protected boolean negEdge;
	protected double negEdgeRatio;
	
	protected String rtmTrainGraphFileName;
	protected String rtmTestGraphFileName;
	protected String predFileName;
	protected String regFileName;
	
	public void parseCommand()
	{
		super.parseCommand();
		
		nu=Double.valueOf(props.getProperty("nu", "1.0"));
		directed=Boolean.valueOf(props.getProperty("directed", "false"));
		PLRInterval=Integer.valueOf(props.getProperty("plr_interval", "20"));
		negEdge=Boolean.valueOf(props.getProperty("neg", "false"));
		negEdgeRatio=Double.valueOf(props.getProperty("neg_ratio", "1.0"));
		
		rtmTrainGraphFileName=props.getProperty("rtm_train_graph");
		rtmTestGraphFileName=props.getProperty("rtm_test_graph");
		predFileName=props.getProperty("pred");
		regFileName=props.getProperty("reg");
	}
	
	protected boolean checkCommand()
	{
		if (!super.checkCommand()) return false;
		
		if ((rtmTrainGraphFileName==null || rtmTrainGraphFileName.length()==0) && !test)
		{
			println("RTM train graph file is not specified.");
			return false;
		}
		
		if (!test && (rtmTestGraphFileName==null || rtmTestGraphFileName.length()==0))
		{
			rtmTestGraphFileName=rtmTrainGraphFileName;
		}
		
		if ((rtmTestGraphFileName==null || rtmTestGraphFileName.length()==0) && test)
		{
			println("RTM test graph is not specified.");
			return false;
		}
		
		if (nu<=0.0)
		{
			println("Parameter nu must be a positive real number.");
			return false;
		}
		
		if (PLRInterval<=0)
		{
			println("Interval of computing PLR must be a positive integer.");
			return false;
		}
		
		if (negEdge && negEdgeRatio<0.0)
		{
			println("Negative edge ratio must be a non-negative real number.");
			return false;
		}
		
		return true;
	}
	
	protected LDAParam createParam() throws IOException
	{
		LDAParam param=super.createParam();
		param.nu=nu;
		param.directed=directed;
		param.showPLRInterval=PLRInterval;
		param.negEdge=negEdge;
		param.negEdgeRatio=negEdgeRatio;
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
		RTM lda=null;
		if (!test)
		{
			lda=new RTM(param);
			lda.readCorpus(corpusFileName);
			lda.readGraph(rtmTrainGraphFileName, RTM.TRAIN_GRAPH);
			lda.readGraph(rtmTestGraphFileName, RTM.TEST_GRAPH);
			lda.initialize();
			lda.sample(numIters);
			lda.writeModel(modelFileName);
		}
		else
		{
			lda=new RTM(modelFileName, param);
			lda.readCorpus(corpusFileName);
			if (rtmTrainGraphFileName!=null && rtmTrainGraphFileName.length()>0)
			{
				lda.readGraph(rtmTrainGraphFileName, RTM.TRAIN_GRAPH);
			}
			lda.readGraph(rtmTestGraphFileName, RTM.TEST_GRAPH);
			lda.initialize();
			lda.sample(numIters);
		}
		writeFiles(lda);
	}
	
	protected void writeFiles(RTM lda) throws IOException
	{
		super.writeFiles(lda);
		if (predFileName!=null && predFileName.length()>0)
		{
			lda.writePred(predFileName);
		}
		if (regFileName!=null && regFileName.length()>0)
		{
			lda.writeRegValues(regFileName);
		}
	}
	
	public void printHelp()
	{
		super.printHelp();
		println("RTM arguments:");
		println("\trtm_train_graph [optional in test]: Link file for RTM to train.");
		println("\trtm_test_graph [optional in training]: Link file for RTM to evaluate. Can be the same with RTM train graph.");
		println("\tnu [optional]: Variance of normal priors for weight vectors/matrices in RTM and its extensions (default: 1.0).");
		println("\tplr_interval [optional]: Interval of computing predictive link rank (default: 20).");
		println("\tneg [optional]: Sample negative links (default: false).");
		println("\tneg_ratio [optional]: The ratio of number of negative links to number of positive links (default 1.0).");
		println("\tpred [optional]: Predicted document link probability matrix file.");
		println("\tdirected [optional]: Set all edges directed or undirected (default: false).");
	}
	
	public ToolRTM(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolRTM(Properties props)
	{
		super(props);
	}
}
