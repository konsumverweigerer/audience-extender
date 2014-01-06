package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class CookieStatData extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	private Long id;

	@Required
	private String timestep;

	private String sub;

	@Required
	private long views = 0;

	@ManyToOne(fetch = FetchType.LAZY)
	private Cookie cookie;

	public static Finder<Long, CookieStatData> find = new Finder<Long, CookieStatData>(
			Long.class, CookieStatData.class);

	public CookieStatData(String timestep) {
		this.timestep = timestep;
	}

	/**
	 * Retrieve all cookies.
	 */
	public static List<CookieStatData> findAll() {
		return find.all();
	}

	public Cookie getCookie() {
		return cookie;
	}

	public Long getId() {
		return id;
	}

	public String getSub() {
		return sub;
	}

	public String getTimestep() {
		return timestep;
	}

	public long getViews() {
		return views;
	}

	public void setCookie(Cookie cookie) {
		this.cookie = cookie;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public void setTimestep(String timestep) {
		this.timestep = timestep;
	}

	public void setViews(long views) {
		this.views = views;
	}

	@Override
	public String toString() {
		return "Cookie(" + timestep + ":" + views + ")";
	}
}