package models;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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

	@Temporal(TemporalType.TIMESTAMP)
	public Date created;

	/*
	 * allowed values: pending, active, cancelled
	 */
	public String state;
	/*
	 * allowed values: code, url
	 */
	public String variant;

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
		this.created = new Date();
	}

	public static Cookie instance(String name, String variant, Audience audience,
			Website website, Collection<PathTarget> paths) {
		final Cookie cookie = new Cookie(name);
		cookie.variant = variant;
		cookie.pathhash = calculateHash(paths);
		return cookie;
	}

	public static Cookie instance(Cookie cookie, String name, String variant,
			Audience audience, Website website, Collection<PathTarget> paths) {
		if (cookie.audience.id.equals(audience.id)
				&& cookie.website.id.equals(website.id)
				&& cookie.pathhash == calculateHash(paths)) {
			return cookie;
		}
		return instance(name, variant, audience, website, paths);
	}

	public boolean checkCookie(Audience audience,
			Website website, Collection<PathTarget> paths) {
		if (this.audience.id.equals(audience.id)
				&& this.website.id.equals(website.id)
				&& this.pathhash == calculateHash(paths)) {
			return true;
		}
		return false;
	}

	public static int calculateHash(Collection<PathTarget> paths) {
		int h = 0;
		for (final PathTarget path : paths) {
			h = h ^ path.variant.hashCode();
			h = h ^ path.urlPath.hashCode();
		}
		return h;
	}

	public static Cookie fromMap(Map<String, Object> data) {
		final Cookie website = new Cookie("New Cookie");
		website.updateFromMap(data);
		return website;
	}

	public static Finder<String, Cookie> find = new Finder<String, Cookie>(
			String.class, Cookie.class);

	public List<Message> write() {
		if (this.uuid == null || this.uuid.isEmpty()) {
			this.uuid = UuidHelper
					.randomUUIDString("com.audienceextender.cookie");
		}
		save();
		return Collections.emptyList();
	}

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
			return find.fetch("audience").fetch("website").findList();
		}
		return find.fetch("audience").fetch("website").where()
				.eq("audience.publisher.owners.id", admin.id).findList();
	}

	public static List<Cookie> findByWebsite(Long websiteid, Long audienceid) {
		final List<Cookie> ret = find.where().eq("website.id", websiteid)
				.eq("audience.id", audienceid).findList();
		return ret;
	}

	public static Option<Cookie> findByUUID(String uuid) {
		final List<Cookie> ret = find.where().eq("uuid", uuid).findList();
		if (!ret.isEmpty()) {
			return new Some<Cookie>(ret.get(0));
		}
		return Option.empty();
	}

	public static Option<Cookie> findById(String cookieid, Admin admin) {
		final Long id = cookieid != null ? Long.valueOf(cookieid) : 0L;
		return findById(id, admin);
	}

	public static Option<Cookie> findById(Long id, Admin admin) {
		List<Cookie> ret = null;
		if (admin.isSysAdmin()) {
			ret = find.fetch("audience").fetch("website").where().eq("id", id)
					.findList();
		} else {
			ret = find.fetch("audience").fetch("website").where()
					.eq("audience.publisher.owners.id", admin.id).eq("id", id)
					.findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Cookie>(ret.get(0));
		}
		return Option.empty();
	}

	public static List<Cookie> findByUuid(String uuid) {
		return find.where().eq("uuid", uuid).findList();
	}

	public Audience getAudience() {
		return audience;
	}

	public Website getWebsite() {
		return website;
	}

	@Override
	public String toString() {
		return "Cookie(" + name + ")";
	}
}