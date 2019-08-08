package de.codesourcery.quix;

public class Player extends Entity
{
    public float area;
    public int score;

    public Direction previousMovement;

    @Override
    public String toString()
    {
        return "Player "+super.toString();
    }
}
