package org.jenkinsci.plugins.attention;

import hudson.XmlFile;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@ExportedBean
public final class VolunteerHistory {

    private static final Logger LOGGER = Logger.getLogger(VolunteerHistory.class.getName());

    private transient File historyFile;

    private LinkedList<UserOperation> userOperations;

    public VolunteerHistory(File configDir) {
        this.historyFile = new File(configDir, "VolunteerHistory.xml");
        if (historyFile.exists()) {
            load();
        } else {
            userOperations = new LinkedList<>();
        }
    }

    public synchronized void add(UserOperation newOp) {
        // Find the last operation with the same build number and insert it after
        ListIterator<UserOperation> it = userOperations.listIterator(userOperations.size());
        while (it.hasPrevious()) {
            UserOperation op = it.previous();
            if (op.getBuildNumber() <= newOp.getBuildNumber()) {
                it.next();
                break;
            }
        }
        it.add(newOp);
        save();
    }

    @Exported(visibility = 3)
    public List<UserOperation> getUserOperations() {
        return Collections.unmodifiableList(userOperations);
    }

    public List<UserOperation> getLastUserOperations() {
        // Find the last green build event and return all operations after that
        ListIterator<UserOperation> it = userOperations.listIterator(userOperations.size());
        while (it.hasPrevious()) {
            UserOperation op = it.previous();
            if (op.getType() == UserOperation.Type.GREEN_BUILD) {
                it.next();
                break;
            }
        }

        // it is now after the last green build or at the start of the history
        LinkedList<UserOperation> lastOps = new LinkedList<>();
        while (it.hasNext()) {
            lastOps.add(it.next());
        }

        return Collections.unmodifiableList(lastOps);
    }

    private synchronized void load() {
        try {
            new XmlFile(historyFile).unmarshal(this);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load "+ historyFile, e);
        }
    }

    public synchronized void save() {
        try {
            new XmlFile(historyFile).write(this);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save "+ historyFile,e);
        }
    }
}
