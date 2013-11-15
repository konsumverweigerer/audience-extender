package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Creative extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	public String uuid;
	
	public Creative(String name) {
		this.name = name;
	}

	public static Finder<String, Creative> find = new Finder<String, Creative>(
			String.class, Creative.class);

	/**
	 * Retrieve all creatives.
	 */
	public static List<Creative> findAll() {
		return find.all();
	}

	public String toString() {
		return "Creative(" + name + ")";
	}
}