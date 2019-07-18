package de.codesourcery.quix;

public interface ICollisionCheck
{
    boolean isBorder(Line tmp);

    Line getLine(int x, int y);

    Line intersects(Line line);

    Line intersects(int x0,int y0,int x1,int y1);

    Node split(Line line, int xSplit, int ySplit);
}
