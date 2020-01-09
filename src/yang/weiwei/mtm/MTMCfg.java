package yang.weiwei.mtm;

import java.io.File;

import yang.weiwei.cfg.Cfg;

public class MTMCfg
{
	private static String dataPath=Cfg.dataPath+"mtm"+File.separator;
	private static String outputPath=Cfg.outputPath+"mtm"+File.separator;
	
	public static String dictFileName=dataPath+"dict";
	public static String[] vocabFileNames=new String[]
			{dataPath+"vocab-en", dataPath+"vocab-zh"};
	public static String[] trainCorporaFileNames=new String[]
			{dataPath+"corpus-train-en", dataPath+"corpus-train-zh"};
	public static String[] testCorporaFileNames=new String[]
			{dataPath+"corpus-test-en", dataPath+"corpus-test-zh"};
	
	public static int numTrainIters=20;
	public static int numTestIters=20;
	
	public static String modelFileName=outputPath+"mtm-model";
}
