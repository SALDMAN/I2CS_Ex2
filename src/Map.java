import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * This class represents a 2D map (int[w][h]) as a "screen" or a raster matrix or maze over integers.
 * This is the main class needed to be implemented.
 *
 * @author Yair_Saldman
 *
 */
public class Map implements Map2D, Serializable{

    private int[][] map;
    private int width;
    private int height;

    /**
     * Constructs a w*h 2D raster map with an init value v.
     * @param w
     * @param h
     * @param v
     */
    public Map(int w, int h, int v) {init(w, h, v);}
    /**
     * Constructs a square map (size*size).
     * @param size
     */
    public Map(int size) {this(size,size, 0);}

    /**
     * Constructs a map from a given 2D array.
     * @param data
     */
    public Map(int[][] data) {
        init(data);
    }
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
    @Override
    public int[][] getMap() {
        int[][] ans = new int[this.width][this.height];
        for (int x = 0; x < this.width; x++) {
            System.arraycopy(this.map[x], 0, ans[x], 0, this.height);
        }
        return ans;
    }
    @Override
    public int getWidth() {
        int ans = this.width;

        return ans;
    }
    @Override
    public int getHeight() {
        int ans = this.height;

        return ans;
    }
    @Override
    public int getPixel(int x, int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            throw new RuntimeException("Pixel out of bounds");
        }
        return this.map[x][y];
    }
    @Override
    public int getPixel(Pixel2D p) {
        if (p == null) throw new RuntimeException("Null pixel");
        return getPixel(p.getX(), p.getY());
    }
    @Override
    public void setPixel(int x, int y, int v) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            throw new RuntimeException("Pixel out of bounds");
        }
        this.map[x][y] = v;

    }
    @Override
    public void setPixel(Pixel2D p, int v) {
        if (p == null) throw new RuntimeException("Null pixel");
        setPixel(p.getX(), p.getY(), v);

    }

    @Override
    public boolean isInside(Pixel2D p) {
        if (p == null) return false;
        return p.getX() >= 0 && p.getX() < this.width && p.getY() >= 0 && p.getY() < this.height;
    }

    @Override
    public boolean sameDimensions(Map2D p) {
        if (p == null) return false;
        return this.width == p.getWidth() && this.height == p.getHeight();
    }

    @Override
    public void addMap2D(Map2D p) {
        if (!sameDimensions(p)) return;
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.map[x][y] += p.getPixel(x, y);
            }
        }
    }

    @Override
    public void mul(double scalar) {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.map[x][y] = (int) Math.round(this.map[x][y] * scalar);
            }
        }

    }

    @Override
    public void rescale(double sx, double sy) {
        if (sx <= 0 || sy <= 0) throw new RuntimeException("Scale must be positive");
        int newW = Math.max(1, (int) Math.round(this.width * sx));
        int newH = Math.max(1, (int) Math.round(this.height * sy));
        int[][] dst = new int[newW][newH];
        for (int x = 0; x < newW; x++) {
            for (int y = 0; y < newH; y++) {
                // nearest neighbor sampling
                int srcX = Math.min(this.width - 1, Math.max(0, (int) Math.floor(x / sx)));
                int srcY = Math.min(this.height - 1, Math.max(0, (int) Math.floor(y / sy)));
                dst[x][y] = this.map[srcX][srcY];
            }
        }
        this.width = newW;
        this.height = newH;
        this.map = dst;
    }

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

    @Override
    public void drawLine(Pixel2D p1, Pixel2D p2, int color) {
        if (p1 == null || p2 == null) throw new RuntimeException("Null endpoint");
        int x1 = p1.getX(), y1 = p1.getY();
        int x2 = p2.getX(), y2 = p2.getY();
        // handle identical
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
    @Override
    /**
     * Fills this map with the new color (new_v) starting from p.
     * https://en.wikipedia.org/wiki/Flood_fill
     */
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
            // neighbors 4-way
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

    @Override
    /**
     * BFS like shortest the computation based on iterative raster implementation of BFS, see:
     * https://en.wikipedia.org/wiki/Breadth-first_search
     */
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
        // reconstruct path
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
    ////////////////////// Private Methods ///////////////////////

}
