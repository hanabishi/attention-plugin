var blockerSpan = null;

function disableScrolling() {
	var x = window.scrollX;
	var y = window.scrollY;
	window.onscroll = function() {
		window.scrollTo(x, y);
	};
}

function enableScrolling() {
	window.onscroll = function() {
	};
}

function blockPage() {
	blockPageWithMessage("Jenkins is processing your request");
}

function blockPageWithMessage(message) {
	if (blockerSpan === null) {
		unblockPage();
	}
	disableScrolling();
	blockerSpan = document.createElement("span");
	var displaySpan = document.createElement("span");
	var spinnerDiv = document.createElement("div");
	blockerSpan.appendChild(displaySpan);
	displaySpan.appendChild(spinnerDiv);
	displaySpan.appendChild(document.createTextNode(message));
	blockerSpan.setAttribute("class", "overlay");
	displaySpan.setAttribute("class", "popup");
	spinnerDiv.setAttribute("class", "spinnerkitCircle");
	document.body.appendChild(blockerSpan);
}

function unblockPage() {
	if (blockerSpan === null) {
		return;
	}
	enableScrolling();
	document.body.removeChild(blockerSpan);
	blockerSpan = null;
}