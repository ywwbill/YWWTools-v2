package yang.weiwei.tools.lda;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.lda.LDAParam;
import yang.weiwei.lda.slda.lex_wsb_med_lda.LexWSBMedLDA;

public class ToolLexWSBMedLDA extends ToolLexWSBBSLDA
{
	protected double c;
	
	public void parseCommand()
	{
		super.parseCommand();
		c=Double.valueOf(props.getProperty("c", "1.0"));
	}
	
	public boolean checkCommand()
	{
		if (!super.checkCommand()) return false;
		
		if (c<=0.0)
		{
			println("Parameter c must be a positive real number.");
			return false;
		}
		
		return true;
	}
	
	protected LDAParam createParam() throws IOException
	{
		LDAParam param=super.createParam();
		param.c=c;
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
		LexWSBMedLDA lda=null;
		if (!test)
		{
			lda=new LexWSBMedLDA(param);
			lda.readCorpus(corpusFileName);
			lda.readLabels(labelFileName);
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
			lda=new LexWSBMedLDA(modelFileName, param);
			lda.readCorpus(corpusFileName);
			if (labelFileName!=null && labelFileName.length()>0)
			{
				lda.readLabels(labelFileName);
			}
			if (wsbmGraphFileName!=null && wsbmGraphFileName.length()>0)
			{
				lda.readBlockGraph(wsbmGraphFileName);
			}
			lda.initialize();
			lda.sample(numIters);
		}
		writeFiles(lda);
	}
	
	public void printHelp()
	{
		super.printHelp();
		println("Lex-WSB-Med-LDA arguments:");
		println("\tc [optional]: Regularization parameter in hinge loss (default: 1.0).");
	}
	
	public ToolLexWSBMedLDA(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolLexWSBMedLDA(Properties props)
	{
		super(props);
	}
}
