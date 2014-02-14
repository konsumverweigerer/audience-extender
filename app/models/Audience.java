package models;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.Option;
import scala.Some;
import scala.Tuple5;
import scala.collection.JavaConversions;
import services.StatsHandler;

import com.avaje.ebean.Ebean;

/**
 * @author grp14818
 * 
 */
@Entity
public class Audience extends Model {
	public static final long DAY = 24 * 60 * 60 * 1000L;

	private static final long serialVersionUID = 2627475585121741565L;

	/**
	 * Retrieve all users.
	 */
	public static List<Audience> findAll() {
		return find.all();
	}

	public static List<Audience> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		return find.where().eq("publisher.owners.id", admin.getId()).findList();
	}

	public static Option<Audience> findById(Long id, Admin admin) {
		List<Audience> ret = null;
		if (admin.isSysAdmin()) {
			ret = find.fetch("websites").where().eq("id", id).findList();
		} else {
			ret = find.fetch("websites").where()
					.eq("publisher.owners.id", admin.getId()).eq("id", id)
					.findList();
		}
		if (!ret.isEmpty()) {
			return new Some<Audience>(ret.get(0));
		}
		return Option.empty();
	}

	public static Option<Audience> findById(String audienceid, Admin admin) {
		final Long id = audienceid != null ? Long.valueOf(audienceid) : 0L;
		return findById(id, admin);
	}

	public static Audience fromMap(Map<String, Object> data) {
		final Audience audience = new Audience("New Audience");
		audience.updateFromMap(data);
		return audience;
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
						} catch (final ParseException e) {
						}
					}
				}
				dataset.setTable(values);
				stats.add(dataset);
			}
		}
		return stats;
	}

	@Id
	private Long id;

	@Required
	private String name;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	/*
	 * allowed: values pending, active, cancelled
	 * 
	 * pending: not all needed cookies are active yet
	 * 
	 * active: all needed cookies are active
	 * 
	 * cancelled: deleted
	 */
	private String state;

	private String tracking;

	@ManyToOne(fetch = FetchType.LAZY)
	private Publisher publisher;

	@ManyToMany(fetch = FetchType.EAGER)
	private List<Website> websites = new ArrayList<Website>();

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "audience")
	private List<PathTarget> pathTargets = new ArrayList<PathTarget>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "audience")
	private List<Cookie> cookies = new ArrayList<Cookie>();

	public static Finder<Long, Audience> find = new Finder<Long, Audience>(
			Long.class, Audience.class);

	public Audience(String name) {
		this.name = name;
		this.state = "P";
		this.created = new Date();
	}

	public List<Cookie> getCookies() {
		return this.cookies;
	}

	public Date getCreated() {
		return this.created;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public List<PathTarget> getPathTargets() {
		return this.pathTargets;
	}

	public Publisher getPublisher() {
		return this.publisher;
	}

	public String getState() {
		return this.state;
	}

	public String getTracking() {
		return this.tracking;
	}

	public List<PathTarget> getWebsitePathTargets(Website website) {
		final List<PathTarget> ret = new ArrayList<PathTarget>();
		for (final PathTarget pathTarget : getPathTargets()) {
			if (website != null && pathTarget.getWebsite() != null
					&& website.getId().equals(pathTarget.getWebsite().getId())) {
				ret.add(pathTarget);
			}
		}
		return ret;
	}

	public List<Website> getWebsites() {
		return this.websites;
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public void setCookies(List<Cookie> cookies) {
		this.cookies = cookies;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPathTargets(List<PathTarget> pathTargets) {
		this.pathTargets = pathTargets;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setTracking(String tracking) {
		this.tracking = tracking;
	}

	public void setWebsites(List<Website> websites) {
		this.websites = websites;
	}

	@Override
	public String toString() {
		return "Audience(" + this.name + ")";
	}

	public Audience updateFromMap(Map<String, Object> data) {
		return this;
	}

	/**
	 * check if all required cookies are active
	 * 
	 * @return
	 */
	public boolean checkState() {
		// TODO: implement
		return false;
	}

	public List<Message> write() {
		// TODO: check website/paths if new cookies are needed
		save();
		for (final PathTarget pathTarget : this.pathTargets) {
			pathTarget.save();
			pathTarget.update();
		}
		Ebean.saveManyToManyAssociations(this, "websites");
		for (final Website website : getWebsites()) {
			boolean valid = false;
			final Collection<PathTarget> paths = getWebsitePathTargets(website);
			for (final Cookie cookie : getCookies()) {
				if (cookie.checkCookie(this, website, paths)) {
					valid = true;
				} else if (website.getId().equals(cookie.getWebsite().getId())) {
					cookie.setState("C");
					cookie.update();
					setState("P");
				}
			}
			if (!valid) {
				final Cookie cookie = Cookie
						.instance("Cookie for " + getName(), "code", this,
								website, paths);
				cookie.save();
				setState("P");
			}
		}
		update();
		return Collections.emptyList();
	}
}