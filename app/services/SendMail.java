package services;

import play.Logger;
import play.api.Application;
import play.api.Play;
import scala.Option;
import scala.collection.immutable.Set;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;

public class SendMail {
	public static Boolean sendWebsiteCodeEmail(Application app, String email,
			String code) {
		// TODO: add HTML version
		final Option<String> sendTo = Play.configuration(app).getString(
				"contactemail", Option.<Set<String>> empty());
		return sendMail("Audience extender: Cookie code",
				sendTo.nonEmpty() ? sendTo.get() : "root@localhost",
				"The code to integrate into your website is:\n\n" + code
						+ "\n\n" + "Please make sure you copy it as is", null,
				null, email);
	}

	public static Boolean sendContactMessage(Application app, String email,
			String name, String message) {
		final Option<String> sendTo = Play.configuration(app).getString(
				"contactemail", Option.<Set<String>> empty());
		return sendMail("Audience extender: Contact form", email, "From: "
				+ name + "\nMessage:\n\n" + message, null, null,
				sendTo.nonEmpty() ? sendTo.get() : "root@localhost");
	}

	private static boolean sendMail(String subject, String from, String text,
			String html, String[] ccs, String... recipients) {
		final MailerPlugin mailer = play.Play.application().plugin(
				MailerPlugin.class);
		try {
			final MailerAPI mail = mailer.email();
			mail.setSubject(subject);
			for (final String r : recipients) {
				mail.setRecipient(r);
			}
			if (ccs != null) {
				for (final String cc : ccs) {
					mail.setCc(cc);
				}
			}
			mail.setFrom(from);
			if (text == null) {
				mail.sendHtml(html);
			} else if (html == null) {
				mail.send(text);
			} else {
				mail.send(text, html);
			}
		} catch (final Exception e) {
			Logger.warn(e.getMessage(), e);
			return false;
		}
		return true;
	}
}
