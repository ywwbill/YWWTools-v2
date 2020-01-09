package yang.weiwei.mtm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import cc.mallet.optimize.LimitedMemoryBFGS;
import cc.mallet.util.Randoms;
import yang.weiwei.mtm.util.MTMDoc;
import yang.weiwei.mtm.util.MTMTopic;
import yang.weiwei.mtm.util.MTMWord;
import yang.weiwei.util.MathUtil;
import yang.weiwei.util.format.Fourmat;
import yang.weiwei.util.IOUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.ejml.simple.SimpleMatrix;

/**
 * Multilingual Topic Model
 * @author Yang Weiwei
 *
 */
public class MTM
{	
	public static final int TRAIN=0;
	public static final int TEST=1;
	
	public final MTMParam param;
	
	public static final double epsilon=0.0;
	
	protected static Randoms randoms;
	protected static Gson gson;
	
	@Expose protected double[][] alpha;
	protected double[] updateDenom;

	protected int[] numDocs;
	protected int[] numWords;
	protected int[] numTestWords;
	protected final int type;
	protected int numWordLinks;
	protected int[][] numLangWordLinks;
	protected int[][] wordTf;
	protected int[][] wordDf;
	protected double[][] wordTfIdf;
	@Expose protected double[][][] omega;
	protected double[][][][] tOmega;
	protected double[][] wordWeights;
	
	protected ArrayList<ArrayList<ArrayList<HashSet<Integer>>>> wordLinks;
	@Expose protected double[][][][] rho;
	
	protected ArrayList<ArrayList<MTMDoc>> corpus;
	protected MTMTopic[][] topics;
	protected double[] maxTopicEntropies;
	
	protected double[][][] theta;
	@Expose protected double[][][] phi;
	
	protected double[] logLikelihood;
	protected double[] perplexity;
	
	/**
	 * Read corpora
	 * @param corpusFileNames Corpus file names of all languages
	 * @throws IOException IOException
	 */
	public void readCorpus(String corpusFileNames[]) throws IOException
	{
		for (int lang=0; lang<param.numLangs; lang++)
		{
			readCorpus(lang, corpusFileNames[lang]);
		}
	}
	
	public void readCorpus(int lang, String corpusFileName) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(corpusFileName));
		String line;
		while ((line=br.readLine())!=null)
		{
			corpus.get(lang).add(new MTMDoc(lang, line, param.numTopics[lang], param.numVocab[lang]));
		}
		br.close();
		numDocs[lang]=corpus.get(lang).size();
		double[] tf=new double[param.numVocab[lang]];
		for (int doc=0; doc<numDocs[lang]; doc++)
		{
			numWords[lang]+=corpus.get(lang).get(doc).docLength();
			double unitTf=1.0/corpus.get(lang).get(doc).docLength();
			for (int token=0; token<corpus.get(lang).get(doc).docLength(); token++)
			{
				int word=corpus.get(lang).get(doc).getWord(token);
				wordTf[lang][word]++;
				tf[word]+=unitTf;
			}
			Set<Integer> wordSet=corpus.get(lang).get(doc).getWordSet();
			for (int word : wordSet)
			{
				wordDf[lang][word]++;
			}
		}
		for (int vocab=0; vocab<param.numVocab[lang]; vocab++)
		{
			if (wordTf[lang][vocab]==0) continue;
			wordTfIdf[lang][vocab]=tf[vocab]*Math.log((double)numDocs[lang]/(double)wordDf[lang][vocab]);
		}
	}
	
	/**
	 * Read word translation dictionary
	 * @param associationFileName Word translation dictionary file name
	 * @throws IOException IOException
	 */
	public void readWordAssociations(String associationFileName) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(associationFileName));
		String line,seg[];
		while ((line=br.readLine())!=null)
		{
			seg=line.split("\t");
			int lang1=Integer.valueOf(seg[0]);
			int v1=param.vocab[lang1].getID(seg[1]);
			int lang2=Integer.valueOf(seg[2]);
			int v2=param.vocab[lang2].getID(seg[3]);
			if (v1==-1 || v2==-1 || lang1==lang2) continue;
			if (wordTf[lang1][v1]<=param.wordTfThreshold[lang1] || wordTf[lang2][v2]<=param.wordTfThreshold[lang2]) continue;
			if (wordLinks.get(lang1).get(lang2).get(v1).contains(v2)) continue;
			if (wordLinks.get(lang2).get(lang1).get(v2).contains(v1)) continue;
			numWordLinks++;
			wordLinks.get(lang1).get(lang2).get(v1).add(v2);
			wordLinks.get(lang2).get(lang1).get(v2).add(v1);
			numLangWordLinks[lang1][lang2]++;
			numLangWordLinks[lang2][lang1]++;
		}
		br.close();
	}
	
	protected void printParam()
	{
		IOUtil.println("Running "+this.getClass().getSimpleName());
		IOUtil.println("\t#docs: "+Arrays.toString(numDocs));
		IOUtil.println("\t#tokens: "+Arrays.toString(numTestWords));
		IOUtil.println("\t#word links: "+numWordLinks);
		param.printBasicParam("\t");
	}
	
	/**
	 * Initialize MTM member variables
	 */
	public void initialize()
	{
		initDocVariables();
		initTopicAssigns();
		if (type==TRAIN)
		{
			for (int lang=0; lang<param.numLangs; lang++)
			{
				computeOmega(lang);
			}
			for (int lang=0; lang<param.numLangs; lang++)
			{
				computeWordWeights(lang);
			}
			for (int lang=0; lang<param.numLangs; lang++)
			{
				updateRho(lang);
			}
			for (int lang=0; lang<param.numLangs; lang++)
			{
				computeTOmega(lang);
			}
		}
		if (param.verbose) printParam();
	}
	
	protected void initTopicAssigns()
	{
		for (int lang=0; lang<param.numLangs; lang++)
		{
			for (MTMDoc doc : corpus.get(lang))
			{
				int interval=getSampleInterval();
				for (int token=0; token<doc.docLength(); token+=interval)
				{
					int topic=randoms.nextInt(param.numTopics[lang]);
					doc.assignTopic(token, topic);
					
					int word=doc.getWord(token);
					topics[lang][topic].addVocab(word);
				}
			}
		}
	}
	
	protected void initDocVariables()
	{
		for (int lang=0; lang<param.numLangs; lang++)
		{
			updateDenom[lang]=0.0;
			for (int doc=0; doc<numDocs[lang]; doc++)
			{
				int sampleSize=getSampleSize(corpus.get(lang).get(doc).docLength());
				updateDenom[lang]+=(double)(sampleSize)/(double)(sampleSize+param.alpha[lang]*param.numTopics[lang]);
			}
			theta[lang]=new double[numDocs[lang]][param.numTopics[lang]];
			getNumTestWords(lang);
		}
	}
	
	/**
	 * Sample for given number of iterations
	 * @param numIters Number of iterations
	 * @throws IOException IOException
	 */
	public void sample(int numIters) throws IOException
	{
		for (int iteration=1; iteration<=numIters; iteration++)
		{
			for (int lang=0; lang<param.numLangs; lang++)
			{
				for (int doc=0; doc<numDocs[lang]; doc++)
				{
					sampleDoc(doc, lang, iteration>50);
				}
				computeLogLikelihood(lang);
				perplexity[lang]=Math.exp(-logLikelihood[lang]/numTestWords[lang]);
				if (param.verbose)
				{
					if (lang==0)
					{
						IOUtil.print("<"+iteration+">");
					}
					IOUtil.println("\tLang "+lang+" Log-LLD: "+format(logLikelihood[lang])+
							"\tPPX: "+format(perplexity[lang]));
				}
				if (param.updateAlpha && iteration%param.updateAlphaInterval==0 && type==TRAIN)
				{
					updateHyperParam(lang);
				}
				if (type==TRAIN)
				{
					computeOmega(lang);
					computeWordWeights(lang);
					updateRho(lang);
					computeTOmega(lang);
				}
			}
			if (param.verbose && type==TRAIN)
			{
				for (int l1=0; l1<param.numLangs; l1++)
				{
					for (int l2=0; l2<param.numLangs; l2++)
					{	
						if (l1==l2) continue;
						double avgEntropy=0.0;
						for (int t2=0; t2<param.numTopics[l2]; t2++)
						{
							double entropy=0.0;
							for (int t1=0; t1<param.numTopics[l1]; t1++)
							{
								if (rho[l1][l2][t1][t2]>0.0)
								{
									entropy-=rho[l1][l2][t1][t2]*Math.log(rho[l1][l2][t1][t2]);
								}
							}
							avgEntropy+=entropy;
						}
						IOUtil.println("\tAvg Entropy: "+avgEntropy/param.numTopics[l2]);
						
						double avgScore=0.0;
						double avgTopScore=0.0;
						for (int t1=0; t1<param.numTopics[l1]; t1++)
						{
							double max=Double.MIN_VALUE;
							for (int t2=0; t2<param.numTopics[l2]; t2++)
							{
								avgScore+=rho[l1][l2][t1][t2];
								if (rho[l1][l2][t1][t2]>max)
								{
									max=rho[l1][l2][t1][t2];
								}
							}
							avgTopScore+=max;
						}
						avgScore/=(double)param.numTopics[l1]*param.numTopics[l2];
						avgTopScore/=(double)param.numTopics[l1];
						IOUtil.println("\tAvg Link Score of Languages "+l1+" and "+l2+": "+avgScore);
						IOUtil.println("\tAvg Top Link Score of Languages "+l1+" and "+l2+": "+avgTopScore);
						IOUtil.println("\tRatio of Top to All of Languages "+l1+" and "+l2+": "+Math.abs(avgTopScore/avgScore));
					}
				}
			}
		}
		
		if (type==TRAIN && param.verbose)
		{
			for (int lang=0; lang<param.numLangs; lang++)
			{
				for (int topic=0; topic<param.numTopics[lang]; topic++)
				{
					IOUtil.println(topWordsByFreq(lang, topic, param.numTopWords));
				}
			}
		}
	}
	
	protected void sampleDoc(int doc, int lang, boolean includeTopicLink)
	{
		int oldTopic,newTopic,interval=getSampleInterval();
		for (int token=0; token<corpus.get(lang).get(doc).docLength(); token+=interval)
		{			
			oldTopic=unassignTopic(lang, doc, token);
			newTopic=sampleTopic(lang, doc, token, oldTopic, includeTopicLink);
			assignTopic(lang, doc, token, newTopic);
		}
	}
	
	protected int unassignTopic(int lang, int doc, int token)
	{
		int oldTopic=corpus.get(lang).get(doc).getTopicAssign(token);
		int word=corpus.get(lang).get(doc).getWord(token);
		corpus.get(lang).get(doc).unassignTopic(token);
		topics[lang][oldTopic].removeVocab(word);
		return oldTopic;
	}
	
	protected int sampleTopic(int lang, int doc, int token, int oldTopic, boolean includeTopicLink)
	{
		int word=corpus.get(lang).get(doc).getWord(token);
		double topicScores[]=new double[param.numTopics[lang]];
		for (int topic=0; topic<param.numTopics[lang]; topic++)
		{
			topicScores[topic]=topicUpdating(lang, doc, topic, word, includeTopicLink);
		}
		
		int newTopic=MathUtil.selectLogDiscrete(topicScores);
		if (newTopic==-1)
		{
			newTopic=oldTopic;
			IOUtil.println(format(topicScores));
		}
		
		return newTopic;
	}
	
	protected void assignTopic(int lang, int doc, int token, int newTopic)
	{
		int word=corpus.get(lang).get(doc).getWord(token);
		corpus.get(lang).get(doc).assignTopic(token, newTopic);
		topics[lang][newTopic].addVocab(word);
	}
	
	protected double topicUpdating(int lang, int doc, int topic, int vocab, boolean includeTopicLink)
	{
		if (type==TRAIN)
		{
			double score=Math.log((alpha[lang][topic]+corpus.get(lang).get(doc).getTopicCount(topic))*
					(param.beta[lang]+topics[lang][topic].getVocabCount(vocab))/
					(param.beta[lang]*param.numVocab[lang]+topics[lang][topic].getTotalTokens()));
			double[][] tempOmega=new double[param.numTopics[lang]][1];
			double tempEntropy=0.0;
			for (int t=0; t<param.numTopics[lang]; t++)
			{
				if (t==topic)
				{
					tempOmega[t][0]=(topics[lang][t].getVocabCount(vocab)+1.0+epsilon)/
							(wordTf[lang][vocab]+param.numTopics[lang]*epsilon);
					tempEntropy-=tempOmega[t][0]*Math.log(tempOmega[t][0]);
				}
				else
				{
					tempOmega[t][0]=(topics[lang][t].getVocabCount(vocab)+epsilon)/
							(wordTf[lang][vocab]+param.numTopics[lang]*epsilon);
					if (topics[lang][t].getVocabCount(vocab)>0)
					{
						tempEntropy-=tempOmega[t][0]*Math.log(tempOmega[t][0]);
					}
				}
			}
			double tempWeight=1.0;
			if (param.tfidf) tempWeight*=wordTfIdf[lang][vocab];
			if (param.topicEntropy) tempWeight*=(maxTopicEntropies[lang]-tempEntropy);
			SimpleMatrix tempOmegaMatrix=new SimpleMatrix(tempOmega);
			for (int l=0; l<param.numLangs; l++)
			{
				if (l==lang) continue;
				SimpleMatrix rhoMatrix=new SimpleMatrix(rho[l][lang]);
				SimpleMatrix tempTOmegaMatrix=rhoMatrix.mult(tempOmegaMatrix);
				double[] tempTOmega=new double[param.numTopics[l]];
				double sum=0.0;
				for (int t=0; t<param.numTopics[l]; t++)
				{
					tempTOmega[t]=tempTOmegaMatrix.get(t, 0);
					sum+=tempTOmega[t];
				}
				if (Math.abs(sum-1.0)>1e-3)
				{
					for (int t=0; t<param.numTopics[l]; t++)
					{
						tempTOmega[t]/=sum;
					}
				}
				for (int v : wordLinks.get(lang).get(l).get(vocab))
				{
					double dis=0.0;
					for (int t=0; t<param.numTopics[l]; t++)
					{
						dis+=(omega[l][v][t]-tempTOmega[t])*(omega[l][v][t]-tempTOmega[t]);
					}
					score-=0.5*Math.min(tempWeight, wordWeights[l][v])*Math.log(dis);
				}
			}
			for (int l=0; l<param.numLangs; l++)
			{
				if (l==lang) continue;
				for (int v : wordLinks.get(lang).get(l).get(vocab))
				{
					double dis=0.0;
					for (int t=0; t<param.numTopics[lang]; t++)
					{
						dis+=(tempOmega[t][0]-tOmega[lang][l][v][t])*(tempOmega[t][0]-tOmega[lang][l][v][t]);
					}
					score-=0.5*Math.min(tempWeight, wordWeights[l][v])*Math.log(dis);
				}
			}
			return score;
		}
		
		return Math.log((alpha[lang][topic]+corpus.get(lang).get(doc).getTopicCount(topic))*phi[lang][topic][vocab]);
	}
	
	protected double sigmoid(double x)
	{
		return 1.0/(1.0+Math.exp(-x));
	}
	
	protected void updateHyperParam(int lang)
	{
		double oldAlpha[]=new double[param.numTopics[lang]];
		for (int topic=0; topic<param.numTopics[lang]; topic++)
		{
			oldAlpha[topic]=alpha[lang][topic];
		}
		
		double numer;
		for (int topic=0; topic<param.numTopics[lang]; topic++)
		{
			numer=0.0;
			for (MTMDoc doc : corpus.get(lang))
			{
				numer+=(double)(doc.getTopicCount(topic))/(double)(doc.getTopicCount(topic)+oldAlpha[topic]);
			}
			alpha[lang][topic]=oldAlpha[topic]*numer/updateDenom[lang];
		}
		
		double newAlphaSum=0.0;
		for (int topic=0; topic<param.numTopics[lang]; topic++)
		{
			newAlphaSum+=alpha[lang][topic];
		}
		for (int topic=0; topic<param.numTopics[lang]; topic++)
		{
			alpha[lang][topic]*=param.alpha[lang]*param.numTopics[lang]/newAlphaSum;
		}
	}
	
	protected void updateRho(int lang)
	{
		for (int l=0; l<param.numLangs; l++)
		{
			if (l==lang) continue;
			double[][] omegaS=new double[numLangWordLinks[lang][l]][param.numTopics[lang]];
			double[][] omegaT=new double[numLangWordLinks[lang][l]][param.numTopics[l]];
			double[] weights=new double[numLangWordLinks[l][lang]];
			int i=0;
			double sum=0.0;
			for (int vs=0; vs<param.numVocab[lang]; vs++)
			{
				for (int vt : wordLinks.get(lang).get(l).get(vs))
				{
					for (int t1=0; t1<param.numTopics[lang]; t1++)
					{
						omegaS[i][t1]=omega[lang][vs][t1];
					}
					for (int t2=0; t2<param.numTopics[l]; t2++)
					{
						omegaT[i][t2]=omega[l][vt][t2];
					}
					weights[i]=Math.min(wordWeights[lang][vs], wordWeights[l][vt]);
					sum+=weights[i];
					i++;
				}
			}
			if (param.optWeightNorm)
			{
				for (int k=0; k<weights.length; k++)
				{
					weights[k]=weights[k]/sum*weights.length;
				}
			}
			
			MTMFunction optimizable1=new MTMFunction(omegaS, omegaT, weights, rho[lang][l], rho[l][lang], param);
			LimitedMemoryBFGS lbfgs1=new LimitedMemoryBFGS(optimizable1);
			try
			{
				lbfgs1.optimize();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				continue;
			}
			for (int t1=0; t1<param.numTopics[lang]; t1++)
			{
				for (int t2=0; t2<param.numTopics[l]; t2++)
				{
					rho[lang][l][t1][t2]=optimizable1.getParameter(t1*param.numTopics[l]+t2);
				}
			}
			
			MTMFunction optimizable2=new MTMFunction(omegaT, omegaS, weights, rho[l][lang], rho[lang][l], param);
			LimitedMemoryBFGS lbfgs2=new LimitedMemoryBFGS(optimizable2);
			try
			{
				lbfgs2.optimize();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				continue;
			}
			for (int t2=0; t2<param.numTopics[l]; t2++)
			{
				for (int t1=0; t1<param.numTopics[lang]; t1++)
				{
					rho[l][lang][t2][t1]=optimizable2.getParameter(t2*param.numTopics[lang]+t1);
				}
			}
		}
	}
	
	protected void computeLogLikelihood(int lang)
	{
		computeTheta(lang);
		if (type==TRAIN)
		{
			computePhi(lang);
		}
		
		int word;
		double sum;
		logLikelihood[lang]=0.0;
		for (int doc=0; doc<numDocs[lang]; doc++)
		{
			int startPos=getStartPos();
			int interval=getSampleInterval();
			for (int token=startPos; token<corpus.get(lang).get(doc).docLength(); token+=interval)
			{
				word=corpus.get(lang).get(doc).getWord(token);
				sum=0.0;
				for (int topic=0; topic<param.numTopics[lang]; topic++)
				{
					sum+=theta[lang][doc][topic]*phi[lang][topic][word];
				}
				logLikelihood[lang]+=Math.log(sum);
			}
		}
	}
	
	protected void computeTheta(int lang)
	{
		for (int doc=0; doc<numDocs[lang]; doc++)
		{
			for (int topic=0; topic<param.numTopics[lang]; topic++)
			{
				theta[lang][doc][topic]=(alpha[lang][topic]+corpus.get(lang).get(doc).getTopicCount(topic))/
						(param.alpha[lang]*param.numTopics[lang]+getSampleSize(corpus.get(lang).get(doc).docLength()));
			}
		}
	}
	
	protected void computePhi(int lang)
	{
		for (int topic=0; topic<param.numTopics[lang]; topic++)
		{
			for (int vocab=0; vocab<param.numVocab[lang]; vocab++)
			{
				phi[lang][topic][vocab]=(param.beta[lang]+topics[lang][topic].getVocabCount(vocab))/
						(param.beta[lang]*param.numVocab[lang]+topics[lang][topic].getTotalTokens());
			}
		}
	}
	
	protected void computeOmega(int lang)
	{
		for (int vocab=0; vocab<param.numVocab[lang]; vocab++)
		{
			if (wordTf[lang][vocab]==0) continue;
			for (int topic=0; topic<param.numTopics[lang]; topic++)
			{
				omega[lang][vocab][topic]=((double)topics[lang][topic].getVocabCount(vocab)+epsilon)/
						((double)wordTf[lang][vocab]+param.numTopics[lang]*epsilon);
			}
		}
	}
	
	protected void computeWordWeights(int lang)
	{
		Arrays.fill(wordWeights[lang], 1.0);
		double sum=0.0;
		for (int vocab=0; vocab<param.numVocab[lang]; vocab++)
		{
			if (wordTf[lang][vocab]==0)
			{
				wordWeights[lang][vocab]=0.0;
				continue;
			}
			if (param.tfidf) wordWeights[lang][vocab]*=wordTfIdf[lang][vocab];
			if (param.topicEntropy)
			{
				double entropy=0.0;
				for (int topic=0; topic<param.numTopics[lang]; topic++)
				{
					if (topics[lang][topic].getVocabCount(vocab)==0) continue;
					entropy-=omega[lang][vocab][topic]*Math.log(omega[lang][vocab][topic]);
				}
				wordWeights[lang][vocab]*=(maxTopicEntropies[lang]-entropy);
			}
			sum+=wordWeights[lang][vocab];
		}
		if (param.wordWeightNorm)
		{
			for (int vocab=0; vocab<param.numVocab[lang]; vocab++)
			{
				if (wordTf[lang][vocab]==0) continue;
				wordWeights[lang][vocab]=wordWeights[lang][vocab]/sum*10000;
			}
		}
	}
	
	protected void computeTOmega(int lang)
	{
		for (int l=0; l<param.numLangs; l++)
		{
			if (l==lang) continue;
			SimpleMatrix rhoMatrix=new SimpleMatrix(rho[l][lang]);
			SimpleMatrix omegaMatrix=new SimpleMatrix(omega[lang]);
			SimpleMatrix tOmegaMatrix=rhoMatrix.mult(omegaMatrix.transpose()).transpose();
			for (int v=0; v<param.numVocab[lang]; v++)
			{
				double sum=0.0;
				for (int t=0; t<param.numTopics[l]; t++)
				{
					tOmega[l][lang][v][t]=tOmegaMatrix.get(v, t);
					sum+=tOmega[l][lang][v][t];
				}
				if (wordTf[lang][v]>0 && Math.abs(sum-1.0)>1e-3)
				{
					for (int t=0; t<param.numTopics[l]; t++)
					{
						tOmega[l][lang][v][t]/=sum;
					}
				}
			}
		}
	}
	
	protected void getNumTestWords(int lang)
	{
		numTestWords[lang]=numWords[lang];
	}
	
	protected int getStartPos()
	{
		return 0;
	}
	
	protected int getSampleSize(int docLength)
	{
		return docLength;
	}
	
	protected int getSampleInterval()
	{
		return 1;
	}
	
	/**
	 * Get document distribution over topics
	 * @param lang Language ID
	 * @return Document distribution over topics
	 */
	public double[][] getDocTopicDist(int lang)
	{
		return theta[lang].clone();
	}
	
	/**
	 * Get document distributions over topics
	 * @return Document distributions over topics
	 */
	public double[][][] getDocTopicDist()
	{
		return theta.clone();
	}
	
	/**
	 * Get topic distribution over words
	 * @param lang Language ID
	 * @return Topic distribution over words
	 */
	public double[][] getTopicVocabDist(int lang)
	{
		return phi[lang].clone();
	}
	
	/**
	 * Get topic distributions over words
	 * @return Topic distributions over words
	 */
	public double[][][] getTopicVocabDist()
	{
		return phi.clone();
	}
	
	/**
	 * Get number of documents
	 * @param lang Language ID
	 * @return Number of documents
	 */
	public int getNumDocs(int lang)
	{
		return numDocs[lang];
	}
	
	/**
	 * Get number of tokens in the corpus
	 * @param lang Language ID
	 * @return Number of tokens
	 */
	public int getNumWords(int lang)
	{
		return numWords[lang];
	}
	
	/**
	 * Get a specific document
	 * @param lang Language ID
	 * @param doc Document ID
	 * @return Corresponding document object
	 */
	public MTMDoc getDoc(int lang, int doc)
	{
		return corpus.get(lang).get(doc);
	}
	
	/**
	 * Get a specific topic
	 * @param lang Language ID
	 * @param topic Topic number
	 * @return Corresponding topic object
	 */
	public MTMTopic getTopic(int lang, int topic)
	{
		return topics[lang][topic];
	}
	
	/**
	 * Get log likelihood
	 * @param lang Language ID
	 * @return Log likelihood
	 */
	public double getLogLikelihood(int lang)
	{
		return logLikelihood[lang];
	}
	
	/**
	 * Get perplexity
	 * @param lang Language ID
	 * @return Perplexity
	 */
	public double getPerplexity(int lang)
	{
		return perplexity[lang];
	}
	
	/**
	 * Get topic link weights of two languages
	 * @param lang1 Language ID 1
	 * @param lang2 Language ID 2
	 * @return The topic link weights between the two languages
	 */
	public double[][] getTopicLinkValue(int lang1, int lang2)
	{
		if (lang1==lang2) return null;
		return rho[lang1][lang2].clone();
	}
	
	public double[][] getTopicLinkWeight(int lang1, int lang2)
	{
		if (lang1==lang2) return null;
		return rho[lang1][lang2].clone();
	}
	
	/**
	 * Get documents' number of tokens assigned to every topic
	 * @param lang Language ID
	 * @return Documents' number of tokens assigned to every topic
	 */
	public int[][] getDocTopicCounts(int lang)
	{
		int docTopicCounts[][]=new int[numDocs[lang]][param.numTopics[lang]];
		for (int doc=0; doc<numDocs[lang]; doc++)
		{
			for (int topic=0; topic<param.numTopics[lang]; topic++)
			{
				docTopicCounts[doc][topic]=corpus.get(lang).get(doc).getTopicCount(topic);
			}
		}
		return docTopicCounts;
	}
	
	/**
	 * Get tokens' topic assignments
	 * @param lang Language ID
	 * @return Tokens' topic assignments
	 */
	public int[][] getTokenTopicAssign(int lang)
	{
		int tokenTopicAssign[][]=new int[numDocs[lang]][];
		for (int doc=0; doc<numDocs[lang]; doc++)
		{
			tokenTopicAssign[doc]=new int[corpus.get(lang).get(doc).docLength()];
			for (int token=0; token<corpus.get(lang).get(doc).docLength(); token++)
			{
				tokenTopicAssign[doc][token]=corpus.get(lang).get(doc).getTopicAssign(token);
			}
		}
		return tokenTopicAssign;
	}
	
	/**
	 * Get a topic's top words (with highest number of assignments)
	 * @param lang Language ID
	 * @param topic Topic number
	 * @param numTopWords Number of top words
	 * @return Given topic's top words
	 */
	public String topWordsByFreq(int lang, int topic, int numTopWords)
	{
		String result="Language "+lang+" Topic "+topic+":";
		MTMWord words[]=new MTMWord[param.numVocab[lang]];
		for (int vocab=0; vocab<param.numVocab[lang]; vocab++)
		{
			words[vocab]=new MTMWord(param.vocab[lang].getVocab(vocab), topics[lang][topic].getVocabCount(vocab));
		}
		
		Arrays.sort(words);
		for (int i=0; i<numTopWords; i++)
		{
			result+="   "+words[i];
		}
		return result;
	}
	
	/**
	 * Get a topic's top words (with highest weight)
	 * @param lang Language ID
	 * @param topic Topic number
	 * @param numTopWords Number of top words
	 * @return Given topic's top words
	 */
	public String topWordsByWeight(int lang, int topic, int numTopWords)
	{
		String result="Language "+lang+" Topic "+topic+":";
		MTMWord words[]=new MTMWord[param.numVocab[lang]];
		for (int vocab=0; vocab<param.numVocab[lang]; vocab++)
		{
			words[vocab]=new MTMWord(param.vocab[lang].getVocab(vocab), phi[lang][topic][vocab]);
		}
		
		Arrays.sort(words);
		for (int i=0; i<numTopWords; i++)
		{
			result+="   "+words[i];
		}
		return result;
	}
	
	public String[] getTopWordsByWeight(int lang, int topic, int numTopWords)
	{
		MTMWord words[]=new MTMWord[param.numVocab[lang]];
		for (int vocab=0; vocab<param.numVocab[lang]; vocab++)
		{
			words[vocab]=new MTMWord(param.vocab[lang].getVocab(vocab), phi[lang][topic][vocab]);
		}
		
		Arrays.sort(words);
		String result[]=new String[numTopWords];
		for (int i=0; i<numTopWords; i++)
		{
			result[i]=words[i].getWord();
		}
		return result;
	}
	
	public void visualizeTopics(int numTopWords, int numTopTopics)
	{
		String[][][] topWords=new String[param.numLangs][][];
		for (int lang=0; lang<param.numLangs; lang++)
		{
			topWords[lang]=new String[param.numTopics[lang]][];
			for (int topic=0; topic<param.numTopics[lang]; topic++)
			{
				topWords[lang][topic]=getTopWordsByWeight(lang, topic, numTopWords);
			}
		}
		
		HashSet<Integer> selected=new HashSet<Integer>();
		for (int l1=0; l1<param.numLangs; l1++)
		{
			for (int l2=0; l2<param.numLangs; l2++)
			{
				if (l1==l2) continue;
				for (int t2=0; t2<param.numTopics[l2]; t2++)
				{
					IOUtil.print(t2);
					for (int i=0; i<numTopWords; i++)
					{
						IOUtil.print("\t"+topWords[l2][t2][i]);
					}
					IOUtil.println();
					IOUtil.println();
					
					selected.clear();
					for (int k=0; k<numTopTopics; k++)
					{
						double max=Double.NEGATIVE_INFINITY;
						int maxTopic=-1;
						for (int t1=0; t1<param.numTopics[l1]; t1++)
						{
							if (!selected.contains(t1) && rho[l1][l2][t1][t2]>max)
							{
								max=rho[l1][l2][t1][t2];
								maxTopic=t1;
							}
						}
						if (maxTopic!=-1)
						{
							selected.add(maxTopic);
							IOUtil.print("\t"+rho[l1][l2][maxTopic][t2]+"\t"+maxTopic);
							for (int i=0; i<numTopWords; i++)
							{
								IOUtil.print("\t"+topWords[l1][maxTopic][i]);
							}
							IOUtil.println();
							IOUtil.println();
						}
					}
				}
			}
		}
	}
	
	/**
	 * Write topics' top words to file
	 * @param resultFileName Result file name
	 * @param numTopWords Number of top words
	 * @throws IOException IOException
	 */
	public void writeResult(String resultFileName, int numTopWords) throws IOException
	{
		BufferedWriter bw=new BufferedWriter(new FileWriter(resultFileName));
		for (int lang=0; lang<param.numLangs; lang++)
		{
			for (int topic=0; topic<param.numTopics[lang]; topic++)
			{
				bw.write(topWordsByFreq(lang, topic, numTopWords));
				bw.newLine();
			}
		}
		bw.close();
	}
	
	/**
	 * Write document distributions over topics to file
	 * @param docTopicDistFileNames Distribution file names
	 * @throws IOException IOException
	 */
	public void writeDocTopicDist(String docTopicDistFileNames[]) throws IOException
	{
		for (int lang=0; lang<param.numLangs; lang++)
		{
			writeDocTopicDist(lang, docTopicDistFileNames[lang]);
		}
	}
	
	/**
	 * Write document distribution over topics to file
	 * @param lang Language ID
	 * @param docTopicDistFileName Distribution file name
	 * @throws IOException IOException
	 */
	public void writeDocTopicDist(int lang, String docTopicDistFileName) throws IOException
	{
		BufferedWriter bw=new BufferedWriter(new FileWriter(docTopicDistFileName));
		IOUtil.writeMatrix(bw, theta[lang]);
		bw.close();
	}
	
	/**
	 * Write documents' number of tokens assigned to topics to file
	 * @param topicCountFileNames Documents' topic count file names
	 * @throws IOException IOException
	 */
	public void writeDocTopicCounts(String topicCountFileNames[]) throws IOException
	{
		for (int lang=0; lang<param.numLangs; lang++)
		{
			writeDocTopicCounts(lang, topicCountFileNames[lang]);
		}
	}
	
	/**
	 * Write documents' number of tokens assigned to topics to file
	 * @param lang Language ID
	 * @param topicCountFileName Documents' topic count file name
	 * @throws IOException IOException
	 */
	public void writeDocTopicCounts(int lang, String topicCountFileName) throws IOException
	{
		BufferedWriter bw=new BufferedWriter(new FileWriter(topicCountFileName));
		IOUtil.writeMatrix(bw, getDocTopicCounts(lang));
		bw.close();
	}
	
	/**
	 * Write tokens' topic assignments to files
	 * @param topicAssignFileNames Topic assignment file names
	 * @throws IOException IOException
	 */
	public void writeTokenTopicAssign(String topicAssignFileNames[]) throws IOException
	{
		for (int lang=0; lang<param.numLangs; lang++)
		{
			writeTokenTopicAssign(lang, topicAssignFileNames[lang]);
		}
	}
	
	/**
	 * Write tokens' topic assignments to file
	 * @param lang Language ID
	 * @param topicAssignFileName Topic assignment file name
	 * @throws IOException IOException
	 */
	public void writeTokenTopicAssign(int lang, String topicAssignFileName) throws IOException
	{
		BufferedWriter bw=new BufferedWriter(new FileWriter(topicAssignFileName));
		IOUtil.writeMatrix(bw, getTokenTopicAssign(lang));
		bw.close();
	}
	
	/**
	 * Write model to file
	 * @param modelFileName Model file name
	 * @throws IOException IOException
	 */
	public void writeModel(String modelFileName) throws IOException
	{
		BufferedWriter bw=new BufferedWriter(new FileWriter(modelFileName));
		bw.write(gson.toJson(this));
		bw.close();
	}
	
	protected void initVariables()
	{
		corpus=new ArrayList<ArrayList<MTMDoc>>();
		topics=new MTMTopic[param.numLangs][];
		alpha=new double[param.numLangs][];
		theta=new double[param.numLangs][][];
		phi=new double[param.numLangs][][];
		numDocs=new int[param.numLangs];
		updateDenom=new double[param.numLangs];
		numWords=new int[param.numLangs];
		wordTf=new int[param.numLangs][];
		wordDf=new int[param.numLangs][];
		wordTfIdf=new double[param.numLangs][];
		wordWeights=new double[param.numLangs][];
		numTestWords=new int[param.numLangs];
		logLikelihood=new double[param.numLangs];
		perplexity=new double[param.numLangs];
		omega=new double[param.numLangs][][];
		for (int lang=0; lang<param.numLangs; lang++)
		{
			corpus.add(new ArrayList<MTMDoc>());
			topics[lang]=new MTMTopic[param.numTopics[lang]];
			alpha[lang]=new double[param.numTopics[lang]];
			phi[lang]=new double[param.numTopics[lang]][param.numVocab[lang]];
			for (int topic=0; topic<param.numTopics[lang]; topic++)
			{
				topics[lang][topic]=new MTMTopic(lang, param.numVocab[lang]);
			}
			wordTf[lang]=new int[param.numVocab[lang]];
			wordDf[lang]=new int[param.numVocab[lang]];
			wordTfIdf[lang]=new double[param.numVocab[lang]];
			wordWeights[lang]=new double[param.numVocab[lang]];
			omega[lang]=new double[param.numVocab[lang]][param.numTopics[lang]];
		}
		
		numLangWordLinks=new int[param.numLangs][param.numLangs];
		wordLinks=new ArrayList<ArrayList<ArrayList<HashSet<Integer>>>>();
		for (int l1=0; l1<param.numLangs; l1++)
		{
			wordLinks.add(new ArrayList<ArrayList<HashSet<Integer>>>());
			for (int l2=0; l2<param.numLangs; l2++)
			{
				wordLinks.get(l1).add(new ArrayList<HashSet<Integer>>());
				for (int v1=0; v1<param.numVocab[l1]; v1++)
				{
					wordLinks.get(l1).get(l2).add(new HashSet<Integer>());
				}
			}
		}
		
		rho=new double[param.numLangs][param.numLangs][][];
		tOmega=new double[param.numLangs][param.numLangs][][];
		for (int l1=0; l1<param.numLangs; l1++)
		{
			for (int l2=0; l2<param.numLangs; l2++)
			{
				if (l1==l2) continue;
				rho[l1][l2]=new double[param.numTopics[l1]][param.numTopics[l2]];
				tOmega[l1][l2]=new double[param.numVocab[l2]][param.numTopics[l1]];
				for (int t2=0; t2<param.numTopics[l2]; t2++)
				{
					double sum=0.0;
					for (int t1=0; t1<param.numTopics[l1]; t1++)
					{
						rho[l1][l2][t1][t2]=randoms.nextUniform();
						sum+=rho[l1][l2][t1][t2];
					}
					for (int t1=0; t1<param.numTopics[l1]; t1++)
					{
						rho[l1][l2][t1][t2]/=sum;
					}
				}
			}
		}
		
		maxTopicEntropies=new double[param.numLangs];
		for (int lang=0; lang<param.numLangs; lang++)
		{
			maxTopicEntropies[lang]=-Math.log(param.numTopics[lang]);
		}
	}
	
	protected void copyModel(MTM MTMModel)
	{
		alpha=MTMModel.alpha.clone();
		phi=MTMModel.phi.clone();
		rho=MTMModel.rho.clone();
		omega=MTMModel.omega.clone();
	}
	
	protected static String format(double num)
	{
		return Fourmat.format(num);
	}
	
	protected static String format(double nums[])
	{
		return Fourmat.format(nums);
	}
	
	static
	{
		randoms=new Randoms();
		gson=new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	}
	
	/**
	 * Initialize an MTM object for training
	 * @param parameters Parameters
	 */
	public MTM(MTMParam parameters)
	{
		this.type=TRAIN;
		this.param=parameters;
		initVariables();
		
		for (int lang=0; lang<param.numLangs; lang++)
		{
			for (int topic=0; topic<param.numTopics[lang]; topic++)
			{
				alpha[lang][topic]=param.alpha[lang];
			}
		}
	}
	
	/**
	 * Initialize an MTM object for test using a pre-trained MTM object
	 * @param MTMTrain Pre-trained MTM object
	 * @param parameters Parameters
	 */
	public MTM(MTM MTMTrain, MTMParam parameters)
	{
		this.type=TEST;
		this.param=parameters;
		initVariables();
		copyModel(MTMTrain);
	}
	
	/**
	 * Initialize an MTM object for test using a pre-trained MTM model in file
	 * @param modelFileName Model file name
	 * @param parameters Parameters
	 * @throws IOException IOException
	 */
	public MTM(String modelFileName, MTMParam parameters) throws IOException
	{
		MTM MTMTrain=gson.fromJson(new FileReader(modelFileName), this.getClass());
		this.type=TEST;
		this.param=parameters;
		initVariables();
		copyModel(MTMTrain);
	}
	
	public static void main(String[] args) throws IOException
	{
		MTMParam param=new MTMParam(MTMCfg.vocabFileNames);
		
		MTM mtmTrain=new MTM(param);
		mtmTrain.readCorpus(MTMCfg.trainCorporaFileNames);
		mtmTrain.readWordAssociations(MTMCfg.dictFileName);
		mtmTrain.initialize();
		mtmTrain.sample(MTMCfg.numTrainIters);
//		mtmTrain.writeModel(MTMCfg.modelFileName);
		
		MTM mtmTest=new MTM(mtmTrain, param);
//		MTM mtmTrain=new MTM(MTMCfg.modelFileName, param);
		mtmTest.readCorpus(MTMCfg.testCorporaFileNames);
		mtmTest.initialize();
		mtmTest.sample(MTMCfg.numTestIters);
	}
}