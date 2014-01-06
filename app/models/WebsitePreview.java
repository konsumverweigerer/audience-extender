package models;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.db.ebean.Model;
import scala.Option;
import scala.Some;

@Entity
public class WebsitePreview extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Temporal(TemporalType.TIMESTAMP)
	public Date created;

	/*
	 * allowed values: image/png, image/gif
	 */
	public String variant;

	public byte[] data;

	@ManyToOne(fetch = FetchType.LAZY)
	public Website website;

	public WebsitePreview() {
	}

	public static WebsitePreview fromMap(Map<String, Object> data) {
		final WebsitePreview creative = new WebsitePreview();
		creative.updateFromMap(data);
		return creative;
	}

	public static Finder<Long, WebsitePreview> find = new Finder<Long, WebsitePreview>(
			Long.class, WebsitePreview.class);

	public static List<WebsitePreview> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("website.publisher.owners.id", admin.getId())
				.findList();
	}

	public static Option<WebsitePreview> addUpload(Publisher publisher,
			String contentType, String filename, File file) {
		return null;
	}

	public WebsitePreview updateFromMap(Map<String, Object> data) {
		return this;
	}

	/**
	 * Retrieve all creatives.
	 */
	public static List<WebsitePreview> findAll() {
		return find.all();
	}

	public static Option<WebsitePreview> findById(String creativeid, Admin admin) {
		final Long id = creativeid != null ? Long.valueOf(creativeid) : 0L;
		return findById(id, admin);
	}

	public static Option<WebsitePreview> findById(Long id, Admin admin) {
		List<WebsitePreview> ret = null;
		if (admin.isSysAdmin()) {
			ret = find.where().eq("id", id).findList();
		} else {
			ret = find.where().eq("website.publisher.owners.id", admin.getId())
					.eq("id", id).findList();
		}
		if (!ret.isEmpty()) {
			return new Some<WebsitePreview>(ret.get(0));
		}
		return Option.empty();
	}

	@Override
	public String toString() {
		return "WebsitePreview()";
	}
}