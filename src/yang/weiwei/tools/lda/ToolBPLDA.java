package yang.weiwei.tools.lda;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.lda.bp_lda.BPLDA;
import yang.weiwei.lda.LDAParam;

public class ToolBPLDA extends ToolLDA
{
	protected String blockFileName;
	
	protected double _alpha;
	
	public void parseCommand()
	{
		super.parseCommand();
		blockFileName=props.getProperty("block_graph");
		_alpha=Double.valueOf(props.getProperty("alpha_prime", "1.0"));
	}
	
	protected boolean checkCommand()
	{
		if (!super.checkCommand()) return false;
		
		if ((blockFileName==null || blockFileName.length()==0) && !test)
		{
			println("Block file is not specified.");
			return false;
		}
		
		if (_alpha<=0.0)
		{
			println("Hyperparameter alpha' must be a positive real number.");
			return false;
		}
		
		return true;
	}
	
	protected LDAParam createParam() throws IOException
	{
		LDAParam param=super.createParam();
		param._alpha=_alpha;
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
		BPLDA lda=null;
		if (!test)
		{
			lda=new BPLDA(param);
			lda.readCorpus(corpusFileName);
			lda.readBlocks(blockFileName);
			lda.initialize();
			lda.sample(numIters);
			lda.writeModel(modelFileName);
		}
		else
		{
			lda=new BPLDA(modelFileName, param);
			lda.readCorpus(corpusFileName);
			if (blockFileName!=null && blockFileName.length()>0)
			{
				lda.readBlocks(blockFileName);
			}
			lda.initialize();
			lda.sample(numIters);
		}
		writeFiles(lda);
	}
	
	public void printHelp()
	{
		super.printHelp();
		println("BP-LDA arguments:");
		println("\tblock_graph [optional in test]: Pre-computed block file.");
		println("\talpha_prime [optional]: Parameter of Dirichlet prior of block distribution over topics (default: 1.0).");
	}
	
	public ToolBPLDA(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolBPLDA(Properties props)
	{
		super(props);
	}
}
