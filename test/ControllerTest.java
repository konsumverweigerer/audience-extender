import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;

import java.util.logging.Logger;

import models.Admin;

import org.junit.Before;
import org.junit.Test;

import play.api.libs.json.JsValue;
import play.test.WithApplication;
import controllers.PublisherController;

public class ControllerTest extends WithApplication {
	@Before
	public void setUp() {
		start(fakeApplication(inMemoryDatabase()));
	}

	@Test
	public void publisherStatsJson() {
		JsValue json = PublisherController.publisherJson((Admin) Admin
				.findById("1").orNull(null));
		Logger.getLogger("test").info(json.toString());
		assertThat(json.toString()).contains("name");
	}
}
