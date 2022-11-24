let data;

window.onkeydown = () => {
  
  let info = document.getElementById("info");
  
  info.innerHTML = "Kanske polis";
  info.style.color = "black";
  
  if (event.keyCode !== 13) return;
  
  let reg = document.getElementById("reg");
  let p = reg.value.toUpperCase();
  
  for (let plate of data.plates) {
    if (plate.number == p) {
      info.innerHTML = "Polis";
      info.style.color = "blue";
      reg.value = "";
      return;
    }
  }
  
  info.innerHTML = "Inte polis";
  reg.value = "";
}

window.onload = () => {
  fetch("data.json")
  .then(response => response.json())
  .then(json => {
    data = json;
  });
}