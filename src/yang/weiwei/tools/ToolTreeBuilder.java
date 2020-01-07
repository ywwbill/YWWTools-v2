package yang.weiwei.tools;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.tlda.TreeBuilder;

public class ToolTreeBuilder extends ToolInterface
{
	private String vocabFileName;
	private String scoreFileName;
	private String treeFileName;
	private int treeType;
	private int numChild;
	private double threshold;
	
	public void parseCommand()
	{
		vocabFileName=props.getProperty("vocab");
		scoreFileName=props.getProperty("score");
		treeFileName=props.getProperty("tree");
		treeType=Integer.valueOf(props.getProperty("type", "1"));
		numChild=Integer.valueOf(props.getProperty("child", "10"));
		threshold=Double.valueOf(props.getProperty("thresh", "0.0"));
	}

	protected boolean checkCommand()
	{
		if (help) return false;
		
		if (vocabFileName==null || vocabFileName.length()==0)
		{
			println("Vocabulary file is not given.");
			return false;
		}
		
		if (scoreFileName==null || scoreFileName.length()==0)
		{
			println("Word association file is not given.");
			return false;
		}
		
		if (treeFileName==null || treeFileName.length()==0)
		{
			println("Tree prior file is not given.");
			return false;
		}
		
		if (treeType<1 || treeType>3)
		{
			println("Tree type must be 1, 2, or 3.");
			return false;
		}
		
		if (numChild<0)
		{
			println("Number of child nodes must be a positive integer.");
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
		
		TreeBuilder tBuilder=new TreeBuilder();
		switch (treeType)
		{
		case 1: tBuilder.build2LevelTree(scoreFileName, vocabFileName, treeFileName, numChild); break;
		case 2: tBuilder.hac(scoreFileName, vocabFileName, treeFileName, threshold); break;
		case 3: tBuilder.hacWithLeafDup(scoreFileName, vocabFileName, treeFileName, threshold); break;
		}
	}

	public void printHelp()
	{
		println("Arguments for tree builder tool:");
		println("\thelp [optional]: Print help information.");
		println("\tvocab: Vocabulary file.");
		println("\tscore: Word association file.");
		println("\ttree: Tree prior file.");
		println("\ttype [optional]: Tree prior type. 1 for two-level tree; 2 for hierarchical agglomerative clustering (HAC) tree; 3 for HAC tree with leaf duplication. (default 1)");
		println("\tchild [optional]: Number of child nodes per internal node for a two-level tree. (default 10)");
		println("\tthresh [optional]: The confidence threshold for HAC. (default 0.0)");
	}
	
	public ToolTreeBuilder(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolTreeBuilder(Properties props)
	{
		super(props);
	}
}
