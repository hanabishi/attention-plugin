var viewCollection = Object();
var buildList = Object();

function renderPage() {
	console.log("RENDER");
	volProxy.getPageData(function(t) {
		result = t.responseObject();
		console.log(result);
		viewCollection = result.views;
		buildList = result.buildList;
		populateViewList(viewCollection);
		setSelectedDDLValue("selViews", rootViewName);
		renderBuildList();
	});
}

function filterBuilds() {
	blockPage();
	var selectedView = getDropDownValue("selViews");
	document.getElementById('result').innerHTML = "<span class='nobuilds'>Loading builds for the view "
			+ selectedView + "</span>";
	document.getElementById("selViews").style.display = "none";
	volProxy.getBuilds(selectedView, function(t) {
		unblockPage();
		document.getElementById("selViews").style.display = "";
		buildList = t.responseObject().buildList;
		if (t.responseObject().error) {
			document.getElementById('volunteerResult').className = "error";
			document.getElementById('volunteerResult').innerHTML = t
					.responseObject().message;
		}

		renderBuildList();
	});
}

function renderBuildList() {
	var bodyVolunteers = "";
	var bodyNoVolunteers = "";
	document.getElementById('result').innerHTML = "";
	if (buildList.length == 0) {
		document.getElementById('result').innerHTML = "<span class=\"nobuilds\">No builds found</span>";
		return;
	}

	for (var buildIndex = 0; buildIndex < buildList.length; buildIndex++) {
		var build = buildList[buildIndex];
		var redBuildDiv = document.createElement('div');

		var header = document.createElement('span');
		var buildFailureDesc = document.createElement('span');
		header.className = "buildHeader";
		buildFailureDesc.className = "buildFailureDesc";

		header.innerHTML = "";
		if (build.fixSubmitted) {
			header.innerHTML = "<img src=\"" + pluginRoot + "/fixing.png\" />"
		}
		header.innerHTML += "<a href=\"" + build.lastFailedBuildLink + "\">"
				+ build.lastFailedBuildname + "</a>";

		buildFailureDesc.innerHTML = "Failed <span class=\"errorHighlight\">"
				+ build.failedBuilds + "</span> time"
				+ ((build.failedBuilds > 1) ? "s in a row" : "")
				+ " and has failed <span class=\"errorHighlight\">"
				+ build.failureString + "</span> last builds.";
		if (build.fixSubmitted) {
			buildFailureDesc.innerHTML += "<br />A fix was submitted by <span class=\"fixerHighlight\">"
					+ build.fixSubmittedByName + "</span>.";
		}

		redBuildDiv.appendChild(header);
		redBuildDiv.appendChild(buildFailureDesc);

		if (build.originalBreakers.length > 0) {
			var buildFailureMembersBreakers = document.createElement('span');
			var buildFailureCategoryBreakers = document.createElement('span');
			buildFailureCategoryBreakers.className = "buildFailureCategory";
			buildFailureMembersBreakers.className = "buildFailureMembers";
			buildFailureCategoryBreakers.innerHTML = "Initial breakers: ";

			buildFailureMembersBreakers.innerHTML = build.originalBreakers
					.join(", ");
			redBuildDiv.appendChild(buildFailureCategoryBreakers);
			redBuildDiv.appendChild(buildFailureMembersBreakers);

		}

		if (build.volunteers.length > 0) {
			var fixers = [];
			var investigators = [];

			for (var vIndex = 0; vIndex < build.volunteers.length; vIndex++) {

				if (build.volunteers[vIndex].fixing) {
					fixers[fixers.length] = build.volunteers[vIndex].fullName
				} else {
					investigators[investigators.length] = build.volunteers[vIndex].fullName
				}
			}

			if (investigators.length > 0) {
				var buildFailureCategoryInvestigators = document
						.createElement('span');
				var buildFailureMembersInvestigators = document
						.createElement('span');

				buildFailureCategoryInvestigators.innerHTML = "Investigators: ";
				buildFailureCategoryInvestigators.className = "buildFailureCategory";
				buildFailureMembersInvestigators.className = "buildFailureMembers";

				buildFailureMembersInvestigators.innerHTML = investigators
						.join(", ");
				redBuildDiv.appendChild(buildFailureCategoryInvestigators);
				redBuildDiv.appendChild(buildFailureMembersInvestigators);
			}
			if (fixers.length > 0) {
				var buildFailureCategoryFixers = document.createElement('span');
				var buildFailureMembersFixers = document.createElement('span');

				buildFailureCategoryFixers.className = "buildFailureCategory";
				buildFailureMembersFixers.className = "buildFailureMembers";
				buildFailureCategoryFixers.innerHTML = "Fixers: ";

				buildFailureMembersFixers.innerHTML = fixers.join(", ");
				redBuildDiv.appendChild(buildFailureCategoryFixers);
				redBuildDiv.appendChild(buildFailureMembersFixers);

			}
		}

		document.getElementById('result').appendChild(redBuildDiv);
	}
}
