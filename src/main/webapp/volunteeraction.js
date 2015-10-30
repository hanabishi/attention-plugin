var users = Object();
var teams = Object();
var volunteers = Object();
var issues = Object();

function renderPage(fixSubmitted, fixSubmittedBy, isIntermittent, taggedIntermittentBy){
	document.getElementById('chkFixSubmitted').checked = fixSubmitted;
	document.getElementById('submittedBy').innerHTML = fixSubmittedBy;
	document.getElementById('chkIntermittent').checked =isIntermittent;
	document.getElementById('intermittentBy').innerHTML = taggedIntermittentBy;

	volProxy.getPageData(function(t) {
		response = t.responseObject();
		console.log(response);
		users = response.users;
		teams = response.teams;
		volunteers = response.volunteers;
		issues = response.issues;

		populateTeamsList(teams);
		populateIssuesList(issues);
		targetChange();
		updateVolunteersList(volunteers, document.getElementById('volunteerList'), pluginURL);
		loadComments();
	});
}

function targetChange()
{
	if(getDropDownValue("selTarget") === "1"){
		populateTeamsList(teams);
	} else {
		populateList("selID", users);
    	setSelectedDDLValue("selID", currentUser);
	}
	loadComments();
}

function updatefixIsSubmitted(){
	
	volProxy.flagFixSubmitted(document.getElementById('chkFixSubmitted').checked, function(t) {
		document.getElementById('volunteerResult').innerHTML = "";
		if(t.responseObject().error){
			document.getElementById('volunteerResult').className ="error";
			document.getElementById('volunteerResult').innerHTML = t.responseObject().message;
		}else{
			document.getElementById('volunteerResult').className ="success";
			volunteers = t.responseObject().volunteerList;
			document.getElementById('submittedBy').innerHTML = t.responseObject().message;
		}
		updateVolunteersList(volunteers, document.getElementById('volunteerList'), pluginURL);
	});
}

function updateIntermittentProblem(){
	volProxy.flagIntermittent(document.getElementById('chkIntermittent').checked, function(t) {
		document.getElementById('volunteerResult').innerHTML = "";
		if(t.responseObject().error){
			document.getElementById('volunteerResult').className ="error";
			document.getElementById('volunteerResult').innerHTML = t.responseObject().message;
		}else{
			document.getElementById('volunteerResult').className ="success";
			volunteers = t.responseObject().volunteerList;
			document.getElementById('intermittentBy').innerHTML = t.responseObject().message;
		}
		updateVolunteersList(volunteers, document.getElementById('volunteerList'), pluginURL);
	});
}

function updateVolunteer(){
	var comments = document.getElementById('txtComment').value;
	var volunteerID = getDropDownValue("selID");
	var isTeam = getDropDownValue("selTarget") === "1";
	var isseueHeader = getDropDownValue("selIssue");
	if(volunteerID === "" || volunteerID === null){
		return;
	}
	document.getElementById("btnVolunteer").style.display = "none";
	document.getElementById('volunteerResult').innerHTML = "processing";
	volProxy.updateVolunteer(comments, volunteerID, isTeam, isseueHeader, function(t) {
		document.getElementById("btnVolunteer").style.display = "";
		volunteers = t.responseObject().volunteerList;
		if(t.responseObject().error){
			document.getElementById('volunteerResult').className ="error";
		}else{
			document.getElementById('volunteerResult').className ="success";
		}
		document.getElementById('volunteerResult').innerHTML = t.responseObject().message;
		updateVolunteersList(volunteers, document.getElementById('volunteerList'), pluginURL);
	});
}

function volunteerMeAsInvestigating(){
	document.getElementById("btnInvestigating").style.display = "none";
	document.getElementById('volunteerResult').innerHTML = "processing";
	volProxy.updateVolunteer("", currentUser, false, -1, function(t) {
		document.getElementById("btnInvestigating").style.display = "";
		volunteers = t.responseObject().volunteerList;
		if(t.responseObject().error){
			document.getElementById('volunteerResult').className ="error";
		}else{
			document.getElementById('volunteerResult').className ="success";
		}
		document.getElementById('volunteerResult').innerHTML = t.responseObject().message;
		updateVolunteersList(volunteers, document.getElementById('volunteerList'), pluginURL);
	});
}

function unvolunteer(id){
	volProxy.unVolunteer(id, function(t) {
		console.log(t.responseObject());
		volunteers = t.responseObject().volunteerList;
		if(t.responseObject().error){
			document.getElementById('volunteerResult').className ="error";
		}else{
			document.getElementById('volunteerResult').className ="success";
		}
		document.getElementById('volunteerResult').innerHTML = t.responseObject().message;
		updateVolunteersList(volunteers, document.getElementById('volunteerList'), pluginURL);
	});
}
function displayIssueDescription(){
	updateIssueDescription(getDropDownValue("selIssue"))
}
function updateVolunteersList(volunteerList, htmlElement, pluginURL) {
	var body = "";

	for (var volunteerIndex = 0; volunteerIndex < volunteerList.length; volunteerIndex++) {
		body += "<span class=\"volunteerLine\"><img class=\"showMeYourHands\" src=\""
				+ rootURL
				+ "/images/16x16/edit-delete.png\" onclick=\"unvolunteer('"
				+ volunteerList[volunteerIndex].id
				+ "')\"/> "
				+ volunteerList[volunteerIndex].message.split("\n").join(
						"<br />");
		body += "<br /><span class=\"comment\"><b>Issue: </b>"
				+ volunteerList[volunteerIndex].issue.errorHeader + "</span>";
		body += "<br /><span class=\"comment\"><b>Comment: </b>"
				+ volunteerList[volunteerIndex].comment + "</span><br />";
	}
	htmlElement.innerHTML = body;
}

function loadComments() {
	document.getElementById('txtComment').value = "";
	var selectedID = getDropDownValue("selID");
	var isTeam = getDropDownValue("selTarget") === "1";
	for (var volunteerIndex = 0; volunteerIndex < volunteers.length; volunteerIndex++) {
		if (volunteers[volunteerIndex].id === selectedID) {
			document.getElementById('txtComment').value = volunteers[volunteerIndex].comment;
			for (var issueIndex = 0; issueIndex < issues.length; issueIndex++) {
				if (issues[issueIndex].name === volunteers[volunteerIndex].issue.errorHeader) {
					setSelectedDDLValue("selIssue", issues[issueIndex].id);
					updateIssueDescription(getDropDownValue("selIssue"))
					break;
				}
			}
			break;
		}
	}
}
function updateIssueDescription(id) {
	for (var issueIndex = 0; issueIndex < issues.length; issueIndex++) {
		if (issues[issueIndex].errorHeader === id) {
			document.getElementById("issueDescription").innerHTML = issues[issueIndex].errorMessage;
			return;
		}
	}
	document.getElementById("issueDescription").innerHTML = "<br />";
}
