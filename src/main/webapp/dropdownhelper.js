function populateViewList(list) {
	var ddlSection = document.getElementById("selViews");
	ddlSection.length = 0;
	for (var listIndex = 0; listIndex < list.length; listIndex++) {
		ddlSection.options[ddlSection.options.length] = new Option(
				list[listIndex], list[listIndex]);
	}
}

function populateIssuesList(list) {
	var ddlSection = document.getElementById("selIssue");
	ddlSection.length = 0;
	for (var listIndex = 0; listIndex < list.length; listIndex++) {
		ddlSection.options[ddlSection.options.length] = new Option(
				list[listIndex].errorHeader, list[listIndex].errorHeader);
	}
}

function populateTeamsList(list) {
	var ddlSection = document.getElementById("selID");
	ddlSection.length = 0;
	for (var listIndex = 0; listIndex < list.length; listIndex++) {
		ddlSection.options[ddlSection.options.length] = new Option(
				list[listIndex].name, list[listIndex].name);
	}
}

function populateList(tag, list) {
	var ddlSection = document.getElementById(tag);
	ddlSection.length = 0;
	for (var listIndex = 0; listIndex < list.length; listIndex++) {
		ddlSection.options[ddlSection.options.length] = new Option(
				list[listIndex].name, list[listIndex].id);
	}
}
function getDropDownValueFromElement(element) {
	if (element.selectedIndex == -1) {
		return null;
	}
	return element.options[element.selectedIndex].value;
}

function getDropDownValue(elementID) {
	return getDropDownValueFromElement(document.getElementById(elementID));
}

function setSelectedDDLValue(htmlElement, id) {
	var options = document.getElementById(htmlElement).options
	for (var optionIndex = 0; optionIndex < options.length; optionIndex++) {
		options[optionIndex].selected = (options[optionIndex].value == id) ? "selected"
				: "";
	}
}