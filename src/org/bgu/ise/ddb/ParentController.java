/**
 * 
 */
package org.bgu.ise.ddb;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.HandlerMapping;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 * @author Alex
 *
 */
public class ParentController {
	
	protected MongoClient mongoClient;
	static public MongoClient _mongoClient;

	@ModelAttribute
	public void tagController(HttpServletRequest request) {
		Set<org.springframework.http.MediaType> supportedMediaTypes = new HashSet<>();
		supportedMediaTypes.add(MediaType.APPLICATION_JSON);
		request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE,
				supportedMediaTypes);
	}
	
	protected DBCollection getCollection(String name) {
		this.mongoClient = new MongoClient("localhost",27017);
		return this.mongoClient.getDB("db_project").getCollection(name);
	}

	public static DBCollection getCollectionMethod(String name) {
		_mongoClient = new MongoClient("localhost",27017);
		return _mongoClient.getDB("db_project").getCollection(name);
	}
}
