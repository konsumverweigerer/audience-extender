package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Audience extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	public Audience(String name) {
		this.name = name;
	}

	public static Finder<String, Audience> find = new Finder<String, Audience>(
			String.class, Audience.class);

	/**
	 * Retrieve all users.
	 */
	public static List<Audience> findAll() {
		return find.all();
	}

	public String toString() {
		return "Audience(" + name + ")";
	}

}