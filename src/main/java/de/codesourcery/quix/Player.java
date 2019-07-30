package de.codesourcery.quix;

public class Player extends Entity
{
    public int score;

    @Override
    public String toString()
    {
        return "Player "+super.toString();
    }
}
