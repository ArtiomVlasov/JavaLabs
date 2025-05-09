# Donkey Kong Game

A Java implementation of the classic Donkey Kong game using the MVC (Model-View-Controller) design pattern.

## Features

- Player movement using arrow keys or WASD
- Enemy AI and collision detection
- Score tracking system
- High score system with file persistence
- Pause/Resume functionality
- Simple 2D graphics using Java Swing

## Requirements

- Java 8 or higher
- Java Development Kit (JDK)
- Any Java IDE (IntelliJ IDEA recommended)

## Installation

1. Clone the repository or download the source code
2. Open the project in your Java IDE
3. Build the project to resolve dependencies
4. Run the `Main` class to start the game

## How to Play

### Controls
- Use Arrow keys or WASD to move the player
- Space to shoot (if implemented)
- ESC or Game menu to pause

### Gameplay
- Avoid enemies and barrels
- Collect points by surviving and defeating enemies
- Game ends when player loses all lives
- High scores are automatically saved

### Menu Options
- New Game: Start a new game
- Pause: Pause/Resume the current game
- Exit: Close the game

## Project Structure

The game follows the MVC pattern:

- Model: Contains game logic and state
  - `GameModel.java`: Main game state
  - `PlayerModel.java`: Player state
  - `EnemyModel.java`: Enemy behavior
  
- View: Handles game rendering
  - `GameView.java`: Main game rendering
  
- Controller: Handles user input and game flow
  - `GameController.java`: Main game controller
  - `PlayerController.java`: Player input handling

## Development

The project is open for contributions. Feel free to submit issues and pull requests.

## License

This project is open source and available under the MIT License. 