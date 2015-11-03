package org.jenkinsci.plugins.attention;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.text.SimpleDateFormat;
import java.util.Date;

@ExportedBean
public class UserOperation {

    public enum Type {
        VOLUNTEER, UN_VOLUNTEER,
        FIX_SUBMITTED, NO_FIX_SUBMITTED,
        INTERMITTENT, NOT_INTERMITTENT,
        GREEN_BUILD, RED_BUILD,
    }

    private Date date;
    private int buildNumber;
    private String user;
    private Type type;

    private String volunteer;
    private boolean volunteer_is_team;
    private String comment;

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static UserOperation volunteerOperation(
            int buildNumber, String user,
            String volunteer, boolean is_team, String comment) {
        UserOperation op = new UserOperation(buildNumber, user, UserOperation.Type.VOLUNTEER);
        op.volunteer = volunteer;
        op.volunteer_is_team = is_team;
        op.comment = comment;
        return op;
    }

    public static UserOperation unVolunteerOperation(
            int buildNumber, String user,
            String volunteer, boolean is_team) {
        UserOperation op = new UserOperation(buildNumber, user, UserOperation.Type.UN_VOLUNTEER);
        op.volunteer = volunteer;
        op.volunteer_is_team = is_team;
        return op;
    }

    public static UserOperation fixSubmittedOperation(int buildNumber, String user) {
        return new UserOperation(buildNumber, user, UserOperation.Type.FIX_SUBMITTED);
    }

    public static UserOperation noFixSubmittedOperation(int buildNumber, String user) {
        return new UserOperation(buildNumber, user, UserOperation.Type.NO_FIX_SUBMITTED);
    }

    public static UserOperation intermittentOperation(int buildNumber, String user) {
        return new UserOperation(buildNumber, user, UserOperation.Type.INTERMITTENT);
    }

    public static UserOperation notIntermittentOperation(int buildNumber, String user) {
        return new UserOperation(buildNumber, user, UserOperation.Type.NOT_INTERMITTENT);
    }

    public static UserOperation greenBuildOperation(int buildNumber) {
        return new UserOperation(buildNumber, null, UserOperation.Type.GREEN_BUILD);
    }

    public static UserOperation redBuildOperation(int buildNumber) {
        return new UserOperation(buildNumber, null, UserOperation.Type.RED_BUILD);
    }

    private UserOperation(int buildNumber, String user, Type type) {
        this.date = new Date();
        this.buildNumber = buildNumber;
        this.user = user;
        this.type = type;
    }

    @Exported(visibility = 4)
    public Date getDate() {
        return date;
    }

    @Exported(visibility = 4)
    public int getBuildNumber() {
        return buildNumber;
    }

    @Exported(visibility = 4)
    public String getUser() {
        return user;
    }

    @Exported(visibility = 4)
    public Type getType() {
        return type;
    }

    @Exported(visibility = 4)
    public String getVolunteer() {
        return volunteer;
    }

    @Exported(visibility = 4)
    public boolean isVolunteer_is_team() {
        return volunteer_is_team;
    }

    @Exported(visibility = 4)
    public String getComment() {
        return comment;
    }

    public String getDisplayDate() {
        return DATE_FORMAT.format(date);
    }

    public String getDisplayOperation() {
        switch (type) {
            case VOLUNTEER:
                return String.format("%s volunteered %s",
                        user,
                        volunteer_is_team
                                ? "the " + volunteer + " team"
                                : volunteer);
            case UN_VOLUNTEER:
                return String.format("%s un-volunteered %s",
                        user,
                        volunteer_is_team
                                ? "the " + volunteer + " team"
                                : volunteer);
            case FIX_SUBMITTED:
                return String.format("%s submitted a fix", user);
            case NO_FIX_SUBMITTED:
                return String.format("%s un-submitted a fix", user);
            case INTERMITTENT:
                return String.format("%s marked issue as intermittent", user);
            case NOT_INTERMITTENT:
                return String.format("%s marked issue as not intermittent", user);
            case GREEN_BUILD:
                return "Build succeeded!";
            case RED_BUILD:
                return "Build failed!";
            default:
                return "";
        }
    }
}
