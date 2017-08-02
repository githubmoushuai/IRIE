package link.xushuai.ir;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class restApi
{
	TermCollector tc = new TermCollector();
	public restApi() throws IOException
	{
		tc.collectTerms("./resource/b.json");
		tc.calculateDocWeight();
	}
	@RequestMapping("/query/{query}")
	@ResponseBody
	List<Movie> result(@PathVariable("query")String query)
	{
		List<Movie> a = tc.docRank(tc.calculateQueryWeight(query));
		return a;
	}

}
