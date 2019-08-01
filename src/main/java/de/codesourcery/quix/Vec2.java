package de.codesourcery.quix;

public class Vec2
{
    public float x,y;

    public Vec2()
    {
    }

    public Vec2(Node node) {
        this(node.x,node.y);
    }

    public Vec2(Vec2 vec2)
    {
        this.x = vec2.x;
        this.y = vec2.y;
    }

    public Vec2 copy() {
        return new Vec2(this);
    }

    public void apply(Node n) {
        n.x = (int) x;
        n.y = (int) y;
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
        this.x -= b.x;
        this.y -= b.y;
        return this;
    }

    public Vec2 norm() {
        final float len2 = len2();
        if ( len2 == 0 ) {
            return new Vec2(x,y);
        }
        final float len = (float) Math.sqrt( len2 );
        this.x /= len;
        this.y /= len;
        return this;
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
        this.x *= factor;
        this.y *= factor;
        return this;
    }

    public Vec2 add(Vec2 other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vec2 set(int x, int y)
    {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec2 set(Vec2 other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }
}
