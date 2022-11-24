window.onkeydown = () => {
  
  let info = document.getElementById("info");
  
  info.innerHTML = "Kanske polis";
  info.style.color = "black";
  
  if (event.keyCode !== 13) return;
  
  let reg = document.getElementById("reg").value.toUpperCase();
  
  fetch("https://yacdn.org/proxy/https://api.biluppgifter.se/api/v1/vehicle/regno/" + reg, {
    method: "GET",
    headers: {
      "Authorization": "Bearer BoEiDFNjlA9A7lpeRECO3lJwAlDZkjMFtJ6iYOGx4MjTWtwclEJ9wmfE63gy",
      "Content-Type": "application/json",
      "Accept": "application/json",
      "User-Agent": "My Test Client"
    }})
  .then((response) => response.json())
  .then(function (data) {
      if (data.data.technical.data.chassi == "Polisbil") {
        info.innerHTML = "Polis";
        info.style.color = "blue";
      } else {
        info.innerHTML = "Inte polis";
      }
    
      document.getElementById("reg").value = "";
  })
  .catch(() => {
    info.innerHTML = "Fel";
  })
}