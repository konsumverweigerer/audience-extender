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

	@Id
	public Long id;

	@Required
	public String name;

	public String variant;

	@Temporal(TemporalType.TIMESTAMP)
	public Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	public Date endDate;

	public Long count;
	public Long reach;
	public Long goal;

	@Column(precision = 6, scale = 6)
	public BigDecimal buyCpm;
	@Column(precision = 6, scale = 6)
	public BigDecimal salesCpm;

	@ManyToOne(fetch = FetchType.LAZY)
	public Campaign campaign;

	public CampaignPackage(String name) {
		this.name = name;
	}

	public static CampaignPackage fromMap(Map<String, Object> data) {
		final CampaignPackage campaignPackage = new CampaignPackage(
				"New Package");
		campaignPackage.updateFromMap(data);
		return campaignPackage;
	}

	public static Finder<String, CampaignPackage> find = new Finder<String, CampaignPackage>(
			String.class, CampaignPackage.class);

	public static List<CampaignPackage> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		// TODO: search spezific packages
		return find.where().isNull("campaign.id").findList();
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public List<Message> write() {
		save();
		return Collections.emptyList();
	}

	public CampaignPackage updateFromMap(Map<String, Object> data) {
		return this;
	}

	public static Option<CampaignPackage> findById(String packageid, Admin admin) {
		List<CampaignPackage> ret = null;
		final Long id = packageid != null ? Long.valueOf(packageid) : 0L;
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

	/**
	 * Retrieve all packages.
	 */
	public static List<CampaignPackage> findAll() {
		return find.all();
	}

	@Override
	public String toString() {
		return "CampaignPackage(" + name + ")";
	}
}