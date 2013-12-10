package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.Option;
import scala.Some;

@Entity
public class Audience extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	public String state;
	public String tracking;

	@ManyToOne(fetch = FetchType.LAZY)
	public Publisher publisher;

	@ManyToMany(fetch = FetchType.EAGER)
	public List<Website> websites = new ArrayList<Website>();

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "audience")
	public List<PathTarget> pathTargets = new ArrayList<PathTarget>();

	public Audience(String name) {
		this.name = name;
	}

	public static Audience fromMap(Map<String, Object> data) {
		final Audience audience = new Audience("New Audience");
		audience.updateFromMap(data);
		return audience;
	}

	public static Finder<String, Audience> find = new Finder<String, Audience>(
			String.class, Audience.class);

	public static List<Dataset> statsByAdmin(Admin admin) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		return stats;
	}

	public static List<Dataset> statsByAdmin(Admin admin, String from, String to) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		return stats;
	}

	/**
	 * Retrieve all users.
	 */
	public static List<Audience> findAll() {
		return find.all();
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public List<Message> write() {
		// TODO: check website/paths if new cookies are needed
		save();
		return Collections.emptyList();
	}

	public Audience updateFromMap(Map<String, Object> data) {
		return this;
	}

	public static List<Audience> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("publisher.owners.id", admin.id).findList();
	}

	public static Option<Audience> findById(String audienceid, Admin admin) {
		List<Audience> ret = null;
		final Long id = audienceid != null ? Long.valueOf(audienceid) : 0L;
		if (admin.isSysAdmin()) {
			ret = find.where().eq("id", id).findList();
		} else {
			ret = find.where().eq("publisher.owners.id", admin.id).eq("id", id)
					.findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Audience>(ret.get(0));
		}
		return Option.empty();
	}

	@Override
	public String toString() {
		return "Audience(" + name + ")";
	}
}