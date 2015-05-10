package circle.internet.email;

import java.security.Security;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import circle.box.gallery.ConvertImage;
import circle.db.models.Gallery;

import android.util.Log;

public class MailSenderClass extends Authenticator {
	private static final String LOG_TAG = "monitoring";
	private String mailhost = "smtp.gmail.com";
	private static String user;
	private static String password;
	private static Session session;

	private Multipart _multipart;

	static {
		Security.addProvider(new JSSEProvider());
	}

	public MailSenderClass(String user, String password) {
		MailSenderClass.user = user;
		MailSenderClass.password = password;

		_multipart = new MimeMultipart();

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mailhost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.quitwait", "false");

		session = Session.getDefaultInstance(props, this);
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, password);
	}

	public synchronized void sendMail(String subject, String body,
			String sender, String recipients, ArrayList<Gallery> galleryList)
			throws Exception {

		Log.i(LOG_TAG, "sendMail - " + subject + "\n" + body + "\n" + sender
				+ "\n" + recipients + "\n");
		MimeMessage message = new MimeMessage(session);

		// ���
		message.setSender(new InternetAddress(sender));
		// � ���
		message.setSubject(subject);
		// ����
		if (recipients.indexOf(',') > 0)
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(recipients));
		else
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(
					recipients));

		// ����� �������
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(body);
		_multipart.addBodyPart(messageBodyPart);

		// � ��� ��������
		for (Gallery gallery : galleryList) {
			BodyPart attachBodyPart = new MimeBodyPart();
			byte[] imageByte = ConvertImage
					.convertBase64StringToByteArray(gallery.getData());
			DataSource source = new ByteArrayDataSource(imageByte, "image/bmp");
			attachBodyPart.setDataHandler(new DataHandler(source));
			attachBodyPart.setFileName(gallery.getTitle());

			_multipart.addBodyPart(attachBodyPart);
		}

		message.setContent(_multipart);

		Transport.send(message);

	}
}
