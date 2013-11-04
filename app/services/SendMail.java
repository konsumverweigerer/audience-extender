package services;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;

public class SendMail {
	private boolean sendHtmlMail(String subject, String from, String text,
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
			return false;
		}
		return true;
	}
}
