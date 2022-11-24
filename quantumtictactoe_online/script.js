class Game {
  constructor() {
    this.grid = new Grid();
    this.state = "start";
    this.turn = true;
    
    this.moving_tile = null;
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
      console.log("checked", row)
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

window.onload = () => {
  let canvas = document.getElementById("frame"),
    ctx = canvas.getContext("2d");

  canvas.width = canvas.height = window.innerHeight * 0.99;
  r = canvas.width / 6;
  
  let game = new Game();
  draw(canvas, ctx, game, r);  
  
  canvas.onclick = (e) => {
    let clicked_slot_x = Math.floor((e.offsetX / canvas.width) * 3);
    let clicked_slot_y = Math.floor((e.offsetY / canvas.height) * 3);
    
    let clicked_slot = clicked_slot_x + 3*clicked_slot_y;
    if (game.slot_clicked(clicked_slot)) {
      setTimeout(() => {
        game = new Game();
        draw(canvas, ctx, game, r);
      }, 500);
    }
    
    console.log(game.grid.slots);
    console.log(game.state, game.turn);
    
    draw(canvas, ctx, game, r);
  }
}


function draw(canvas, ctx, game, r) {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.lineWidth = 5;
  
  for (let y = 0; y < 3; y++) {
    for (let x = 0; x < 3; x++) {
      ctx.beginPath();
      
      if (game.turn) {
        ctx.strokeStyle = "orange";
      } else {
        ctx.strokeStyle = "grey";
      }

      ctx.arc(x*2*r + r, y*2*r + r, r-2, 0, Math.PI * 2);
      ctx.stroke();
      
      tile = game.grid.slots[3*y + x];
      
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