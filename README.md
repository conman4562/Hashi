# Description
This project was built for a school project my senior year of high school. It is a replication of the game 'Hashi.' You can find the rules and other information on the wiki: [https://en.wikipedia.org/wiki/Hashiwokakero](https://en.wikipedia.org/wiki/Hashiwokakero)
# How to run
#### You must have Java 8+ installed on your computer
- If you do not you can download it from here: [https://adoptium.net/](https://adoptium.net/)
- If you are struggling with that, there are plenty of written and YouTube guides, feel free to look one up.
#### Once you have Java installed, download Hashi.jar from [https://github.com/conman4562/Hashi/releases/latest](https://github.com/conman4562/Hashi/releases/latest) and run it.
- On Mac, it will say "Hashi.jar not opened, Apple could not verify “Hashi.jar” is free of malware that may harm your Mac or compromise your privacy." This program is not malware.
- To avoid this, go into System Settings -> Privacy & Security (scroll down to Security section) -> "Hashi.jar" was blocked to protect your Mac -> Open Anyway

# Features
## Mouse controls
- Click and drag on the nodes to draw edges between nodes. It cycles between 0 -> 1 -> 2 -> 0 ... edges. The nodes are initially highlighted black, and are highlighted green when they have the correct number of edges, and red when they have too many. 
- Click and drag on the white background to pan around the puzzle.
- Scroll with your scroll wheel to zoom in and out of the puzzle.
## Sidebar buttons
- Press 'I' or click on the 'Instructions' button on the sidebar to view instructions about the program, including Hashi Rules, General info, Controls, and Buttons.
- Press 'R' or click on the 'Re-Center' button on the sidebar to move the puzzle in view.
- Press 'C' or click on the 'Clear Bridges' button on the sidebar to clear all bridges.
  - Immediately after this button is pressed, you can press 'Z' or click on the 'Undo' button that pops up next to the clear button to undo the clear. You are allowed to press this undo button until you make a change to the puzzle (manually or using the solver).
 - Press 'J' or click on the 'Import' button to import a puzzle in the format outlined below.
 - Press 'E' or click on the 'Export' button to export the current puzzle to a text (.txt) file with your choice of name and location in the format outlined below.
 - Press 'V' or click the 'Check solution' button to check if the current puzzle is solved. If unsolved, it will put a blue box around all island clusters with less than 50% of the total islands to show they are disconnected from the main cluster and where. It will also circle all nodes that do not have the correct amount of edges in a red circle. It will pop up with a red "NOT SOLVED" or a green "SOLVED" in the top left corner depending on whether or not it is solved.
 - Press 'S' or click the 'Solve Puzzle' button to solve the puzzle. This will clear all of the current edges and solve the puzzle and pop up with a green 'SOLVED' message in the top left or determine that it is unsolvable and will notify you with a red "UNSOLVABLE" message in the top left corner. During solving, 4 buttons appear below the solve button ('Slow'/'Med'/'Fast'/'Instant') that you can click on to change how fast the program solves the problem visually.
## Hotkeys only
- Press 'ESC' to close the application.
## Format of input/output files
 ```
1) Must be a text (.txt) file.
2) Use numbers (1-8) to represent the number of connections required
3) Use '-' to represent 1 horizontal connection
4) Use '=' to represent 2 horizontal connections
5) Use '|' to represent 1 vertical connection
6) Use '#' to represent 2 vertical connections
6) Use ' ' (space) to represent an empty cell, these must be used to align rows
7) Connections can travel however long is needed
8) Every horizontal grid row must be terminated by a newline ("\n") character, 
and can be terminated by a carraige return ("\r\n)
```
<img align="right" width="300" alt="Screenshot 2026-10-01 at 00 42 10" src="https://github.com/user-attachments/assets/b1fd4efc-9aa6-4365-9120-cb6a6bacf467" />

```
Example:
2 2-5=2    
# | #1-4--1
6=3 #  # 
#2--6-1#  
3|1 #2=6  
|2# #  #  
1|3=5  3  
 2 3--1  
   |      
 1-2
```
