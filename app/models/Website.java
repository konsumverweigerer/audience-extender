package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.api.Application;
import play.api.Play;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.Option;
import scala.Some;
import scala.collection.immutable.Set;
import services.UuidHelper;

@Entity
public class Website extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	@Temporal(TemporalType.TIMESTAMP)
	public Date created;

	public String uuid;
	public String url;
	public String email;

	@ManyToOne(fetch = FetchType.LAZY)
	public Publisher publisher;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "website")
	public List<PathTarget> pathTargets = new ArrayList<PathTarget>();

	public Website(String name) {
		this.name = name;
		this.created = new Date();
	}

	public static Website fromMap(Map<String, Object> data) {
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
		save();
		if (this.uuid == null || this.uuid.isEmpty()) {
			this.uuid = UuidHelper
					.randomUUIDString("com.audienceextender.website");
		}
		update();
		return Collections.emptyList();
	}

	public Website updateFromMap(Map<String, Object> data) {
		return this;
	}

	public String extendedCode(Application app) {
		final Option<String> domain = Play.configuration(app).getString(
				"cookiedomain", Option.<Set<String>> empty());
		return String
				.format("<script type=\"text/javascript\">\n"
						+ "(function(){\n"
						+ "var path='%s';\n"
						+ "var prot=document.location.protocol;\n"
						+ "var dom='%s'\n;"
						+ "var loc=encodeURIComponent(document.location);\n"
						+ "document.write('<script src=\"'+prot+'//'+dom+path+'?l='+loc+'\"></scripts>');\n"
						+ "})()\n</script>\n",
						controllers.routes.ContentController.cookie(this.uuid,
								"<sub>").url(),
						domain.nonEmpty() ? domain.get()
								: "cookiedomain.com");
	}

	public String code(Application app) {
		final Option<String> domain = Play.configuration(app).getString(
				"cookiedomain", Option.<Set<String>> empty());
		return String.format(
				"<script type=\"text/javascript\" src=\"//%s%s\">\n"
						+ "</script>\n", domain.nonEmpty() ? domain.get()
						: "cookiedomain.com",
				controllers.routes.ContentController.cookie(this.uuid, "<sub>")
						.url());
	}

	public static Option<Website> findByUUID(String uuid) {
		final List<Website> ret = find.fetch("pathTargets")
				.fetch("pathTargets.audience").where().eq("uuid", uuid)
				.findList();
		if (!ret.isEmpty()) {
			return new Some<Website>(ret.get(0));
		}
		return Option.empty();
	}

	public static Option<Website> findById(String websiteid, Admin admin) {
		final Long id = websiteid != null ? Long.valueOf(websiteid) : 0L;
		return findById(id, admin);
	}

	public static Option<Website> findById(Long id, Admin admin) {
		List<Website> ret = null;
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

	@Override
	public String toString() {
		return "Website(" + name + ")";
	}
}