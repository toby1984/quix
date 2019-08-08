package de.codesourcery.quix;

public class Difficulty
{
    public int quixSpeed;
    public int quixLineCount=1;
    public int enemyCount;
    public int enemySpeed=2;

    public Difficulty(int quixSpeedX,int enemyCount)
    {
        this.quixSpeed = quixSpeedX;
        this.enemyCount = enemyCount;
    }
}
