package org.jenkinsci.plugins.attention;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.RootAction;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.View;
import hudson.util.RunList;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.attention.VolunteerRecorder.VolunteerDescriptor;
import org.jenkinsci.plugins.attention.buildfailure.ReportObject;
import org.jenkinsci.plugins.attention.response.PageData;
import org.jenkinsci.plugins.attention.response.ReportJavaScriptResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.bind.JavaScriptMethod;

@Extension
public class BuildFailureReport implements RootAction {

    @Override
    public String getIconFileName() {
        return null;//"/plugin/attention/buildfailurereport.png";
    }

    @Override
    public String getDisplayName() {
        return null;//"Build Failure Report";
    }

    @Override
    public String getUrlName() {
        return null;//"buildfailurereport";
    }

    @JavaScriptMethod
    public PageData getPageData(String currentViewName) {
        PageData page = new PageData();
        VolunteerDescriptor descriptor = (VolunteerDescriptor) Jenkins.getInstance().getDescriptor(
                VolunteerRecorder.class);

        for (View v : Jenkins.getInstance().getViews()) {
            if (descriptor.isShowAllView() || !v.getDisplayName().equalsIgnoreCase("all")) {
                page.getViews().add(v.getDisplayName());
            }
        }

        page.setBuildList(getRedBuildList(currentViewName));

        return page;
    }

    @JavaScriptMethod
    public ReportJavaScriptResponse getBuilds(String branch) {
        List<ReportObject> redBuildList = getRedBuildList(branch);
        return new ReportJavaScriptResponse("", false, redBuildList);
    }

    public String getVolunteerReport(Run<?, ?> build) {
        VolunteerAction va = build.getAction(VolunteerAction.class);
        if (va == null) {
            return "";
        }
        return va.formatVolunteerString();
    }

    public String getCurrentViewName() {
        return this.getRootView().getDisplayName();
    }

    public View getRootView() {
        View view = Stapler.getCurrentRequest().findAncestorObject(View.class);
        return view != null ? view : Jenkins.getInstance().getPrimaryView();
    }

    public String getPluginURL() {
        return Jenkins.getInstance().getRootUrl() + "plugin/attention";
    }

    public String getLastBuildTime(Run<?, ?> lastBuild) {
        return lastBuild != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm")
                .format(new Date(lastBuild.getTimeInMillis())) : "";
    }

    @SuppressWarnings("rawtypes")
    public List<ReportObject> getRedBuildList(String viewName) {
        View view = Jenkins.getInstance().getView(viewName);
        RunList builds = view.getBuilds();
        RunList failedBuilds = builds.failureOnly();
        Set<String> processedProjects = new HashSet<String>();
        List<ReportObject> objects = new LinkedList<ReportObject>();
        @SuppressWarnings("unchecked")
        Iterator<Run> failedBuildIterator = failedBuilds.iterator();
        while (failedBuildIterator.hasNext()) {
            Job parent = failedBuildIterator.next().getParent();
            if (!processedProjects.contains(parent.getFullName())) {
                Run lastBuildCompleted = parent.getLastBuild();
                if (lastBuildCompleted != null && lastBuildCompleted.isBuilding()) {
                    lastBuildCompleted = lastBuildCompleted.getPreviousBuild();
                }
                if (parent.getLastFailedBuild() != null && lastBuildCompleted != null
                        && lastBuildCompleted.getResult().isWorseThan(Result.SUCCESS)
                        && (lastBuildCompleted.getAction(VolunteerAction.class) != null)) {
                    objects.add(ReportObject.getInstance(parent, lastBuildCompleted));
                    processedProjects.add(parent.getFullName());
                }
            } else {
                System.out.println("Already processed parent " + parent.getDisplayName());
            }
        }

        Collections.sort(objects, new Comparator<ReportObject>() {
            @Override
            public int compare(ReportObject o1, ReportObject o2) {
                return (o2.getFailedBuilds() - o1.getFailedBuilds()) * 10
                        + o1.getLastFailedBuildname().compareTo(o2.getLastFailedBuildname());
            }
        });

        return objects;
    }

}
