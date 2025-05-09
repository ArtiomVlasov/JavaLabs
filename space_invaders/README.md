# Space Invaders

A Java implementation of the classic Space Invaders arcade game from 1978.

## Requirements

- Java 8 or higher
- Maven (for building)

## Installation

1. Clone the repository
2. Navigate to the project directory
3. Build the project with Maven:
```bash
mvn clean package
```
4. Run the game:
```bash
java -jar target/space-invaders-1.0.jar
```

## How to Play

### Controls
- Left Arrow: Move left
- Right Arrow: Move right
- Space: Shoot
- R: Restart game (when game is over)

### Gameplay
- Defend Earth from waves of alien invaders
- Shoot the aliens to earn points
- Different rows of aliens are worth different points
- Avoid alien shots
- You have 3 lives
- Game ends when:
  - All aliens are destroyed (Victory!)
  - Aliens reach the bottom (Game Over)
  - Player loses all lives (Game Over)

### Scoring
- Top row aliens: 40 points
- Second row aliens: 30 points
- Third row aliens: 20 points
- Fourth row aliens: 10 points
- Fifth row aliens: 10 points

## Features

- Classic Space Invaders gameplay
- High score system
- Sprite-based graphics
- Smooth animations
- Sound effects (coming soon)

## Development

The game follows the Model-View-Controller (MVC) pattern:
- Model: Game state and logic
- View: Rendering and graphics
- Controller: Input handling

### Project Structure
```
src/main/java/org/spaceinvaders/
├── SpaceInvaders.java       # Main class
├── model/                   # Game model classes
├── view/                    # View classes
└── controller/              # Controller classes
``` 