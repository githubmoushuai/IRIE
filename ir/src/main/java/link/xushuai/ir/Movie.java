package link.xushuai.ir;

public class Movie implements Comparable<Movie>
{
	private String url;
	private String article;
	private String title;
	private String property;
	public String getProperty()
	{
		return property;
	}
	public void setProperty(String property)
	{
		this.property = property;
	}
	private double sim;
	public double getSim()
	{
		return sim;
	}
	public void setSim(double sim)
	{
		this.sim = sim;
	}
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public String getArticle()
	{
		return article;
	}
	public void setArticle(String article)
	{
		this.article = article;
	}
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	@Override
	public int compareTo(Movie o)
	{
		double i = this.getSim() - o.getSim();//先按照年龄排序 
		if (i>0) return -1;
		else return 1;
	}
	
	
}
