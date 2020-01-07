package yang.weiwei.tools;

import java.util.HashSet;
import java.util.Properties;
import java.io.IOException;

import yang.weiwei.tools.lda.ToolLDA;
import yang.weiwei.tools.lda.ToolSTLDA;
import yang.weiwei.tools.lda.ToolBPLDA;
import yang.weiwei.tools.lda.ToolWSBTM;
import yang.weiwei.tools.lda.ToolRTM;
import yang.weiwei.tools.lda.ToolLexWSBRTM;
import yang.weiwei.tools.lda.ToolLexWSBMedRTM;
import yang.weiwei.tools.lda.ToolSLDA;
import yang.weiwei.tools.lda.ToolBSLDA;
import yang.weiwei.tools.lda.ToolLexWSBBSLDA;
import yang.weiwei.tools.lda.ToolLexWSBMedLDA;

public class ToolLDAInterface extends ToolInterface
{
	protected static HashSet<String> ldaNames;
	
	protected String model;
	
	public void parseCommand()
	{
		model=props.getProperty("model").toLowerCase();
	}

	protected boolean checkCommand()
	{
		if (model==null || model.length()==0)
		{
			model="lda";
		}
		
		if (!ldaNames.contains(model))
		{
			println("Model is not supported.");
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
		
		ToolLDAInterface toolLDA;
		switch (model)
		{
		case "wsb-tm": toolLDA=new ToolWSBTM(props); break;
		case "bp-lda": toolLDA=new ToolBPLDA(props); break;
		case "st-lda": toolLDA=new ToolSTLDA(props); break;
		case "rtm": toolLDA=new ToolRTM(props); break;
		case "lex-wsb-rtm": toolLDA=new ToolLexWSBRTM(props); break;
		case "lex-wsb-med-rtm": toolLDA=new ToolLexWSBMedRTM(props); break;
		case "slda": toolLDA=new ToolSLDA(props); break;
		case "bs-lda": toolLDA=new ToolBSLDA(props); break;
		case "lex-wsb-bs-lda": toolLDA=new ToolLexWSBBSLDA(props); break;
		case "lex-wsb-med-lda": toolLDA=new ToolLexWSBMedLDA(props); break;
		default: toolLDA=new ToolLDA(props); break;
		}
		toolLDA.parseCommand();
		toolLDA.execute();
	}

	public void printHelp()
	{
		println("Arguments for LDA:");
		println("Basic arguments:");
		println("\thelp [optional]: Print help information.");
		println("\tmodel [optional]: The topic model you want to use (default: LDA). Supported models are");
		println("\t\tLDA: Vanilla LDA");
		println("\t\tBP-LDA: LDA with block priors. Blocks are pre-computed.");
		println("\t\tST-LDA: Single topic LDA. Each document can only be assigned to one topic.");
		println("\t\tWSB-TM: LDA with block priors. Blocks are computed by WSBM.");
		println("\t\tRTM: Relational topic model.");
		println("\t\t\tLex-WSB-RTM: RTM with WSB-computed block priors and lexical weights.");
		println("\t\t\tLex-WSB-Med-RTM: Lex-WSB-RTM with hinge loss.");
		println("\t\tSLDA: Supervised LDA. Support multi-class classification.");
		println("\t\t\tBS-LDA: Binary SLDA.");
		println("\t\t\tLex-WSB-BS-LDA: BS-LDA with WSB-computed block priors and lexical weights.");
		println("\t\t\tLex-WSB-Med-LDA: Lex-WSB-BS-LDA with hinge loss.");
	}
	
	static
	{
		ldaNames=new HashSet<String>();
		ldaNames.add("lda");
		ldaNames.add("bp-lda");
		ldaNames.add("st-lda");
		ldaNames.add("wsb-tm");
		ldaNames.add("rtm");
		ldaNames.add("lex-wsb-rtm");
		ldaNames.add("lex-wsb-med-rtm");
		ldaNames.add("slda");
		ldaNames.add("bs-lda");
		ldaNames.add("lex-wsb-bs-lda");
		ldaNames.add("lex-wsb-med-lda");
	}
	
	public ToolLDAInterface(String cfgFileName) throws IOException
	{
		super(cfgFileName);
	}
	
	public ToolLDAInterface(Properties props)
	{
		super(props);
	}
}
