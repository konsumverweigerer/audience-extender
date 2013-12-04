package models;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

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

	@ManyToOne(fetch = FetchType.LAZY)
	public Campaign campaign;

	public CampaignPackage(String name) {
		this.name = name;
	}

	public static CampaignPackage fromMap(Map<String, String> data) {
		final CampaignPackage campaignPackage = new CampaignPackage("New Package");
		campaignPackage.updateFromMap(data);
		return campaignPackage;
	}

	public static Finder<String, CampaignPackage> find = new Finder<String, CampaignPackage>(
			String.class, CampaignPackage.class);

	public static List<CampaignPackage> findByAdmin(Admin admin) {
		if (admin.isSysAdmin()) {
			return find.findList();
		}
		//TODO: search spezific packages
		return find.where().isNull("campaign.id").findList();
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public List<Message> write() {
		save();
		return Collections.emptyList();
	}

	public void updateFromMap(Map<String, String> data) {

	}

	public static Option<CampaignPackage> findById(String packageid, Admin admin) {
		List<CampaignPackage> ret = null;
		final Long id = packageid != null ? Long.valueOf(packageid) : 0L;
		if (admin.isSysAdmin()) {
			ret = find.where().eq("id", id).findList();
		} else {
			ret = find.where().isNull("campaign.id").eq("id", id)
					.findList();
		}
		//TODO: search spezific packages
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

	public String toString() {
		return "CampaignPackage(" + name + ")";
	}
}