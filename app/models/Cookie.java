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
	private Long id;

	@Required
	private String name;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	/*
	 * allowed values: pending, active, cancelled
	 */
	private String state;
	/*
	 * allowed values: code, url
	 */
	private String variant;

	private String uuid;
	private Integer pathhash;
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	private Audience audience;
	@ManyToOne(fetch = FetchType.LAZY)
	private Website website;

	public Cookie(String name) {
		this.name = name;
		this.uuid = UuidHelper.randomUUIDString("com.audienceextender.cookie");
		this.created = new Date();
	}

	public static Cookie instance(String name, String variant, Audience audience,
			Website website, Collection<PathTarget> paths) {
		final Cookie cookie = new Cookie(name);
		cookie.setVariant(variant);
		cookie.setPathhash(calculateHash(paths));
		cookie.setWebsite(website);
		cookie.setAudience(audience);
		return cookie;
	}

	public static Cookie instance(Cookie cookie, String name, String variant,
			Audience audience, Website website, Collection<PathTarget> paths) {
		if (cookie.getAudience().getId().equals(audience.getId())
				&& cookie.getWebsite().getId().equals(website.getId())
				&& cookie.getPathhash() == calculateHash(paths)) {
			return cookie;
		}
		return instance(name, variant, audience, website, paths);
	}

	public boolean checkCookie(Audience audience,
			Website website, Collection<PathTarget> paths) {
		if (this.getAudience().getId().equals(audience.getId())
				&& getWebsite().getId().equals(website.getId())
				&& getPathhash() == calculateHash(paths)) {
			return true;
		}
		return false;
	}

	public static int calculateHash(Collection<PathTarget> paths) {
		int h = 0;
		for (final PathTarget path : paths) {
			h = h ^ path.getVariant().hashCode();
			h = h ^ path.getUrlPath().hashCode();
		}
		return h;
	}

	public static Cookie fromMap(Map<String, Object> data) {
		final Cookie website = new Cookie("New Cookie");
		website.updateFromMap(data);
		return website;
	}

	public static Finder<Long, Cookie> find = new Finder<Long, Cookie>(
			Long.class, Cookie.class);

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
				.eq("audience.publisher.owners.id", admin.getId()).findList();
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
					.eq("audience.publisher.owners.id", admin.getId()).eq("id", id)
					.findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Cookie>(ret.get(0));
		}
		return Option.empty();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getVariant() {
		return variant;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Integer getPathhash() {
		return pathhash;
	}

	public void setPathhash(Integer pathhash) {
		this.pathhash = pathhash;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setAudience(Audience audience) {
		this.audience = audience;
	}

	public void setWebsite(Website website) {
		this.website = website;
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