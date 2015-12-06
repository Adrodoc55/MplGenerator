package de.adrodoc55.minecraft.mpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.adrodoc55.minecraft.Coordinate3D;
import de.adrodoc55.minecraft.mpl.antlr.MplCompiler;
import de.adrodoc55.minecraft.mpl.chain_computing.ChainComputer;
import de.adrodoc55.minecraft.mpl.chain_computing.IterativeChainComputer;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "Expected two Arguments: imputFile, outputDir");
        }
        File inputFile = new File(args[0]);
        if (!inputFile.canRead()) {
            throw new IOException("Can't read inputFile: " + inputFile);
        }

        File outputDir = new File(args[1]);

        // File inputFile = new File(
        // "C:/Users/adrian/Programme/workspace/ApertureCraftVanilla/src/main/minecraft/ACV_online.txt");
        // File outputDir = new
        // File("C:/Users/adrian/Programme/workspace/ApertureCraftVanilla/build");

        // File inputDir = new
        // File("C:/Users/adrian/Programme/workspace/ApertureCraftVanilla/src/main/minecraft/methods");
        // File outputDir = new
        // File("C:/Users/adrian/Minecraft/MCEdit.v1.4.0.1.Win.64bit/stock-filters");

        // File[] inputFiles = inputDir.listFiles();
        // File[] inputFiles = { new File(inputDir,
        // "ACV_createPortalFailed.txt") };
        // for (File inputFile : inputFiles) {
        // File inputFile = new File(inputDir,
        // "ACV_validateDirections.txt");
        File outputFile = new File(outputDir, inputFile.getName() + ".py");

        Program program = MplCompiler.compile(inputFile);

        StringBuilder python = new StringBuilder(
                "from pymclevel.entity import TileEntity\n"
                        + "from pymclevel.nbt import TAG_String\n"
                        + "from pymclevel.nbt import TAG_Byte\n" + "\n"
                        + "displayName = 'Generate "
                        + inputFile.getName()
                        + "'\n"
                        + "\n"
                        + "def create_command_block(level, xyz, command, direction='south', mode='chain', conditional=False, auto=True):\n"
                        + "    x, y, z = xyz\n"
                        + "    		\n"
                        + "    idDict = {'impulse' : 137, 'chain' : 211, 'repeat' : 210}\n"
                        + "    blockId = idDict[mode]\n"
                        + "    level.setBlockAt(x, y, z, blockId)\n"
                        + "    \n"
                        + "    damageList = ['down', 'up', 'north', 'south', 'west', 'east']\n"
                        + "    damage = damageList.index(direction)\n"
                        + "    if conditional:\n"
                        + "        damage += 8\n"
                        + "\n"
                        + "    level.setBlockDataAt(x, y, z, damage)\n"
                        + "    control = TileEntity.Create('Control', xyz)\n"
                        + "    control['Command'] = TAG_String(command)\n"
                        + "    control['auto'] = TAG_Byte(auto)\n"
                        + "    level.addTileEntity(control)\n"
                        + "\n"
                        + "def perform(level, box, options):\n");
        String indent = "    ";

        for (Map.Entry<Coordinate3D, CommandChain> entry : program.getChains()
                .entrySet()) {
            Coordinate3D start = entry.getKey();
            CommandChain commandChain = entry.getValue();

            // JFrame frame = new JFrame(chain.getName());
            // ChainRenderer renderer = new ChainRenderer(commands);
            // frame.getContentPane().add(renderer, BorderLayout.CENTER);
            // ChainRenderer optimalRenderer = new ChainRenderer(commands);
            // frame.getContentPane().add(optimalRenderer, BorderLayout.EAST);
            // frame.pack();
            // frame.setLocationRelativeTo(null);
            // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // frame.setVisible(true);

            // ChainCalculator calculator = new ChainCalculator(renderer,
            // optimalRenderer);
            // boolean containsConditional = false;
            // for (Command command : commandChain.getCommands()) {
            // if (command != null && command.isConditional()) {
            // containsConditional = true;
            // break;
            // }
            // }
            ChainComputer calculator;
            // if (containsConditional) {
            calculator = new IterativeChainComputer();
            // } else {
            // calculator = new NoConditionalChainCalculator(false, true, true);
            // }
            CommandBlockChain optimal = calculator.computeOptimalChain(start,
                    commandChain);
            List<CommandBlock> commandBlocks = optimal.getCommandBlocks();

            // ChainCalculator calculator = new ChainCalculator(commands,
            // renderer,
            // optimalRenderer);

            // optimalRenderer.render(optimal);

            insertRelativeCoordinates(commandBlocks);
            for (CommandBlock current : commandBlocks) {
                python.append(indent + current.toPython() + "\n");
            }
        }

        outputFile.delete();
        try {
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(
                outputFile));) {
            writer.write(python.toString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        // }
    }

    private static void insertRelativeCoordinates(
            List<CommandBlock> commandBlocks) {
        for (int i = 0; i < commandBlocks.size(); i++) {
            CommandBlock current = commandBlocks.get(i);
            if (current.toCommand() == null) {
                continue;
            }

            Pattern referencePattern = Pattern.compile("\\$\\{(-?\\d+)\\}");
            if (current != null) {
                Matcher matcher = referencePattern
                        .matcher(current.getCommand());
                StringBuffer commandSb = new StringBuffer();
                while (matcher.find()) {
                    int relative = Integer.parseInt(matcher.group(1));
                    Coordinate3D referenced = commandBlocks.get(i + relative)
                            .getCoordinate();
                    Coordinate3D relativeCoordinate = referenced.minus(current
                            .getCoordinate());
                    matcher.appendReplacement(commandSb,
                            relativeCoordinate.toRelativeString());
                }
                matcher.appendTail(commandSb);
                current.setCommand(commandSb.toString());
            }

        }
    }

}
