/*
 * MPL (Minecraft Programming Language): A language for easy development of commandblock
 * applications including and IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * This file is part of MPL (Minecraft Programming Language).
 *
 * MPL is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MPL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MPL. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 *
 * MPL (Minecraft Programming Language): Eine Sprache für die einfache Entwicklung von Commandoblock
 * Anwendungen, beinhaltet eine IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * Diese Datei ist Teil von MPL (Minecraft Programming Language).
 *
 * MPL ist Freie Software: Sie können es unter den Bedingungen der GNU General Public License, wie
 * von der Free Software Foundation, Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
 * veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * MPL wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG,
 * bereitgestellt; sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN
 * BESTIMMTEN ZWECK. Siehe die GNU General Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit MPL erhalten haben. Wenn
 * nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.adrodoc55.minecraft.mpl.chain_computing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.adrodoc55.minecraft.Coordinate3D;
import de.adrodoc55.minecraft.mpl.Command;

class PathElement implements Comparable<PathElement> {
  private final Coordinate3D pos;
  private final int index;
  private final List<Command> commands;
  private final PathElement previous;

  public PathElement(Coordinate3D pos, int index, List<Command> commands) {
    this(pos, index, commands, null);
  }

  public PathElement(Coordinate3D pos, int index, List<Command> commands, PathElement previous) {
    this.pos = pos;
    this.index = index;
    this.commands = commands;
    this.previous = previous;
  }

  public Coordinate3D getPos() {
    return pos;
  }

  public int getIndex() {
    return index;
  }

  public List<Command> getCommands() {
    return commands;
  }

  public PathElement getPrevious() {
    return previous;
  }

  private int score;

  public int getScore() {
    if (score == 0) {
      Coordinate3D pos = this.getPos();
      int minX = pos.getX();
      int minY = pos.getY();
      int minZ = pos.getZ();
      int maxX = pos.getX();
      int maxY = pos.getY();
      int maxZ = pos.getZ();
      PathElement prev = this.getPrevious();
      for (PathElement it = prev; it != null; it = it.getPrevious()) {
        Coordinate3D c = it.getPos();
        minX = Math.min(minX, c.getX());
        minY = Math.min(minY, c.getY());
        minZ = Math.min(minZ, c.getZ());
        maxX = Math.max(maxX, c.getX());
        maxY = Math.max(maxY, c.getY());
        maxZ = Math.max(maxZ, c.getZ());
      }
      int x = 1 + maxX - minX;
      int y = 1 + maxY - minY;
      int z = 1 + maxZ - minZ;
      score = Math.max(Math.max(x, y), z);
      // score = (x * y) + (y * z) + (z * x);
    }
    return score;
  }

  @Override
  public int compareTo(PathElement o) {
    int scoreCompare = Integer.compare(this.getScore(), o.getScore());
    if (scoreCompare == 0) {
      return Integer.compare(o.getIndex(), this.getIndex());
    } else {
      return scoreCompare;
    }
  }

  private int pathLength;

  public int getPathLength() {
    if (pathLength == 0) {
      for (PathElement it = this; it != null; it = it.getPrevious()) {
        pathLength++;
      }
    }
    return pathLength;
  }

  public Iterable<PathElement> getValidContinuations() {
    List<Coordinate3D> possibleCoodinates;
    Command command = getCommands().get(index);
    if (command != null && command.isConditional()) {
      possibleCoodinates = new ArrayList<Coordinate3D>(1);
      PathElement previous = getPrevious();
      if (previous == null) {
        throw new IllegalStateException("The first Command can't be conditional!");
      }
      Coordinate3D previousPos = previous.getPos();
      Coordinate3D direction = getPos().minus(previousPos);
      Coordinate3D next = getPos().plus(direction);
      possibleCoodinates.add(next);
    } else {
      possibleCoodinates = getPos().getAdjacent();
    }

    for (Iterator<Coordinate3D> it = possibleCoodinates.iterator(); it.hasNext();) {
      Coordinate3D coordinate = it.next();
      if (pathContains(coordinate)) {
        it.remove();
      }
    }

    Collection<PathElement> validContinuations =
        new ArrayList<PathElement>(possibleCoodinates.size());
    for (Coordinate3D coordinate3d : possibleCoodinates) {
      validContinuations.add(new PathElement(coordinate3d, getIndex() + 1, getCommands(), this));
    }
    return validContinuations;
  }

  public boolean pathContains(Coordinate3D other) {
    for (PathElement it = this; it != null; it = it.getPrevious()) {
      if (it.getPos().equals(other)) {
        return true;
      }
    }
    return false;
  }

  public boolean pathEquals(PathElement other) {
    if (other == null) {
      return false;
    }
    if (!this.getPos().equals(other.getPos())) {
      return false;
    }
    if (!this.getPathSet().equals(other.getPathSet())) {
      return false;
    }
    return true;
  }

  // TODO: Bitmap Implementierung
  private Set<Coordinate3D> pathSet;

  private Set<Coordinate3D> getPathSet() {
    if (pathSet == null) {
      pathSet = new HashSet<Coordinate3D>(getPathLength());
      for (PathElement it = this; it != null; it = it.getPrevious()) {
        pathSet.add(it.getPos());
      }
    }
    return pathSet;
  }

  public boolean hasEnoughSpace() {
    LinkedList<Coordinate3D> found = new LinkedList<Coordinate3D>();
    LinkedList<Coordinate3D> todos = new LinkedList<Coordinate3D>();
    todos.addAll(getPos().getAdjacent());
    int reqiredSpace = commands.size() - getPathLength();
    while (!todos.isEmpty()) {
      Coordinate3D current = todos.poll();
      if (!pathContains(current) && !found.contains(current)) {
        found.add(current);
        if (found.size() > reqiredSpace) {
          return true;
        }
        todos.addAll(current.getAdjacent());
      }
    }
    return false;
  }

  // TODO: Remove
  @Override
  public String toString() {
    String[][] matrix = new String[this.getMaxXY()][this.getMaxXY()];
    for (PathElement it = this; it != null; it = it.getPrevious()) {
      Coordinate3D pos = it.getPos();
      Command command = null;
      List<Command> commands = it.getCommands();
      int index = it.getIndex();
      if (index < commands.size()) {
        command = commands.get(index);
      }
      String cond = command != null && command.isConditional() ? "c" : "";
      matrix[pos.getY()][pos.getX()] = cond + String.valueOf(index);
    }
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < matrix.length; y++) {
      String[] row = matrix[y];
      for (int x = 0; x < row.length; x++) {
        String string = row[x];
        if (string == null) {
          string = "";
        }
        sb.append(string + "\t");
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  private int getMaxXY() {
    Coordinate3D pos = this.getPos();
    int maxX = pos.getX();
    int maxY = pos.getY();
    PathElement prev = this.getPrevious();
    for (PathElement it = prev; it != null; it = it.getPrevious()) {
      Coordinate3D c = it.getPos();
      maxX = Math.max(maxX, c.getX());
      maxY = Math.max(maxY, c.getY());
    }
    return Math.max(maxX + 1, maxY + 1);
  }

}
