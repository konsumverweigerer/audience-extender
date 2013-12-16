import java.util.List;
import java.util.Map;

import models.Admin;
import models.Audience;
import models.Campaign;
import models.CampaignPackage;
import models.PathTarget;
import models.Publisher;
import models.Website;
import play.Application;
import play.GlobalSettings;
import play.libs.Yaml;

import com.avaje.ebean.Ebean;

public class Global extends GlobalSettings {
	public void onStart(Application app) {
		InitialData.insert(app);
	}

	static class InitialData {
		public static void insert(Application app) {
			@SuppressWarnings("unchecked")
			Map<String, List<Object>> all = (Map<String, List<Object>>) Yaml
					.load("initial-data.yml");

			if (Ebean.find(Admin.class).findRowCount() == 0) {
				Ebean.save(all.get("admins"));
			}
			if (Ebean.find(Publisher.class).findRowCount() == 0) {
				Ebean.save(all.get("publishers"));
				for (Publisher publisher : Publisher.findAll()) {
					Ebean.saveManyToManyAssociations(publisher, "owners");
				}
			}
			if (Ebean.find(Website.class).findRowCount() == 0) {
				Ebean.save(all.get("websites"));
			}
			if (Ebean.find(CampaignPackage.class).findRowCount() == 0) {
				Ebean.save(all.get("campaign-packages"));
			}
			if (Ebean.find(Audience.class).findRowCount() == 0) {
				Ebean.save(all.get("audiences"));
			}
			if (Ebean.find(PathTarget.class).findRowCount() == 0) {
				Ebean.save(all.get("path-targets"));
			}
			if (Ebean.find(Campaign.class).findRowCount() == 0) {
				Ebean.save(all.get("campaigns"));
			}
		}
	}
}