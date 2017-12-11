var speed = 5; //speed of particle movement
var noofparticles = 15000; //number of particles
var sizeofparticles = 40; //size of particle
var speedcam = 10; //speed of camera movement..set to ZERO for NO CAMERA MOVEMENT
var flagcam = 0;
var lastz = 5000;
lastz = lastz - lastz%speedcam;

var camera, scene, renderer, particles, geometry, material, i, h, color, size;
var mouseX = 0, mouseY = 0;
var flagy,flagx,flagz;
var ox1;
var oy1;
var oz1;
var exx = 0;
var eyy = 0;
var click1 = 0;

var windowHalfX = window.innerWidth / 2;
var windowHalfY = window.innerHeight / 2;

init();
animate();

function init() {
ox1 = new Array();
oy1 = new Array();
flagx = new Array();
flagy = new Array();
flagz = new Array();
oz1 = new Array();

camera = new THREE.PerspectiveCamera( 55, window.innerWidth / window.innerHeight, 2, noofparticles );
camera.position.z = 0;

$('body').click(
  function(){if(click1 == 0)click1 = 1;else click1 = 0;}
);

$('*').mousemove(
  function abc(e)
  {
exx = (e.pageX - windowHalfX);
exx =  exx - exx%speed;
eyy = (windowHalfY-e.pageY);
eyy =  eyy - eyy%speed;
});

$('*').keydown(function(e)
{
if(event.which == 38){camera.position.z-=2*speedcam;}
if(event.which == 40){camera.position.z+=2*speedcam;}
if(event.which == 37){camera.position.y -=2*speed;}
if(event.which == 39){camera.position.y +=2*speed;} 
});

scene = new THREE.Scene();
scene.fog = new THREE.FogExp2( 0xffffff, 0.0001 );
geometry = new THREE.Geometry();
sprite = THREE.ImageUtils.loadTexture( "https://lh3.ggpht.com/fqJP-rHMUAoCm5ua8EWKMEF4VAqdZl83LRfJn8TLzBY7fEYfUD5Nfu329RNyLXcsRso=w170" );
for ( i = 0; i < noofparticles; i ++ ) 
{
var vertex = new THREE.Vector3();

ox1[i] = parseInt(4000 * Math.random()) - 2000;
ox1[i] = ox1[i] - (ox1[i]%speed);
vertex.x = ox1[i];
flagx[i] = 0;

oy1[i] = parseInt(2000 * Math.random()) - 1000;
oy1[i] = oy1[i] - (oy1[i]%speed);
vertex.y = oy1[i];
flagy[i] = 0;

oz1[i] = parseInt(-noofparticles/2 * Math.random()) + noofparticles/4;
oz1[i] = oz1[i] - (oz1[i]%speed);
vertex.z = oz1[i];
flagz[i] = 0;
geometry.vertices.push( vertex );
}

material = new THREE.ParticleBasicMaterial( { size:sizeofparticles, sizeAttenuation: true, map: sprite, transparent: true } );
material.color.setHSL( 1.0, 0.3, 0.7 );
particles = new THREE.ParticleSystem( geometry, material );
particles.sortParticles = true;
scene.add( particles );

renderer = new THREE.WebGLRenderer( { clearAlpha: 1 } );
renderer.setSize( window.innerWidth, window.innerHeight );

document.body.appendChild( renderer.domElement );
document.addEventListener( 'mousemove', onDocumentMouseMove, false );
document.addEventListener( 'touchstart', onDocumentTouchStart, false );
document.addEventListener( 'touchmove', onDocumentTouchMove, false );
window.addEventListener( 'resize', onWindowResize, false );
}

function onWindowResize() {
	windowHalfX = window.innerWidth / 2;
	windowHalfY = window.innerHeight / 2;
	camera.aspect = window.innerWidth / window.innerHeight;
	camera.updateProjectionMatrix();
	renderer.setSize( window.innerWidth, window.innerHeight );
}

function onDocumentMouseMove( event ) {
	mouseX = event.clientX - windowHalfX;
	mouseY = event.clientY - windowHalfY;
}

function onDocumentTouchStart( event ) {

	if ( event.touches.length == 1 ) {
      	event.preventDefault();
		mouseX = event.touches[ 0 ].pageX - windowHalfX;
		mouseY = event.touches[ 0 ].pageY - windowHalfY;
	}
}

function onDocumentTouchMove( event ) {
	if ( event.touches.length == 1 ) {
		event.preventDefault();
		mouseX = event.touches[ 0 ].pageX - windowHalfX;
		mouseY = event.touches[ 0 ].pageY - windowHalfY;
	}
}

function animate() {
	requestAnimationFrame( animate );
	render();
}

function render() {
var time = Date.now() * 0.0005;

if(exx != 0 && eyy !=0)
for(var j = 0;j < geometry.vertices.length;j++)
{
if (flagy[j] == 0 && click1 == 0)
{
if(geometry.vertices[j].y > eyy)geometry.vertices[j].y-=speed;
else if(geometry.vertices[j].y < eyy){geometry.vertices[j].y+=speed;}
else if(geometry.vertices[j].y == eyy)
flagy[j] = 1;
}
if (flagy[j] == 1 && click1 == 1)
{
if(geometry.vertices[j].y > oy1[j])
geometry.vertices[j].y -= speed;
else if(geometry.vertices[j].y < oy1[j]){geometry.vertices[j].y+=speed;}
else if(geometry.vertices[j].y == oy1[j])
flagy[j] = 0;
} 
}
  if(speedcam != 0)
  {
    if(flagcam == 0)camera.position.z += speedcam;
    else if(flagcam == 1)camera.position.z -= speedcam;
  }
  if(camera.position.z == lastz || camera.position.z == -lastz)
  {
   if(flagcam == 0)flagcam = 1;else flagcam = 0;
  }
 // $('div').text(flagcam +" "+camera.position.z);
  camera.position.x += ( mouseX - camera.position.x ) * 0.05;
camera.position.y += ( - mouseY - camera.position.y ) * 0.05;
camera.lookAt( scene.position );

h = ( 360 * ( 1.0 + time ) % 360 ) / 360;
	material.color.setHSL( h, 0.5, 0.5 );

renderer.render( scene, camera );
}