package org.changx03.boardGame2048;

/**
 * Created by gungr on 1/12/2016.
 */
public class Location {

    private int x;
    private int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getLayoutX(int CELL_SIZE) {
        return (x * CELL_SIZE) + CELL_SIZE / 2;
    }

    public double getLayoutY(int CELL_SIZE) {
        return (y * CELL_SIZE) + CELL_SIZE / 2;
    }

    public boolean isValidFor() {
        return x >= 0 && x < 4 && y >= 0 && y < 4;
    }

    public Location offset(Direction direction) {

        return new Location(x + direction.getX(), y + direction.getY());
    }

    @Override
    public String toString() {
        return "Location{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + this.x;
        hash = 61 * hash + this.y;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Location other = (Location) obj;
        if (this.x != other.x) {
            return false;
        }
        return this.y == other.y;
    }
}
