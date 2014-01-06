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
					&& admin.getPublisher().getId().equals(publisher.getId())) {
				publisher.setActive(true);
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
				if (publisher.getOwners().contains(admin)) {
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
	private Long id;
	@Required
	private String name;
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	@Temporal(TemporalType.TIMESTAMP)
	private Date changed;

	private String url;
	private String streetaddress1;
	private String streetaddress2;
	private String streetaddress3;
	private String state;
	private String country;
	private String telephone;

	@ManyToMany
	private List<Admin> owners = new ArrayList<Admin>();

	@Transient
	private boolean active = false;

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

	public Date getChanged() {
		return changed;
	}

	public String getCountry() {
		return country;
	}

	public Date getCreated() {
		return created;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Admin> getOwners() {
		return owners;
	}

	public String getState() {
		return state;
	}

	public String getStreetaddress1() {
		return streetaddress1;
	}

	public String getStreetaddress2() {
		return streetaddress2;
	}

	public String getStreetaddress3() {
		return streetaddress3;
	}

	public String getTelephone() {
		return telephone;
	}

	public String getUrl() {
		return url;
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

	public void setChanged(Date changed) {
		this.changed = changed;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOwners(List<Admin> owners) {
		this.owners = owners;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setStreetaddress1(String streetaddress1) {
		this.streetaddress1 = streetaddress1;
	}

	public void setStreetaddress2(String streetaddress2) {
		this.streetaddress2 = streetaddress2;
	}

	public void setStreetaddress3(String streetaddress3) {
		this.streetaddress3 = streetaddress3;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public void setUrl(String url) {
		this.url = url;
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