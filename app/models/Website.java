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
	private Long id;

	@Required
	private String name;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	private String uuid;
	private String url;
	private String email;

	@ManyToOne(fetch = FetchType.LAZY)
	private Publisher publisher;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "website")
	private List<PathTarget> pathTargets = new ArrayList<PathTarget>();

	public Website(String name) {
		this.name = name;
		this.created = new Date();
	}

	public static Website fromMap(Map<String, Object> data) {
		final Website website = new Website("New Website");
		website.updateFromMap(data);
		return website;
	}

	public static Finder<Long, Website> find = new Finder<Long, Website>(
			Long.class, Website.class);

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
		return find.where().eq("publisher.owners.id", admin.getId()).findList();
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public List<Message> write() {
		save();
		if (getUuid() == null || getUuid().isEmpty()) {
			setUuid(UuidHelper
					.randomUUIDString("com.audienceextender.website"));
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
		final String url = controllers.routes.ContentController.cookie(
				getUuid(), "sub").url();
		return String
				.format("<script type=\"text/javascript\">\n"
						+ "(function(){\n"
						+ "var path='%s';\n"
						+ "var prot=document.location.protocol;\n"
						+ "var dom='%s'\n;"
						+ "var loc=encodeURIComponent(document.location);\n"
						+ "document.write('<script src=\"'+prot+'//'+dom+path+'?l='+loc+'\"></scripts>');\n"
						+ "})()\n</script>\n", url,
						domain.nonEmpty() ? domain.get() : "cookiedomain.com");
	}

	public String code(Application app) {
		final Option<String> domain = Play.configuration(app).getString(
				"cookiedomain", Option.<Set<String>> empty());
		final String url = controllers.routes.ContentController.cookie(
				getUuid(), "sub").url();
		return String.format(
				"<script type=\"text/javascript\" src=\"//%s%s\">\n"
						+ "</script>\n", domain.nonEmpty() ? domain.get()
						: "cookiedomain.com", url);
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
			ret = find.where().eq("publisher.owners.id", admin.getId())
					.eq("id", id).findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Website>(ret.get(0));
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

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public List<PathTarget> getPathTargets() {
		return pathTargets;
	}

	public void setPathTargets(List<PathTarget> pathTargets) {
		this.pathTargets = pathTargets;
	}

	@Override
	public String toString() {
		return "Website(" + name + ")";
	}
}