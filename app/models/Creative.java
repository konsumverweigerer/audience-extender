package models;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.Option;

@Entity
public class Creative extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	public String url;

	public byte[] data;

	public String uuid;

	@ManyToOne(fetch = FetchType.LAZY)
	public Campaign campaign;

	public Creative(String name, Option<String> url) {
		this.name = name;
		this.url = url.orNull(null);
	}

	public static Creative fromMap(Map<String, Object> data) {
		final Creative creative = new Creative("New Creative",
				Option.<String> empty());
		creative.updateFromMap(data);
		return creative;
	}

	public static Finder<String, Creative> find = new Finder<String, Creative>(
			String.class, Creative.class);

	public static Option<Creative> addUpload(Publisher publisher,
			String contentType, String filename, File file) {
		return null;
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

	public String getPreview() {
		return this.url;
	}

	@Override
	public String toString() {
		return "Creative(" + name + ")";
	}
}