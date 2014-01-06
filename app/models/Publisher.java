package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import com.avaje.ebean.Ebean;

@Entity
public class Publisher extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	/**
	 * Retrieve all users.
	 */
	public static List<Publisher> findAll() {
		return find.all();
	}

	public static List<Publisher> findByAdmin(Admin admin) {
		List<Publisher> ret = null;
		if (admin.isSysAdmin()) {
			ret = find.findList();
		} else {
			ret = find.fetch("owners").where().eq("owners.id", admin.getId())
					.findList();
		}
		for (final Publisher publisher : ret) {
			if (admin.getPublisher() != null
					&& admin.getPublisher().id.equals(publisher.id)) {
				publisher.active = true;
			}
		}
		return ret;
	}

	/**
	 * Retrieve a Publisher from id.
	 */
	public static Publisher findById(Long id) {
		return find.byId(id);
	}

	public static Option<Publisher> findById(String publisherid, Admin admin) {
		List<Publisher> ret = null;
		final Long id = publisherid != null ? Long.valueOf(publisherid) : 0L;
		if (admin.isSysAdmin()) {
			ret = find.where().eq("id", id).findList();
		} else {
			ret = find.where().eq("owners.id", admin.getId()).eq("id", id)
					.findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Publisher>(ret.get(0));
		}
		return Option.empty();
	}

	public static Publisher fromMap(Map<String, Object> data) {
		final Publisher publisher = new Publisher("New Publisher");
		publisher.updateFromMap(data);
		return publisher;
	}

	public static boolean isAdmin(Long publisher_id, String admin_id) {
		final Admin admin = Admin.findById(admin_id).orNull(null);
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

	public static List<Dataset> statsByAdmin(Admin admin) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		return stats;
	}

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

	public static Finder<Long, Publisher> find = new Finder<Long, Publisher>(
			Long.class, Publisher.class);

	public Publisher(String name) {
		this.name = name;
		created = new Date();
	}

	public Publisher(String name, Option<String> url) {
		this.name = name;
		this.url = url.orNull(null);
	}

	public List<Admin> getAdmins() {
		return owners != null ? owners : new ArrayList<Admin>();
	}

	public boolean isActive() {
		return active;
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "Publisher(" + name + ")";
	}

	public Publisher updateFromMap(Map<String, Object> data) {
		return this;
	}

	public List<Message> write() {
		save();
		Ebean.saveManyToManyAssociations(this, "admins");
		update();
		return Collections.emptyList();
	}
}