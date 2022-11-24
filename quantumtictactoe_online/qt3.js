let http = require("http"),
    socket = require("websocket").server,
    port = 12345,
    games = [],
    game_ids = [];

let htServer = http.createServer().listen(port);

let wsServer = new socket({
  httpServer: htServer,
  autoAcceptConnections: true
});

console.log("Listening on: " + port);

wsServer.on("connect", (connection) => {
  
  let handleIncomingConnection = (e) => {
    let data = getData(e);
    
    if (!data) {
      console.log("No data.");
      sendErrorResponse(connection, "No data.");
      return;
    }
    if (!data.gameId) {
      console.log("No game id.");
      sendErrorResponse(connection, "No game id!");
      return;
    }
    
    if (data.commandType == "newgame") {
      if (game_ids.includes(data.gameId)) {
        console.log(data.gameId, ": Game id already exists.");
        sendErrorResponse(connection, "Game id taken!");
        return;
      }
      
      console.log(data.gameId, ": New game started.");
      
      let new_game = new Game(connection, data.gameId);
      
      connection.removeListener("message", handleIncomingConnection);
      connection.on("message", (e) => handlePlayerMessage(e, true, new_game));
      connection.on("close", (e) => handlePlayerDisconnect(e, true, new_game));
      
      games.push(new_game);
      game_ids.push(data.gameId);
      
      let response = {
        "responseType" : "newgame",
        "grid" : new_game.grid,
        "turn" : new_game.turn
      }
      
      connection.send(JSON.stringify(response));
    }
    
    if (data.commandType == "joingame") {
      if (!game_ids.includes(data.gameId)) {
        console.log(data.gameId, ": No game found.");
        sendErrorResponse(connection, "No game found!");
        return;
      }
      
      let game = games[game_ids.indexOf(data.gameId)];
      
      if (game.isActive) {
        console.log(data.gameId, ": Game is active.");
        sendErrorResponse(connection, "Game is full!");
        return;
      }
      if (game.connections.includes(connection)) {
        console.log(data.gameId, ": Game has double connection.");
        sendErrorResponse(connection, "Double connection!");
        return;
      }
      
      console.log(data.gameId, ": Second player joining game.");
      
      game.start(connection);
      
      connection.removeListener("message", handleIncomingConnection);
      connection.on("message", (e) => handlePlayerMessage(e, false, game));
      connection.on("close", (e) => handlePlayerDisconnect(e, false, game));
      
      let response = {
        "responseType" : "gamestarted",
        "grid" : game.grid,
        "turn" : game.turn
      }
      
      game.connections[0].send(JSON.stringify(response));
      game.connections[1].send(JSON.stringify(response));
    }
  }
  
  let handlePlayerMessage = (e, team, game) => {
    let data = getData(e);

    if (!data) {
      console.log("No data.");
      sendErrorResponse(connection, "No data!");
      return;
    }
    if (data.slot == null) {
      console.log("No slot data.");
      sendErrorResponse(connection, "No slot data!");
      return;
    }
    if (!([0,1,2,3,4,5,6,7,8].includes(data.slot))) {
      console.log("Invalid slot.");
      sendErrorResponse(connection, "Invalid slot!");
      return;
    }
    
    if (!game.isActive) return;
    
    let win = game.play(data.slot, team);

    for (let con of game.connections) {
      sendGameUpdate(con, game);
      if (win) {
        sendGameWin(con, game, team);
        games.splice(games.indexOf(game), 1);
        game_ids.splice(game_ids.indexOf(game.gameId), 1);
      }
    }
  }
  
  let getData = (e) => {
    try {
      return JSON.parse(e.utf8Data);
    }
    catch(err) {
      console.log(err);
      return null;
    }
  }
  
  let handlePlayerDisconnect = (e, team, game) => {
    console.log(game.id, " :Connection closed for team", team);
    if (game.isActive) {
      sendGameQuit(game.connections[[true, false].indexOf(!team)], game, !team);
    }
    games.splice(games.indexOf(game), 1);
    game_ids.splice(game_ids.indexOf(game.gameId), 1);
  }
  
  let sendErrorResponse = (connection, message) => {
    let response = {
      "responseType" : "error",
      "message" : message
    }
    connection.send(JSON.stringify(response));
  }
  
  let sendGameUpdate = (connection, game) => {
    let response = {
      "responseType" : "gameupdate",
      "grid" : game.grid,
      "turn" : game.turn
    }
    connection.send(JSON.stringify(response));
  }
  
  let sendGameQuit = (connection, game, team) => {
    let response = {
      "responseType" : "gamequit",
      "team" : team
    }
    connection.send(JSON.stringify(response));
  }
  
  let sendGameWin = (connection, game, team) => {
    let response = {
      "responseType" : "gamewin",
      "team" : team
    }
    connection.send(JSON.stringify(response));
  }
  
  connection.on("message", handleIncomingConnection);
});

class Game {
  constructor(connection, gameId) {
    this.grid = new Grid();
    this.state = "start";
    this.turn = true;
    this.connections = [connection];
    this.id = gameId;
    this.isActive = false;
    
    this.moving_tile = null;
  }
  play(slot, team) {
    console.log(this.id, ": Team", team, "sent slot", slot);
    
    if (this.turn == team && this.isActive) {
      let clicked_tile = this.grid.slots[slot];

      this.do_logic(clicked_tile, slot);
      
      this.grid.check_wins(false);
      if (this.grid.check_wins(true)) {
        console.log(this.id, ": Win for team", team);
        
        this.isActive = false;
        return true;
      }
    }
    
    return false;
  }
  start(connection) {
    this.connections.push(connection);
    this.isActive = true;
  }
  do_logic(clicked_tile, slot) {
    if (this.state == "start") {

      if (clicked_tile) {

        if (clicked_tile.team == this.turn) {
          clicked_tile.move_from(slot);
          this.moving_tile = clicked_tile;
          this.state = "move";
          return;
        }
        
        if (clicked_tile.probabilites[slot] == 1) {
          return;
        }

        if (this.grid.measure(clicked_tile) == slot) {
          this.turn = !this.turn;
          return;
        }
      }
      
      if (this.grid.team_count(this.turn) == 3) return;
      
      this.grid.place(new Tile(this.turn, slot), slot);
      this.turn = !this.turn;
    }
    
    if (this.state == "move") {
      
      if (clicked_tile) {
        if (this.moving_tile == clicked_tile) return;
        if (this.grid.measure(clicked_tile) == slot) {
          this.moving_tile.abort_move();
          this.moving_tile = null;
          this.state = "start";
          this.turn = !this.turn;
          return;
        }
      }
      
      this.grid.place(this.moving_tile, slot);
      this.moving_tile.move_to(slot);
      this.moving_tile = null;
      this.state = "start";
      this.turn = !this.turn;
    }
  }
  slot_clicked(slot) {
    let clicked_tile = this.grid.slots[slot];
    
    this.do_logic(clicked_tile, slot);
    this.grid.check_wins(false);
    if (this.grid.check_wins(true)) {
      return true;
    }
    
    return false;
  }
}

class Grid {
  constructor() {
    this.slots = [
      null, null, null,
      null, null, null,
      null, null, null
    ]
  }
  place(tile, slot) {
    this.slots[slot] = tile;
  }
  measure(tile) {
    let random_number = Math.random();
    let sum = 0;
    let measured_slot = -1;
    
    for (let i = 0; i < 9; i++) {
      if (tile.probabilites[i] > 0) {
        sum += tile.probabilites[i];
        if (random_number < sum && measured_slot == -1) {
          measured_slot = i;
          continue;
        }
        this.slots[i] = null;
      }
    }
    
    tile.collapse(measured_slot);
    return measured_slot;
  }
  check_wins(measured) {
    for (let i = 0; i < 3; i++) {
      let row = this.slots.slice(i*3, i*3+3);
      if (this.check_row(row, measured)) return true;
      
      let col = [this.slots[i], this.slots[i+3], this.slots[i+6]];
      if (this.check_row(col, measured)) return true;
    }
    
    let diag = [this.slots[0], this.slots[4], this.slots[8]];
    if (this.check_row(diag, measured)) return true;
    
    let antidiag = [this.slots[2], this.slots[4], this.slots[6]];
    if (this.check_row(antidiag, measured)) return true;
  }
  check_row(row, measured) {
    if (!row.includes(null) && 
        row[0].team == row[1].team && 
        row[1].team == row[2].team &&
        row[0].move_probability == 0 &&
        row[1].move_probability == 0 &&
        row[2].move_probability == 0) {
      if (measured) return true;
      for (let tile of row) {
        this.measure(tile);
      }
    }
    return false;
  }
  team_count(team) {
    let team_tiles = [];
    for (let slot of this.slots) {
      if (slot != null && slot.team == team && !team_tiles.includes(slot)) {
        team_tiles.push(slot);
      }
    }
    return team_tiles.length;
  }
}

class Tile {
  constructor(team, slot) {
    this.team = team;
    this.probabilites = [
      0, 0, 0,
      0, 0, 0,
      0, 0, 0
    ]
    this.probabilites[slot] = 1;
    this.move_probability = 0;
    this.moving_from = -1;
  }
  move_from(slot) {
    this.probabilites[slot] /= 2;
    this.move_probability = this.probabilites[slot];
    this.moving_from = slot;
  }
  move_to(slot) {
    this.probabilites[slot] = this.move_probability;
    this.move_probability = 0;
    this.moving_from = -1;
  }
  abort_move() {
    this.probabilites[this.moving_from] *= 2;
    this.move_probability = 0;
    this.moving_from = -1;
    
  }
  collapse(slot) {
     this.probabilites = [
      0, 0, 0,
      0, 0, 0,
      0, 0, 0
    ]
    this.probabilites[slot] = 1;
  }
}