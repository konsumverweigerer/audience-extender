import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;

import java.util.UUID;

import org.junit.Test;

import play.mvc.Content;
import services.UuidHelper;

public class ApplicationTest {
	@Test
	public void uuidCheck() {
		final UUID a = UuidHelper.randomUUID("com.audienceextender.cookie");
		final UUID b = UuidHelper.randomUUID("com.audienceextender.cookie");
		final UUID c = UuidHelper.randomUUID("com.audienceextender.publisher");
		assertThat(a).isNotEqualTo(b);
		assertThat(a.toString()).isNotEqualTo(b.toString());
		assertThat(a.toString()).isNotEqualTo(c.toString());
		assertThat(UuidHelper.nameMd5(a)).isEqualTo(UuidHelper.nameMd5(b));
		assertThat(UuidHelper.nameMd5(a)).isNotEqualTo(UuidHelper.nameMd5(c));
	}

	@Test
	public void renderTemplate() {
		Content html = views.html.index.render(null);
		assertThat(contentType(html)).isEqualTo("text/html");
		assertThat(contentAsString(html)).contains("html");
	}
}
