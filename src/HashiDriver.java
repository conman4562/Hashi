import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class HashiDriver extends JFrame {

    public static void main(String[] args) {
        String b64_image = "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAACbklEQVR4Xu2bQXLDMAhF63N005P1cD1ZNz1HGmesjEol8YUAoYm6S0e2+Y8PyI5zvL343+Gs/9ZxPZfYrC/yR/D75wes/+frm641idXipE/RPYI5MgSIWtxqJ7oLeAjXFF2DksEYjn/4BJ7CKRANEKMAbh4ZB8tDpEV00BVQCPEJzuWGbj3dB8y0POiEcxmsC14YMetMk4S0QYtWEt9bEiiAUPXOlUIPBATAkuJRCGoACltXNFHQOum45aYDBwDOvjWAREkCogWhBQAWfwYXGUAW3z+9NQBd4lcAUIOwARQ6UHf2V3FAyQUlB4gAQK0cWMT1EkkTzC9LGyIFMFU84qRRANQFoQBYZ7+0OdoAsvqYan8u+2ecGvanLsgdEBqApvi8D4QA4J395QBoZ78EYJr9Z2Q/7wOpBMICsMj+BnARON1n4gDE1sCumF0y6o4NYDtgl8DuAbsJXq3WbR+ATIjR7s6OD+sx2AqAA+AlPm2HXW+GOPHat7xAIo5QALyzfwfkByBS9qfcDnMAPLNfA3D+32waRAKQPxp3eSgaSfyUx+IrAVAvA0685+ij2T8/m381xgGY1fzSHkHt2+HSpoMTPzv7NQeolQEHYHb2WwBUIEQCUHtNRu0dIUkJeDlA+o5Q0iTeHLUcEEE8VwJmALzEl8YedSpXAsMQ0IcTFuu4dwRRBywJARHfC0BlMlhkmp4TFS8B8IDgvYFBoWVNFy1t/IcFhSDE0wEV1LOuJ+v5eWFSlWBCQJCKl5YAZTGtJCSWl45BxI1uIDSEJ0GjJVAC87K/HG3C6J0chW20RbKGpgBSFsV+AR5oIphe+xdUDG5FE0AAFAAAAABJRU5ErkJggg==";

        // default/starting stuff
        JFrame jFrame = new JFrame();
        //jFrame.setResizable(false);
        //jFrame.setUndecorated(true);
        jFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setTitle("Connor's Hashi Puzzle");
        jFrame.setMinimumSize(new Dimension(800, 600));
        try {
            byte[] imageBytes = Base64.getDecoder().decode(b64_image);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            jFrame.setIconImage(image);
        } catch (Exception ignored) {


        }

        String a = "2 2-5=2    \r\n"
                + "# | #1-4--1\r\n"
                + "6=3 #  #   \r\n"
                + "#2--6-1#   \r\n"
                + "3|1 #2=6   \r\n"
                + "|2# #  #   \r\n"
                + "1|3=5  3   \r\n"
                + " 2 3--1    \r\n"
                + "   |       \r\n"
                + " 1-2   ";
        String[] stuff = a.split("\r\n");

        Hashi jPanel = new Hashi(Arrays.asList(stuff));
        jFrame.add(jPanel);
        jFrame.setVisible(true);
    }

    public static List<String> txtToList(String fileOriginPath) throws FileNotFoundException {
        // scan down each line in the file
        List<String> data = new ArrayList<>();
        Scanner fileScanner = new Scanner(new File(fileOriginPath));
        while (fileScanner.hasNextLine()) {
            data.add(fileScanner.nextLine());
        }
        fileScanner.close();
        return data;
    }

}
