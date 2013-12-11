package models;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.Option;
import scala.Some;
import services.UuidHelper;

@Entity
public class Cookie extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	public String variant;
	public String state;

	public String uuid;
	public Integer pathhash;
	public String content;

	@ManyToOne(fetch = FetchType.LAZY)
	public Audience audience;

	@ManyToOne(fetch = FetchType.LAZY)
	public Website website;

	public Cookie(String name) {
		this.name = name;
		this.uuid = UuidHelper.randomUUIDString("com.audienceextender.cookie");
	}

	public static Cookie fromMap(Map<String, Object> data) {
		final Cookie website = new Cookie("New Cookie");
		website.updateFromMap(data);
		return website;
	}

	public static Finder<String, Cookie> find = new Finder<String, Cookie>(
			String.class, Cookie.class);

	public Cookie updateFromMap(Map<String, Object> data) {
		return this;
	}

	/**
	 * Retrieve all cookies.
	 */
	public static List<Cookie> findAll() {
		return find.all();
	}

	public static List<Cookie> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("audience.publisher.owners.id", admin.id)
				.findList();
	}

	public static Option<Cookie> findById(String cookieid, Admin admin) {
		final Long id = cookieid != null ? Long.valueOf(cookieid) : 0L;
		return findById(id, admin);
	}

	public static Option<Cookie> findById(Long id, Admin admin) {
		List<Cookie> ret = null;
		if (admin.isSysAdmin()) {
			ret = find.where().eq("id", id).findList();
		} else {
			ret = find.where().eq("audience.publisher.owners.id", admin.id)
					.eq("id", id).findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Cookie>(ret.get(0));
		}
		return Option.empty();
	}

	public static List<Cookie> findByUuid(String uuid) {
		return find.where().eq("uuid", uuid).findList();
	}

	@Override
	public String toString() {
		return "Cookie(" + name + ")";
	}
}