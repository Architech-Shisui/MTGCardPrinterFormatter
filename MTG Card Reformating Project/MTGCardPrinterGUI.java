import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.datatransfer.DataFlavor;

public class MTGCardPrinterGUI {
    private JFrame frame;

    public MTGCardPrinterGUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("MTG Card Printer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        setupImagePanels();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setupImagePanels() {
        JPanel mainPanel = new JPanel(new GridLayout(3, 3));
        for (int i = 0; i < 9; i++) {
            JPanel cardPanel = createCardPanel();
            mainPanel.add(cardPanel);
        }
        frame.add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createCardPanel() {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setPreferredSize(new Dimension(63, 88)); // Approximate size for MTG card

        // Panel for 'X' Button (top-right)
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("X");
        closeButton.addActionListener(e -> clearPanel(cardPanel));
        topRightPanel.add(closeButton);

        // Adding top-right panel to cardPanel
        cardPanel.add(topRightPanel, BorderLayout.NORTH);

        // Panel to hold the image and upload button
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setName("ImagePanel"); // Unique identifier
        JButton uploadButton = new JButton("Upload");
        uploadButton.addActionListener(e -> uploadImageUsingFileChooser(imagePanel));
        imagePanel.add(uploadButton, BorderLayout.CENTER);

        cardPanel.add(imagePanel, BorderLayout.CENTER);
        cardPanel.putClientProperty("imagePanel", imagePanel); // Store a reference

        cardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        cardPanel.setTransferHandler(new ImageTransferHandler(this));

        return cardPanel;
    }


    private void clearPanel(JPanel cardPanel) {
        JPanel imagePanel = (JPanel) cardPanel.getComponent(1); // Assuming imagePanel is the second component
        imagePanel.removeAll();
        JButton uploadButton = new JButton("Upload");
        uploadButton.addActionListener(e -> uploadImageUsingFileChooser(cardPanel));
        imagePanel.add(uploadButton, BorderLayout.CENTER);
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    private void uploadImageUsingFileChooser(JPanel targetPanel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            processAndDisplayImage(file, targetPanel);
        }
    }

    void processAndDisplayImage(File file, JPanel targetPanel) {
        try {
            BufferedImage originalImage = ImageIO.read(file);
            Image resizedImage = originalImage.getScaledInstance(63, 88, Image.SCALE_SMOOTH);
            ImageIcon imageIcon = new ImageIcon(resizedImage);

            // Find the imagePanel by its unique name
            JPanel imagePanel = null;
            for (Component comp : targetPanel.getComponents()) {
                if (comp instanceof JPanel && "ImagePanel".equals(comp.getName())) {
                    imagePanel = (JPanel) comp;
                    break;
                }
            }

            if (imagePanel != null) {
                imagePanel.removeAll();
                imagePanel.add(new JLabel(imageIcon));
                imagePanel.revalidate();
                imagePanel.repaint();
            } else {
                throw new IllegalStateException("ImagePanel not found in cardPanel");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(targetPanel, "Error loading image.", "Image Load Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(targetPanel, ex.getMessage(), "Layout Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ImageTransferHandler extends TransferHandler {
        private MTGCardPrinterGUI gui;

        public ImageTransferHandler(MTGCardPrinterGUI gui) {
            this.gui = gui;
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            try {
                java.util.List<File> files = (java.util.List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                JPanel cardPanel = (JPanel) support.getComponent();
            JPanel imagePanel = (JPanel) cardPanel.getClientProperty("imagePanel"); // Retrieve the reference

            if (imagePanel != null) {
                for (File file : files) {
                    gui.processAndDisplayImage(file, imagePanel);
                }
            }
            return true;
        }
    }

    // ... remaining methods and inner classes ...
}