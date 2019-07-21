package de.codesourcery.quix;

public class Entity
{
    private Line currentLine;

    // location on current line
    public int x;
    public int y;

    public Line getCurrentLine()
    {
        return currentLine;
    }

    public void setCurrentLine(Line currentLine)
    {
        System.out.println("SWITCHING CURRENT LINE: "+this.currentLine+" -> "+currentLine);
        if ( ! currentLine.contains( x,y ) ) {
            System.err.println("WARNING: Entity ("+x+","+y+") not on current line "+currentLine);
        }
        this.currentLine = currentLine;
    }
}
