import { Room } from './room.js';

let template = document.querySelector('#remoteViewTemplate').content;
let videos = document.querySelector('.allVideos');
var room = null;

function init(evt) {
 //remove overlay
 evt.target.parentElement.remove();

 room = new Room('stun:iphone-stun.strato-iphone.de:3478');

 room.addOnSessionIncomming(function (sessionId, oleConnection) {
  let rv = template.cloneNode(true);
  rv.querySelector('p').innerText = sessionId;

  let v = rv.querySelector('video');
  v.setAttribute('id', 'v' + sessionId);
  v.setAttribute('autoplay', '');
  v.setAttribute('title', sessionId);
  v.onloadedmetadata = function (e) {
   v.play(); //autoplay
  };
  videos.appendChild(rv);

  oleConnection.addOnStreamEventsChangedHandler(function (ev) {
   console.log('Adding remote streams to video element');
   //if they sent MediaStream object
   if (ev.streams && ev.streams[0]) {
    v.srcObject = ev.streams[0];
   } else {
    //if they sent each track separately
    if (!v.inboundStream) {
     v.inboundStream = new MediaStream();
     v.srcObject = v.inboundStream;
    }
    v.inboundStream.addTrack(ev.track);
   }
  });
 });

 room.addOnSessionOutgoing((sessionId) => {
  console.log('removing video element for: ', sessionId);
  var v = document.getElementById('v' + sessionId);
  if (v) v.parentElement.remove();
 });

 room.showLocalVideo = (cam) => {
  if (cam) {
   document.getElementById('localVideo').srcObject = cam;
  } else {
   document.querySelector('#localView>p').innerText = 'Device not available';
  }
 };

 room.showSessionId = (id) => {
  let sessionEle = document.getElementById('sessionId');
  if (sessionEle) sessionEle.innerText = `sessionId: ${id}`;
 };
}

document.querySelector('.overlay>button').addEventListener('click', init);

//get the user media before user get past overlay Sfari asks permissions every time.
//so that we'll have acquired the streams before we start making offers.
document.addEventListener('DOMContentLoaded', () => {
 navigator.mediaDevices.getUserMedia({ audio: true, video: true }).then(function (cam) {
  window.webcam = cam;
 });
});
