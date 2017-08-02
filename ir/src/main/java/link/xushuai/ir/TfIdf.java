package link.xushuai.ir;

import java.util.List;

public class TfIdf
{

	
	public double calculateTf(String[] totalterms, String termToCheck)
	{
		double count = 0; // to count the overall occurrence of the term
							// termToCheck
		for (String s : totalterms)
		{
			if (s.equalsIgnoreCase(termToCheck))
			{
				count++;
			}
		}
		return count;
	}

	public double calculateIdf(List<String[]> wordsFromDocs, String termToCheck)
	{
		double count = 0;
		for (String[] ss : wordsFromDocs)
		{
			for (String s : ss)
			{
				if (s.equalsIgnoreCase(termToCheck))
				{
					count++;
					break;
				}
			}
		}
		return Math.log(wordsFromDocs.size() / count);
	}

}
