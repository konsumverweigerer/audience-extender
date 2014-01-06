package models;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.Option;
import scala.Some;
import scala.Tuple4;
import services.StatsHandler;

import com.avaje.ebean.Ebean;

@Entity
public class Campaign extends Model {
	public static final long DAY = 24 * 60 * 60 * 1000L;

	private static final long serialVersionUID = 2627475585121741565L;

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
		return find.where().eq("publisher.owners.id", admin.getId()).findList();
	}

	public static Option<Campaign> findById(Long id, Admin admin) {
		List<Campaign> ret = null;
		if (admin.isSysAdmin()) {
			ret = find.fetch("audiences").where().eq("id", id).findList();
		} else {
			ret = find.fetch("audiences").where().eq("publisher.owners.id", admin.getId())
					.eq("id", id).findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Campaign>(ret.get(0));
		}
		return Option.empty();
	}

	public static Option<Campaign> findById(String campaignid, Admin admin) {
		final Long id = campaignid != null ? Long.valueOf(campaignid) : 0L;
		return findById(id, admin);
	}

	public static Campaign fromMap(Map<String, Object> data) {
		final Campaign campaign = new Campaign("New Campaign");
		campaign.updateFromMap(data);
		return campaign;
	}

	private static Map<Number, Number> initValues(long from, long to,
			String timeframe) {
		long step = 7 * 24 * 60 * 60 * 1000;
		if ("hours".equals(timeframe)) {
			step = 60 * 60 * 1000;
		} else if ("days".equals(timeframe)) {
			step = 24 * 60 * 60 * 1000;
		}
		final Map<Number, Number> map = new HashMap<Number, Number>();
		for (long i = from; i < to; i += step) {
			map.put(i, 0);
		}
		Logger.debug("created " + map.size() + " bins from " + from + " to "
				+ to);
		return map;
	}

	public static List<Dataset> statsByAdmin(Admin admin) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		return stats;
	}

	public static List<Dataset> statsByAdmin(Admin admin, Long from, Long to) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		if (from != null && to != null) {
			final Date startDate = new Date(from);
			final Date endDate = new Date(to);
			String timeframe = "months";
			DateFormat df = new SimpleDateFormat("yyyyMM");
			int prec = 6;
			if (to - from < 2 * DAY) {
				timeframe = "hours";
				prec = 10;
				df = new SimpleDateFormat("yyyyMMddHH");
			} else if (to - from < 3500 * DAY) {
				timeframe = "days";
				prec = 8;
				df = new SimpleDateFormat("yyyyMMdd");
			}
			final Map<Number, Number> iv = initValues(from, to, timeframe);
			final Dataset revenue = new Dataset();
			final Dataset cost = new Dataset();
			final Dataset profit = new Dataset();
			revenue.setName("Revenue");
			revenue.setCls("revenue");
			revenue.setTimeframe(timeframe);
			cost.setName("Cost");
			cost.setCls("cost");
			cost.setTimeframe(timeframe);
			profit.setName("Profit");
			profit.setCls("profit");
			profit.setTimeframe(timeframe);
			final Map<Number, Number> revenues = new TreeMap<Number, Number>(iv);
			final Map<Number, Number> costs = new TreeMap<Number, Number>(iv);
			final Map<Number, Number> profits = new TreeMap<Number, Number>(iv);
			for (final Campaign campaign : findByAdmin(admin)) {
				final Option<scala.collection.immutable.List<Tuple4<Object, Object, String, BigDecimal>>> data = StatsHandler
						.findcreativestats(campaign.getId(), startDate,
								endDate, prec);
				if (data.nonEmpty()) {
					Logger.debug("have " + data.get().size()
							+ " creative stat columns for " + campaign);
					for (final Tuple4<Object, Object, String, BigDecimal> dat : scala.collection.JavaConversions
							.seqAsJavaList(data.get())) {
						try {
							final long t = df.parse(dat._3()).getTime();
							double s = dat._4().doubleValue();
							if (campaign.getValue() != null) {
								s *= campaign.getValue().doubleValue();
							}
							if (revenues.containsKey(t)) {
								revenues.put(t, revenues.get(t).intValue() + s);
							} else {
								revenues.put(t, s);
							}
						} catch (final ParseException e) {
						}
					}
				}
			}
			revenue.setTable(revenues);
			cost.setTable(costs);
			profit.setTable(profits);
			stats.add(revenue);
			stats.add(cost);
			stats.add(profit);
		}
		return stats;
	}

	@Id
	private Long id;

	@Required
	private String name;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@ManyToOne(fetch = FetchType.LAZY)
	private Publisher publisher;

	@Column(precision = 6, scale = 4)
	private BigDecimal value;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	/*
	 * allowed: values pending, active, cancelled
	 */
	private String state;

	private String variant;

	@ManyToOne(fetch = FetchType.EAGER)
	private CampaignPackage campaignPackage;

	@ManyToMany(fetch = FetchType.EAGER)
	private List<Audience> audiences;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "campaign")
	private List<Creative> creatives;

	public static Finder<Long, Campaign> find = new Finder<Long, Campaign>(
			Long.class, Campaign.class);

	public Campaign(String name) {
		this.name = name;
		this.created = new Date();
	}

	public List<Audience> getAudiences() {
		return this.audiences;
	}

	public CampaignPackage getCampaignPackage() {
		return this.campaignPackage;
	}

	public Date getCreated() {
		return this.created;
	}

	public List<Creative> getCreatives() {
		return this.creatives;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public Publisher getPublisher() {
		return this.publisher;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public String getState() {
		return this.state;
	}

	public BigDecimal getValue() {
		return this.value;
	}

	public String getVariant() {
		return this.variant;
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public void setAudiences(List<Audience> audiences) {
		this.audiences = audiences;
	}

	public void setCampaignPackage(CampaignPackage campaignPackage) {
		this.campaignPackage = campaignPackage;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setCreatives(List<Creative> creatives) {
		this.creatives = creatives;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	@Override
	public String toString() {
		return "Campaign(" + this.name + ")";
	}

	public Campaign updateFromMap(Map<String, Object> data) {
		return this;
	}

	public List<Message> write() {
		save();
		for (final Creative creative : getCreatives()) {
			creative.save();
			creative.setCampaign(this);
			creative.update();
		}
		if (getCampaignPackage() != null) {
			getCampaignPackage().save();
		}
		Ebean.saveManyToManyAssociations(this, "audiences");
		update();
		return Collections.emptyList();
	}
}