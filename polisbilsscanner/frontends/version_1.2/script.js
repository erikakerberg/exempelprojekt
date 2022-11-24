window.onload = async () => {
    
  let width, height;
  
  var video = document.getElementById('video');
  var canvas = document.getElementById('canvas');

  // Get access to the camera!
  if(navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
    const devices = await navigator.mediaDevices.enumerateDevices();
    const videoDevices = devices.filter(device => device.kind === 'videoinput');
      
    // Not adding `{ audio: true }` since we only want video now
    navigator.mediaDevices.getUserMedia({video: {facingMode: "environment", width: { ideal: 1024 }, height: { ideal: 768 } }}).then(async function(stream) {
      //video.src = window.URL.createObjectURL(stream);
      video.srcObject = stream;
      video.play();
      
      await sleep(1000);
      
      const track = stream.getVideoTracks()[0];
      const capabilities = track.getCapabilities();
      const settings = track.getSettings();
      
      width = settings.width;
      height = settings.height;
      
      canvas.width = width;
      canvas.height = height;
      
      if (!('zoom' in capabilities)) {
        return Promise.reject('Zoom is not supported by ' + track.label);
      }
      
      const input = document.getElementById("input");
      
      input.min = capabilities.zoom.min;
      input.max = capabilities.zoom.max;
      input.step = capabilities.zoom.step;
      input.value = settings.zoom;
      input.oninput = function(event) {
        track.applyConstraints({advanced: [ {zoom: event.target.value} ]});
      }
    })
  }
  
  var context = canvas.getContext('2d');
  
  let img_base64;

  // Trigger photo take
  document.getElementById("video").addEventListener("click", function() {
    document.getElementById("text").innerHTML = "...";
      
    context.drawImage(video, 0, 0, width, height);
    img_base64 = canvas.toDataURL("image/jpg").substr(22);

    fetch("https://haito.pythonanywhere.com/test/", {
      method: "post",
      body: img_base64
    })
    .then((response) => response.text())
    .then(function (data) {
      document.getElementById("text").innerHTML = data;
    })
    .catch((error) => {
      console.log(error);
    })
  });
  
  window.onkeydown = (e) => {
    if (e.keyCode !== 13) return;
    
    document.getElementById("text").innerHTML = "...";
      
    context.drawImage(video, 0, 0, width, height);
    img_base64 = canvas.toDataURL("image/jpg").substr(22);

    fetch("https://haito.pythonanywhere.com/test/", {
      method: "post",
      body: img_base64
    })
    .then((response) => response.text())
    .then(function (data) {
      document.getElementById("text").innerHTML = data;
    })
    .catch((error) => {
      console.log(error);
    })
  }
}

function sleep(ms = 0) {
  return new Promise(r => setTimeout(r, ms));
}