package de.codesourcery.quix;

public class LinAlg
{

    // https://stackoverflow.com/questions/27161533/find-the-shortest-distance-between-a-point-and-line-segments-not-line
    
    public static int dot(int x0, int y0, int x1, int y1)
    {
       return x0*x1+y0*y1;
    }

    public static float length(float x0,float y0) {
        return (float) Math.sqrt( x0*x0 + y0*y0 ),
    }

    public static final class Point {
        public float x,y;

        public Point(float x, float y)
        {
            this.x = x;
            this.y = y;
        }
    }

    public static Point vector(float x0,float y0,float x1,float y1) {
        return new Point(x1-x0,y1-y0);
    }

    public static Point unit(int x0,int y0) {
        float len = length( x0,y0 );
        return new Point( x0 / len, y0 / len );
    }

    public static Point distance(Point p0, Point p1) {
        return length(vector( p0.x, p0.y, p1.x,p1.y ) );
    }
    /*

import math

def distance(p0,p1):
    return length(vector(p0,p1))

def scale(v,sc):
    x,y,z = v
    return (x * sc, y * sc, z * sc)

def add(v,w):
    x,y,z = v
    X,Y,Z = w
    return (x+X, y+Y, z+Z)


# Given a line with coordinates 'start' and 'end' and the
# coordinates of a point 'pnt' the proc returns the shortest
# distance from pnt to the line and the coordinates of the
# nearest point on the line.
#
# 1  Convert the line segment to a vector ('line_vec').
# 2  Create a vector connecting start to pnt ('pnt_vec').
# 3  Find the length of the line vector ('line_len').
# 4  Convert line_vec to a unit vector ('line_unitvec').
# 5  Scale pnt_vec by line_len ('pnt_vec_scaled').
# 6  Get the dot product of line_unitvec and pnt_vec_scaled ('t').
# 7  Ensure t is in the range 0 to 1.
# 8  Use t to get the nearest location on the line to the end
#    of vector pnt_vec_scaled ('nearest').
# 9  Calculate the distance from nearest to pnt_vec_scaled.
# 10 Translate nearest back to the start/end line.
# Malcolm Kesson 16 Dec 2012

def pnt2line(pnt, start, end):
    line_vec = vector(start, end)
    pnt_vec = vector(start, pnt)
    line_len = length(line_vec)
    line_unitvec = unit(line_vec)
    pnt_vec_scaled = scale(pnt_vec, 1.0/line_len)
    t = dot(line_unitvec, pnt_vec_scaled)
    if t < 0.0:
        t = 0.0
    elif t > 1.0:
        t = 1.0
    nearest = scale(line_vec, t)
    dist = distance(nearest, pnt_vec)
    nearest = add(nearest, start)
    return (dist, nearest)
     */
}
