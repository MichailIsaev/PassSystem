package Desktop;


import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

/*
* Предназначен для реализации GUI к серверу , посредством Swing - технологий .
*/

public class Frame1 extends JFrame {
    private JPanel mainPanel;
    private JButton createButton;
    private JButton addPhoto;
    private JLabel statusLabel;
    private JTextField nameTextField1;
    private JTextField commentTextField2;
    private JTextField idTextField;
    private JLabel resultLabel;
    private String id, name, comment;

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    private byte[] photo;
    private boolean status;

    public Frame1() {
        setContentPane(mainPanel);
        setVisible(true);
        setSize(300, 250);
        setResizable(false);
        status = false;
        createButton.addActionListener(e -> {
            /*
            * Реализация формирования объекта для отправки данных к БД.
            * Происходит перевод   выбранного пользователем фото в массив байтов  , его упаковка  ,  для последующей его отправки.
            */
            id = idTextField.getText();
            name = nameTextField1.getText();
            comment = commentTextField2.getText();
            JFileChooser fileopen = new JFileChooser();
            int ret = fileopen.showDialog(null, "Открыть файл");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage image = null;
            try {
                image = ImageIO.read(fileopen.getSelectedFile());
                ImageIO.write(image, "jpeg", baos);
                baos.flush();
                photo = baos.toByteArray();
                System.out.println(photo.length);
                System.out.println(photo[0] + "dfdfd" + photo[photo.length - 1]);
                String base64String = Base64.encode(baos.toByteArray());
                baos.close();
                byte[] resByteArray = Base64.decode(base64String);
                System.out.println(resByteArray[0] + " --- " + resByteArray[resByteArray.length - 1] + " --- " + resByteArray.length);

            } catch (IOException e1) {
                e1.printStackTrace();
            }
            status = true;

        });

    }

    public void setStatus() {
        statusLabel.setText("Connected");
    }

    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }


    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setResultLabel(String message) {
        this.resultLabel.setText(message);
    }


}


