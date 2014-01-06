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

	public static Option<WebsitePreview> addUpload(Publisher publisher,
			String contentType, String filename, File file) {
		return null;
	}

	/**
	 * Retrieve all creatives.
	 */
	public static List<WebsitePreview> findAll() {
		return find.all();
	}

	public static List<WebsitePreview> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("website.publisher.owners.id", admin.getId())
				.findList();
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

	public static Option<WebsitePreview> findById(String creativeid, Admin admin) {
		final Long id = creativeid != null ? Long.valueOf(creativeid) : 0L;
		return findById(id, admin);
	}

	public static WebsitePreview fromMap(Map<String, Object> data) {
		final WebsitePreview creative = new WebsitePreview();
		creative.updateFromMap(data);
		return creative;
	}

	@Id
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	/*
	 * allowed values: image/png, image/gif
	 */
	private String variant;

	private byte[] data;

	@ManyToOne(fetch = FetchType.LAZY)
	private Website website;

	public static Finder<Long, WebsitePreview> find = new Finder<Long, WebsitePreview>(
			Long.class, WebsitePreview.class);

	public WebsitePreview() {
	}

	public Date getCreated() {
		return created;
	}

	public byte[] getData() {
		return data;
	}

	public Long getId() {
		return id;
	}

	public String getVariant() {
		return variant;
	}

	public Website getWebsite() {
		return website;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	public void setWebsite(Website website) {
		this.website = website;
	}

	@Override
	public String toString() {
		return "WebsitePreview()";
	}

	public WebsitePreview updateFromMap(Map<String, Object> data) {
		return this;
	}
}