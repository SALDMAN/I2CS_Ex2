import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class MapTest {
    private int[][] _map_3_3 = {{0,1,0}, {1,0,1}, {0,1,0}};
    private Map2D _m0, _m1, _m3_3;

    @BeforeEach
    public void setUp() {
        _m0 = new Map(3);
        _m1 = new Map(3);
        _m3_3 = new Map(_map_3_3);
    }

    @Test
    @Timeout(value = 1, unit = SECONDS)
    void initWithLargeArraySetsDimensionsAndAllowsFill() {
        int[][] bigarr = new int[500][500];
        _m1.init(bigarr);
        assertEquals(bigarr.length, _m1.getWidth());
        assertEquals(bigarr[0].length, _m1.getHeight());
        Pixel2D p1 = new Index2D(3,2);
        // should not throw
        _m1.fill(p1,1, true);
    }

    @Test
    void initFromArrayCreatesIndependentCopyAndEqualsWorks() {
        _m0.init(_map_3_3);
        _m1.init(_map_3_3);
        assertEquals(_m0, _m1);
        int[][] original = _map_3_3;
        original[0][0] = 9; // modify original array
        // maps should remain equal because constructor/init used a deep copy
        assertEquals(_m0, _m1);
    }

    @Test
    void getAndSetPixelInsideBoundsWorks() {
        _m0.init(4, 4, 0);
        _m0.setPixel(2, 1, 7);
        assertEquals(7, _m0.getPixel(2,1));
    }

    @Test
    void getPixelOutOfBoundsThrowsRuntimeException() {
        assertThrows(RuntimeException.class, () -> _m3_3.getPixel(-1, 0));
        assertThrows(RuntimeException.class, () -> _m3_3.getPixel(0, -1));
        assertThrows(RuntimeException.class, () -> _m3_3.getPixel(_m3_3.getWidth(), 0));
        assertThrows(RuntimeException.class, () -> _m3_3.getPixel(0, _m3_3.getHeight()));
    }

    @Test
    void isInsideReturnsTrueForValidCoordinatesAndFalseOtherwise() {
        Pixel2D inside = new Index2D(1,1);
        Pixel2D outside = new Index2D(3,3);
        assertTrue(_m3_3.isInside(inside));
        assertFalse(_m3_3.isInside(outside));
        assertFalse(_m3_3.isInside(null));
    }

    @Test
    void sameDimensionsDetectsEqualAndDifferentMaps() {
        Map2D other = new Map(3,3,0);
        assertTrue(_m3_3.sameDimensions(other));
        Map2D different = new Map(4,3,0);
        assertFalse(_m3_3.sameDimensions(different));
    }

    @Test
    void addMap2DAddsValuesWhenDimensionsMatch() {
        _m0.init(2,2,1);
        Map2D other = new Map(2,2,2);
        _m0.addMap2D(other);
        assertEquals(3, _m0.getPixel(0,0));
        assertEquals(3, _m0.getPixel(1,1));
    }

    @Test
    void mulScalesAllPixelsAndRounds() {
        _m0.init(2,2,1);
        _m0.setPixel(0,0,3);
        _m0.mul(1.5);
        // 3 * 1.5 = 4.5 -> rounded to 5
        assertEquals(5, _m0.getPixel(0,0));
        // 1 * 1.5 = 1.5 -> rounded to 2
        assertEquals(2, _m0.getPixel(1,1));
    }

    @Test
    void rescaleEnlargesUsingNearestNeighbor() {
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
    void drawRectSetsAllPixelsInBoundingBox() {
        _m0.init(5,5,0);
        Pixel2D p1 = new Index2D(1,1);
        Pixel2D p2 = new Index2D(3,3);
        _m0.drawRect(p1,p2,9);
        for (int x=1;x<=3;x++) for (int y=1;y<=3;y++) assertEquals(9, _m0.getPixel(x,y));
    }

    @Test
    void drawLineSinglePixelAndStraightLinesWork() {
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
    void drawCircleFillsNeighborhoodAroundCenter() {
        _m0.init(5,5,0);
        Pixel2D c = new Index2D(2,2);
        _m0.drawCircle(c, 1.0, 8);
        // center must be drawn
        assertEquals(8, _m0.getPixel(2,2));
        // at radius 1 some neighbors should be drawn
        assertEquals(8, _m0.getPixel(2,3));
    }

    @Test
    void fillReplacesConnectedComponentAndReturnsCount() {
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
    void shortestPathFindsPathOrReturnsNullWhenBlocked() {
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
    void allDistanceComputesCorrectDistancesAndMarksUnreachable() {
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