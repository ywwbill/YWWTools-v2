package yang.weiwei.tools.lda;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.lda.LDAParam;
import yang.weiwei.lda.rtm.lex_wsb_med_rtm.LexWSBMedRTM;

public class ToolLexWSBMedRTM extends ToolLexWSBRTM
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
		LexWSBMedRTM lda=null;
		if (!test)
		{
			lda=new LexWSBMedRTM(param);
			lda.readCorpus(corpusFileName);
			lda.readGraph(rtmTrainGraphFileName, LexWSBMedRTM.TRAIN_GRAPH);
			lda.readGraph(rtmTestGraphFileName, LexWSBMedRTM.TEST_GRAPH);
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
			lda=new LexWSBMedRTM(modelFileName, param);
			lda.readCorpus(corpusFileName);
			if (rtmTrainGraphFileName!=null && rtmTrainGraphFileName.length()>0)
			{
				lda.readGraph(rtmTrainGraphFileName, LexWSBMedRTM.TRAIN_GRAPH);
			}
			lda.readGraph(rtmTestGraphFileName, LexWSBMedRTM.TEST_GRAPH);
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
		println("Lex-WSB-Med-RTM arguments:");
		println("\tc [optional]: Regularization parameter in hinge loss (default: 1.0).");
	}
	
	public ToolLexWSBMedRTM(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolLexWSBMedRTM(Properties props)
	{
		super(props);
	}
}
