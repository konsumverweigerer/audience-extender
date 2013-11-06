package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class CreativeStatData extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String timestep;

	@Required
	public long views = 0;

	@ManyToOne(fetch = FetchType.LAZY)
	public Creative creative;

	public CreativeStatData(String timestep) {
		this.timestep = timestep;
	}

	public static Finder<String, CreativeStatData> find = new Finder<String, CreativeStatData>(
			String.class, CreativeStatData.class);

	/**
	 * Retrieve all cookies.
	 */
	public static List<CreativeStatData> findAll() {
		return find.all();
	}

	public String toString() {
		return "Creative(" + timestep + ":" + views + ")";
	}
}