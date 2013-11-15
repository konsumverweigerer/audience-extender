package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import services.UuidHelper;

@Entity
public class Cookie extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	public String variant;

	public String uuid;
	public String content;

	@ManyToOne(fetch = FetchType.LAZY)
	public Audience audience;

	@ManyToOne(fetch = FetchType.LAZY)
	public Website website;

	public Cookie(String name) {
		this.name = name;
		this.uuid = UuidHelper.randomUUIDString("com.audienceextender.cookie");
	}

	public static Finder<String, Cookie> find = new Finder<String, Cookie>(
			String.class, Cookie.class);

	/**
	 * Retrieve all cookies.
	 */
	public static List<Cookie> findAll() {
		return find.all();
	}

	public static List<Cookie> findByUuid(String uuid) {
		return find.where().eq("uuid", uuid).findList();
	}

	public String toString() {
		return "Cookie(" + name + ")";
	}
}