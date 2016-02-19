/**
 * 
 */
var xmlHttp = null;
var map;
var circles = [];
var values = [];
var server = "localhost"; //"128.101.96.158";
var markers = [];

function updateData(x1,y1,x2,y2,level)
{
    var Url = "http://"+server+":8095/query?x1="+x1+"&y1="+y1+"&x2="+x2+"&y2="+y2+"&level="+level;

    xmlHttp = new XMLHttpRequest(); 
    xmlHttp.onreadystatechange = ProcessRequest;
    xmlHttp.open( "GET", Url, true );
    xmlHttp.send( null );
}


function ProcessRequest() 
{
    if ( xmlHttp.readyState == 4 && xmlHttp.status == 200 ) 
    {
        if ( xmlHttp.responseText == null ) 
        {
         	// do nothing 
        }
        else
        {
            var info = eval ( "(" + xmlHttp.responseText + ")" ); 

            for (var key in info.locations) {
               //alert('x ='+info.locations[key].x+' y ='+info.locations[key].y+' value ='+info.locations[key].value);
               markers.push([Number(info.locations[key].x) ,Number(info.locations[key].y),Number(info.locations[key].value)]);
				}
	    testFucntion();
        }                    
    }
}



/////////////////////////////////////

function testFucntion(){
	var bounds = map.getBounds();
	markers.forEach(function(point) {
		generateIcon(point[2], function(src) {
			//alert('im in generate icon');
			var pos = new google.maps.LatLng(point[1], point[0]);
			bounds.extend(pos);
			var Lobject = new google.maps.Marker({
			position : pos,
			map : map,
			icon : src
				});
			values.push(Lobject);
			Lobject.setMap(map);
			});
		});
}


var generateIconCache = {};

function generateIcon(number, callback) {
	if (generateIconCache[number] !== undefined) {
		callback(generateIconCache[number]);
		}
	var fontSize = 16, imageWidth = imageHeight = 40;
	if (number >= 1000) {
		fontSize = 10;
		imageWidth = imageHeight = 55;
		} 
	else if (number < 1000 && number > 100) {
		fontSize = 14;
		imageWidth = imageHeight = 55;
		}
	var svg = d3.select(document.createElement('div')).append('svg').attr('viewBox', '0 0 54.4 54.4').append('g')
	
	// circle 
	var circles = svg.append('circle').attr('cx', '27.2').attr('cy', '27.2').attr('r', '21.2').style('fill', '#c43235');
	
	// border 
	//var path = svg.append('path').attr('d','M27.2,0C12.2,0,0,12.2,0,27.2s12.2,27.2,27.2,27.2s27.2-12.2,27.2-27.2S42.2,0,27.2,0z M6,27.2 C6,15.5,15.5,6,27.2,6s21.2,9.5,21.2,21.2c0,11.7-9.5,21.2-21.2,21.2S6,38.9,6,27.2z').attr('fill', '#ff3366');
	
	// text color
	var text = svg.append('text').attr('dx', 27).attr('dy', 32).attr('text-anchor', 'middle').attr('style','font-size:'+ fontSize + 'px; fill: #152737; font-family: "Comic Sans MS", cursive, sans-serif; font-weight: bolder').text(number);
	
	
	var svgNode = svg.node().parentNode.cloneNode(true), image = new Image();
	d3.select(svgNode).select('clippath').remove();
	var xmlSource = (new XMLSerializer()).serializeToString(svgNode);
	image.onload = (function(imageWidth, imageHeight) {
		var canvas = document.createElement('canvas'), context = canvas.getContext('2d'), dataURL;
		d3.select(canvas).attr('width', imageWidth).attr('height', imageHeight);
		context.drawImage(image, 0, 0, imageWidth, imageHeight);
		dataURL = canvas.toDataURL();
		generateIconCache[number] = dataURL;
		callback(dataURL);
		}).bind(this, imageWidth, imageHeight);
	
	image.src = 'data:image/svg+xml;base64,'+ btoa(encodeURIComponent(xmlSource).replace(/%([0-9A-F]{2})/g,
	
	function(match, p1) {
		return String.fromCharCode('0x' + p1);
		}));
	} // generateIcon

/////////////////////////////////////

function clearCircle() {
	for(i = 0; i< values.length; i++){
		values[i].setMap(null);
		}
	values = [];
	markers = [];
	}

	
function initMap() {
	// Map Style
	var styles = [ {
		"featureType" : "water",
		"stylers" : [ {
			"color" : "#0e171d"
		} ]
	}, {
		"featureType" : "landscape",
		"stylers" : [ {
			"color" : "#1e303d"
		} ]
	}, {
		"featureType" : "road",
		"stylers" : [ {
			"color" : "#1e303d"
		} ]
	}, {
		"featureType" : "poi.park",
		"stylers" : [ {
			"color" : "#1e303d"
		} ]
	}, {
		"featureType" : "transit",
		"stylers" : [ {
			"color" : "#182731"
		}, {
			"visibility" : "simplified"
		} ]
	}, {
		"featureType" : "poi",
		"elementType" : "labels.icon",
		"stylers" : [ {
			"color" : "#f0c514"
		}, {
			"visibility" : "off"
		} ]
	}, {
		"featureType" : "poi",
		"elementType" : "labels.text.stroke",
		"stylers" : [ {
			"color" : "#1e303d"
		}, {
			"visibility" : "off"
		} ]
	}, {
		"featureType" : "transit",
		"elementType" : "labels.text.fill",
		"stylers" : [ {
			"color" : "#e77e24"
		}, {
			"visibility" : "off"
		} ]
	}, {
		"featureType" : "road",
		"elementType" : "labels.text.fill",
		"stylers" : [ {
			"color" : "#94a5a6"
		} ]
	}, {
		"featureType" : "administrative",
		"elementType" : "labels",
		"stylers" : [ {
			"visibility" : "simplified"
		}, {
			"color" : "#e84c3c"
		} ]
	}, {
		"featureType" : "poi",
		"stylers" : [ {
			"color" : "#e84c3c"
		}, {
			"visibility" : "off"
		} ]
	} ];
	var styledMap = new google.maps.StyledMapType(styles, {
		name : "Styled Map"
	});
	map = new google.maps.Map(document.getElementById('map'), {
		center : {
			lat : 51.5073509,
			lng : -0.1277583
		},
		zoom : 2,
		maxZoom : 14,
		minZoom : 2,
		mapTypeControlOptions : {
			mapTypeIds : [ google.maps.MapTypeId.ROADMAP, 'map_style' ]
		}

	});
	map.mapTypes.set('map_style', styledMap);
	map.setMapTypeId('map_style');
	//Event when center changed 
	map.addListener('idle', function() {
		// 3 seconds after the center of the map has changed, pan back to the
		// marker.
		zoomLevel = map.getZoom();
		var bound = map.getBounds();
		var ne = bound.getNorthEast(); // LatLng of the north-east corner
		var sw = bound.getSouthWest(); // LatLng of the south-west corder
		clearCircle();
		updateData(sw.lng(), sw.lat(), ne.lng(), ne.lat(), zoomLevel);
	});//endEvent idle listener      
}//end init map 