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

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.Option;
import scala.Some;
import services.UuidHelper;

@Entity
public class Website extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	public String uuid;
	public String url;

	@ManyToOne(fetch = FetchType.LAZY)
	public Publisher publisher;

	@ManyToMany(fetch = FetchType.LAZY)
	public List<Audience> audience = new ArrayList<Audience>();

	public Website(String name) {
		this.name = name;
	}

	public static Website fromMap(Map<String, String> data) {
		final Website website = new Website("New Website");
		website.updateFromMap(data);
		return website;
	}

	public static Finder<String, Website> find = new Finder<String, Website>(
			String.class, Website.class);

	/**
	 * Retrieve all website.
	 */
	public static List<Website> findAll() {
		return find.all();
	}

	public static List<Website> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("publisher.owners.id", admin.id).findList();
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public List<Message> write() {
		if (this.uuid == null || this.uuid.isEmpty()) {
			this.uuid = UuidHelper
					.randomUUIDString("com.audienceextender.website");
		}
		save();
		return Collections.emptyList();
	}

	public void updateFromMap(Map<String, String> data) {

	}

	public static Option<Website> findById(String websiteid, Admin admin) {
		List<Website> ret = null;
		final Long id = websiteid != null ? Long.valueOf(websiteid) : 0L;
		if (admin.isSysAdmin()) {
			ret = find.where().eq("id", id).findList();
		} else {
			ret = find.where().eq("publisher.owners.id", admin.id).eq("id", id)
					.findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Website>(ret.get(0));
		}
		return Option.empty();
	}

	public String toString() {
		return "Website(" + name + ")";
	}
}