package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.Option;
import scala.Some;

@Entity
public class Campaign extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	@Temporal(TemporalType.TIMESTAMP)
	public Date created;

	@ManyToOne(fetch = FetchType.LAZY)
	public Publisher publisher;

	@Column(precision = 6, scale = 4)
	public BigDecimal value;

	@Temporal(TemporalType.TIMESTAMP)
	public Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	public Date endDate;

	@ManyToOne(fetch = FetchType.EAGER)
	public CampaignPackage campaignPackage;

	@ManyToMany(fetch = FetchType.EAGER)
	public List<Audience> audiences;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "campaign")
	public List<Creative> creatives;

	public Campaign(String name) {
		this.name = name;
	}

	public static Campaign fromMap(Map<String, Object> data) {
		final Campaign campaign = new Campaign("New Website");
		campaign.updateFromMap(data);
		return campaign;
	}

	public static Finder<String, Campaign> find = new Finder<String, Campaign>(
			String.class, Campaign.class);

	public static List<Dataset> statsByAdmin(Admin admin) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		return stats;
	}

	public static List<Dataset> statsByAdmin(Admin admin, String from, String to) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		return stats;
	}

	/**
	 * Retrieve all users.
	 */
	public static List<Campaign> findAll() {
		return find.all();
	}

	public static List<Campaign> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("publisher.owners.id", admin.id).findList();
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public List<Message> write() {
		save();
		return Collections.emptyList();
	}

	public Campaign updateFromMap(Map<String, Object> data) {
		return this;
	}

	public static Option<Campaign> findById(String campaignid, Admin admin) {
		List<Campaign> ret = null;
		final Long id = campaignid != null ? Long.valueOf(campaignid) : 0L;
		if (admin.isSysAdmin()) {
			ret = find.where().eq("id", id).findList();
		} else {
			ret = find.where().eq("publisher.owners.id", admin.id).eq("id", id)
					.findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Campaign>(ret.get(0));
		}
		return Option.empty();
	}

	@Override
	public String toString() {
		return "Campaign(" + name + ")";
	}
}