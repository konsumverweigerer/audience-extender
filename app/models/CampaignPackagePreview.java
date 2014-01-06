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

	public static Option<CampaignPackagePreview> addUpload(Publisher publisher,
			String contentType, String filename, File file) {
		return null;
	}

	/**
	 * Retrieve all creatives.
	 */
	public static List<CampaignPackagePreview> findAll() {
		return find.all();
	}

	public static Option<CampaignPackagePreview> findById(Long id, Admin admin) {
		List<CampaignPackagePreview> ret = null;
		ret = find.where().eq("id", id).findList();
		if (!ret.isEmpty()) {
			return new Some<CampaignPackagePreview>(ret.get(0));
		}
		return Option.empty();
	}

	public static Option<CampaignPackagePreview> findById(String creativeid,
			Admin admin) {
		final Long id = creativeid != null ? Long.valueOf(creativeid) : 0L;
		return findById(id, admin);
	}

	public static CampaignPackagePreview fromMap(Map<String, Object> data) {
		final CampaignPackagePreview creative = new CampaignPackagePreview();
		creative.updateFromMap(data);
		return creative;
	}

	@Id
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	/*
	 * allowed values: image/png, image/gif
	 */
	private String variant;

	private byte[] data;

	@ManyToOne(fetch = FetchType.LAZY)
	private CampaignPackage campaignPackage;

	public static Finder<Long, CampaignPackagePreview> find = new Finder<Long, CampaignPackagePreview>(
			Long.class, CampaignPackagePreview.class);

	public CampaignPackagePreview() {
	}

	public CampaignPackage getCampaignPackage() {
		return campaignPackage;
	}

	public Date getCreated() {
		return created;
	}

	public byte[] getData() {
		return data;
	}

	public Long getId() {
		return id;
	}

	public String getVariant() {
		return variant;
	}

	public void setCampaignPackage(CampaignPackage campaignPackage) {
		this.campaignPackage = campaignPackage;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	@Override
	public String toString() {
		return "CampaignPackage()";
	}

	public CampaignPackagePreview updateFromMap(Map<String, Object> data) {
		return this;
	}
}