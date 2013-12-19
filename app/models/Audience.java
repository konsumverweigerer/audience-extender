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
import scala.Tuple5;
import scala.collection.JavaConversions;
import services.StatsHandler;

@Entity
public class Audience extends Model {
	public static final long DAY = 24 * 60 * 60 * 1000L;

	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	@Temporal(TemporalType.TIMESTAMP)
	public Date created;

	/*
	 * allowed: values pending, active, cancelled
	 *
	 * pending: not all needed cookies are active yet
	 *
	 * active: all needed cookies are active
	 *
	 * cancelled: deleted
	 */
	public String state;
	public String tracking;

	@ManyToOne(fetch = FetchType.LAZY)
	public Publisher publisher;

	@ManyToMany(fetch = FetchType.EAGER)
	public List<Website> websites = new ArrayList<Website>();

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "audience")
	public List<PathTarget> pathTargets = new ArrayList<PathTarget>();

	public Audience(String name) {
		this.name = name;
		this.state = "P";
		this.created = new Date();
	}

	public static Audience fromMap(Map<String, Object> data) {
		final Audience audience = new Audience("New Audience");
		audience.updateFromMap(data);
		return audience;
	}

	public static Finder<String, Audience> find = new Finder<String, Audience>(
			String.class, Audience.class);

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
			for (final Audience audience : findByAdmin(admin)) {
				final Dataset dataset = new Dataset();
				dataset.setName(audience.name);
				dataset.setTimeframe(timeframe);
				final Option<scala.collection.immutable.List<Tuple5<Object, Object, String, String, BigDecimal>>> data = StatsHandler
						.findcookiestats(audience.id, startDate, endDate, prec);
				final Map<Number, Number> values = new TreeMap<Number, Number>(
						iv);
				if (data.nonEmpty()) {
					Logger.debug("have " + data.get().size()
							+ " cookie stat columns for " + audience);
					for (final Tuple5<Object, Object, String, String, BigDecimal> dat : JavaConversions
							.seqAsJavaList(data.get())) {
						try {
							final long t = df.parse(dat._3()).getTime();
							final int s = dat._5().intValue();
							if (values.containsKey(t)) {
								values.put(t, values.get(t).intValue() + s);
							} else {
								values.put(t, s);
							}
						} catch (ParseException e) {
						}
					}
				}
				dataset.setTable(values);
				stats.add(dataset);
			}
		}
		return stats;
	}

	/**
	 * Retrieve all users.
	 */
	public static List<Audience> findAll() {
		return find.all();
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public List<Message> write() {
		// TODO: check website/paths if new cookies are needed
		save();
		for (final PathTarget pathTarget: this.pathTargets) {
			pathTarget.save();
			pathTarget.update();
		}
		Ebean.saveManyToManyAssociations(this, "websites");
		update();
		return Collections.emptyList();
	}

	public Audience updateFromMap(Map<String, Object> data) {
		return this;
	}

	public static List<Audience> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("publisher.owners.id", admin.id).findList();
	}

	public static Option<Audience> findById(String audienceid, Admin admin) {
		final Long id = audienceid != null ? Long.valueOf(audienceid) : 0L;
		return findById(id, admin);
	}

	public static Option<Audience> findById(Long id, Admin admin) {
		List<Audience> ret = null;
		if (admin.isSysAdmin()) {
			ret = find.where().eq("id", id).findList();
		} else {
			ret = find.where().eq("publisher.owners.id", admin.id).eq("id", id)
					.findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Audience>(ret.get(0));
		}
		return Option.empty();
	}

	@Override
	public String toString() {
		return "Audience(" + name + ")";
	}
}