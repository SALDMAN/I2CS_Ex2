import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * This class represents a 2D map (int[w][h]) as a "screen" or a raster matrix or maze over integers.
 * This is the main class needed to be implemented.
 *
 * It implements the Map2D interface and provides common raster operations such as:
 * - initialization and defensive copying of an integer matrix
 * - pixel accessors and mutators
 * - simple arithmetic operations (add, multiply)
 * - geometric drawing helpers (circle, line, rectangle)
 * - image rescaling (nearest-neighbor)
 * - BFS-based algorithms: flood-fill, shortest path, and distance map (allDistance)
 *
 * Design notes:
 * - The internal representation is an int[][] array with dimensions [width][height].
 * - Public operations validate inputs and throw RuntimeException for invalid arguments (consistent with the provided tests).
 * - BFS implementations use a boolean visited matrix and an ArrayDeque as the queue.
 *
 * Usage example (high level):
 * Map m = new Map(10, 10, 0);
 * m.drawRect(new Pixel2D(1,1), new Pixel2D(3,3), 5);
 * Pixel2D[] path = m.shortestPath(new Pixel2D(0,0), new Pixel2D(9,9), -1, false);
 */
public class Map implements Map2D, Serializable{

    private int[][] map;
    private int width;
    private int height;

    /**
     * Constructs a w*h 2D raster map with an initial value v for every pixel.
     * @param w the width (number of columns) of the map; must be > 0
     * @param h the height (number of rows) of the map; must be > 0
     * @param v the initial integer value to fill every pixel with
     * @throws RuntimeException if width or height are not positive
     */
    public Map(int w, int h, int v) {init(w, h, v);}

    /**
     * Constructs a square map of size*size with an initial value of 0.
     * @param size the side length (both width and height); must be > 0
     */
    public Map(int size) {this(size,size, 0);}

    /**
     * Constructs a map from a given 2D integer array. The input array is defensively copied.
     * The outer dimension of the input is interpreted as width and the inner dimension as height
     * (i.e., data.length == width and data[0].length == height). All rows must have the same length.
     * @param data a non-null, non-empty, rectangular 2D array to copy from
     * @throws RuntimeException if the input is null, empty, ragged, or has zero height
     */
    public Map(int[][] data) {
        init(data);
    }

    /**
     * Initialize this Map to the given width and height and fill every pixel with value v.
     * This method replaces the internal buffer with a newly allocated array.
     * @param w width (number of columns), must be > 0
     * @param h height (number of rows), must be > 0
     * @param v initial value to fill
     * @throws RuntimeException for invalid dimensions
     */
    @Override
    public void init(int w, int h, int v) {
        if (w <= 0 || h <= 0) {
            throw new RuntimeException("Invalid dimensions");
        }
        this.width = w;
        this.height = h;
        this.map = new int[w][h];
        for (int x = 0; x < w; x++) {
            Arrays.fill(this.map[x], v);
        }
    }

    /**
     * Initialize this Map from a rectangular 2D array by performing a defensive copy.
     * The provided array is interpreted as int[width][height]. All rows must be the same length.
     * @param arr a rectangular 2D int array (outer length = width, inner length = height)
     * @throws RuntimeException if arr is null, empty, ragged, or has zero height
     */
    @Override
    public void init(int[][] arr) {
        if (arr == null || arr.length == 0) {
            throw new RuntimeException("Array is null or empty");
        }
        int w = arr.length;
        int h = arr[0].length;
        if (h == 0) throw new RuntimeException("Array has zero height");
        for (int i = 0; i < w; i++) {
            if (arr[i] == null || arr[i].length != h) {
                throw new RuntimeException("Ragged array");
            }
        }
        this.width = w;
        this.height = h;
        this.map = new int[w][h];
        for (int x = 0; x < w; x++) {
            System.arraycopy(arr[x], 0, this.map[x], 0, h);
        }
    }

    /**
     * Returns a defensive copy of the internal int[][] map array.
     * The returned array has dimensions [width][height], and callers may modify it
     * without affecting this Map instance.
     * @return a newly allocated 2D int array containing the same pixel values
     */
    @Override
    public int[][] getMap() {
        int[][] ans = new int[this.width][this.height];
        for (int x = 0; x < this.width; x++) {
            System.arraycopy(this.map[x], 0, ans[x], 0, this.height);
        }
        return ans;
    }

    /**
     * Get the map width (number of columns).
     * @return width the current width
     */
    @Override
    public int getWidth() {
        int ans = this.width;

        return ans;
    }

    /**
     * Get the map height (number of rows).
     * @return height the current height
     */
    @Override
    public int getHeight() {
        int ans = this.height;

        return ans;
    }

    /**
     * Read a single pixel value at coordinates (x,y).
     * Coordinates follow the convention: 0 <= x < width and 0 <= y < height.
     * @param x x-coordinate (column)
     * @param y y-coordinate (row)
     * @return the integer value stored at (x,y)
     * @throws RuntimeException if the coordinates are outside the map bounds
     */
    @Override
    public int getPixel(int x, int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            throw new RuntimeException("Pixel out of bounds");
        }
        return this.map[x][y];
    }

    /**
     * Read a single pixel using a Pixel2D object.
     * Validates that the pixel object is non-null and delegates to getPixel(x,y).
     * @param p the Pixel2D coordinate to read
     * @return the integer value at the pixel
     * @throws RuntimeException if p is null or its coordinates are outside the map
     */
    @Override
    public int getPixel(Pixel2D p) {
        if (p == null) throw new RuntimeException("Null pixel");
        return getPixel(p.getX(), p.getY());
    }

    /**
     * Set the value of a pixel at (x,y) to v.
     * @param x column index
     * @param y row index
     * @param v new integer value for the pixel
     * @throws RuntimeException if coordinates are outside the map bounds
     */
    @Override
    public void setPixel(int x, int y, int v) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            throw new RuntimeException("Pixel out of bounds");
        }
        this.map[x][y] = v;

    }

    /**
     * Set a pixel's value using a Pixel2D object.
     * @param p Pixel2D coordinate to set
     * @param v new integer value
     * @throws RuntimeException if p is null or out of bounds
     */
    @Override
    public void setPixel(Pixel2D p, int v) {
        if (p == null) throw new RuntimeException("Null pixel");
        setPixel(p.getX(), p.getY(), v);

    }

    /**
     * Check whether the provided Pixel2D is inside the map bounds.
     * This method returns false for a null argument.
     * @param p the Pixel2D to test
     * @return true if inside the map, false otherwise
     */
    @Override
    public boolean isInside(Pixel2D p) {
        if (p == null) return false;
        return p.getX() >= 0 && p.getX() < this.width && p.getY() >= 0 && p.getY() < this.height;
    }

    /**
     * Check whether another Map2D has the same dimensions as this one.
     * @param p the other Map2D to compare to
     * @return true if both maps are non-null and have equal width and height
     */
    @Override
    public boolean sameDimensions(Map2D p) {
        if (p == null) return false;
        return this.width == p.getWidth() && this.height == p.getHeight();
    }

    /**
     * Add another Map2D to this one element-wise. The operation is performed in-place.
     * If the dimensions do not match, the method returns without modifying this map.
     * @param p a Map2D to add element-wise
     */
    @Override
    public void addMap2D(Map2D p) {
        if (!sameDimensions(p)) return;
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.map[x][y] += p.getPixel(x, y);
            }
        }
    }

    /**
     * Multiply every pixel by the given scalar value. The result is rounded to the nearest integer.
     * This operation is performed in-place.
     * @param scalar the multiplication factor (double); pixels are cast back to int using Math.round
     */
    @Override
    public void mul(double scalar) {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.map[x][y] = (int) Math.round(this.map[x][y] * scalar);
            }
        }

    }

    /**
     * Rescale the map using nearest-neighbor sampling. The method computes new integer dimensions
     * using Math.round on the provided scale factors and re-samples the image into a new internal buffer.
     * @param sx scale factor in X (width) dimension; must be > 0
     * @param sy scale factor in Y (height) dimension; must be > 0
     * @throws RuntimeException if either scale factor is not positive
     */
    @Override
    public void rescale(double sx, double sy) {
        if (sx <= 0 || sy <= 0) throw new RuntimeException("Scale must be positive");
        int newW = Math.max(1, (int) Math.round(this.width * sx));
        int newH = Math.max(1, (int) Math.round(this.height * sy));
        int[][] dst = new int[newW][newH];
        for (int x = 0; x < newW; x++) {
            for (int y = 0; y < newH; y++) {
                /** nearest neighbor sampling */
                int srcX = Math.min(this.width - 1, Math.max(0, (int) Math.floor(x / sx)));
                int srcY = Math.min(this.height - 1, Math.max(0, (int) Math.floor(y / sy)));
                dst[x][y] = this.map[srcX][srcY];
            }
        }
        this.width = newW;
        this.height = newH;
        this.map = dst;
    }

    /**
     * Draw a filled circle on the map using the given integer color.
     * The circle includes all pixels whose Euclidean distance from the center is <= rad.
     * Points outside the map bounds are ignored.
     * @param center center coordinates of the circle
     * @param rad radius (in the same coordinate units as Pixel2D.distance2D)
     * @param color integer color (value) to set for covered pixels
     * @throws RuntimeException if center is null
     */
    @Override
    public void drawCircle(Pixel2D center, double rad, int color) {
        if (center == null) throw new RuntimeException("Center null");
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                Index2D p = new Index2D(x, y);
                if (center.distance2D(p) <= rad) {
                    this.map[x][y] = color;
                }
            }
        }
    }

    /**
     * Draw a straight line between two pixel endpoints using simple interpolation and rounding.
     * The implementation iterates along the dominant axis (x or y) and rounds the orthogonal coordinate
     * to the nearest integer to produce a continuous line. Pixels outside the map bounds are ignored.
     * @param p1 first endpoint (non-null)
     * @param p2 second endpoint (non-null)
     * @param color integer color to set along the line
     * @throws RuntimeException if either endpoint is null
     */
    @Override
    public void drawLine(Pixel2D p1, Pixel2D p2, int color) {
        if (p1 == null || p2 == null) throw new RuntimeException("Null endpoint");
        int x1 = p1.getX(), y1 = p1.getY();
        int x2 = p2.getX(), y2 = p2.getY();
        /** handle identical endpoints */
        if (x1 == x2 && y1 == y2) {
            if (isInside(p1)) setPixel(p1, color);
            return;
        }
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        if (dx >= dy) {
            // iterate over x
            int sx = x1 < x2 ? 1 : -1;
            for (int xi = x1; xi != x2 + sx; xi += sx) {
                double t = (x2 == x1) ? 0 : (double) (xi - x1) / (double) (x2 - x1);
                double yf = y1 + t * (y2 - y1);
                int yi = (int) Math.round(yf);
                if (xi >= 0 && xi < this.width && yi >= 0 && yi < this.height) this.map[xi][yi] = color;
            }
        } else {
            int sy = y1 < y2 ? 1 : -1;
            for (int yi = y1; yi != y2 + sy; yi += sy) {
                double t = (y2 == y1) ? 0 : (double) (yi - y1) / (double) (y2 - y1);
                double xf = x1 + t * (x2 - x1);
                int xi = (int) Math.round(xf);
                if (xi >= 0 && xi < this.width && yi >= 0 && yi < this.height) this.map[xi][yi] = color;
            }
        }
    }

    /**
     * Draw a filled axis-aligned rectangle between p1 and p2 (inclusive). Coordinates outside the map
     * are clipped and ignored.
     * @param p1 first corner
     * @param p2 opposite corner
     * @param color integer color to fill the rectangle
     * @throws RuntimeException if either corner is null
     */
    @Override
    public void drawRect(Pixel2D p1, Pixel2D p2, int color) {
        if (p1 == null || p2 == null) throw new RuntimeException("Null endpoint");
        int x1 = Math.min(p1.getX(), p2.getX());
        int x2 = Math.max(p1.getX(), p2.getX());
        int y1 = Math.min(p1.getY(), p2.getY());
        int y2 = Math.max(p1.getY(), p2.getY());
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                if (x >= 0 && x < this.width && y >= 0 && y < this.height) this.map[x][y] = color;
            }
        }
    }

    /**
     * Compare this Map with another object for pixel-wise equality. The other object must implement Map2D
     * and have the same dimensions. Equality is defined by identical integer values at every coordinate.
     * @param ob any object (typically another Map2D)
     * @return true if ob is a Map2D with same dimensions and identical pixels; false otherwise
     */
    @Override
    public boolean equals(Object ob) {
        if (ob == null) return false;
        if (!(ob instanceof Map2D)) return false;
        Map2D other = (Map2D) ob;
        if (!sameDimensions(other)) return false;
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                if (this.map[x][y] != other.getPixel(x, y)) return false;
            }
        }
        return true;
    }

    /**
     * Fill (flood-fill) algorithm starting from pixel xy and replacing all connected pixels
     * that have the same value as the starting pixel with new_v. The connectivity is 4-way (N,E,S,W).
     * Optionally performs cyclic wrapping along edges if cyclic is true (top connects to bottom, left to right).
     *
     * Returns the number of pixels changed.
     *
     * @param xy starting Pixel2D to begin the fill; if null or outside the map nothing is changed and 0 is returned
     * @param new_v new integer value to paint the connected region
     * @param cyclic when true the search wraps around edges (toroidal topology)
     * @return the number of pixels that were changed from the original value to new_v
     */
    @Override
    public int fill(Pixel2D xy, int new_v,  boolean cyclic) {
        if (xy == null || !isInside(xy)) return 0;
        int sx = xy.getX();
        int sy = xy.getY();
        int orig = getPixel(sx, sy);
        if (orig == new_v) return 0;
        boolean[][] vis = new boolean[this.width][this.height];
        Deque<Index2D> q = new ArrayDeque<>();
        q.add(new Index2D(sx, sy));
        vis[sx][sy] = true;
        int filled = 0;
        while (!q.isEmpty()) {
            Index2D cur = q.removeFirst();
            int cx = cur.getX(), cy = cur.getY();
            if (getPixel(cx, cy) == orig) {
                setPixel(cx, cy, new_v);
                filled++;
            }
            /** neighbors 4-way */
            int[][] nbrs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : nbrs) {
                int nx = cx + d[0];
                int ny = cy + d[1];
                if (cyclic) {
                    if (nx < 0) nx = this.width - 1;
                    if (nx >= this.width) nx = 0;
                    if (ny < 0) ny = this.height - 1;
                    if (ny >= this.height) ny = 0;
                }
                if (nx < 0 || nx >= this.width || ny < 0 || ny >= this.height) continue;
                if (vis[nx][ny]) continue;
                if (getPixel(nx, ny) != orig) continue;
                vis[nx][ny] = true;
                q.addLast(new Index2D(nx, ny));
            }
        }
        return filled;
    }

    /**
     * Compute the shortest path between p1 and p2 using BFS on the raster grid where
     * pixels equal to obsColor are treated as obstacles (untraversable).
     * Connectivity is 4-way. If cyclic is true, coordinates wrap around the map edges.
     *
     * Returns an array of Pixel2D objects that represent the path from p1 to p2 inclusive,
     * or null if no path exists or if input validation fails.
     *
     * @param p1 starting coordinate (non-null and must be inside the map)
     * @param p2 target coordinate (non-null and must be inside the map)
     * @param obsColor color value considered as obstacle (cells with this value are blocked)
     * @param cyclic whether the BFS should wrap around the borders
     * @return Pixel2D[] ordered from start to goal if a path exists; null otherwise
     */
    @Override
    public Pixel2D[] shortestPath(Pixel2D p1, Pixel2D p2, int obsColor, boolean cyclic) {
        if (p1 == null || p2 == null) return null;
        if (!isInside(p1) || !isInside(p2)) return null;
        if (getPixel(p1) == obsColor || getPixel(p2) == obsColor) return null;
        boolean[][] vis = new boolean[this.width][this.height];
        Index2D[][] parent = new Index2D[this.width][this.height];
        Deque<Index2D> q = new ArrayDeque<>();
        Index2D start = new Index2D(p1);
        Index2D goal = new Index2D(p2);
        q.add(start);
        vis[start.getX()][start.getY()] = true;
        boolean found = false;
        int[][] nbrs = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!q.isEmpty()) {
            Index2D cur = q.removeFirst();
            if (cur.equals(goal)) { found = true; break; }
            for (int[] d : nbrs) {
                int nx = cur.getX() + d[0];
                int ny = cur.getY() + d[1];
                if (cyclic) {
                    if (nx < 0) nx = this.width - 1;
                    if (nx >= this.width) nx = 0;
                    if (ny < 0) ny = this.height - 1;
                    if (ny >= this.height) ny = 0;
                }
                if (nx < 0 || nx >= this.width || ny < 0 || ny >= this.height) continue;
                if (vis[nx][ny]) continue;
                if (getPixel(nx, ny) == obsColor) continue;
                vis[nx][ny] = true;
                parent[nx][ny] = cur;
                Index2D next = new Index2D(nx, ny);
                q.addLast(next);
            }
        }
        if (!found) return null;
        /** reconstruct path */
        List<Pixel2D> path = new ArrayList<>();
        Index2D cur = new Index2D(p2);
        while (cur != null && !cur.equals(start)) {
            path.add(cur);
            cur = parent[cur.getX()][cur.getY()];
        }
        path.add(start);
        // reverse
        Pixel2D[] ans = new Pixel2D[path.size()];
        for (int i = 0; i < path.size(); i++) ans[i] = path.get(path.size() - 1 - i);
        return ans;
    }

    /**
     * Compute the distance (in number of steps) from start to every reachable pixel using BFS.
     * Cells equal to obsColor are treated as obstacles and left as -1 in the returned Map2D.
     * The returned Map2D contains integers representing the shortest distance in steps from start
     * to each cell, or -1 for unreachable or obstacle cells. The BFS uses 4-way connectivity and
     * optionally wraps coordinates when cyclic is true.
     *
     * @param start the starting Pixel2D (if null or outside the map, returns a map filled with -1)
     * @param obsColor integer color representing obstacles
     * @param cyclic whether to treat the domain as toroidal (wrap-around)
     * @return a Map2D where each pixel contains the shortest distance from start or -1
     */
    @Override
    public Map2D allDistance(Pixel2D start, int obsColor, boolean cyclic) {
        Map res = new Map(this.width, this.height, -1);
        if (start == null || !isInside(start)) return res;
        boolean[][] vis = new boolean[this.width][this.height];
        Deque<Index2D> q = new ArrayDeque<>();
        q.add(new Index2D(start));
        vis[start.getX()][start.getY()] = true;
        res.setPixel(start, 0);
        int[][] nbrs = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!q.isEmpty()) {
            Index2D cur = q.removeFirst();
            int curd = res.getPixel(cur);
            for (int[] d : nbrs) {
                int nx = cur.getX() + d[0];
                int ny = cur.getY() + d[1];
                if (cyclic) {
                    if (nx < 0) nx = this.width - 1;
                    if (nx >= this.width) nx = 0;
                    if (ny < 0) ny = this.height - 1;
                    if (ny >= this.height) ny = 0;
                }
                if (nx < 0 || nx >= this.width || ny < 0 || ny >= this.height) continue;
                if (vis[nx][ny]) continue;
                if (getPixel(nx, ny) == obsColor) continue;
                vis[nx][ny] = true;
                res.setPixel(nx, ny, curd + 1);
                q.addLast(new Index2D(nx, ny));
            }
        }
        return res;
    }
}
