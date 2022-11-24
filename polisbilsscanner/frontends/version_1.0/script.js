window.onload = async () => {
  
  var video = document.getElementById('video');

  // Get access to the camera!
  if(navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      const devices = await navigator.mediaDevices.enumerateDevices();
      const videoDevices = devices.filter(device => device.kind === 'videoinput');
      
    // Not adding `{ audio: true }` since we only want video now
    navigator.mediaDevices.getUserMedia({video: {facingMode: "environment"}}).then(function(stream) {
      //video.src = window.URL.createObjectURL(stream);
      video.srcObject = stream;
      video.play();
    });
  }
  
  var canvas = document.getElementById('canvas');
  var context = canvas.getContext('2d');
  
  let img_base64;

  // Trigger photo take
  document.getElementById("video").addEventListener("click", function() {
    context.drawImage(video, 0, 0, 640, 480);
    img_base64 = canvas.toDataURL("image/jpg").substr(22);

    fetch("https://haito.pythonanywhere.com/test/", {
      method: "post",
      body: img_base64
    })
    .then((response) => response.text())
    .then(function (data) {
      document.getElementById("text").innerHTML = data;
    })
    .catch(() => {
      console.log("Error");
    })
  });
}