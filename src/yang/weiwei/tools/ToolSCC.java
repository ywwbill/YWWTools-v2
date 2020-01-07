package yang.weiwei.tools;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.scc.SCC;

public class ToolSCC extends ToolInterface
{
	private int numNodes;
	private String graphFileName;
	private String outputFileName;
	
	public void parseCommand()
	{
		numNodes=Integer.valueOf(props.getProperty("nodes", "-1"));
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
		
		return true;
	}

	public void execute() throws IOException
	{
		if (!checkCommand())
		{
			printHelp();
			return;
		}
		
		SCC scc=new SCC(numNodes);
		scc.readGraph(graphFileName);
		scc.cluster();
		scc.writeClusters(outputFileName);
	}

	public void printHelp()
	{
		println("Arguments for SCC:");
		println("\thelp [optional]: Print help information.");
		println("\tnodes: Number of nodes.");
		println("\tgraph: Graph file.");
		println("\toutput: Result file.");
	}
	
	public ToolSCC(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolSCC(Properties props)
	{
		super(props);
	}
}
