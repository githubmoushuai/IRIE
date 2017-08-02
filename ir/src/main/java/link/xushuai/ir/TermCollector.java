package link.xushuai.ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TermCollector
{
	List<String> termsFromDocs = new ArrayList<String>();
	List<String[]> wordsFromDocs = new ArrayList<String[]>();
	List<double[]> tfidfDocsVector = new ArrayList<double[]>(); // 存放每个文档的向量
	MovieList MovieList;

	public List<double[]> getTfidfDocsVector()
	{
		return tfidfDocsVector;
	}

	public List<String> getTermsFromDocs()
	{
		return termsFromDocs;
	}

	List<String> files = new ArrayList<String>();
	List<String> stopwords = Arrays.asList("imdb","title","the","a","1","full","crew","center","article","pf_rd_s","pf_rd_m","15506","a2fgeluunoqjnl","ref_","pf_rd_p","pf_rd_t","cast","com","http","url","pf_rd_r","pf_rd_i","www","nsee","of","and","to","u00bb","in","series","top","tv","2398042102","14c4z7gnpxkzbjcgvext","nyears","2398042182","toptv","0kag5s319xwhme32tz1w","u00bbseasons","director","his","stars","screenplay","u00bbstars","more","is","n1","2","as","an","with","by","u2013","all","see","on","3","story","creator","for","from","their","u2026","credits","4","who","life","n2","that","njohn","after","n3","he","credit","ndavid","creators","5","novel","her","new","young","mini","two","nmichael","are","they","6","war","world","njames","n2017","nrobert","man","one","when","unknown","at","find","up","7","into","years","n5","but","n4","friends","while","old","lives","has","police","them","nchristopher","it","show","through","out","help","8","him","about","npeter","summary","npaul","during","nwilliam","family","directors","j","other","crime","time","original","murder","day","must","ncharles","which","boy","be","over","year","finds","will","de","no","n2015","set","play","nsteven","ngeorge","u00f4","against","himself","way","have","between","adventures","based","home","nfrank","son","four","s","this","follows","its","nj","first","nandrew","only","american","people","brothers","9","most","town","back","book","team","being","each","small","former","nmartin","nstanley","10","nmatt","l");

	public void collectTerms(String filePath) throws IOException
	{

		ObjectMapper objMapper = new ObjectMapper();
		MovieList = objMapper.readValue(new File(filePath), MovieList.class);
		for (int i = 0; i < MovieList.getList().size(); i++)
		{
			String[] a = MovieList.getList().get(i).getArticle().substring(0,60).replaceAll("[\\W&&[^\\s]]", " ")
					.split("\\W+");
			String[] b = MovieList.getList().get(i).getTitle().replaceAll("[\\W&&[^\\s]]", " ")
					.split("\\W+");
			 List list = new ArrayList(Arrays.asList(a));
			    list.addAll(Arrays.asList(b));
			    String[] tokenizedTerms = new String[list.size()];
			    list.toArray(tokenizedTerms);    
			for (String term : tokenizedTerms)
			{
				term = term.toLowerCase();
				if (stopwords.contains(term))
					continue;
				if (!termsFromDocs.contains(term))
				{
					termsFromDocs.add(term);
				}
			}
			wordsFromDocs.add(tokenizedTerms); // 为每份文档保存所有有效单词（不去重复词汇）
		}

	}

	public void calculateDocWeight()
	{
		double tf; // term frequency
		double idf; // inverse document frequency
		double tfidf; // term requency inverse document frequency
		for (String[] words : wordsFromDocs)
		{
			double[] tfidfvectors = new double[termsFromDocs.size()];
			int count = 0;
			int num = 0;
			int max = 1;
			for (String term : termsFromDocs)
			{
				for (String word : words)
				{
					if (word.equalsIgnoreCase(term))
						count++;
				}
				max = max < count ? count : max;
			}
			for (String term : termsFromDocs)
			{
				tf = new TfIdf().calculateTf(words, term) / max;
				idf = new TfIdf().calculateIdf(wordsFromDocs, term);
				tfidf = tf * idf;
				tfidfvectors[num] = tfidf;
				num++;
			}
			tfidfDocsVector.add(tfidfvectors); // 存放文档向量
		}
	}

	public double[] calculateQueryWeight(String query)
	{
		String[] queryKeys = query.split(" ");
		double[] weightVector = new double[termsFromDocs.size()]; // 存放查询语句的向量
		int max = 1;
		int num = 0;
		for (String term : termsFromDocs)
		{
			int count = 0;
			for (String queryKey : queryKeys)
			{
				if (queryKey.equalsIgnoreCase(term))
					count++;
			}
			weightVector[num] = count;
			max = max < count ? count : max;
			num++;
		}
		num = 0;
		for (String term : termsFromDocs)
		{
			double idf = new TfIdf().calculateIdf(wordsFromDocs, term);
			// weightVector[num] = (weightVector[num] / max * 0.5 + 0.5) * idf;
			// //这个公式居然不好用
			weightVector[num] = weightVector[num] / max * idf;
			num++;
		}
		return weightVector;
	}

	public List<Movie> docRank(double[] weightVector)
	{
		List<Movie> list = new ArrayList<Movie>();
		double[] result = new double[wordsFromDocs.size()];
		for (int i = 0; i < wordsFromDocs.size(); i++)
		{
			result[i] = new CosineCalculator().cosineSimilarity(weightVector, tfidfDocsVector.get(i));
		}
		for (int i = 0; i < wordsFromDocs.size(); i++)
		{
			if (result[i] > 0)
			{
				Movie news = MovieList.getList().get(i);
				news.setSim(result[i]);
				list.add(news);
			}
		}
		for (Movie m : list) // 这里做信息抽取IE
		{
			String title = m.getTitle();
			String article = m.getArticle();
			String property = "";
			if (m.getUrl().indexOf("toptv&ref") != -1) // 如果是电视剧
			{
				String name = "";
				if (title.indexOf("(") != -1)
					name = title.substring(0, title.indexOf("("));
				System.out.println("1");
				String year = "";
				if (title.indexOf(")") != -1)
					year = title.substring(title.indexOf("Series") + 7, title.indexOf(")"));
				System.out.println("2");
				String stars = "";
				if (article.indexOf("|") > article.indexOf("Star") && article.indexOf("Star") != -1)
					stars = article.substring(article.indexOf("Star"), article.indexOf("|"));
				System.out.println("3");
				String creator = "";
				if (article.indexOf("Creator") != -1 && article.indexOf("Star") > article.indexOf("Creator"))
					creator = article.substring(article.indexOf("Creator"), article.indexOf("Star"));
				System.out.println("4");
				property = "Name:" + name + "</br>" + "Year:" + year + "</br>" + stars + "</br>" + creator;

			} else
			{
				String name = "";
				if (title.indexOf("(") != -1)
					name = title.substring(0, title.indexOf("("));
				System.out.println("5");
				String year = "";
				if (title.indexOf("(") != -1 && title.indexOf(")") != -1)
					year = title.substring(title.indexOf("(") + 1, title.indexOf(")"));
				System.out.println("6");
				String stars = "";
				if (article.indexOf("|") > article.indexOf("Star") && article.indexOf("Star") != -1)
				{
					System.out.println(article.indexOf("Star"));
					System.out.println(article.indexOf("|"));
					stars = article.substring(article.indexOf("Star"), article.indexOf("|"));
				}
				;
				System.out.println("7");
				String director = "";
				if (article.indexOf("Director") != -1 && article.indexOf("Writer") != -1)
					director = article.substring(article.indexOf("Director"), article.indexOf("Writer"));
				System.out.println("8");
				String writers = "";
				if (article.indexOf("Writer") != -1 && article.indexOf("Star") != -1)
					writers = article.substring(article.indexOf("Writer"), article.indexOf("Star"));
				System.out.println("9");
				property = "Name:" + name + "</br>" + "Year:" + year + "</br>" + stars + "</br>" + director + "</br>"
						+ writers;
			}
			m.setProperty(property);
		}
		Collections.sort(list);
		return list;
	}

}
