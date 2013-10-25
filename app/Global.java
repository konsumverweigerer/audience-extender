import java.util.List;
import java.util.Map;

import models.Admin;
import models.Publisher;
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
			if (Ebean.find(Admin.class).findRowCount() == 0
					|| Ebean.find(Publisher.class).findRowCount() == 0) {

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
			}
		}
	}
}