/* 
 * (c) 2012 Michael Rohs, LMU Munich
 * (c) 2013 Henning Pohl, University of Hanover 
 * (c) 2014 Max Pfeiffer, University of Hanover  
 * 
 * Improved version. Added shuffle. Sample size for each condition 50,100, .. 250 is now equal. Idea by Jakob Riga.
 */

function median(arr) {
	arr.sort(function (a, b) { return a - b; });
	var middle = Math.floor(arr.length / 2);
	if (arr.length % 2) {
		return arr[middle];
	} else {
		return (arr[middle - 1] + arr[middle]) * 0.5;
	}
}

function rotate(x, y, angle) {
	return [x * Math.cos(angle) - y * Math.sin(angle), x * Math.sin(angle) + y * Math.cos(angle)];
}

function shuffleArray(array) {
	for (var i = array.length - 1; i > 0; i--) {
		var j = Math.floor(Math.random() * (i + 1));
		var temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}
	return array;
}


// methodToUse = ["halo" | "wedge"]
// trialCount = N
// deviceBounds = [minX, minY, maxX, maxY]
// distances = targets are placed one of those number of pixels away from the device edge
// intrusion = N pixels halos and wedges should poke into the screen
function OffscreenPointing(methodToUse, trialCount, deviceBounds, distances, intrusion) {

	this.methodToUse = methodToUse;

	this.deviceBounds = deviceBounds;
	this.distances = distances;
	this.intrusion = intrusion;

	this.trialCount = trialCount;
	document.getElementById('trialCount').innerHTML = this.trialCount;
	document.getElementById('methodToUse').innerHTML = methodToUse;
	this.trialIndex = 0;


	this.trialData = new Array();
	var instance = this;


	this.distancesTrial = [];
	for (var i = 0; i < trialCount; i++)
		this.distancesTrial.push(distances[i % distances.length]);

	this.distancesTrial = shuffleArray(this.distancesTrial);


	this.nextTrial = function () {
		var distance = this.distancesTrial[this.trialIndex]; //this.distances[Math.floor(Math.random() * this.distances.length)];

		// compute new position and radius
		var quadrant = Math.random() * 4.0;
		if (quadrant < 1.0) {
			this.trialX = this.deviceBounds[0] - distance;
			this.trialY = this.deviceBounds[1] + (0.1 + 0.9 * Math.random()) * (this.deviceBounds[3] - this.deviceBounds[1]);
		} else if (quadrant < 2.0) {
			this.trialX = this.deviceBounds[0] + (0.1 + 0.9 * Math.random()) * (this.deviceBounds[2] - this.deviceBounds[0]);
			this.trialY = this.deviceBounds[1] - distance;
		} else if (quadrant < 3.0) {
			this.trialX = this.deviceBounds[2] + distance;
			this.trialY = this.deviceBounds[1] + (0.1 + 0.9 * Math.random()) * (this.deviceBounds[3] - this.deviceBounds[1]);
		} else {
			this.trialX = this.deviceBounds[0] + (0.1 + 0.9 * Math.random()) * (this.deviceBounds[2] - this.deviceBounds[0]);
			this.trialY = this.deviceBounds[3] + distance;
		}

		// compute distance to device edge
		this.dx = this.trialX < this.deviceBounds[0] ? this.trialX - this.deviceBounds[0] : Math.max(0, this.trialX - this.deviceBounds[2]);
		this.dy = this.trialY < this.deviceBounds[1] ? this.trialY - this.deviceBounds[1] : Math.max(0, this.trialY - this.deviceBounds[3]);
		this.dist = Math.sqrt(this.dx * this.dx + this.dy * this.dy);

		//console.log(this.trialX, this.trialY, this.dx, this.dy, this.dist);

		if (this.methodToUse === "halo") {
			var e = document.getElementById('halocirc');
			var a = document.createAttribute("cx");
			a.nodeValue = this.trialX;
			e.setAttributeNode(a);

			a = document.createAttribute("cy");
			a.nodeValue = this.trialY;
			e.setAttributeNode(a);

			a = document.createAttribute("r");
			a.nodeValue = this.dist + this.intrusion;;
			e.setAttributeNode(a);
		} else if (this.methodToUse === "wedge") {
			this.leg = this.dist + Math.log((this.dist + this.intrusion) / 12.0) * 10.0
			this.aperture = (5.0 + this.dist * 0.3) / this.leg;

			var leftDir = rotate(-this.dx, -this.dy, this.aperture * -0.5);
			var rightDir = rotate(-this.dx, -this.dy, this.aperture * 0.5);
			var scaleFactor = (this.dist + this.intrusion) / this.dist;

			var e = document.getElementById('wedge');
			var a = document.createAttribute("points");
			a.nodeValue = [
				this.trialX, this.trialY,
				this.trialX + leftDir[0] * scaleFactor, this.trialY + leftDir[1] * scaleFactor,
				this.trialX + rightDir[0] * scaleFactor, this.trialY + rightDir[1] * scaleFactor
			].join(",");

			e.setAttributeNode(a);
		}

		this.trialIndex++;
		document.getElementById('trial').innerHTML = this.trialIndex;
	}

	this.init = function () {
		if (document.addEventListener) {
			document.addEventListener('click', function (event) {
				instance.onClick(event);
			}, false);
		} else if (document.attachEvent) {
			document.attachEvent('onclick', function (event) {
				instance.onClick(event);
			});
		} else {
			console.log("No adequate browser support :(");
		}
	}


	this.onClick = function (event) {
		// compute error
		var ox = this.trialX - event.pageX;
		var oy = this.trialY - event.pageY;
		var offset = Math.sqrt(ox * ox + oy * oy);
		// save trial data
		this.trialData.push([
			this.methodToUse, // halo or wedge
			this.intrusion, // how much it poked into the screen
			this.trialX, // target x
			this.trialY, // target y
			this.dist + this.intrusion, // halo radius
			this.dist, // distance from device boundary
			event.pageX, // mouse x position
			event.pageY, // mouse y position
			offset, // pointing error
		]);


		// show next trial
		this.nextTrial();

		// check whether all trials have been entered
		// if so, show results
		if (this.trialIndex > this.trialCount) {
			this.dumpResults();
		}
	}

	this.dumpResults = function () {
		var now = new Date();

		document.open();
		document.writeln("<html><head><title>Result for " + this.methodToUse + "</title></head><body>");

		// output each input event
		document.writeln("<table>");
		document.writeln("<tr><th>method</th><th>intrusion</th><th>cx</th><th>cy</th><th>r</th><th>dEdge</th><th>mx</th><th>my</th><th>dmc</th></tr>");
		for (var i = 0; i < this.trialData.length; i++) {
			document.writeln("<tr>");
			row = this.trialData[i];
			for (j = 0; j < row.length; j++) {
				document.writeln("<td>" + row[j] + "</td>");
			}
			document.writeln("</tr>");
		}
		document.writeln("</table>");

		document.writeln("<br/><b>Legend:</b>");
		document.writeln("<ul>");
		document.writeln("<li>method: off-screen visualization used</li>");
		document.writeln("<li>intrusion: how much the off-screen visualization reaches into the screen</li>");
		document.writeln("<li>cx,cy: center position of off-screen object</li>");
		document.writeln("<li>r: halo radius</li>");
		document.writeln("<li>dEdge: distance of off-screen object (cx,cy) from closest edge of device display</li>");
		document.writeln("<li>mx,my: mouse click position</li>");
		document.writeln("<li>dmc: distance between (cx,cy) and (mx,my)</li>");
		document.writeln("</ul>");

		document.writeln("</body></html>");
		document.close();
	}
}