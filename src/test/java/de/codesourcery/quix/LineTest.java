package de.codesourcery.quix;

import org.junit.Test;

import static org.junit.Assert.*;

public class LineTest
{

    @Test
    public void intersects1()
    {
        // Line (87,79) -> (240,79), m = 0.0, vertical=false, horizontal=true <=> Line (120,79) -> (208,79), m = 0.0, vertical=false, horizontal=true
        Line a = new Line(87,79,240,79);
        Line b = new Line(120,79,208,79);
        assertTrue( a.intersects( b ) );
    }

    @Test
    public void intersects2()
    {
        Line a = new Line(87,79,240,79);
        Line b = new Line(87,79,240,79);
        assertTrue( a.intersects( b ) );
    }

    @Test
    public void intersects3()
    {
        // horizontal
        Line a = new Line(50,50,100,50);
        Line b = new Line(50,50,100,50);
        assertTrue( a.intersects( b ) );
    }

    @Test
    public void intersects4()
    {
        // vertical
        Line a = new Line(50,50,50,100);
        Line b = new Line(50,50,50,100);
        assertTrue( a.intersects( b ) );
    }
}