package yang.weiwei.preprocess;

import yang.weiwei.cfg.Cfg;

public final class PreprocessCfg
{	
	public static String lemmaDictFileName=Cfg.dictPath+"en-lemmatizer.txt";
	public static String tokenModelFileName=Cfg.dictPath+"en-token.bin";
	public static String posModelFileName=Cfg.dictPath+"en-pos-maxent.bin";
	public static String stopListFileName=Cfg.dictPath+"stoplist.txt";
}
