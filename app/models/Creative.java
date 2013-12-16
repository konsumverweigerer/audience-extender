package models;

import java.io.File;
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
public class Creative extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	@Temporal(TemporalType.TIMESTAMP)
	public Date created;

	/*
	 * allowed values: pending, active
	 */
	public String state;
	/*
	 * allowed values: image/png, image/gif, video/flv
	 */
	public String variant;

	public String uuid;
	public String url;

	public byte[] data;

	@ManyToOne(fetch = FetchType.LAZY)
	public Campaign campaign;

	public Creative(String name, Option<String> url) {
		this.name = name;
		this.url = url.orNull(null);
		this.uuid = UuidHelper.randomUUIDString("com.audienceextender.creative");
	}

	public static Creative fromMap(Map<String, Object> data) {
		final Creative creative = new Creative("New Creative",
				Option.<String> empty());
		creative.updateFromMap(data);
		return creative;
	}

	public static Finder<String, Creative> find = new Finder<String, Creative>(
			String.class, Creative.class);

	public static List<Creative> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("campaign.publisher.owners.id", admin.id)
				.findList();
	}

	public static Option<Creative> addUpload(Publisher publisher,
			String contentType, String filename, File file) {
		return null;
	}

	public List<Message> write() {
		if (this.uuid == null || this.uuid.isEmpty()) {
			this.uuid = UuidHelper
					.randomUUIDString("com.audienceextender.creative");
		}
		save();
		return Collections.emptyList();
	}

	public Creative updateFromMap(Map<String, Object> data) {
		return this;
	}

	/**
	 * Retrieve all creatives.
	 */
	public static List<Creative> findAll() {
		return find.all();
	}

	public static Option<Creative> findById(String creativeid, Admin admin) {
		final Long id = creativeid != null ? Long.valueOf(creativeid) : 0L;
		return findById(id, admin);
	}

	public static Option<Creative> findById(Long id, Admin admin) {
		List<Creative> ret = null;
		if (admin.isSysAdmin()) {
			ret = find.where().eq("id", id).findList();
		} else {
			ret = find.where().eq("campaign.publisher.owners.id", admin.id)
					.eq("id", id).findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Creative>(ret.get(0));
		}
		return Option.empty();
	}

	public String getPreview() {
		return this.url;
	}

	@Override
	public String toString() {
		return "Creative(" + name + ")";
	}
}