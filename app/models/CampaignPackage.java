package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import services.UuidHelper;

@Entity
public class CampaignPackage extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Required
	public String name;

	public String variant;

	public CampaignPackage(String name) {
		this.name = name;
	}

	public static Finder<String, CampaignPackage> find = new Finder<String, CampaignPackage>(
			String.class, CampaignPackage.class);

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