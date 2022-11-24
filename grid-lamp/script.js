let canvas, ctx, width, height, inc;

function click(e) {
  
  let x = Math.floor(e.offsetX / (2 * inc));
  let y = Math.floor(e.offsetY / (2 * inc));
  
  let ind = 39 - (y * 8 + x);
  
  while (ind.toString().length < 2) ind = "0" + ind;
  
  let c = document.getElementById("col").value;
  
  let cols = [];
  
  ctx.beginPath();
  ctx.rect(x * inc, y * inc, inc, inc);
  ctx.strokeStyle = "white";
  ctx.fillStyle = c;
  ctx.fill();
  ctx.stroke();
  
  
  for (let i = 1; i < 6; i += 2) {
    let c0 = c.substring(i, i+2);
    
    let c1 = parseInt(c0, 16).toString();
    
    
    while (c1.length < 3) c1 = "0" + c1;
    
    cols.push(c1)
  }
  
  
  let s = cols.join("");
  
  try {
      fetch("http://192.168.0.33/?" + ind + s);
  } catch (error) {
      fetch("http://81.232.18.118:12346/?" + ind + s);
  }
}

function colorAll() {
  let c = document.getElementById("col").value;
  let cols = [];
  
  for (let x = 0; x < 8 * inc; x += inc) {
    for (let y = 0; y < 5 * inc; y += inc) {
      ctx.beginPath();
      ctx.rect(x, y, inc, inc);
      ctx.strokeStyle = "white";
      ctx.fillStyle = c;
      ctx.fill();
      ctx.stroke();
    }
  }
  
  
  for (let i = 1; i < 6; i += 2) {
    let c0 = c.substring(i, i+2);
    
    let c1 = parseInt(c0, 16).toString();
    
    
    while (c1.length < 3) c1 = "0" + c1;
    
    cols.push(c1)
  }
  
  
  let s = cols.join("");
  
  try {
      fetch("http://192.168.0.33/?" + 40 + s);
  } catch (error) {
      fetch("http://81.232.18.118:12346/?" + 40 + s);
  }
}

function draw() {
  canvas.width = window.innerWidth * 0.2;
  canvas.height = canvas.width / 1.6;
  
  inc = canvas.width / 8;
  
  for (let x = 0; x < 8 * inc; x += inc) {
    for (let y = 0; y < 5 * inc; y += inc) {
      ctx.beginPath();
      ctx.rect(x, y, inc, inc);
      ctx.strokeStyle = "white";
      ctx.stroke();
    }
  }
}


window.onresize = draw;

window.onload = () => {
  canvas = document.getElementById("canvas");
  
  canvas.width = window.innerWidth * 0.2;
  canvas.height = canvas.width / 1.6;
  
  inc = canvas.width / 8;
  
  canvas.onclick = click;

  ctx = canvas.getContext("2d");

  draw();
}

//fetch("http://81.232.18.118:12346/?" + "1" + s);