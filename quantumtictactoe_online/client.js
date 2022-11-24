window.onload = async () => {
  let canvas = document.getElementById("frame"),
      ctx = canvas.getContext("2d"),
      gameIdField = document.getElementById("gameId"),
      joinButton = document.getElementById("join"),
      newButton = document.getElementById("new"),
      menuDiv = document.getElementById("menu"),
      waitingDiv = document.getElementById("waiting");

  canvas.width = canvas.height = window.innerHeight * 0.99;
  r = canvas.width / 6;
  
  let url = "ws://192.168.1.204:12345";
  let connection;
  
  let gameId;
  
  newButton.onclick = () => buttonOnclick("newgame");
  joinButton.onclick = () => buttonOnclick("joingame");
  
  canvas.onclick = (e) => {
    let clicked_slot_x = Math.floor((e.offsetX / canvas.width) * 3);
    let clicked_slot_y = Math.floor((e.offsetY / canvas.height) * 3);
    
    let clicked_slot = clicked_slot_x + 3*clicked_slot_y;
    
    let data = {
      slot : clicked_slot
    }
    
    if (connection) {
      connection.send(JSON.stringify(data));
      console.log("Sending slot clicked:", clicked_slot);
    }
  }
  
  buttonOnclick = (type) => {
    if (connection) return;
    
    gameId = gameIdField.value;
    
    connection = new WebSocket(url);
    
    connection.onerror = () => {
      console.log("Connection error.");
      connection = null;
    }
    connection.onopen = () => {
      let data = {
        "commandType" : type,
        "gameId" : gameId
      }
      connection.send(JSON.stringify(data));
    }
    connection.onclose = () => {
      console.log("Connection closed.");
      clear(canvas, ctx);
      menuDiv.style.visibility = "visible";
      waitingDiv.style.visibility = "hidden";
      
      connection = null;
    }
    connection.onmessage = (e) => {
      let data;

      try {
        data = JSON.parse(e.data);
      }
      catch(err) {
        console.log(err);
      }

      if (data.responseType == "newgame") {
        menuDiv.style.visibility = "hidden";
        waitingDiv.style.visibility = "visible";
        console.log("Game waiting.");
      }

      if (data.responseType == "gamestarted") {
        draw(canvas, ctx, data.grid, data.turn, r);
        menuDiv.style.visibility = "hidden";
        waitingDiv.style.visibility = "hidden";
        console.log("Game started.");
      }

      if (data.responseType == "gameupdate") {
        draw(canvas, ctx, data.grid, data.turn, r);
        console.log("Game updated.");
      }
      
      if (data.responseType == "gamequit") {
        console.log("Opponent quit.");
        try {
          connection.close();
        } catch (err) {
          console.log("Connection already closed.");
        }
      }

      if (data.responseType == "gamewin") {
        console.log("Game over. Win for:", data.team);

        setTimeout(() => {
          connection.close();
        }, 1000);
      }

      if (data.responseType == "error") {
        console.log("Error from server:", data.message);
        connection.close();
        connection = null;
      }
    }
  }
}

function draw(canvas, ctx, grid, turn, r) {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.lineWidth = 5;
  
  for (let y = 0; y < 3; y++) {
    for (let x = 0; x < 3; x++) {
      ctx.beginPath();
      
      if (turn) {
        ctx.strokeStyle = "orange";
      } else {
        ctx.strokeStyle = "grey";
      }

      ctx.arc(x*2*r + r, y*2*r + r, r-2, 0, Math.PI * 2);
      ctx.stroke();
      
      tile = grid.slots[3*y + x];
      
      if (tile) {

        ctx.beginPath();
        
        if (tile.team) {
          ctx.fillStyle = "orange";
        } else {
          ctx.fillStyle = "grey";
        }
        probability_weighted_radius = r*Math.sqrt(tile.probabilites[3*y + x]);
        ctx.arc(x*2*r + r, y*2*r + r, probability_weighted_radius - 10, 0, Math.PI * 2);
        ctx.fill();
      }
    }
  }
}

function clear(canvas, ctx) {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
}