package de.codesourcery.quix;

public class LinAlg
{
    /*
    *
    * https://stackoverflow.com/questions/27161533/find-the-shortest-distance-between-a-point-and-line-segments-not-line
    * Given a line with coordinates 'start' and 'end' and the
    * coordinates of a point 'pnt' the proc returns the shortest
    * distance from pnt to the line and the coordinates of the
    * nearest point on the line.
    *
    * 1  Convert the line segment to a vector ('line_vec').
    * 2  Create a vector connecting start to pnt ('pnt_vec').
    * 3  Find the length of the line vector ('line_len').
    * 4  Convert line_vec to a unit vector ('line_unitvec').
    * 5  Scale pnt_vec by line_len ('pnt_vec_scaled').
    * 6  Get the dot product of line_unitvec and pnt_vec_scaled ('t').
    * 7  Ensure t is in the range 0 to 1.
    * 8  Use t to get the nearest location on the line to the end
    *    of vector pnt_vec_scaled ('nearest').
    * 9  Calculate the distance from nearest to pnt_vec_scaled.
    * 10 Translate nearest back to the start/end line.
    * Malcolm Kesson 16 Dec 2012
    */
    public static float distToLineSegment(Line line, Vec2 pnt) {
        return (float) Math.sqrt( dist2ToLineSegment( line, pnt ) );
    }

    public static float dist2ToLineSegment(Line line, Vec2 pnt)
    {
        final Vec2 start= new Vec2(line.x0,line.y0);
        final Vec2 end = new Vec2(line.x1,line.y1);
        final Vec2 line_vec = end.subtract( start );
        final Vec2 pnt_vec = pnt.subtract( start );
        final float line_len = line_vec.len();
        final Vec2 line_unitvec = line_vec.norm();
        final Vec2 pnt_vec_scaled = pnt_vec.scl( 1.0f / line_len );
        float t = line_unitvec.dot( pnt_vec_scaled );
        if ( t < 0.0 )
        {
            t = 0;
        }
        else if ( t >1.0 )
        {
            t = 1;
        }
        final Vec2 nearest = line_vec.scl( t );
        return nearest.dist2( pnt_vec );
    }
}