
package hu.bakaibalazs.dynatrace.redmine;

import java.util.logging.Logger;

import com.dynatrace.diagnostics.pdk.Action;
import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.Incident;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Violation;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;

public class RedmineAction implements Action {

	private static final Logger log = Logger.getLogger(RedmineAction.class.getName());

	@Override
	public Status setup(ActionEnvironment env) throws Exception {
		log.fine("Redmine Action Setup");
		return new Status(Status.StatusCode.Success);
	}

	@Override
	public Status execute(ActionEnvironment env) throws Exception {
		log.info("Redmine Action execute");

		String redmineServerURL = env.getConfigString("redmineServerURL");
		String apiAccessKey = env.getConfigString("apiAccessKey");
		String projectKey = env.getConfigString("projectKey");
		String issueSubjectPrefix = env.getConfigString("issueSubjectPrefix");

		for (Incident i : env.getIncidents()) {
			logIncidents(i);

			RedmineManager mgr = RedmineManagerFactory.createWithApiKey(redmineServerURL, apiAccessKey);
			IssueManager issueManager = mgr.getIssueManager();
			Issue issueToCreate = IssueFactory.create(mgr.getProjectManager().getProjectByKey(projectKey).getId(),
					issueSubjectPrefix + " " + i.getKey().getSystemProfile());
			issueToCreate.setDescription(i.getMessage());
			issueToCreate.setPriorityId(getRedminePriority(i));
			issueManager.createIssue(issueToCreate);
			log.info("Issue was created in Redmine");
		}

		return new Status(Status.StatusCode.Success);
	} 

	private void logIncidents(Incident incident) {
		log.fine("RULE:" + incident.getIncidentRule().getName());
		log.fine("MSG:" + incident.getMessage());
		log.fine("SERVER:" + incident.getKey().getSystemProfile());

		String message = incident.getMessage();
		log.fine("Incident " + message + " triggered.");
		for (Violation violation : incident.getViolations()) {
			log.info("Measure " + violation.getViolatedMeasure().getName() + " violoated threshold.");
		}
	}
	
	private int getRedminePriority(Incident incident) {
		Incident.Severity localSeverity = incident.getSeverity();

		if (localSeverity == Incident.Severity.Error) // Redmine High == 3
			return 3;
		if (localSeverity == Incident.Severity.Warning) // Redmine Normal == 2
			return 2;
		if (localSeverity == Incident.Severity.Informational) { // Redmine Low == 1
			return 1;
		}
		return 1;
	}

	@Override
	public void teardown(ActionEnvironment env) throws Exception {
		log.fine("Redmine Action tear down");
	}
	
	
	public static void main(String[] args) throws Exception {
		String uri = "http://hibajegy.kh.hu";
	    String apiAccessKey = "3b72ae72c583f28b83c10b124d47928388db22c1";
	    String projectKey = "ci-nlr-teszt";
	    Integer queryId = null; 

	    RedmineManager mgr = RedmineManagerFactory.createWithApiKey(uri, apiAccessKey);
	    IssueManager issueManager = mgr.getIssueManager();
	    java.util.List<Issue> issues = issueManager.getIssues(projectKey, queryId);
	    for (Issue issue : issues) {
	        System.out.println(issue.getSubject()+" - "+issue.getPriorityId());
	    }

	}
	
	
}
