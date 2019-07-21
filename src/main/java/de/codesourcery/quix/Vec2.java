package de.codesourcery.quix;

public class Vec2
{
    public float x,y;

    public Vec2()
    {
    }

    public Vec2(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public float dot(Vec2 b)
    {
        return this.x*b.x+this.y*b.y;
    }

    public float len() {
        return (float) Math.sqrt( len2() );
    }

    public float len2() {
        return this.x*this.x + this.y*this.y;
    }

    public Vec2 subtract(Vec2 b) {
        return new Vec2(this.x - b.x, this.y -b.y );
    }

    public Vec2 norm() {
        final float len2 = len2();
        if ( len2 == 0 ) {
            return new Vec2(x,y);
        }
        final float len = (float) Math.sqrt( len2 );
        return new Vec2( x / len, y / len );
    }

    public float dist2(Vec2 p1)
    {
        float dx = p1.x - this.x;
        float dy = p1.y - this.y;
        return dx * dx + dy * dy;
    }

    public float dist(Vec2 p1)
    {
        return (float) Math.sqrt( dist2( p1 ) );
    }

    public Vec2 scl(float factor) {
        return new Vec2(x*factor, y*factor );
    }

    public Vec2 add(Vec2 other) {
        return new Vec2(this.x +other.x, this.y + other.y );
    }

    public void set(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
}
