package yang.weiwei.tools.lda;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.lda.st_lda.STLDA;
import yang.weiwei.lda.LDAParam;

public class ToolSTLDA extends ToolLDA
{
	protected String shortCorpusFileName;
	
	protected String shortThetaFileName;
	protected String shortTopicAssignFileName;
	
	public void parseCommand()
	{
		super.parseCommand();
		shortCorpusFileName=props.getProperty("short_corpus");
		shortThetaFileName=props.getProperty("short_theta");
		shortTopicAssignFileName=props.getProperty("short_topic_assign");
	}
	
	protected boolean checkCommand()
	{
		boolean emptyCorpusFileName=false;
		if (corpusFileName==null || corpusFileName.length()==0)
		{
			corpusFileName="test";
			emptyCorpusFileName=true;
		}
		if (!super.checkCommand()) return false;
		if (emptyCorpusFileName)
		{
			corpusFileName="";
		}
		
		if ((corpusFileName==null || corpusFileName.length()==0) &&
				(shortCorpusFileName==null || shortCorpusFileName.length()==0))
		{
			println("Corpus file name is not specified.");
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
		
		LDAParam param=createParam();
		STLDA lda=null;
		if (!test)
		{
			lda=new STLDA(param);
			if (corpusFileName!=null && corpusFileName.length()>0)
			{
				lda.readCorpus(corpusFileName);
			}
			if (shortCorpusFileName!=null && shortCorpusFileName.length()>0)
			{
				lda.readShortCorpus(shortCorpusFileName);
			}
			lda.initialize();
			lda.sample(numIters);
			lda.writeModel(modelFileName);
		}
		else
		{
			lda=new STLDA(modelFileName, param);
			if (corpusFileName!=null && corpusFileName.length()>0)
			{
				lda.readCorpus(corpusFileName);
			}
			if (shortCorpusFileName!=null && shortCorpusFileName.length()>0)
			{
				lda.readShortCorpus(shortCorpusFileName);
			}
			lda.initialize();
			lda.sample(numIters);
		}
		writeFiles(lda);
	}
	
	protected void writeFiles(STLDA lda) throws IOException
	{
		super.writeFiles(lda);
		if (shortThetaFileName!=null && shortThetaFileName.length()>0)
		{
			lda.writeShortDocTopicDist(shortThetaFileName);
		}
		if (shortTopicAssignFileName!=null && shortTopicAssignFileName.length()>0)
		{
			lda.writeShortDocTopicAssign(shortTopicAssignFileName);
		}
	}
	
	public void printHelp()
	{
		super.printHelp();
		println("BP-LDA arguments:");
		println("\tshort_theta [optional]: Short documents' background topic distribution file.");
		println("\tshort_topic_assign [optional]: Short documents' topic assignment file.");
	}
	
	public ToolSTLDA(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolSTLDA(Properties props)
	{
		super(props);
	}
}
