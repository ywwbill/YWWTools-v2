package yang.weiwei.tools.lda;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.lda.LDAParam;
import yang.weiwei.lda.slda.bs_lda.BSLDA;

public class ToolBSLDA extends ToolSLDA
{
	public void execute() throws IOException
	{
		if (!checkCommand())
		{
			printHelp();
			return;
		}
		
		LDAParam param=createParam();
		BSLDA lda=null;
		if (!test)
		{
			lda=new BSLDA(param);
			lda.readCorpus(corpusFileName);
			lda.readLabels(labelFileName);
			lda.initialize();
			lda.sample(numIters);
			lda.writeModel(modelFileName);
		}
		else
		{
			lda=new BSLDA(modelFileName, param);
			lda.readCorpus(corpusFileName);
			if (labelFileName!=null && labelFileName.length()>0)
			{
				lda.readLabels(labelFileName);
			}
			lda.initialize();
			lda.sample(numIters);
		}
		writeFiles(lda);
	}
	
	public ToolBSLDA(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolBSLDA(Properties props)
	{
		super(props);
	}
}
