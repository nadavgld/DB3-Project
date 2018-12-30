/**
 * 
 */
package org.bgu.ise.ddb.history;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
import org.bgu.ise.ddb.items.ItemsController;
import org.bgu.ise.ddb.registration.RegistarationController;
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
@RequestMapping(value = "/history")
public class HistoryController extends ParentController {

	/**
	 * The function inserts to the system storage triple(s)(username, title,
	 * timestamp). The timestamp - in ms since 1970 Advice: better to insert the
	 * history into two structures( tables) in order to extract it fast one with the
	 * key - username, another with the key - title
	 * 
	 * @param username
	 * @param title
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value = "insert_to_history", method = { RequestMethod.GET })
	public void insertToHistory(@RequestParam("username") String username, @RequestParam("title") String title,
			HttpServletResponse response) {
		System.out.println(username + " " + title);

		if(!RegistarationController.isExistUserMethod(username)) {
			HttpStatus status = HttpStatus.METHOD_FAILURE;
			response.setStatus(status.value());
			return;
		}
		
		if(!ItemsController.isTitleExists(title)) {
			HttpStatus status = HttpStatus.METHOD_FAILURE;
			response.setStatus(status.value());
			return;
		}
				
		try {
			DBCollection collection = this.getCollection("history");

			collection.insert(new BasicDBObject().append("username", username).append("title", title)
					.append("timestamp", new Date().getTime()));

			this.mongoClient.close();
		} catch (Exception e) {
			System.out.println(e);

		}

		HttpStatus status = HttpStatus.OK;
		response.setStatus(status.value());
	}

	/**
	 * The function retrieves users' history The function return array of pairs
	 * <title,viewtime> sorted by VIEWTIME in descending order
	 * 
	 * @param username
	 * @return
	 */
	@RequestMapping(value = "get_history_by_users", headers = "Accept=*/*", method = {
			RequestMethod.GET }, produces = "application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
	public HistoryPair[] getHistoryByUser(@RequestParam("entity") String username) {
		ArrayList<HistoryPair> hp = new ArrayList();
		try {
			DBCollection collection = this.getCollection("history");

			for (DBObject doc : collection.find(new BasicDBObject().append("username", username))) {
				hp.add(new HistoryPair(doc.get("title").toString(), new Date((long) doc.get("timestamp"))));
			}

			this.mongoClient.close();

		} catch (Exception e) {

			System.out.println(e);

		}

		return hp.toArray(new HistoryPair[hp.size()]);
	}

	/**
	 * The function retrieves items' history The function return array of pairs
	 * <username,viewtime> sorted by VIEWTIME in descending order
	 * 
	 * @param title
	 * @return
	 */
	@RequestMapping(value = "get_history_by_items", headers = "Accept=*/*", method = {
			RequestMethod.GET }, produces = "application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
	public HistoryPair[] getHistoryByItems(@RequestParam("entity") String title) {
		// :TODO your implementation
		ArrayList<HistoryPair> hp = new ArrayList();
		try {
			DBCollection collection = this.getCollection("history");

			for (DBObject doc : collection.find(new BasicDBObject().append("title", title))) {
				hp.add(new HistoryPair(doc.get("username").toString(), new Date((long) doc.get("timestamp"))));
			}

			this.mongoClient.close();

		} catch (Exception e) {

			System.out.println(e);

		}

		return hp.toArray(new HistoryPair[hp.size()]);
	}

	/**
	 * The function retrieves all the users that have viewed the given item
	 * 
	 * @param title
	 * @return
	 */
	@RequestMapping(value = "get_users_by_item", headers = "Accept=*/*", method = {
			RequestMethod.GET }, produces = "application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
	public User[] getUsersByItem(@RequestParam("title") String title) {
		// :TODO your implementation
		ArrayList<User> users = new ArrayList<User>();
		try {
			DBCollection collection = this.getCollection("history");

			for (DBObject doc : collection.find(new BasicDBObject().append("title", title))) {
				users.add(new User(doc.get("username").toString()));
			}

			this.mongoClient.close();

		} catch (Exception e) {

			System.out.println(e);

		}

		return users.toArray(new User[users.size()]);
	}

	/**
	 * The function calculates the similarity score using Jaccard similarity
	 * function: sim(i,j) = |U(i) intersection U(j)|/|U(i) union U(j)|, where U(i)
	 * is the set of usernames which exist in the history of the item i.
	 * 
	 * @param title1
	 * @param title2
	 * @return
	 */
	@RequestMapping(value = "get_items_similarity", headers = "Accept=*/*", method = {
			RequestMethod.GET }, produces = "application/json")
	@ResponseBody
	public double getItemsSimilarity(@RequestParam("title1") String title1, @RequestParam("title2") String title2) {
		// :TODO your implementation
		double ret = 0.0;
		User[] _firstItem = getUsersByItem(title1);
		User[] _secondItem = getUsersByItem(title2);
		
		if(_firstItem.length == 0 || _secondItem.length == 0)
			return 0.0;
		
		double intersection = new Double(calculateIntersectionBetweenUsers(_firstItem, _secondItem));
		double union = new Double(calculateUnionBetweenUsers(_firstItem, _secondItem));

		System.out.println(intersection);
		System.out.println(union);
		
		ret = (intersection)/(union);
		System.out.println(ret);
		return ret;
	}

	private int calculateIntersectionBetweenUsers(User[] _firstItem, User[] _secondItem) {
		ArrayList<User> result = new ArrayList<User>();
		for(User u: _firstItem) {
			for(User u2: _secondItem) {
				if(u.getUsername().equals(u2.getUsername()))
					if(!isUsernameExists(result, u.getUsername()))
						result.add(u);
			}
		}
		
		return result.size();
	}
	
	private boolean isUsernameExists(ArrayList<User> result, String username) {
		boolean res = false;
		
		for(User u: result) {
			if(u.getUsername().equals(username)) {
				res = true;
				break;
			}
		}
		
		return res;
	}

	private int calculateUnionBetweenUsers(User[] _firstItem, User[] _secondItem) {
		ArrayList<User> result = new ArrayList<User>();
		for(User u: _firstItem) {
			if(!isUsernameExists(result, u.getUsername()))
				result.add(u);
		}
		
		for(User u: _secondItem) {
			if(!isUsernameExists(result,u.getUsername()))
				result.add(u);
		}
		
		return result.size();
	}

}
