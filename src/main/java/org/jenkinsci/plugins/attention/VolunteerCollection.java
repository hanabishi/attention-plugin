package org.jenkinsci.plugins.attention;

import hudson.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.jenkinsci.plugins.attention.pbe.DetectedIssue;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class VolunteerCollection {
    private String volunteeredByID = "";
    private String id = "";
    private boolean team = false;
    private Date volunteerDate = null;
    private String comment = "";
    private boolean fixing = false;
    private String volunteerDateString = "";

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private DetectedIssue issue;

    public VolunteerCollection() {
    }

    public VolunteerCollection(String id, boolean team, String comment, boolean fixing, DetectedIssue issue) {
        this.setIssue(issue);
        volunteerDate = new Date();
        this.team = team;
        this.id = id;
        this.comment = comment;
        this.fixing = fixing;
        this.volunteeredByID = User.current().getId();
        this.setVolunteerDateString(DATE_FORMAT.format(volunteerDate));
    }

    public VolunteerCollection copy() {
        VolunteerCollection copy = new VolunteerCollection();
        copy.setId(this.getId());
        copy.setTeam(this.isTeam());
        copy.volunteerDate = this.volunteerDate;
        copy.comment = this.comment;
        copy.volunteeredByID = this.volunteeredByID;
        copy.setVolunteerDateString(this.getVolunteerDateString());
        copy.issue = this.getIssue();
        return copy;
    }

    public String getFullName() {
        if (isTeam()) {
            return getId();
        } else {
            if (getId().contains("@")) {
                return getId();
            }
            User u = User.get(getId(), false);
            return (u == null) ? getId() : u.getDisplayName();
        }
    }

    public String getVolunteeredByFullName() {
        @SuppressWarnings("deprecation")
        User u = User.get(volunteeredByID, false);
        return (u == null) ? volunteeredByID : u.getDisplayName();
    }

    public String getMessage() {
        if (fixing) {
            if (getId().equalsIgnoreCase(volunteeredByID)) {
                return String
                        .format("<span class='volunteerNameFixHighlight'>%s</span> volunteered to <span class='volunteerFixHighlight'>fix</span> the build on %s",
                                getFullName(), this.getVolunteerDateString());
            } else {
                return String
                        .format("<span class='volunteerNameFixHighlight'>%s</span> was volunteered by %s to <span class='volunteerFixHighlight'>fix</span> the build on %s",
                                getFullName(), getVolunteeredByFullName(), this.getVolunteerDateString());
            }
        } else {
            if (getId().equalsIgnoreCase(volunteeredByID)) {
                return String
                        .format("<span class='volunteerNameInvestigateHighlight'>%s</span> volunteered to <span class='volunteerInvestigateHighlight'>investigate</span> the build",
                                getFullName(), this.getVolunteerDateString());
            } else {
                if (this.isTeam()) {
                    return String
                            .format("<span class='volunteerNameInvestigateHighlight'>%s</span> were volunteered by %s to <span class='volunteerInvestigateHighlight'>investigate</span> the build on %s",
                                    getFullName(), getVolunteeredByFullName(), this.getVolunteerDateString());
                } else {
                    return String
                            .format("<span class='volunteerNameInvestigateHighlight'>%s</span> was volunteered by %s to <span class='volunteerInvestigateHighlight'>investigate</span> the build on %s",
                                    getFullName(), getVolunteeredByFullName(), this.getVolunteerDateString());
                }
            }
        }
    }

    public String getIcon() {
        return (fixing) ? "fixing.png" : "investigating.png";
    }

    @Exported(visibility = 3)
    public String getVolunteeredByID() {
        return volunteeredByID;
    }

    public void setVolunteeredByID(String volunteeredByID) {
        this.volunteeredByID = volunteeredByID;
    }

    @Exported(visibility = 3)
    public Date getVolunteerDate() {
        return volunteerDate;
    }

    public void setVolunteerDate(Date volunteerDate) {
        this.volunteerDate = volunteerDate;
    }

    public boolean isMe() {
        User user = User.get(getId(), false);
        return user != null && user.equals(User.current());
    }

    @Exported(visibility = 3)
    public String getComment() {
        return comment;
    }

    public String getFormatedComment() {
        return StringEscapeUtils.escapeHtml(comment);
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Exported(visibility = 3)
    public boolean isTeam() {
        return team;
    }

    public void setTeam(boolean team) {
        this.team = team;
    }

    @Exported(visibility = 3)
    public String getId() {
        if (id == null) {
            return "";
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exported(visibility = 3)
    public boolean isFixing() {
        return fixing;
    }

    public void setFixing(boolean fixing) {
        this.fixing = fixing;
    }

    @Exported(visibility = 3)
    public String getVolunteerDateString() {
        return volunteerDateString;
    }

    public void setVolunteerDateString(String volunteerDateString) {
        this.volunteerDateString = volunteerDateString;
    }

    public DetectedIssue getIssue() {
        return issue;
    }

    public void setIssue(DetectedIssue issue) {
        this.issue = issue;
    }

}
