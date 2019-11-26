package iconpacker;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;



public class Iconpacker {
    
    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            System.out.println(" ");
            System.out.println("    RLE Compressor!!!!!!!!!!!");
            System.out.println("    Created by Roman Lahin");
            System.out.println("    Usage: ");
            System.out.println("        Original file name");
            System.out.println(" ");
            System.out.println("    Optional options:");
            System.out.println("        Ask for every option (default is false)");
            System.out.println("        -lazy 1/0");
            return;
        }
        
        for(int fileId=0;fileId<args.length;fileId++) {
            if(args[fileId].startsWith("-")) {
                fileId++; continue;
            }
            
            boolean unpack = args[fileId].toLowerCase().endsWith(".rle");
            
            String newFile = args[fileId].substring(0, args[fileId].lastIndexOf('.'));
            
            if(unpack) newFile = newFile+".png";
            else newFile = newFile+".rle";
            
            if(unpack) unpackFile(args[fileId],newFile);
            else packFile(args[fileId],newFile);
        }
    }
    
    public static void unpackFile(String file, String newFile) throws Exception {
        File fc = new File(file);

        FileInputStream fis = new FileInputStream(fc);
        DataInputStream dis = new DataInputStream(fis);

        int width = dis.readInt();
        int height = dis.readInt();
        byte[] data = new byte[width*height];
        readRLE(data, dis);
        dis.close();

        File fc2 = new File(newFile);
        if(!fc2.exists()) fc2.createNewFile();

        FileOutputStream fos = new FileOutputStream(fc2);
        DataOutputStream dos = new DataOutputStream(fos);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] rgb = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            rgb[i] = ((data[i] & 0xFF) << 24);
        }
        img.setRGB(0, 0, width, height, rgb, 0, width);
        
        ImageIO.write(img, "png", dos);
        
        dos.close();
    }
    
    
    public static void packFile(String file, String newFile) throws Exception {
            BufferedImage img = ImageIO.read(new File(file));
            
            System.out.println("Processing "+file);
            
            int[] data = new int[img.getWidth()*img.getHeight()];
            img.getRGB(0, 0, img.getWidth(),img.getHeight(), data, 0, img.getWidth());
            
            byte[] bdata = new byte[data.length];
            
            for(int i=0; i<data.length; i++) {
                bdata[i] = (byte) (data[i] >> 24);
            }
        
            File fc2 = new File(newFile);
            if(!fc2.exists()) fc2.createNewFile();
            
            FileOutputStream fos = new FileOutputStream(fc2);
            DataOutputStream dos = new DataOutputStream(fos);
            
            dos.writeInt(img.getWidth());
            dos.writeInt(img.getHeight());
            writeRLE(bdata, dos);
            
            dos.close();
            System.out.println("Done!");
    }

    public static void writeRLE(byte[] data, OutputStream os) throws Exception {
        final int minimalRepeatCount = 3;//2*2 + 1;
        int begin = 0;
        while(begin < data.length) {
            int end = Math.min(begin+0x1000, data.length);
            byte repeat = 0;
            int repeatPos = 0, repeatCount = 0;
            
            for(int i=begin; i<end; i++) {
                byte current = data[i];
                if(repeatCount == 0 || repeat != current) {
                    if(repeatPos == begin && repeatCount > minimalRepeatCount) {
                        end = i;
                        break;
                    }
                    repeat = current;
                    repeatPos = i;
                    repeatCount = 1;
                } else repeatCount++;
                
                if(repeatCount > minimalRepeatCount && begin != repeatPos) {
                    end = repeatPos;
                    break;
                }
            }
            
            if(begin == repeatPos) {
                int len = 0x8000 | (repeatCount-1);
                os.write(len>>8);
                os.write(len);
                
                os.write(repeat);
            } else {
                int len = end-begin-1;
                os.write(len>>8);
                os.write(len);
                
                os.write(data, begin, len+1);
            }
            
            begin = end;
        }
    }
    
    public static int readRLE(byte[] data, DataInputStream dis) throws Exception {
        int pos = 0;
        
        while(pos < data.length && dis.available() >= 3) {
            int count = dis.readShort() & 0xffff;
            boolean repeat = count > 0x7fff;
            count = (count & 0x7fff) + 1;

            if(repeat) {
                byte b = dis.readByte();
                int min = pos + count;

                for(; pos < min; pos++) {
                    data[pos] = b;
                }
            } else {
                dis.readFully(data, pos, count);
                pos += count;
            }
            
        }
        
        return data.length;
    }
    
}