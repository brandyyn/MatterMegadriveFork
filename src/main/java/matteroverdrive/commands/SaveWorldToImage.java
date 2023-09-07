package matteroverdrive.commands;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.util.MOLog;
import matteroverdrive.world.MOImageGen;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveWorldToImage extends CommandBase {
    @Override
    public String getCommandName() {
        return "world_image_gen";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "world_image_gen <command> <coordinates> <filename>";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] parameters) {
        if (parameters.length >= 1) {
            if (parameters[0].equalsIgnoreCase("generate")) {
                if (parameters.length >= 2) {
                    try {
                        HashMap<String, Integer> colorMap = new HashMap<>();
                        String[] coordinates = parameters[1].split(",");
                        int x1 = parseInt(commandSender, coordinates[0]);
                        int y1 = parseInt(commandSender, coordinates[1]);
                        int z1 = parseInt(commandSender, coordinates[2]);

                        int x2 = parseInt(commandSender, coordinates[3]);
                        int y2 = parseInt(commandSender, coordinates[4]);
                        int z2 = parseInt(commandSender, coordinates[5]);

                        int zoneSizeX = Math.abs(x1 - x2);
                        int zoneSizeZ = Math.abs(z1 - z2);
                        int zoneLayers = Math.abs(y2 - y1);
                        int imageSizeX = (int) Math.ceil(Math.sqrt(zoneLayers)) * zoneSizeX;
                        int imageSizeZ = (int) Math.ceil(Math.sqrt(zoneLayers)) * zoneSizeZ;
                        int layerPerRow = imageSizeX / zoneSizeX;
                        BufferedImage image = new BufferedImage(imageSizeX, imageSizeZ, BufferedImage.TYPE_INT_ARGB);

                        for (int layer = 0; layer < zoneLayers; layer++) {
                            for (int z = 0; z < zoneSizeZ; z++) {
                                for (int x = 0; x < zoneSizeX; x++) {
                                    int imageX = (layer % layerPerRow) * zoneSizeX + x;
                                    int imageY = Math.floorDiv(layer, layerPerRow) * zoneSizeZ + z;
                                    int worldX = Math.min(x1, x2) + x;
                                    int worldY = Math.min(y1, y2) + layer;
                                    int worldZ = Math.min(z1, z2) + z;
                                    Block block = commandSender.getEntityWorld().getBlock(worldX, worldY, worldZ);
                                    int meta = commandSender.getEntityWorld().getBlockMetadata(worldX, worldY, worldZ);
                                    int color = ((255 - meta) & 0xff) << 24;
                                    String hex = Integer.toHexString(color);
                                    if (MOImageGen.worldGenerationBlockColors.containsKey(block)) {
                                        color += MOImageGen.worldGenerationBlockColors.get(block);
                                    } else if (block != Blocks.air) {
                                        String blockName = Block.blockRegistry.getNameForObject(block);
                                        color += blockName.hashCode() & 0xffffff;
                                        if (!colorMap.containsKey(blockName)) {
                                            colorMap.put(blockName, color & 0xffffff);
                                        }
                                    }
                                    MOLog.info("ImageX: %s, ImageY: %s", imageX, imageY);
                                    image.setRGB(imageX, imageY, color);
                                }
                            }
                        }


                        //image.setRGB(0,0,imageSize,imageSize,pixels,0,4);
                        File imageFile;
                        File mapFile;
                        if (parameters.length >= 3) {
                            mapFile = new File(parameters[2] + ".txt");
                            imageFile = new File(parameters[2] + ".png");
                        } else {
                            mapFile = new File("test_world_gen_image.txt");
                            imageFile = new File("test_world_gen_image.png");
                        }

                        PrintWriter printWriter = new PrintWriter(mapFile);
                        printWriter.println("LayerWidth: " + zoneSizeX + " LayerHeight: " + zoneSizeZ);
                        for (Map.Entry<String, Integer> entry : colorMap.entrySet()) {
                            printWriter.println(entry.getKey() + ": " + Integer.toHexString(entry.getValue()));
                        }
                        printWriter.close();

                        try {
                            ImageIO.write(image, "png", imageFile);
                            image.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        MOLog.error("", e);
                        throw new CommandException(e.getMessage());
                    }
                }
            } else if (parameters[0].equalsIgnoreCase("import")) {
                if (parameters.length >= 5) {
                    File imageFile = new File(parameters[1] + ".png");
                    File mapFile = new File(parameters[1] + ".txt");
                    int layerWidth = parseInt(commandSender, parameters[2]);
                    int layerHeight = parseInt(commandSender, parameters[3]);
                    List<int[][]> layers = MOImageGen.loadTexture(imageFile, layerWidth, layerHeight);
                    String[] coordinates = parameters[4].split(",");
                    int x = parseInt(commandSender, coordinates[0]);
                    int y = parseInt(commandSender, coordinates[1]);
                    int z = parseInt(commandSender, coordinates[2]);
                    Map<Integer, Block> map = new HashMap<>();
                    try {
                        BufferedReader mapReader = new BufferedReader(new FileReader(mapFile));
                        String line = mapReader.readLine();
                        line = mapReader.readLine();
                        while (line != null) {
                            String[] parts = line.split(":");
                            if (parts.length >= 3) {
                                String blockName = parts[0].trim() + ":" + parts[1].trim();
                                String color = parts[2].trim();
                                int colorInt = (int) (Long.valueOf(color, 16) & 0xffffff);
                                Block block = Block.getBlockFromName(blockName);
                                map.put(colorInt, block);
                                line = mapReader.readLine();
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();

                    }

                    MOImageGen.generateFromImage(commandSender.getEntityWorld(), x, y, z, layerWidth, layerHeight, layers, map);
                }
            }
        }
    }
}
