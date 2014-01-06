package models;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class PathTarget extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	/**
	 * Retrieve all packages.
	 */
	public static List<PathTarget> findAll() {
		return find.all();
	}

	public static PathTarget fromMap(Map<String, Object> data) {
		final PathTarget pathTarget = new PathTarget("/");
		pathTarget.updateFromMap(data);
		return pathTarget;
	}

	@Id
	private Long id;

	@Required
	private String urlPath;

	/*
	 * allowed values: include, exclude
	 */
	private String variant;

	@ManyToOne(fetch = FetchType.EAGER)
	private Audience audience;

	@ManyToOne(fetch = FetchType.EAGER)
	private Website website;

	public static Finder<Long, PathTarget> find = new Finder<Long, PathTarget>(
			Long.class, PathTarget.class);

	public PathTarget(String urlPath) {
		this.urlPath = urlPath;
	}

	public Audience getAudience() {
		return audience;
	}

	public Long getId() {
		return id;
	}

	public String getUrlPath() {
		return urlPath;
	}

	public String getVariant() {
		return variant;
	}

	public Website getWebsite() {
		return website;
	}

	public void setAudience(Audience audience) {
		this.audience = audience;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	public void setWebsite(Website website) {
		this.website = website;
	}

	@Override
	public String toString() {
		return "PathTarget(" + urlPath + ")";
	}

	public PathTarget updateFromMap(Map<String, Object> data) {
		return this;
	}
}