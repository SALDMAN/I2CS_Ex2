import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.*;

class MapTest {
    private final int[][] _map_3_3 = {{0,1,0}, {1,0,1}, {0,1,0}};
    private Map2D _m0, _m1, _m3_3;

    @BeforeEach
    public void setUp() {
        _m0 = new Map(3);
        _m1 = new Map(3);
        _m3_3 = new Map(_map_3_3);
    }


    @Test
    public void testDimensions2() {
        assertEquals(3, _m0.getWidth());
        assertEquals(3, _m0.getHeight());
    }

    @Test
    public void testSetAndGetPixel2() {
        _m0.setPixel(1, 1, 7);
        assertEquals(7, _m0.getPixel(1, 1));

        Index2D idx = new Index2D(0, 0);
        _m0.setPixel(idx, 9);
        assertEquals(9, _m0.getPixel(idx));
    }

    @Test
    public void testEqualsAndSameSize2() {
        assertTrue(_m0.equals(_m1));
        assertTrue(_m0.sameDimensions(_m1));

        _m1.setPixel(0, 0, 1);
        assertFalse(_m0.equals(_m1));
    }

    @Test
    public void testIsInside2() {
        assertTrue(_m3_3.isInside(new Index2D(0,0)));
        assertTrue(_m3_3.isInside(new Index2D(2,2)));
        assertFalse(_m3_3.isInside(new Index2D(-1,0)));
        assertFalse(_m3_3.isInside(new Index2D(0,3)));
    }

    @Test
    public void testAddAndMultiply2() {
        Map2D extra = new Map(3);
        extra.setPixel(1, 1, 2);
        _m0.addMap2D(extra);

        assertEquals(2, _m0.getPixel(1,1));

        _m0.mul(2.0);
        assertEquals(4, _m0.getPixel(1,1));
    }

    @Test
    public void testFill2() {
        _m3_3.setPixel(1,1,5);
        int filled = _m3_3.fill(new Index2D(1,1), 9, false);
        assertEquals(1, filled);
        assertEquals(9, _m3_3.getPixel(1,1));
    }

    @Test
    public void testShortestPathSimple2() {
        Index2D start2 = new Index2D(0,0);
        Index2D end2 = new Index2D(2,2);
        Pixel2D[] path = _m3_3.shortestPath(start2, end2, -1, false); // use Pixel2D[]
        assertNotNull(path);
        assertEquals(start2, path[0]);
        assertEquals(end2, path[path.length-1]);
    }


    @Test
    public void testDrawLineAndRect2() {
        _m0.drawLine(new Index2D(0,0), new Index2D(2,0), 7);
        assertEquals(7, _m0.getPixel(0,0));
        assertEquals(7, _m0.getPixel(1,0));
        assertEquals(7, _m0.getPixel(2,0));

        _m0.drawRect(new Index2D(0,1), new Index2D(1,2), 3);
        assertEquals(3, _m0.getPixel(0,1));
        assertEquals(3, _m0.getPixel(1,2));
    }

    @Test
    public void testDrawCircle2() {
        _m0.drawCircle(new Index2D(1,1), 1.5, 9);
        assertEquals(9, _m0.getPixel(1,1));
        assertEquals(9, _m0.getPixel(0,1));
        assertEquals(9, _m0.getPixel(1,0));
    }
    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void initLargeArray() {
        int[][] bigarr = new int[500][500];
        _m1.init(bigarr);
        assertEquals(bigarr.length, _m1.getWidth());
        assertEquals(bigarr[0].length, _m1.getHeight());
        Pixel2D p1 = new Index2D(3,2);
        // should not throw
        _m1.fill(p1,1, true);
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void initCopyEquals() {
        _m0.init(_map_3_3);
        _m1.init(_map_3_3);
        assertEquals(_m0, _m1);
        int[][] original = _map_3_3;
        original[0][0] = 9; // modify original array
        // maps should remain equal because constructor/init used a deep copy
        assertEquals(_m0, _m1);
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void getSetPixel() {
        _m0.init(4, 4, 0);
        _m0.setPixel(2, 1, 7);
        assertEquals(7, _m0.getPixel(2,1));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void getPixelOutOfBounds() {
        assertThrows(RuntimeException.class, () -> _m3_3.getPixel(-1, 0));
        assertThrows(RuntimeException.class, () -> _m3_3.getPixel(0, -1));
        assertThrows(RuntimeException.class, () -> _m3_3.getPixel(_m3_3.getWidth(), 0));
        assertThrows(RuntimeException.class, () -> _m3_3.getPixel(0, _m3_3.getHeight()));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void isInside() {
        Pixel2D inside = new Index2D(1,1);
        Pixel2D outside = new Index2D(3,3);
        assertTrue(_m3_3.isInside(inside));
        assertFalse(_m3_3.isInside(outside));
        assertFalse(_m3_3.isInside(null));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void sameDimensions() {
        Map2D other = new Map(3,3,0);
        assertTrue(_m3_3.sameDimensions(other));
        Map2D different = new Map(4,3,0);
        assertFalse(_m3_3.sameDimensions(different));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void addMap() {
        _m0.init(2,2,1);
        Map2D other = new Map(2,2,2);
        _m0.addMap2D(other);
        assertEquals(3, _m0.getPixel(0,0));
        assertEquals(3, _m0.getPixel(1,1));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void mulRounds() {
        _m0.init(2,2,1);
        _m0.setPixel(0,0,3);
        _m0.mul(1.5);
        // 3 * 1.5 = 4.5 -> rounded to 5
        assertEquals(5, _m0.getPixel(0,0));
        // 1 * 1.5 = 1.5 -> rounded to 2
        assertEquals(2, _m0.getPixel(1,1));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void rescaleNearest() {
        Map2D src = new Map(new int[][]{{1,2},{3,4}}); // width=2,height=2
        src.rescale(2.0, 2.0); // expected width=4,height=4
        assertEquals(4, src.getWidth());
        assertEquals(4, src.getHeight());
        // corners should map from original corners (nearest neighbor)
        assertEquals(1, src.getPixel(0,0));
        assertEquals(2, src.getPixel(0,3));
        assertEquals(3, src.getPixel(3,0));
        assertEquals(4, src.getPixel(3,3));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void drawRectBox() {
        _m0.init(5,5,0);
        Pixel2D p1 = new Index2D(1,1);
        Pixel2D p2 = new Index2D(3,3);
        _m0.drawRect(p1,p2,9);
        for (int x=1;x<=3;x++) for (int y=1;y<=3;y++) assertEquals(9, _m0.getPixel(x,y));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void drawLineWorks() {
        _m0.init(5,5,0);
        Pixel2D p = new Index2D(2,2);
        _m0.drawLine(p,p,7);
        assertEquals(7, _m0.getPixel(2,2));
        Pixel2D p1 = new Index2D(0,0);
        Pixel2D p2 = new Index2D(4,0);
        _m0.drawLine(p1,p2,3);
        for (int x=0;x<=4;x++) assertEquals(3, _m0.getPixel(x,0));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void drawCircleWorks() {
        _m0.init(5,5,0);
        Pixel2D c = new Index2D(2,2);
        _m0.drawCircle(c, 1.0, 8);
        // center must be drawn
        assertEquals(8, _m0.getPixel(2,2));
        // at radius 1 some neighbors should be drawn
        assertEquals(8, _m0.getPixel(2,3));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void fillWorks() {
        Map2D m = new Map(3,3,0);
        m.setPixel(0,0,1); // one obstacle
        int filled = m.fill(new Index2D(1,1), 7, false);
        // all other 8 cells should be filled
        assertEquals(8, filled);
        assertEquals(7, m.getPixel(1,1));
        // filling a pixel that already has the color returns 0
        assertEquals(0, m.fill(new Index2D(1,1), 7, false));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void shortestPathWorks() {
        Map2D m = new Map(5,1,0);
        Pixel2D a = new Index2D(0,0);
        Pixel2D b = new Index2D(4,0);
        Pixel2D[] path = m.shortestPath(a,b,1,false);
        assertNotNull(path);
        assertEquals(5, path.length);
        assertEquals(a, path[0]);
        assertEquals(b, path[path.length-1]);

        // block the middle
        m.setPixel(2,0,1);
        assertNull(m.shortestPath(a,b,1,false));
    }

    @Test
    @Timeout(value = 300, unit = MILLISECONDS)
    void allDistanceWorks() {
        Map2D m = new Map(5,1,0);
        Pixel2D start = new Index2D(0,0);
        Map2D dist = m.allDistance(start, 1, false);
        assertEquals(5, dist.getWidth());
        assertEquals(1, dist.getHeight());
        assertEquals(0, dist.getPixel(0,0));
        assertEquals(4, dist.getPixel(4,0));
        // make cell unreachable
        m.setPixel(2,0,1);
        dist = m.allDistance(start, 1, false);
        assertEquals(-1, dist.getPixel(4,0));
    }
}