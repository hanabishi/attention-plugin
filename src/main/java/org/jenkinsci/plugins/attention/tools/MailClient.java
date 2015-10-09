package org.jenkinsci.plugins.attention.tools;

import hudson.model.Run;
import hudson.model.User;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import jenkins.model.Jenkins;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jenkinsci.plugins.attention.VolunteerCollection;
import org.jenkinsci.plugins.attention.response.Team;

public class MailClient {

    private String host;
    private String user;
    private String password;
    private String replyTo;
    private String from;
    private List<Team> teamList;

    public MailClient(String host, String user, String password, String replyTo, String from,
            List<Team> teamList) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.replyTo = replyTo;
        this.from = from;
        this.teamList = teamList;
    }

    public void sendMail(final String to, final String cc, final Run<?, ?> build, final String body)
            throws AddressException, MessagingException {
        sendMail(to, cc, "Updated volunteer status for " + build.getFullDisplayName(), body);
    }

    public void sendMail(final String to, final String cc, final String subject, final String body)
            throws AddressException, MessagingException {
        final Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", this.host);
        properties.put("mail.smtp.password", this.password);
        properties.put("mail.smtp.user", this.user);
        final Session session = Session.getDefaultInstance(properties);
        final MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));

        try {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
        } catch (Exception e) {
            e.printStackTrace();
        }
        message.setSubject(subject);
        Address[] reply = new Address[1];
        reply[0] = new InternetAddress(this.replyTo);
        message.setReplyTo(reply);
        message.setContent(body, "text/html");
        Transport.send(message);
    }

    public String translatToEmail(User user) throws MalformedURLException, DocumentException {
        String email = "";
        URL apiURL = null;
        if (Jenkins.getInstance().getRootUrl() != null) {
            apiURL = new URL(Jenkins.getInstance().getRootUrl() + "/securityRealm/user/" + user.getId() + "/api/xml");
        } else {
            apiURL = new URL("/securityRealm/user/" + user.getId() + "/api/xml");
        }

        @SuppressWarnings("unchecked")
        List<Element> elements = new SAXReader().read(apiURL).getRootElement().elements("property");
        for (Element elem : elements) {
            Element add = elem.element("address");
            if (add != null) {
                email = add.getText();
                break;
            }
        }

        return email;
    }

    public String translatToEmail(VolunteerCollection volunteerer) throws InvalidParameterException,
            MalformedURLException, DocumentException {
        if (volunteerer.isTeam()) {
            for (Team team : teamList) {
                if (team.getName().equalsIgnoreCase(volunteerer.getId())) {
                    return team.getMail();
                }
            }
        } else {
            return translatToEmail(User.get(volunteerer.getId()));
        }
        throw new InvalidParameterException("Unable to find a mail matching " + volunteerer.getId());
    }

    public void notifyNewInvestigator(VolunteerCollection volunteerer, User current, Run<?, ?> build)
            throws AddressException, MessagingException, InvalidParameterException, MalformedURLException,
            DocumentException {
        if (current.getId().equalsIgnoreCase(volunteerer.getId())) {
            return;
        }

        String body = volunteerer.getFullName() + " was assigned to investigate the build <a href=\""
                + Jenkins.getInstance().getRootUrl() + "/" + build.getUrl() + "\">" + build.getFullDisplayName()
                + "</a> by " + current.getFullName() + ".<br />";

        body += "<br /><b>Comments:</b><br /><i>" + volunteerer.getComment().replaceAll(Pattern.quote("\n"), "<br />")
                + "</i>";
        sendMail(translatToEmail(volunteerer), translatToEmail(current), build, body);
    }

    public void notifyNewFixer(VolunteerCollection volunteerer, User current, Run<?, ?> build) throws AddressException,
            MessagingException, InvalidParameterException, MalformedURLException, DocumentException {
        if (current.getId().equalsIgnoreCase(volunteerer.getId())) {
            return;
        }

        String body = volunteerer.getFullName() + " was assigned to investigate the failing build <a href=\""
                + Jenkins.getInstance().getRootUrl() + "/" + build.getUrl() + "\">" + build.getFullDisplayName()
                + "</a> by " + current.getFullName() + ".<br />";
        body += "<br /><b>Comments:</b><br /><i>" + volunteerer.getComment().replaceAll(Pattern.quote("\n"), "<br />")
                + "</i>";
        sendMail(translatToEmail(volunteerer), translatToEmail(current), build, body);
    }

    public void notifyUnVolunteered(VolunteerCollection volunteerer, User current, Run<?, ?> build)
            throws AddressException, MessagingException, InvalidParameterException, MalformedURLException,
            DocumentException {
        if (current.getId().equalsIgnoreCase(volunteerer.getId())) {
            return;
        }

        String body = volunteerer.getFullName() + " was removed from the volunteer list on <a href=\""
                + Jenkins.getInstance().getRootUrl() + "/" + build.getUrl() + "\">" + build.getFullDisplayName()
                + "</a> by " + current.getFullName() + ".<br />";
        sendMail(translatToEmail(volunteerer), translatToEmail(current), build, body);
    }

}
