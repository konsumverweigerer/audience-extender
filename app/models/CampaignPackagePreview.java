package models;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.db.ebean.Model;
import scala.Option;
import scala.Some;

@Entity
public class CampaignPackagePreview extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	public Long id;

	@Temporal(TemporalType.TIMESTAMP)
	public Date created;

	/*
	 * allowed values: image/png, image/gif
	 */
	public String variant;

	public byte[] data;

	@ManyToOne(fetch = FetchType.LAZY)
	public CampaignPackage campaignPackage;

	public CampaignPackagePreview() {
	}

	public static CampaignPackagePreview fromMap(Map<String, Object> data) {
		final CampaignPackagePreview creative = new CampaignPackagePreview();
		creative.updateFromMap(data);
		return creative;
	}

	public static Finder<String, CampaignPackagePreview> find = new Finder<String, CampaignPackagePreview>(
			String.class, CampaignPackagePreview.class);

	public static Option<CampaignPackagePreview> addUpload(Publisher publisher,
			String contentType, String filename, File file) {
		return null;
	}

	public CampaignPackagePreview updateFromMap(Map<String, Object> data) {
		return this;
	}

	/**
	 * Retrieve all creatives.
	 */
	public static List<CampaignPackagePreview> findAll() {
		return find.all();
	}

	public static Option<CampaignPackagePreview> findById(String creativeid,
			Admin admin) {
		final Long id = creativeid != null ? Long.valueOf(creativeid) : 0L;
		return findById(id, admin);
	}

	public static Option<CampaignPackagePreview> findById(Long id, Admin admin) {
		List<CampaignPackagePreview> ret = null;
		ret = find.where().eq("id", id).findList();
		if (!ret.isEmpty()) {
			return new Some<CampaignPackagePreview>(ret.get(0));
		}
		return Option.empty();
	}

	@Override
	public String toString() {
		return "CampaignPackage()";
	}
}