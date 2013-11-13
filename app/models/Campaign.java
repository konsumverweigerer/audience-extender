package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Campaign extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	@ManyToOne(fetch = FetchType.LAZY)
	public Publisher publisher;

	public Campaign(String name) {
		this.name = name;
	}

	public static Finder<String, Campaign> find = new Finder<String, Campaign>(
			String.class, Campaign.class);

	public static List<Dataset> statsByAdmin(Admin admin) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		return stats;
	}

	public static List<Dataset> statsByAdmin(Admin admin, String from,
			String to, String state, String query) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		return stats;
	}

	public static List<Campaign> findByAdmin(Admin admin, String state, String query) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("publisher.owners.id", admin.id).findList();
	}

	/**
	 * Retrieve all users.
	 */
	public static List<Campaign> findAll() {
		return find.all();
	}

	public static List<Campaign> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("publisher.owners.id", admin.id).findList();
	}

	public String toString() {
		return "Campaign(" + name + ")";
	}
}