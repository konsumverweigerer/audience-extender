package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Cookie extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	public Cookie(String name) {
		this.name = name;
	}

	public static Finder<String, Cookie> find = new Finder<String, Cookie>(
			String.class, Cookie.class);

	/**
	 * Retrieve all users.
	 */
	public static List<Cookie> findAll() {
		return find.all();
	}

	public String toString() {
		return "Cookie(" + name + ")";
	}

}