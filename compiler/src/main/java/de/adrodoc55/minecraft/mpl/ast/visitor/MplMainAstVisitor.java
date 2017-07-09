/*
 * Minecraft Programming Language (MPL): A language for easy development of command block
 * applications including an IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * This file is part of MPL.
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
 * Minecraft Programming Language (MPL): Eine Sprache für die einfache Entwicklung von Commandoblock
 * Anwendungen, inklusive einer IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * Diese Datei ist Teil von MPL.
 *
 * MPL ist freie Software: Sie können diese unter den Bedingungen der GNU General Public License,
 * wie von der Free Software Foundation, Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
 * veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * MPL wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG,
 * bereitgestellt; sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN
 * BESTIMMTEN ZWECK. Siehe die GNU General Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit MPL erhalten haben. Wenn
 * nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.adrodoc55.minecraft.mpl.ast.visitor;

import static de.adrodoc55.minecraft.mpl.ast.ProcessType.INLINE;
import static de.adrodoc55.minecraft.mpl.ast.chainparts.MplNotify.NOTIFY;
import static de.adrodoc55.minecraft.mpl.commands.Mode.IMPULSE;
import static de.adrodoc55.minecraft.mpl.commands.Mode.REPEAT;
import static de.adrodoc55.minecraft.mpl.compilation.CompilerOptions.CompilerOption.DELETE_ON_UNINSTALL;
import static de.adrodoc55.minecraft.mpl.compilation.CompilerOptions.CompilerOption.TRANSMITTER;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

import org.antlr.v4.runtime.CommonToken;

import com.google.common.annotations.VisibleForTesting;

import de.adrodoc55.commons.CopyScope;
import de.adrodoc55.minecraft.coordinate.Coordinate3D;
import de.adrodoc55.minecraft.coordinate.Orientation3D;
import de.adrodoc55.minecraft.mpl.antlr.MplLexer;
import de.adrodoc55.minecraft.mpl.ast.ProcessType;
import de.adrodoc55.minecraft.mpl.ast.chainparts.ChainPart;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplBreakpoint;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplCommand;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplIntercept;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplNotify;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplWaitfor;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplBreak;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplContinue;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplWhile;
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProcess;
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProgram;
import de.adrodoc55.minecraft.mpl.chain.ChainContainer;
import de.adrodoc55.minecraft.mpl.chain.CommandChain;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ChainLink;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.MplSkip;
import de.adrodoc55.minecraft.mpl.compilation.MplCompilerContext;
import de.adrodoc55.minecraft.mpl.compilation.MplSource;
import de.adrodoc55.minecraft.mpl.interpretation.IllegalModifierException;

/**
 * @author Adrodoc55
 */
public class MplMainAstVisitor extends MplProcessAstVisitor {
  @VisibleForTesting
  MplProgram program;

  private Deque<IfNestingLayer> ifNestingLayers = new ArrayDeque<>();
  private MplSource breakpoint;

  public MplMainAstVisitor(MplCompilerContext context) {
    super(context);
  }

  @Override
  public void setBreakpoint(MplSource breakpoint) {
    this.breakpoint = breakpoint;
  }

  @Override
  protected MplProgram getProgram() {
    return program;
  }

  @Override
  public Deque<IfNestingLayer> getIfNestingLayers() {
    return ifNestingLayers;
  }

  public ChainContainer visitProgram(MplProgram program) {
    this.program = program;
    Orientation3D orientation = program.getOrientation();
    Coordinate3D max = program.getMax();
    CommandChain install = visitInstall(program);
    CommandChain uninstall = visitUninstall(program);

    List<CommandChain> chains = new ArrayList<>(program.getProcesses().size());
    for (MplProcess process : program.getProcesses()) {
      CommandChain chain = visitProcess(process);
      if (chain != null) {
        chains.add(chain);
      }
    }
    if (breakpoint != null) {
      chains.add(getBreakpointProcess(program));
    }
    return new ChainContainer(orientation, max, install, uninstall, chains, program.getHash());
  }

  private static MplSource defaultSource(File programFile) {
    return new MplSource(programFile, "", new CommonToken(MplLexer.PROCESS));
  }

  private CommandChain visitInstall(MplProgram program) {
    if (!isInstallRequired(program)) {
      return new CommandChain("install", new ArrayList<>(0));
    }
    MplProcess install = program.getInstall();
    if (install == null) {
      install = new MplProcess("install", defaultSource(program.getProgramFile()));
    }
    return visitProcess(install);
  }

  private boolean isInstallRequired(MplProgram program) {
    return program.getInstall() != null//
        || isUninstallRequired(program);
  }

  private CommandChain visitUninstall(MplProgram program) {
    if (!isUninstallRequired(program)) {
      return new CommandChain("uninstall", new ArrayList<>(0));
    }
    MplProcess uninstall = program.getUninstall();
    if (uninstall == null) {
      uninstall = new MplProcess("uninstall", defaultSource(program.getProgramFile()));
    }
    return visitProcess(uninstall);
  }

  private boolean isUninstallRequired(MplProgram program) {
    return program.getUninstall() != null//
        || options.hasOption(DELETE_ON_UNINSTALL)//
        || program.getProcesses().stream().anyMatch(p -> p.getName() != null);
  }

  @CheckReturnValue
  private CommandChain getBreakpointProcess(MplProgram program) {
    String hash = program.getHash();
    MplProcess process = new MplProcess("breakpoint", breakpoint);
    List<ChainPart> commands = new ArrayList<>();

    // Pause
    if (!options.hasOption(TRANSMITTER)) {
      commands.add(new MplCommand("/execute @e[tag=" + hash + "] ~ ~ ~ clone ~ ~ ~ ~ ~ ~ ~ ~1 ~",
          breakpoint));
    }
    commands.add(new MplCommand("/tp @e[tag=" + hash + "] ~ ~1 ~", breakpoint));
    if (!options.hasOption(TRANSMITTER)) {
      commands.add(new MplCommand("/execute @e[tag=" + hash + "] ~ ~ ~ blockdata ~ ~ ~ {Command:}",
          breakpoint));
    }

    commands.add(new MplCommand(
        "tellraw @a [{\"text\":\"[tp to breakpoint]\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/tp @p @e[name=breakpoint_NOTIFY,c=-1]\"}},{\"text\":\" \"},{\"text\":\"[continue program]\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/execute @e[name=breakpoint_CONTINUE"
            + NOTIFY + "] ~ ~ ~ " + getStartCommand() + "\"}}]",
        breakpoint));

    commands.add(new MplWaitfor("breakpoint_CONTINUE", breakpoint));
    commands.add(new MplCommand("/kill @e[name=breakpoint_CONTINUE" + NOTIFY + "]", breakpoint));

    // Unpause
    commands.add(new MplCommand(
        "/execute @e[tag=" + hash + "] ~ ~ ~ clone ~ ~ ~ ~ ~ ~ ~ ~-1 ~ force move", breakpoint));
    commands.add(new MplCommand("/tp @e[tag=" + hash + "] ~ ~-1 ~", breakpoint));
    if (!options.hasOption(TRANSMITTER)) {
      commands.add(new MplCommand(
          "/execute @e[tag=" + hash + "] ~ ~ ~ blockdata ~ ~ ~ {Command:" + getStopCommand() + "}",
          breakpoint));
    }

    commands.add(new MplNotify("breakpoint", breakpoint));

    process.setChainParts(commands);
    return visitProcess(process);
  }

  /**
   * Returns null if the specified {@code process} is of type {@link ProcessType#INLINE}.
   *
   * @param process the {@link MplProcess} to convert
   * @return result a new {@link CommandChain}
   */
  public @Nullable CommandChain visitProcess(MplProcess process) {
    if (process.getType() == INLINE) {
      return null;
    }
    List<ChainPart> chainParts = new CopyScope().copy(process.getChainParts());
    List<ChainLink> result = new ArrayList<>(chainParts.size());
    boolean containsSkip = containsHighlevelSkip(chainParts);
    String name = process.getName();
    if (name != null) {
      if (process.isRepeating()) {
        if (options.hasOption(TRANSMITTER)) {
          result.add(new MplSkip());
        }
        if (chainParts.isEmpty()) {
          chainParts.add(new MplCommand("", process.getSource()));
        }
        ChainPart first = chainParts.get(0);
        try {
          if (containsSkip) {
            first.setMode(IMPULSE);
          } else {
            first.setMode(REPEAT);
          }
          first.setNeedsRedstone(true);
        } catch (IllegalModifierException ex) {
          throw new IllegalStateException(ex.getMessage(), ex);
        }
      } else {
        result.addAll(newJumpDestination(false));
      }
    } else if (options.hasOption(TRANSMITTER)) {
      result.add(new MplSkip());
    }
    for (ChainPart chainPart : chainParts) {
      result.addAll(chainPart.accept(this));
    }
    if (process.isRepeating() && containsSkip) {
      result.addAll(getRestartBackref(result.get(0), false));
      resolveReferences(result);
    }
    if (!process.isRepeating() && name != null && !"install".equals(name)
        && !"uninstall".equals(name)) {
      result.addAll(visitIgnoringWarnings(new MplNotify(name, process.getSource())));
    }
    return new CommandChain(name, result, process.getTags());
  }

  private boolean containsHighlevelSkip(List<ChainPart> chainParts) {
    for (ChainPart chainPart : chainParts) {
      if (chainPart instanceof MplWaitfor//
          || chainPart instanceof MplIntercept//
          || chainPart instanceof MplBreakpoint//
          || chainPart instanceof MplWhile//
          || chainPart instanceof MplBreak//
          || chainPart instanceof MplContinue//
      ) {
        return true;
      }
    }
    return false;
  }

}
