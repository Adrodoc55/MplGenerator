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
package de.adrodoc55.minecraft.mpl.ide.gui.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;

/**
 * This class will display line numbers for a related text component. The text component must use
 * the same line height for each line. TextLineNumber supports wrapped lines and will highlight the
 * line number of the current line in the text component.
 *
 * This class was designed to be used as a component added to the row header of a JScrollPane.
 *
 * https://tips4java.wordpress.com/2009/05/23/text-component-line-number/
 */
public class TextLineNumber extends JPanel
    implements CaretListener, DocumentListener, PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public final static float LEFT = 0.0f;
  public final static float CENTER = 0.5f;
  public final static float RIGHT = 1.0f;

  private final static Border OUTER = new MatteBorder(0, 0, 0, 2, Color.GRAY);

  // Text component this TextTextLineNumber component is in sync with
  private JTextComponent component;

  // Properties that can be changed
  private boolean updateFont;
  private int borderGap;
  private Color currentLineForeground;
  private float digitAlignment;
  private int minimumDisplayDigits;

  // Keep history information to reduce the number of times the component
  // needs to be repainted
  private int lastPreferredWidth;
  private int lastDigits;
  private int lastHeight;
  private int lastLine;

  private HashMap<String, FontMetrics> fonts;

  /**
   * Create a line number component for a text component. This minimum display width will be based
   * on 3 digits.
   *
   * @param component the related text component
   */
  public TextLineNumber(JTextComponent component) {
    this(component, 3);
  }

  /**
   * Create a line number component for a text component.
   *
   * @param component the related text component
   * @param minimumDisplayDigits the number of digits used to calculate the minimum width of the
   *        component
   */
  public TextLineNumber(JTextComponent component, int minimumDisplayDigits) {
    setComponent(component);

    if (component != null) {
      setFont(component.getFont());
    }

    setBorderGap(5);
    setCurrentLineForeground(Color.RED);
    setDigitAlignment(RIGHT);
    setMinimumDisplayDigits(minimumDisplayDigits);
  }

  public void setComponent(JTextComponent newComponent) {
    JTextComponent oldComponent = component;
    if (oldComponent != null) {
      oldComponent.getDocument().removeDocumentListener(this);
      oldComponent.removeCaretListener(this);
      oldComponent.removePropertyChangeListener("font", this);
    }
    if (newComponent != null) {
      newComponent.getDocument().addDocumentListener(this);
      newComponent.addCaretListener(this);
      newComponent.addPropertyChangeListener("font", this);
    }
    component = newComponent;
  }

  /**
   * Gets the update font property
   *
   * @return the update font property
   */
  public boolean getUpdateFont() {
    return updateFont;
  }

  /**
   * Set the update font property. Indicates whether this Font should be updated automatically when
   * the Font of the related text component is changed.
   *
   * @param updateFont when true update the Font and repaint the line numbers, otherwise just
   *        repaint the line numbers.
   */
  public void setUpdateFont(boolean updateFont) {
    this.updateFont = updateFont;
  }

  /**
   * Gets the border gap
   *
   * @return the border gap in pixels
   */
  public int getBorderGap() {
    return borderGap;
  }

  /**
   * The border gap is used in calculating the left and right insets of the border. Default value is
   * 5.
   *
   * @param borderGap the gap in pixels
   */
  public void setBorderGap(int borderGap) {
    this.borderGap = borderGap;
    Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
    setBorder(new CompoundBorder(OUTER, inner));
    lastDigits = 0;
    setPreferredWidth();
  }

  /**
   * Gets the current line rendering Color
   *
   * @return the Color used to render the current line number
   */
  public Color getCurrentLineForeground() {
    return currentLineForeground == null ? getForeground() : currentLineForeground;
  }

  /**
   * The Color used to render the current line digits. Default is Coolor.RED.
   *
   * @param currentLineForeground the Color used to render the current line
   */
  public void setCurrentLineForeground(Color currentLineForeground) {
    this.currentLineForeground = currentLineForeground;
  }

  /**
   * Gets the digit alignment
   *
   * @return the alignment of the painted digits
   */
  public float getDigitAlignment() {
    return digitAlignment;
  }

  /**
   * Specify the horizontal alignment of the digits within the component. Common values would be:
   * <ul>
   * <li>TextLineNumber.LEFT
   * <li>TextLineNumber.CENTER
   * <li>TextLineNumber.RIGHT (default)
   * </ul>
   *
   * @param currentLineForeground the Color used to render the current line
   */
  public void setDigitAlignment(float digitAlignment) {
    this.digitAlignment =
        digitAlignment > 1.0f ? 1.0f : digitAlignment < 0.0f ? -1.0f : digitAlignment;
  }

  /**
   * Gets the minimum display digits
   *
   * @return the minimum display digits
   */
  public int getMinimumDisplayDigits() {
    return minimumDisplayDigits;
  }

  /**
   * Specify the mimimum number of digits used to calculate the preferred width of the component.
   * Default is 3.
   *
   * @param minimumDisplayDigits the number digits used in the preferred width calculation
   */
  public void setMinimumDisplayDigits(int minimumDisplayDigits) {
    this.minimumDisplayDigits = minimumDisplayDigits;
    setPreferredWidth();
  }

  /**
   * Calculate the width needed to display the maximum line number
   */
  private void setPreferredWidth() {
    setSize(getPreferredSize());
  }

  @Override
  public Dimension getPreferredSize() {
    int lines;
    if (component != null) {
      Element root = component.getDocument().getDefaultRootElement();
      lines = root.getElementCount();
    } else {
      lines = 0;
    }
    int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);

    // Update sizes when number of digits in the line number changes

    if (lastDigits != digits) {
      lastDigits = digits;
      FontMetrics fontMetrics = getFontMetrics(getFont());
      int width = fontMetrics.charWidth('0') * digits;
      Insets insets = getInsets();
      lastPreferredWidth = insets.left + insets.right + width;
    }
    int height;
    if (component != null) {
      height = 2 * lines * component.getFontMetrics(component.getFont()).getHeight();
    } else {
      height = 0;
    }
    return new Dimension(lastPreferredWidth, height);
  }

  /**
   * Draw the line numbers
   */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    // Determine the width of the space available to draw the line number
    FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
    Insets insets = getInsets();
    int availableWidth = getSize().width - insets.left - insets.right;

    // Determine the rows to draw within the clipped bounds.
    Rectangle clip = g.getClipBounds();
    int rowStartOffset = component.viewToModel(new Point(0, clip.y));
    int endOffset = component.viewToModel(new Point(0, clip.y + clip.height));

    while (rowStartOffset <= endOffset) {
      try {
        if (isCurrentLine(rowStartOffset))
          g.setColor(getCurrentLineForeground());
        else
          g.setColor(getForeground());

        // Get the line number as a string and then determine the
        // "X" and "Y" offsets for drawing the string.
        String lineNumber = getTextLineNumber(rowStartOffset);
        int stringWidth = fontMetrics.stringWidth(lineNumber);
        int x = getOffsetX(availableWidth, stringWidth) + insets.left;
        int y = getOffsetY(rowStartOffset, fontMetrics);
        g.drawString(lineNumber, x, y);

        // Move to the next row
        rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
      } catch (Exception e) {
        break;
      }
    }
  }

  /*
   * We need to know if the caret is currently positioned on the line we are about to paint so the
   * line number can be highlighted.
   */
  private boolean isCurrentLine(int rowStartOffset) {
    int caretPosition = component.getCaretPosition();
    Element root = component.getDocument().getDefaultRootElement();

    if (root.getElementIndex(rowStartOffset) == root.getElementIndex(caretPosition))
      return true;
    else
      return false;
  }

  /*
   * Get the line number to be drawn. The empty string will be returned when a line of text has
   * wrapped.
   */
  protected String getTextLineNumber(int rowStartOffset) {
    Element root = component.getDocument().getDefaultRootElement();
    int index = root.getElementIndex(rowStartOffset);
    Element line = root.getElement(index);

    if (line.getStartOffset() == rowStartOffset)
      return String.valueOf(index + 1);
    else
      return "";
  }

  /*
   * Determine the X offset to properly align the line number when drawn
   */
  private int getOffsetX(int availableWidth, int stringWidth) {
    return (int) ((availableWidth - stringWidth) * digitAlignment);
  }

  /*
   * Determine the Y offset for the current row
   */
  private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics) throws BadLocationException {
    // Get the bounding rectangle of the row

    Rectangle r = component.modelToView(rowStartOffset);
    int lineHeight = fontMetrics.getHeight();
    int y = r.y + r.height;
    int descent = 0;

    // The text needs to be positioned above the bottom of the bounding
    // rectangle based on the descent of the font(s) contained on the row.

    if (r.height == lineHeight) // default font is being used
    {
      descent = fontMetrics.getDescent();
    } else // We need to check all the attributes for font changes
    {
      if (fonts == null)
        fonts = new HashMap<String, FontMetrics>();

      Element root = component.getDocument().getDefaultRootElement();
      int index = root.getElementIndex(rowStartOffset);
      Element line = root.getElement(index);

      for (int i = 0; i < line.getElementCount(); i++) {
        Element child = line.getElement(i);
        AttributeSet as = child.getAttributes();
        String fontFamily = (String) as.getAttribute(StyleConstants.FontFamily);
        Integer fontSize = (Integer) as.getAttribute(StyleConstants.FontSize);
        String key = fontFamily + fontSize;

        FontMetrics fm = fonts.get(key);

        if (fm == null) {
          Font font = new Font(fontFamily, Font.PLAIN, fontSize);
          fm = component.getFontMetrics(font);
          fonts.put(key, fm);
        }

        descent = Math.max(descent, fm.getDescent());
      }
    }

    return y - descent;
  }

  //
  // Implement CaretListener interface
  //
  @Override
  public void caretUpdate(CaretEvent e) {
    // Get the line the caret is positioned on
    int caretPosition = e.getDot();
    Element root = component.getDocument().getDefaultRootElement();
    int currentLine = root.getElementIndex(caretPosition);

    // Need to repaint so the correct line number can be highlighted

    if (lastLine != currentLine) {
      repaint();
      lastLine = currentLine;
    }
  }

  //
  // Implement DocumentListener interface
  //
  @Override
  public void changedUpdate(DocumentEvent e) {
    documentChanged();
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    documentChanged();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    documentChanged();
  }

  /*
   * A document change may affect the number of displayed lines of text. Therefore the lines numbers
   * will also change.
   */
  private void documentChanged() {
    // View of the component has not been updated at the time
    // the DocumentEvent is fired

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          int endPos = component.getDocument().getLength();
          Rectangle rect = component.modelToView(endPos);

          if (rect != null && rect.y != lastHeight) {
            setPreferredWidth();
            repaint();
            lastHeight = rect.y;
          }
        } catch (BadLocationException ex) {
          /* nothing to do */ }
      }
    });
  }

  //
  // Implement PropertyChangeListener interface
  //
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getNewValue() instanceof Font) {
      if (updateFont) {
        Font newFont = (Font) evt.getNewValue();
        setFont(newFont);
        lastDigits = 0;
        setPreferredWidth();
      } else {
        repaint();
      }
    }
  }
}