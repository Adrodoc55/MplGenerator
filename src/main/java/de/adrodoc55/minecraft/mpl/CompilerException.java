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
package de.adrodoc55.minecraft.mpl;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.Token;

public class CompilerException extends Exception {

  private static final long serialVersionUID = 2588890897512612205L;
  private File file;
  private Token token;
  private String line;

  public CompilerException(File file, Token token, String line, String message) {
    super(message);
    init(file, token, line);
  }

  // public CompilerException(File file, int line, int startIndex,
  // int stopIndex, String message) {
  // super(message);
  // init(file, line, startIndex, stopIndex);
  // }

  public CompilerException(File file, Token token, String line, String message, Throwable cause) {
    super(message, cause);
    init(file, token, line);
  }

  // public CompilerException(File file, int line, int startIndex,
  // int stopIndex, String message, Throwable cause) {
  // super(message, cause);
  // init(file, line, startIndex, stopIndex);
  // }

  private void init(File file, Token token, String line) {
    this.file = file;
    this.token = token;
    this.line = line;
  }

  public File getFile() {
    return file;
  }

  public Token getToken() {
    return token;
  }

  public String getLine() {
    return line;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    String path;
    try {
      path = this.getFile().getCanonicalPath();
    } catch (IOException e) {
      path = this.getFile().getAbsolutePath();
    }
    sb.append(path).append(':').append(token.getLine()).append(":\n");
    sb.append(this.getLocalizedMessage()).append("\n");
    sb.append(this.getLine()).append("\n");
    int count = this.getToken().getCharPositionInLine();
    sb.append(new String(new char[count]).replace('\0', ' '));
    sb.append("^");
    return sb.toString();
  }
}
