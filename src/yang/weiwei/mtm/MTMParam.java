package yang.weiwei.mtm;

import java.io.IOException;
import java.util.Arrays;

import yang.weiwei.util.IOUtil;
import yang.weiwei.util.format.Fourmat;
import yang.weiwei.mtm.util.MTMVocab;

public class MTMParam
{
	public static final int NONE=0,L1=1,L2=2,ENTROPY=3,IDENTICAL=4;
	
	//for topic model
	/** Number of languages */
	public int numLangs=2;
	/** Parameters of document-topic distribution's Dirichlet prior (default: 0.01) */
	public double[] alpha=new double[] {0.01, 0.01};
	/** Parameters of topic-word distribution's Dirichlet prior (default: 0.01) */
	public double[] beta=new double[] {0.01, 0.01};
	/** Numbers of topics (default: 10) */
	public int[] numTopics=new int[] {10, 10};
	/** Print log on console (default: true) */
	public boolean verbose=true;
	/** The word term frequency thresholds for the word translation pair to be included */
	public int[] wordTfThreshold=new int[] {0, 0};
	/** Number of top words in each topic when printing topics */
	public int numTopWords=10;
	
	/** The coefficient of regularization (default: 0.0) */
	public double lambda=0.0;
	/** Regularization option (default: 0): 0 - None, 1 - L1, 2 - L2, 3 - entropy, 4 - identity matrix */
	public int reg=NONE;
	/** Use TF-IDF weights for word translation pairs (default: false) */
	public boolean tfidf=false;
	public boolean optWeightNorm=false;
	public boolean wordWeightNorm=false;
	public boolean topicEntropy=false;
	
	/** Update alpha (default: false) */
	public boolean updateAlpha=false;
	/** Interval of updating alpha (default: 10) */
	public int updateAlphaInterval=10;
	
	/** Vocabulary sizes */
	public int[] numVocab=new int[numLangs];
	/** Vocabulary for each language */
	public MTMVocab[] vocab=new MTMVocab[numLangs];
	
	public void printBasicParam(String prefix)
	{
		IOUtil.println(prefix+"alpha: "+Fourmat.format(alpha));
		IOUtil.println(prefix+"beta: "+Fourmat.format(beta));
		IOUtil.println(prefix+"#topics: "+Arrays.toString(numTopics));
		IOUtil.println(prefix+"#vocab: "+Arrays.toString(numVocab));
		IOUtil.println(prefix+"word TF threshold: "+Arrays.toString(wordTfThreshold));
		IOUtil.println(prefix+"verbose: "+verbose);
		IOUtil.println(prefix+"update alpha: "+updateAlpha);
		if (updateAlpha) IOUtil.println(prefix+"update alpha interval: "+updateAlphaInterval);
		IOUtil.println(prefix+"TF-IDF: "+tfidf);
		IOUtil.println(prefix+"Topic Entropy: "+topicEntropy);
		if (reg!=NONE && lambda>0.0)
		{
			switch (reg)
			{
			case L1: IOUtil.println(prefix+"reg: L1"); break;
			case L2: IOUtil.println(prefix+"reg: L2"); break;
			case ENTROPY: IOUtil.println(prefix+"reg: entropy"); break;
			case IDENTICAL: IOUtil.println(prefix+"reg: identical"); break;
			}
			IOUtil.println(prefix+"lambda: "+Fourmat.format(lambda));
		}
	}
	
	public MTMParam(int numVocab[])
	{
		for (int lang=0; lang<numLangs; lang++)
		{
			vocab[lang]=new MTMVocab(lang);
			this.numVocab[lang]=numVocab[lang];
			for (int v=0; v<numVocab[lang]; v++)
			{
				vocab[lang].addVocab(v+"");
			}
		}
	}
	
	public MTMParam(String vocabFileName[]) throws IOException
	{
		for (int lang=0; lang<numLangs; lang++)
		{
			vocab[lang]=new MTMVocab(lang);
			vocab[lang].readVocab(vocabFileName[lang]);
			numVocab[lang]=vocab[lang].vocabSize();
		}
	}
}