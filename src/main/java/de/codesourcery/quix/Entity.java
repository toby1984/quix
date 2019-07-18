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
        System.out.println("CURRENT LINE: "+currentLine);
        this.currentLine = currentLine;
    }
}
