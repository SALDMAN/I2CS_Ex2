/**
 *
 * @author Yair
 */

public class Index2D implements Pixel2D {
    private int x;
    private int y;
    public Index2D(int w, int h) {
        x=w;
        y=h;
    }
    public Index2D(Pixel2D other)
    {
     x=other.getX();
     y=other.getY();
    }
    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }
    @Override
    public double distance2D(Pixel2D p2) {
        if(p2==null){
            return 0;
        }
        return Math.sqrt(Math.pow(x-p2.getX(),2)+Math.pow(y-p2.getY(),2));
    }

    @Override
    public String toString() {
        String ans = null;
        return ans;
    }

    @Override
    public boolean equals(Object p) {
        if(p==null){
            return false;
        }
        return p instanceof Index2D && x==((Index2D) p).getX() && y==((Index2D) p).getY();
    }
}
