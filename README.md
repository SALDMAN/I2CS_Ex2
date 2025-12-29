# I2CS_Ex2
This is third assaigemnt counting from 0 in course Intro to computer science in Ariel University.

Map & Index2D — Focused explanation (algorithms & function ideas)
This document explains the role and intent of the two central classes in the project — Map.java and Index2D — and describes the core algorithms implemented in Map (only the algorithmic ideas and function purposes).

Map.java — purpose and functions (short, conceptual)

Map represents a rectangular 2D integer grid and exposes two kinds of capabilities:

Image-like operations (mutating pixels and drawing):

init(w,h,v) / init(int[][] arr) : create or replace the grid (rectangular validation + deep copy).
getMap() : return a defensive deep-copy of the underlying 2D array.
getPixel(x,y) / getPixel(Pixel2D p) and setPixel(...) : single-cell read/write with bounds validation.
drawRect(p1,p2,color) : fill axis-aligned rectangle between two corners (clipped to the map).
drawLine(p1,p2,color) : rasterize a straight line by iterating the dominant axis and rounding the orthogonal coordinate.
drawCircle(center, rad, color) : set cells whose Euclidean distance to center <= rad.
rescale(sx,sy) : nearest-neighbor resampling (compute new sizes, sample nearest source pixels).
addMap2D(other) and mul(scalar) : element-wise add and scalar multiply.
Graph-like BFS operations on the 4-neighbor grid:

fill(p, new_v, cyclic) : flood-fill (connected component) starting at p — repaint all 4-connected cells sharing the start color.
shortestPath(p1, p2, obsColor, cyclic) : BFS shortest path from p1 to p2 avoiding cells equal to obsColor.
allDistance(start, obsColor, cyclic) : produce a new map where each cell contains the shortest number of steps from start (or -1 if unreachable or obstacle).
Small code excerpts (to underline intent, not full implementations):

Defensive copy on init:

public void init(int[][] arr) { /* validate rectangular / / deep-copy into internal int[width][height] buffer */ }

Nearest-neighbor rescale core idea:

int newW = Math.round(width * sx); int newH = Math.round(height * sy); for (int x=0; x<newW; x++) for (int y=0; y<newH; y++) dst[x][y] = src[srcX][srcY]; // srcX = floor(x/sx) clamped

Draw line concept (dominant axis iteration):

if (dx >= dy) { for (xi from x1 to x2) { y = round(interpolate(xi)); setPixel(xi, y); } } else { for (yi from y1 to y2) { x = round(interpolate(yi)); setPixel(x, yi); } }

Index2D — role and functions (concise)

Index2D is a tiny value object for integer coordinates. Its main responsibilities are:

hold an (x,y) pair and return them via getX() / getY();
compute Euclidean distance to another Pixel2D via distance2D(...) (used by drawCircle);
support equality by coordinates (equals) so tests and path reconstruction can compare coordinates;
Example excerpt:

public double distance2D(Pixel2D p2) {
    return Math.sqrt((x - p2.getX())^2 + (y - p2.getY())^2);
}
Index2D is used as the node/vertex type when performing BFS on the grid.

Core algorithms implemented in Map (detailed algorithmic idea)

All three main algorithms share a common pattern:

Work on an implicit graph where each cell is a node and edges connect 4-neighbors (up,down,left,right).
Use a FIFO queue (BFS) to explore nodes layer-by-layer.
Maintain a visited[width][height] boolean matrix to avoid repeated visits.
When cyclic==true neighbor coordinates that fall out of range are wrapped (modular/toroidal topology) before further checks.
Common neighbor enumeration:

int[][] nbrs = {{1,0},{-1,0},{0,1},{0,-1}}; // +x, -x, +y, -y
Flood-fill (fill) — key idea and steps
Goal: Starting from seed p, paint the entire 4-connected component (cells reachable via same-color neighbors) with new_v. Return the number of painted cells.

Algorithm sketch:

Validate p (null or outside => return 0). Read orig = getPixel(p).
If orig == new_v return 0 (nothing to do).
Create visited[][] = false and queue; enqueue p and mark visited.
While queue not empty:
cur = queue.removeFirst(); if getPixel(cur) == orig: setPixel(cur, new_v) and increment counter.
For each neighbor delta in nbrs:
nx = cur.x + dx; ny = cur.y + dy; if cyclic -> wrap nx,ny.
If in bounds AND not visited[nx][ny] AND getPixel(nx,ny) == orig: mark visited and enqueue.
Return painted count.
Notes: BFS ensures every pixel of that connected component is visited exactly once.

Shortest path (shortestPath) — key idea and steps
Goal: Find a shortest (fewest steps) sequence of adjacent 4-neighbor moves from p1 to p2, avoiding cells equal to obsColor. Return an array of coordinates forming the path or null if unreachable.

Algorithm sketch:

Validate inputs (non-null, both inside the map and not obstacles).
visited[][] = false; parent[][] = null (each entry stores the predecessor Index2D); queue = new FIFO.
Enqueue start; visited[start] = true.
While queue not empty:
cur = queue.removeFirst(); if cur == goal: break (found shortest path).
For each neighbor:
compute nx,ny and apply cyclic wrapping if required.
if in bounds AND not visited AND getPixel(nx,ny) != obsColor: visited = true; parent[nx][ny] = cur; enqueue neighbor.
If goal not found return null.
Reconstruct path by following parent pointers from goal back to start, collecting nodes, then reverse to produce ordered path.
Key technique: parent table allows path reconstruction in O(path length) memory/time; BFS guarantees shortest path in an unweighted grid.

All distances (allDistance) — key idea and steps
Goal: Return a map where each cell contains the shortest number of steps from start to that cell, or -1 for obstacles/unreachable cells.

Algorithm sketch:

Create result map filled with -1.
If start null/out-of-bounds => return result (unchanged).
visited[][] = false; queue = FIFO; visited[start] = true; res[start] = 0; enqueue start.
While queue not empty:
cur = queue.removeFirst(); curd = res.getPixel(cur);
For each neighbor:
compute nx,ny; apply cyclic wrap if needed.
if in bounds AND not visited AND getPixel(nx,ny) != obsColor: visited=true; res.setPixel(nx,ny, curd+1); enqueue neighbor.
Return result map.
This is the classical single-source BFS distance expansion — values in res represent BFS layers (distance in steps).

Cyclic wrapping (toroidal grid)

When cyclic == true the neighbor coordinates are wrapped before checking bounds:

if (nx < 0) nx = width - 1;
if (nx >= width) nx = 0;
if (ny < 0) ny = height - 1;
if (ny >= height) ny = 0;
This makes the grid logically connect opposite edges (left↔right, top↔bottom), and BFS traverses accordingly.

Edge cases & defensive behaviors (brief)

Invalid inputs (null pixels, out-of-bounds) are handled by early returns (often 0 or null) as the API specifies.
Obstacles (obsColor) are never enqueued or assigned distances in BFS algorithms.
When the starting pixel already has the target color in fill, the method returns immediately with 0 to avoid extra work.
If you want, I can now paste short, commented pseudocode into the source files (as Javadoc-style comments) or add small private helper methods (neighbors/wrapCoord) to Map.java to reduce duplication in the BFS loops. Which would you prefer?




Image of the run of the progrram :


<img width="737" height="748" alt="Screenshot 2025-12-27 231724" src="https://github.com/user-attachments/assets/c1d9deed-5513-4d4f-b065-2d72e9b2df61" />



