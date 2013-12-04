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
	public Long id;

	@Required
	public String timestep;

	public String sub;

	@Required
	public long views = 0;

	@ManyToOne(fetch = FetchType.LAZY)
	public Cookie cookie;

	public CookieStatData(String timestep) {
		this.timestep = timestep;
	}

	public static Finder<String, CookieStatData> find = new Finder<String, CookieStatData>(
			String.class, CookieStatData.class);

	/**
	 * Retrieve all cookies.
	 */
	public static List<CookieStatData> findAll() {
		return find.all();
	}

	public String toString() {
		return "Cookie(" + timestep + ":" + views + ")";
	}
}