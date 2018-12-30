/**
 * 
 */
package org.bgu.ise.ddb.items;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.bgu.ise.ddb.MediaItems;
import org.bgu.ise.ddb.ParentController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author Alex
 *
 */
@RestController
@RequestMapping(value = "/items")
public class ItemsController extends ParentController {

	private Connection connection =null;
	private String username="zaksg";
	private String password="abcd";
	private String connectionUrl="jdbc:oracle:thin:@ora1.ise.bgu.ac.il:1521/oracle";
	private final String driver="oracle.jdbc.driver.OracleDriver";

	private void ConnectToOracleDB()
	{
		try 
		{
			Class.forName(this.driver); //registration of the driver
			this.connection = DriverManager.getConnection(this.connectionUrl, this.username, this.password);
			this.connection.setAutoCommit(false);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * The function copy all the items(title and production year) from the Oracle
	 * table MediaItems to the System storage. The Oracle table and data should be
	 * used from the previous assignment
	 */
	@RequestMapping(value = "fill_media_items", method = { RequestMethod.GET })
	public void fillMediaItems(HttpServletResponse response) {
		System.out.println("was here");
		ConnectToOracleDB();
		// :TODO your implementation
		HttpStatus status = HttpStatus.OK;
		response.setStatus(status.value());
	}

	/**
	 * The function copy all the items from the remote file, the remote file have
	 * the same structure as the films file from the previous assignment. You can
	 * assume that the address protocol is http
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value = "fill_media_items_from_url", method = { RequestMethod.GET })
	public void fillMediaItemsFromUrl(@RequestParam("url") String urladdress, HttpServletResponse response)
			throws IOException {
		System.out.println(urladdress);

		URL url = new URL(urladdress);
		BufferedReader br = null;
		String line = "";

		try {
			br = new BufferedReader(new InputStreamReader(url.openStream()));
			while ((line = br.readLine()) != null) {
				String title = line.split(",")[0];
				String year = line.split(",")[1];

				DBCollection collection = this.getCollection("items");
				
				if(!isTitleExists(title))
					collection.insert(new BasicDBObject().append("title", title).append("year", year));

				this.mongoClient.close();

			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
		}

		HttpStatus status = HttpStatus.OK;
		response.setStatus(status.value());
	}
	
	public static boolean isTitleExists(String title) {
		DBCollection collection = ParentController.getCollectionMethod("items");
		DBCursor cursor = collection.find(new BasicDBObject().append("title", title));

		Boolean result = (cursor.size() > 0);
		ParentController._mongoClient.close();
		
		return result;
	}

	/**
	 * The function retrieves from the system storage N items, order is not
	 * important( any N items)
	 * 
	 * @param topN - how many items to retrieve
	 * @return
	 */
	@RequestMapping(value = "get_topn_items", headers = "Accept=*/*", method = {
			RequestMethod.GET }, produces = "application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(MediaItems.class)
	public MediaItems[] getTopNItems(@RequestParam("topn") int topN) {
		// :TODO your implementation
		MediaItems[] m = new MediaItems[topN];
		int counter = 0;

		try {
			DBCollection collection = this.getCollection("items");
			for (DBObject doc : collection.find()) {
				if (counter >= topN)
					break;
				MediaItems med = new MediaItems(doc.get("title").toString(),
						Integer.parseInt(doc.get("year").toString()));

				m[counter] = med;
				counter++;
			}

			this.mongoClient.close();

		} catch (Exception e) {

			System.out.println(e);

		}

		return m;
	}

}
