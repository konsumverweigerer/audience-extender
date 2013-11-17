package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class PathTarget extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String urlPath;

	public String variant;

	@ManyToOne(fetch = FetchType.LAZY)
	public Audience audience;

	@ManyToOne(fetch = FetchType.LAZY)
	public Website website;

	public PathTarget(String urlPath) {
		this.urlPath = urlPath;
	}

	public static Finder<String, PathTarget> find = new Finder<String, PathTarget>(
			String.class, PathTarget.class);

	/**
	 * Retrieve all packages.
	 */
	public static List<PathTarget> findAll() {
		return find.all();
	}

	public String toString() {
		return "PathTarget(" + urlPath + ")";
	}
}