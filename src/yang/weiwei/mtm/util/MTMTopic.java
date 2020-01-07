package yang.weiwei.mtm.util;

public class MTMTopic
{
	private final int lang;
	private int vocabCounts[];
	private int totalTokens;
	
	public MTMTopic(int language, int numVocab)
	{
		this.lang=language;
		vocabCounts=new int[numVocab];
		totalTokens=0;
	}
	
	public void addVocab(int vocab)
	{
		vocabCounts[vocab]++;
		totalTokens++;
	}
	
	public void removeVocab(int vocab)
	{
		vocabCounts[vocab]--;
		totalTokens--;
	}
	
	public int getVocabCount(int vocab)
	{
		return vocabCounts[vocab];
	}
	
	public int getTotalTokens()
	{
		return totalTokens;
	}
	
	public int getLanguage()
	{
		return lang;
	}
}