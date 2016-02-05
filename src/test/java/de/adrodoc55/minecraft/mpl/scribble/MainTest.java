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
package de.adrodoc55.minecraft.mpl.scribble;

import java.io.File;
import java.io.IOException;

import de.adrodoc55.minecraft.mpl.Main;
import de.adrodoc55.minecraft.mpl.antlr.CompilationFailedException;

public class MainTest {

  public static void main(String[] args) throws IOException, CompilationFailedException {
    File turretsFile = new File(
        "C:/Users/adrian/Programme/workspace/MplGenerator/src/main/resources/testdata.txt");
    File validateDirections = new File(
        "C:/Users/adrian/Programme/workspace/ApertureCraftVanilla/src/main/minecraft/ACV_validateDirections.txt");
    File teleportation = new File(
        "C:/Users/adrian/Programme/workspace/ApertureCraftVanilla/src/main/minecraft/ACV_teleportation.txt");
    File online = new File(
        "C:/Users/adrian/Programme/workspace/ApertureCraftVanilla/src/main/minecraft/ACV_online.txt");
    File createPortal = new File(
        "C:/Users/adrian/Programme/workspace/ApertureCraftVanilla/src/main/minecraft/ACV_createPortal.txt");

    File acv = new File(
        "C:/Users/adrian/Programme/workspace/ApertureCraftVanilla/src/main/minecraft/ApertureCraft Vanilla.txt");
    File test = new File("C:/Users/adrian/Programme/workspace/MPL/src/test/resources/mpl-test.txt");
    File inputFile = test;
    File outputFile =
        new File("C:/Users/Adrian/Documents/MCEdit/Filters", inputFile.getName() + ".py");
    Main.main(inputFile, outputFile);
  }

}
