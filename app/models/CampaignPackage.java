package models;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
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

@Entity
public class CampaignPackage extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	/**
	 * Retrieve all packages.
	 */
	public static List<CampaignPackage> findAll() {
		return find.all();
	}

	public static List<CampaignPackage> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		// TODO: search spezific packages
		return find.where().isNull("campaign.id").findList();
	}

	public static Option<CampaignPackage> findById(Long id, Admin admin) {
		List<CampaignPackage> ret = null;
		if (admin.isSysAdmin()) {
			ret = find.where().eq("id", id).findList();
		} else {
			ret = find.where().isNull("campaign.id").eq("id", id).findList();
		}
		// TODO: search spezific packages
		if (!ret.isEmpty()) {
			return new Some<CampaignPackage>(ret.get(0));
		}
		return Option.empty();
	}

	public static Option<CampaignPackage> findById(String packageid, Admin admin) {
		final Long id = packageid != null ? Long.valueOf(packageid) : 0L;
		return findById(id, admin);
	}

	public static CampaignPackage fromMap(Map<String, Object> data) {
		final CampaignPackage campaignPackage = new CampaignPackage(
				"New Package");
		campaignPackage.updateFromMap(data);
		return campaignPackage;
	}

	@Id
	private Long id;
	@Required
	private String name;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	/*
	 * allowed values: custom, monthly, weekly, daily
	 */
	private String variant;
	private String state;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	private Long impressions;
	private Long reach;
	private Long goal;

	@Column(precision = 6, scale = 6)
	private BigDecimal buyCpm;

	@Column(precision = 6, scale = 6)
	private BigDecimal salesCpm;

	@ManyToOne(fetch = FetchType.LAZY)
	private Publisher publisher;

	@ManyToOne(fetch = FetchType.EAGER)
	private Campaign campaign;

	@ManyToOne(fetch = FetchType.EAGER)
	private CampaignPackage campaignPackage;

	public static Finder<Long, CampaignPackage> find = new Finder<Long, CampaignPackage>(
			Long.class, CampaignPackage.class);

	public CampaignPackage(String name) {
		this.name = name;
		this.created = new Date();
	}

	public BigDecimal getBuyCpm() {
		return this.buyCpm;
	}

	public Campaign getCampaign() {
		return this.campaign;
	}

	public CampaignPackage getCampaignPackage() {
		return this.campaignPackage;
	}

	public Date getCreated() {
		return this.created;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public Long getGoal() {
		return this.goal;
	}

	public Long getId() {
		return this.id;
	}

	public Long getImpressions() {
		return this.impressions;
	}

	public String getName() {
		return this.name;
	}

	public Publisher getPublisher() {
		return this.publisher;
	}

	public Long getReach() {
		return this.reach;
	}

	public BigDecimal getSalesCpm() {
		return this.salesCpm;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public String getState() {
		return this.state;
	}

	public String getVariant() {
		return this.variant;
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public void setBuyCpm(BigDecimal buyCpm) {
		this.buyCpm = buyCpm;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	public void setCampaignPackage(CampaignPackage campaignPackage) {
		this.campaignPackage = campaignPackage;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setGoal(Long goal) {
		this.goal = goal;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setImpressions(Long impressions) {
		this.impressions = impressions;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public void setReach(Long reach) {
		this.reach = reach;
	}

	public void setSalesCpm(BigDecimal salesCpm) {
		this.salesCpm = salesCpm;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	@Override
	public String toString() {
		return "CampaignPackage(" + this.name + ")";
	}

	public CampaignPackage updateFromMap(Map<String, Object> data) {
		return this;
	}

	public List<Message> write() {
		save();
		update();
		return Collections.emptyList();
	}
}