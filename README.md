# ❌⭕ X and O - Enhanced Tic Tac Toe Game

An advanced Android Tic Tac Toe game built with Kotlin featuring **offline solo mode with AI**, **online multiplayer**, and **local multiplayer**. The app now includes intelligent AI opponents with multiple difficulty levels and real-time online gameplay using Firebase.

---

## 🎮 Features

### ✅ Currently Implemented
- **Classic 3x3 grid gameplay**
- **Three Game Modes:**
  - 🧠 **Solo Mode** - Play against AI with three difficulty levels
  - 👥 **Offline Multiplayer** - Two players on the same device  
  - 🌐 **Online Multiplayer** - Real-time gameplay with friends
- **AI Difficulty Levels:**
  - 🟢 **Easy** - Random moves
  - 🟡 **Medium** - Strategic blocking and winning moves
  - 🔴 **Hard** - Unbeatable MiniMax algorithm
- **Modern Material Design UI**
- **Real-time game status updates**
- **Firebase-powered online matchmaking**
- ** Simple and clean UI using GridLayout
- ** Toasts for player turns and results


---


## 🎯 Game Modes

### 🧠 Solo Mode with AI
- **Easy Difficulty**: AI makes random valid moves
- **Medium Difficulty**: AI can block wins and take winning moves
- **Hard Difficulty**: Uses MiniMax algorithm for optimal play
- Player always plays as **X**, AI plays as **O**

### 👥 Offline Multiplayer
- Traditional two-player gameplay
- Alternate turns between Player X and Player O
- Simple and intuitive interface

### 🌐 Online Multiplayer
- **Real-time synchronization** using Firebase Realtime Database
- **Automatic matchmaking** system
- **Game room management** with unique session IDs
- **Live opponent tracking** and turn-based gameplay

---

## 🙏 Acknowledgments
- **Original game logic inspired by classic Tic Tac Toe implementations
- **Firebase for real-time database services
- **Android Material Design components

---

## 🚀 How to Run Locally

### 📥 Steps

1. Clone the repository from GitHub.
2. Open the project in Android Studio.
3. Connect a physical device or use an emulator.
4. Click the **Run** button.

---

## 🌐 Making the App Online Multiplayer

To support **online play** between two users, follow these steps:

### 🧩 1. Add Firebase to Your Project

* Visit the Firebase Console at [https://console.firebase.google.com/](https://console.firebase.google.com/)
* Create a new Firebase project
* Connect your app to firebase using the following services
* Enable Firebase services:

  * Firebase Realtime Database or Firestore
  * Firebase Authentication (for usernames or anonymous login) - not necessary, can ask user to input username on landing page

Ensure your Gradle files include Firebase services.

### 🌍 2. Implement Online Match Logic

* When user selects **Online Match** from the landing page:

  * Create or join a game session (game room)
  * Use Firebase to store the game board state and player turns under a `games/{roomId}` node
  * Update the board in real-time as players make moves
  * Show opponent's name on screen

#### Example game state (conceptual):

games:
• room123
 • player1: Alice
 • player2: Bob
 • board: \["X", "", "O", "", "", "", "", "", ""]
 • turn: Bob
 • result: ""

---

## 🤖 Solo Mode with Minimax AI

To enable solo gameplay:

1. Add a landing page with mode selection: Solo vs Multiplayer
2. When Solo is selected:

   * Let the user go first
   * On each turn, run the Minimax algorithm to determine the best move for the AI
   * This is a challenege, please use online resources to help
3. Insert the AI’s move automatically after the player’s turn

AI should play as "O" and consider all board states recursively to determine the optimal outcome (win/draw).

---

## 🧠 Code Structure

* MainActivity.kt: Contains all the game logic and board interactions
* activity\_main.xml: The layout with a 3x3 GridLayout and reset button

---

## 💡 Activity Summerized

* Firebase real-time multiplayer
* Minimax AI for single-player mode
* Username and profile management
* Firebase leaderboards - later on
* Animations and UI polish
* Sound effects
