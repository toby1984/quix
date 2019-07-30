package de.codesourcery.quix;

public class Difficulty
{
    public int quixSpeed;
    public int enemyCount;
    public int enemySpeed=2;

    public Difficulty(int quixSpeedX,int enemyCount)
    {
        this.quixSpeed = quixSpeedX;
        this.enemyCount = enemyCount;
    }
}
