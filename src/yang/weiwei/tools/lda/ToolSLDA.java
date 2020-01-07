package yang.weiwei.tools.lda;

import java.io.IOException;
import java.util.Properties;

import yang.weiwei.lda.slda.SLDA;
import yang.weiwei.lda.LDAParam;

public class ToolSLDA extends ToolLDA
{
	protected double sigma;
	protected double nu;
	
	protected String labelFileName;
	protected String predFileName;
	protected String regFileName;
	
	public void parseCommand()
	{
		super.parseCommand();
		
		sigma=Double.valueOf(props.getProperty("sigma", "1.0"));
		nu=Double.valueOf(props.getProperty("nu", "1.0"));
		
		labelFileName=props.getProperty("label");
		predFileName=props.getProperty("pred");
		regFileName=props.getProperty("reg");
	}
	
	protected boolean checkCommand()
	{
		if (!super.checkCommand()) return false;
		
		if ((labelFileName==null || labelFileName.length()==0) && !test)
		{
			println("Label file is not specified.");
			return false;
		}
		
		if (nu<=0.0)
		{
			println("Parameter nu must be a positive real number.");
			return false;
		}
		
		if (sigma<=0.0)
		{
			println("Parameter sigma must be a positive real number.");
			return false;
		}
		
		return true;
	}
	
	protected LDAParam createParam() throws IOException
	{
		LDAParam param=super.createParam();
		param.sigma=sigma;
		param.nu=nu;
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
		SLDA lda=null;
		if (!test)
		{
			lda=new SLDA(param);
			lda.readCorpus(corpusFileName);
			lda.readLabels(labelFileName);
			lda.initialize();
			lda.sample(numIters);
			lda.writeModel(modelFileName);
		}
		else
		{
			lda=new SLDA(modelFileName, param);
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
	
	protected void writeFiles(SLDA lda) throws IOException
	{
		super.writeFiles(lda);
		if (predFileName!=null && predFileName.length()>0)
		{
			lda.writePredLabels(predFileName);
		}
		if (regFileName!=null && regFileName.length()>0)
		{
			lda.writeRegValues(regFileName);
		}
	}
	
	public void printHelp()
	{
		super.printHelp();
		println("SLDA arguments:");
		println("\tlabel [optional in test]: Label file.");
		println("\tsigma [optional]: Variance for the Gaussian generation of response variable in SLDA (default: 1.0).");
		println("\tnu [optional]: Variance of normal priors for weight vectors in SLDA and its extensions (default: 1.0).");
		println("\tpred [optional]: Predicted label file.");
		println("\treg [optional]: Regression value file");
	}
	
	public ToolSLDA(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolSLDA(Properties props)
	{
		super(props);
	}
}
