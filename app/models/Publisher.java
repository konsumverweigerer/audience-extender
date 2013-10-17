package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Publisher extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	@ManyToMany
	public List<Admin> owners = new ArrayList<Admin>();

	public Publisher(String name) {
		this.name = name;
	}

	public static Finder<String, Publisher> find = new Finder<String, Publisher>(
			String.class, Publisher.class);

	/**
	 * Retrieve all users.
	 */
	public static List<Publisher> findAll() {
		return find.all();
	}

	public String toString() {
		return "Publisher(" + name + ")";
	}

}