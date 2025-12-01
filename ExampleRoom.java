import java.util.*;

public class ExampleRoom {
	
	public static void main(String[] args) {
		// Define mazes as strings (X = blocked, space = open)
		//String maze1 = "xxx \n x \n xx";
		//String maze2 = "xxxxx\n x\n xxxx\n x \n xxxx";
		//String maze3 = "xxxxx\n x\nxxx x\nx xxx\nx \nxxxxx";
		//String maze4 = "xxxx\nxx xxx\nx x\nx xxxx";
		String maze5 = "xx xx  xxx\n"
		+ 			   "xx x    x \n"
		+ 			   "x   xxxxx \n"
		+ 			   "xx xx x xx\n"
		+ 			   " xxxx x x \n"
		+ 			   "     xxx x\n"
		+ 			   "xxxxx x xx\n"
		+ 			   "xxxxxxxxxx\n"
		+ 			   "xx    x   \n"
		+ 			   "x xxxxxxxx\n";
		
		// Run each maze
		//mazeRunner(maze1, 0, 0);
		//mazeRunner(maze2, 0, 0);
		//mazeRunner(maze3, 0, 0);
		//mazeRunner(maze4, 0, 0);
		mazeRunner(maze5, 0, 0);
	}
	
	private static void mazeRunner(String mazeString, int startX, int startY) {
		
		// Convert string to 2D array
		String[] rows = mazeString.split("\n");
		int cols = 0;
		for (String row : rows) {
			cols = Math.max(cols, row.length());
		}
		
		boolean[][] grid = new boolean[cols][rows.length];
		for (int row = 0; row < rows.length; row++) {
			for (int col = 0; col < rows[row].length(); col++) {
				grid[col][row] = rows[row].charAt(col) != ' ';
			}
		}
		
		// Door is always at bottom-right
		int endX = cols - 1;
		int endY = rows.length - 1;
		
		// Print the original grid
		printGrid(grid);
		
		// Number all open spaces using BFS and get shortest path
		int[][] numberedGrid = numberOpenSpaces(grid, startX, startY, endX, endY);
		
		// Print numbered grid
		printNumberedGrid(numberedGrid, grid);
		
		// Find shortest path using BFS
		List<String> shortestPath = findShortestPathBFS(grid, startX, startY, endX, endY);
		
		// Print shortest path
		System.out.println("\n--- SHORTEST PATH ---");
		if (shortestPath.isEmpty()) {
			System.out.println("No path found!");
			return;
		}
		
		String pathStr = String.join("", shortestPath);
		System.out.println("Shortest path: " + pathStr);
		System.out.println("Steps: " + shortestPath.size());
		
		// Create robot and run the path
		Robot robot = new Robot(grid, startX, startY);
		
		new Thread(() -> {
			try {
				Thread.sleep(500);
				for (String move : shortestPath) {
					robot.go(move.charAt(0));
					Thread.sleep(300);
				}
				robot.say("Done!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		
		try {
			Thread.sleep(2000 + (shortestPath.size() * 300));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Print the original 2D grid array
	private static void printGrid(boolean[][] grid) {
		System.out.println("\n--- ORIGINAL GRID ---");
		System.out.println("Visual representation:");
		for (int j = 0; j < grid[0].length; j++) {
			for (int i = 0; i < grid.length; i++) {
				if (grid[i][j]) {
					System.out.print("X ");  // Open space
				} else {
					System.out.print("  ");  // Blocked
				}
			}
			System.out.println();
		}
	}
	
	// Number all open spaces using BFS
	private static int[][] numberOpenSpaces(boolean[][] grid, int startX, int startY, int endX, int endY) {
		int width = grid.length;
		int height = grid[0].length;
		int[][] numberedGrid = new int[width][height];
		
		// Initialize with -1 (unvisited)
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				numberedGrid[i][j] = -1;
			}
		}
		
		// BFS to number all open spaces
		Queue<int[]> queue = new LinkedList<>();
		queue.add(new int[]{startX, startY});
		numberedGrid[startX][startY] = 0;
		
		int[] dx = {1, -1, 0, 0};
		int[] dy = {0, 0, 1, -1};
		
		while (!queue.isEmpty()) {
			int[] current = queue.poll();
			int x = current[0];
			int y = current[1];
			int distance = numberedGrid[x][y];
			
			// Explore all 4 directions
			for (int i = 0; i < 4; i++) {
				int nx = x + dx[i];
				int ny = y + dy[i];
				
				// Check bounds and if open space and not visited
				if (nx >= 0 && nx < width && ny >= 0 && ny < height && 
					grid[nx][ny] && numberedGrid[nx][ny] == -1) {
					numberedGrid[nx][ny] = distance + 1;
					queue.add(new int[]{nx, ny});
				}
			}
		}
		
		return numberedGrid;
	}
	
	// Print numbered grid with open spaces showing distance from start
	private static void printNumberedGrid(int[][] numberedGrid, boolean[][] grid) {
		System.out.println("\n------- NUMBERED GRID --------");
		int width = numberedGrid.length;
		int height = numberedGrid[0].length;
		
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				if (numberedGrid[i][j] == -1) {
					System.out.print("   ");  // Open
				} else {
					System.out.print(String.format("%2d ", numberedGrid[i][j]));
				}
			}
			System.out.println();
		}
	}
	
	// Find shortest path using BFS
	private static List<String> findShortestPathBFS(boolean[][] grid, int startX, int startY, int endX, int endY) {
		int width = grid.length;
		int height = grid[0].length;
		
		// Store parent information for path reconstruction
		Map<String, String> parent = new HashMap<>();
		Map<String, Character> moveToParent = new HashMap<>();
		Queue<int[]> queue = new LinkedList<>();
		boolean[][] visited = new boolean[width][height];
		
		queue.add(new int[]{startX, startY});
		visited[startX][startY] = true;
		parent.put(startX + "," + startY, null);
		
		int[] dx = {1, -1, 0, 0};
		int[] dy = {0, 0, 1, -1};
		char[] dirs = {'E', 'W', 'S', 'N'};
		
		// BFS to find shortest path
		while (!queue.isEmpty()) {
			int[] current = queue.poll();
			int x = current[0];
			int y = current[1];
			
			// Check if reached destination
			if (x == endX && y == endY) {
				// Reconstruct path
				return reconstructPath(parent, moveToParent, startX, startY, endX, endY);
			}
			
			// Explore all 4 directions
			for (int i = 0; i < 4; i++) {
				int nx = x + dx[i];
				int ny = y + dy[i];
				
				// Check bounds and if open space and not visited
				if (nx >= 0 && nx < width && ny >= 0 && ny < height && 
					grid[nx][ny] && !visited[nx][ny]) {
					visited[nx][ny] = true;
					queue.add(new int[]{nx, ny});
					parent.put(nx + "," + ny, x + "," + y);
					moveToParent.put(nx + "," + ny, dirs[i]);
				}
			}
		}
		
		// No path found
		return new ArrayList<>();
	}
	
	// Reconstruct path from parent map
	private static List<String> reconstructPath(Map<String, String> parent, Map<String, Character> moveToParent,
											   int startX, int startY, int endX, int endY) {
		List<String> path = new ArrayList<>();
		String current = endX + "," + endY;
		
		while (parent.get(current) != null) {
			path.add(0, String.valueOf(moveToParent.get(current)));
			current = parent.get(current);
		}
		
		return path;
	}
}