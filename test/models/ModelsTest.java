package models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;

import org.junit.Before;
import org.junit.Test;

import play.test.WithApplication;

public class ModelsTest extends WithApplication {
	@Before
	public void setUp() {
		start(fakeApplication(inMemoryDatabase()));
	}

	@Test
	public void createAndRetrieveAdmin() {
		new Admin("bob@gmail.com", "Bob", "secret").save();
		Admin bob = Admin.find.where().eq("email", "bob@gmail.com")
				.findUnique();
		assertNotNull(bob);
		assertEquals("Bob", bob.name);
	}
}
