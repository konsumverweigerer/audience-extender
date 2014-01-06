package models;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class CreativeStatData extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	/**
	 * Retrieve all cookies.
	 */
	public static List<CreativeStatData> findAll() {
		return find.all();
	}

	@Id
	private Long id;

	@Required
	private String timestep;
	@Required
	private long views = 0;
	@Column(precision = 6, scale = 6)
	private BigDecimal revenue;

	@Column(precision = 6, scale = 6)
	private BigDecimal cost;

	@ManyToOne(fetch = FetchType.LAZY)
	private Creative creative;

	public static Finder<Long, CreativeStatData> find = new Finder<Long, CreativeStatData>(
			Long.class, CreativeStatData.class);

	public CreativeStatData(String timestep) {
		this.timestep = timestep;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public Creative getCreative() {
		return creative;
	}

	public Long getId() {
		return id;
	}

	public BigDecimal getRevenue() {
		return revenue;
	}

	public String getTimestep() {
		return timestep;
	}

	public long getViews() {
		return views;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public void setCreative(Creative creative) {
		this.creative = creative;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setRevenue(BigDecimal revenue) {
		this.revenue = revenue;
	}

	public void setTimestep(String timestep) {
		this.timestep = timestep;
	}

	public void setViews(long views) {
		this.views = views;
	}

	@Override
	public String toString() {
		return "Creative(" + timestep + ":" + views + ")";
	}
}