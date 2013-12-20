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

import com.avaje.ebean.Ebean;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.Option;
import scala.Some;
import scala.Tuple4;
import services.StatsHandler;

@Entity
public class Campaign extends Model {
	public static final long DAY = 24 * 60 * 60 * 1000L;

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

	/*
	 * allowed: values pending, active, cancelled
	 *
	 */
	public String state;
	
	@ManyToOne(fetch = FetchType.EAGER)
	public CampaignPackage campaignPackage;

	@ManyToMany(fetch = FetchType.EAGER)
	public List<Audience> audiences;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "campaign")
	public List<Creative> creatives;

	public Campaign(String name) {
		this.name = name;
		this.created = new Date();
	}

	public static Campaign fromMap(Map<String, Object> data) {
		final Campaign campaign = new Campaign("New Campaign");
		campaign.updateFromMap(data);
		return campaign;
	}

	public static Finder<String, Campaign> find = new Finder<String, Campaign>(
			String.class, Campaign.class);

	public static List<Dataset> statsByAdmin(Admin admin) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		return stats;
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

	public static List<Dataset> statsByAdmin(Admin admin, Long from, Long to) {
		final List<Dataset> stats = new ArrayList<Dataset>();
		if (from != null && to != null) {
			final Date startDate = new Date(from);
			final Date endDate = new Date(to);
			String timeframe = "months";
			DateFormat df = new SimpleDateFormat("yyyyMM");
			int prec = 6;
			if ((to - from) < (2 * DAY)) {
				timeframe = "hours";
				prec = 10;
				df = new SimpleDateFormat("yyyyMMddHH");
			} else if ((to - from) < (3500 * DAY)) {
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
						.findcreativestats(campaign.id, startDate, endDate,
								prec);
				if (data.nonEmpty()) {
					Logger.debug("have " + data.get().size()
							+ " creative stat columns for " + campaign);
					for (final Tuple4<Object, Object, String, BigDecimal> dat : scala.collection.JavaConversions
							.seqAsJavaList(data.get())) {
						try {
							final long t = df.parse(dat._3()).getTime();
							double s = dat._4().doubleValue();
							if (campaign.value != null) {
								s *= campaign.value.doubleValue();
							}
							if (revenues.containsKey(t)) {
								revenues.put(t, revenues.get(t).intValue() + s);
							} else {
								revenues.put(t, s);
							}
						} catch (ParseException e) {
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
		for (final Creative creative : this.creatives) {
			creative.save();
			creative.campaign = this;
			creative.update();
		}
		if (this.campaignPackage != null) {
			this.campaignPackage.save();
		}
		Ebean.saveManyToManyAssociations(this, "audiences");
		update();
		return Collections.emptyList();
	}

	public Campaign updateFromMap(Map<String, Object> data) {
		return this;
	}

	public static Option<Campaign> findById(String campaignid, Admin admin) {
		final Long id = campaignid != null ? Long.valueOf(campaignid) : 0L;
		return findById(id, admin);
	}

	public static Option<Campaign> findById(Long id, Admin admin) {
		List<Campaign> ret = null;
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