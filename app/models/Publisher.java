package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.Option;
import scala.Some;

@Entity
public class Publisher extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	@Temporal(TemporalType.TIMESTAMP)
	public Date created;
	@Temporal(TemporalType.TIMESTAMP)
	public Date changed;

	public String url;
	public String streetaddress1;
	public String streetaddress2;
	public String streetaddress3;
	public String state;
	public String country;
	public String telephone;

	@ManyToMany
	public List<Admin> owners = new ArrayList<Admin>();

	@Transient
	public boolean active = false;

	public Publisher(String name) {
		this.name = name;
	}

	public Publisher(String name, Option<String> url) {
		this.name = name;
		this.url = url.isDefined() ? url.get() : null;
	}

	public static Finder<Long, Publisher> find = new Finder<Long, Publisher>(
			Long.class, Publisher.class);

	/**
	 * Retrieve a Publisher from id.
	 */
	public static Publisher findById(Long id) {
		return find.byId(id);
	}

	public static List<Dataset> statsByAdmin(Admin admin) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		return stats;
	}

	public static Option<Publisher> findById(String id, Admin admin) {
		List<Publisher> ret = null;
		if (admin.isSysAdmin()) {
			ret = find.where().eq("id", id).findList();
		} else {
			ret = find.where().eq("owners.id", admin.id).eq("id", id)
					.findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Publisher>(ret.get(0));
		}
		return Option.empty();
	}

	public static List<Publisher> findByAdmin(Admin admin) {
		List<Publisher> ret = null;
		if (admin.isSysAdmin()) {
			ret = find.findList();
		} else {
			ret = find.where().eq("owners.id", admin.id).findList();
		}
		for (final Publisher publisher : ret) {
			if (admin.publisher != null
					&& admin.publisher.id.equals(publisher.id)) {
				publisher.active = true;
			}
		}
		return ret;
	}

	public static boolean isAdmin(Long publisher_id, String admin_id) {
		final Admin admin = Admin.findById(admin_id);
		if (admin != null) {
			final List<String> roles = admin.getRoles();
			if (roles.contains("superadmin")) {
				return true;
			} else {
				final Publisher publisher = findById(publisher_id);
				if (publisher.owners.contains(admin)) {
					return true;
				}
			}
		}
		return false;
	}

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