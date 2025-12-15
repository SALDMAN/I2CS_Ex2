import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Intex2DTest {
    @Test
    void testEquals1() {
        Index2D i1= new Index2D(0,0);
        Index2D i2= new Index2D(0,3);
        assertNotEquals(i1,i2);
    }
    @Test
    void testEquals2() {
        Index2D i1= new Index2D(0,0);
        Object p = new Object();
        assertNotEquals(i1,p);
    }
    @Test
    void testEquals3() {
        Index2D i1= new Index2D(2,8);
        Index2D i2= new Index2D(2,8);
        assertEquals(i1,i2);

    }
    @Test
    void testDistance() {
        Index2D i1= new Index2D(4,0);
        Index2D i2= new Index2D(0,0);
        if(i1.distance2D(i2)!=4){
            fail();
        }
    }
}
