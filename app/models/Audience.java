package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Audience extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	@ManyToOne(fetch = FetchType.LAZY)
	public Publisher publisher;

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

	public static List<Audience> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("publisher.owners.id", admin.id).findList();
	}

	public String toString() {
		return "Audience(" + name + ")";
	}
}