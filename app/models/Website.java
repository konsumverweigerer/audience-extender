package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Website extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	public String url;
	
	@ManyToOne(fetch = FetchType.LAZY)
	public Publisher publisher;

	@ManyToMany(fetch = FetchType.LAZY)
	public List<Audience> audience = new ArrayList<Audience>();

	public Website(String name) {
		this.name = name;
	}

	public static Finder<String, Website> find = new Finder<String, Website>(
			String.class, Website.class);

	/**
	 * Retrieve all website.
	 */
	public static List<Website> findAll() {
		return find.all();
	}

	public String toString() {
		return "Website(" + name + ")";
	}
}