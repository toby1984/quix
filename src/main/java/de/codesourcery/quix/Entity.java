package de.codesourcery.quix;

public abstract class Entity
{
    private Line currentLine;

    // location on current line
    public int x;
    public int y;

    public Entity() {
    }

    public Entity(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public float dst(Entity other) {
        return (float) Math.sqrt( dst2(other) );
    }

    public float dst2(Entity other) {
        float dx = other.x - this.x;
        float dy = other.y - this.y;
        return dx*dx+dy*dy;
    }

    public void set(int x,int y) {
        this.x = x;
        this.y = y;
    }

    public Line getCurrentLine()
    {
        return currentLine;
    }

    public void setCurrentLine(Line currentLine)
    {
        System.out.println("SWITCHING CURRENT LINE: "+this.currentLine+" -> "+currentLine);
        if ( ! currentLine.contains( x,y ) ) {
            System.err.println("WARNING: Entity "+this+" not on current line "+currentLine);
        }
        this.currentLine = currentLine;
    }

    public void move(Direction dir) {
        this.x += dir.dx;
        this.y += dir.dy;
    }

    @Override
    public String toString()
    {
        return " @ ("+x+","+y+")";
    }
}