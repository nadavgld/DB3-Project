/**
 * 
 */
package org.bgu.ise.ddb.registration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
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
@RequestMapping(value = "/registration")
public class RegistarationController extends ParentController{
	
	
	/**
	 * The function checks if the username exist,
	 * in case of positive answer HttpStatus in HttpServletResponse should be set to HttpStatus.CONFLICT,
	 * else insert the user to the system  and set to HttpStatus in HttpServletResponse HttpStatus.OK
	 * @param username
	 * @param password
	 * @param firstName
	 * @param lastName
	 * @param response
	 */
	@RequestMapping(value = "register_new_customer", method={RequestMethod.POST})
	public void registerNewUser(@RequestParam("username") String username,
			@RequestParam("password")    String password,
			@RequestParam("firstName")   String firstname,
			@RequestParam("lastName")  String lastname,
			HttpServletResponse response){
		System.out.println(username+" "+password+" "+lastname+" "+firstname);
		
			
		try {
			DBCollection collection = this.getCollection("users");
						
			collection.insert(new BasicDBObject()
							.append("username", username)
							.append("firstname", firstname)
							.append("lastname", lastname)
							.append("password", password)
							.append("timestamp", new Date())
							);			

			this.mongoClient.close();
		}  catch(Exception e) {			
			System.out.println(e);
			
		}
				
		HttpStatus status = HttpStatus.OK;
		response.setStatus(status.value());
		
	}
	
	/**
	 * The function returns true if the received username exist in the system otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "is_exist_user", method={RequestMethod.GET})
	public boolean isExistUser(@RequestParam("username") String username) throws IOException{
		return isExistUserMethod(username);
	}
	
	public static boolean isExistUserMethod(String username) {
		System.out.println(username);
		boolean result = false;
				
		try {
			DBCollection collection = ParentController.getCollectionMethod("users");
			DBCursor cursor = collection.find(new BasicDBObject().append("username", username));

			if(cursor.size() > 0)
				result = true;
			
			ParentController._mongoClient.close();
			
		}  catch(Exception e) {
			
			System.out.println(e);
			
		}
		return result;
	}
	
	/**
	 * The function returns true if the received username and password match a system storage entry, otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "validate_user", method={RequestMethod.POST})
	public boolean validateUser(@RequestParam("username") String username,
			@RequestParam("password")    String password) throws IOException{
		System.out.println(username+" "+password);
		boolean result = false;
		
		try {
			DBCollection collection = this.getCollection("users");
			DBCursor cursor = collection.find(new BasicDBObject().append("username", username).append("password", password));

			if(cursor.size() > 0)
				result = true;			
			
			this.mongoClient.close();
			
		}  catch(Exception e) {
			
			System.out.println(e);
			
		}
			
		return result;
		
	}
	
	/**
	 * The function retrieves number of the registered users in the past n days
	 * @param days
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "get_number_of_registred_users", method={RequestMethod.GET})
	public int getNumberOfRegistredUsers(@RequestParam("days") int days) throws IOException{
		System.out.println(days+"");
		int result = 0;
		LocalDate nDaysAgo = LocalDate.now().minusDays(days);
		
		try {
			DBCollection collection = this.getCollection("users");
			for(DBObject doc: collection.find()) {		
				Date dateDate = ((Date)doc.get("timestamp"));
				LocalDate _date =
					    (dateDate.toInstant()).atZone(ZoneId.systemDefault()).toLocalDate();

				if(_date.isAfter(nDaysAgo))
					result++;
			}
			this.mongoClient.close();
			
		}  catch(Exception e) {
			
			System.out.println(e);
			
		}
		
		return result;
		
	}
	
	/**
	 * The function retrieves all the users
	 * @return
	 */
	@RequestMapping(value = "get_all_users",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(User.class)
	public  User[] getAllUsers() throws Exception{

		User[] users = null;
		
		try {
			DBCollection collection = this.getCollection("users");
			users = new User[(int) collection.count()];
			int idx = 0;
			for(DBObject doc: collection.find()) {
				
				String un = doc.get("username").toString();
				String fn = doc.get("firstname").toString();
				String ln = doc.get("lastname").toString();
				String pwd = doc.get("password").toString();
				
				users[idx] = new User(un,pwd,fn,ln);
				idx++;
			}
			this.mongoClient.close();
			
		}  catch(Exception e) {
			System.out.println(e);
		}
		
		return users;
	}

}
