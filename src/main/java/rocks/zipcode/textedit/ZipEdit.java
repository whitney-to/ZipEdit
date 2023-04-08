package rocks.zipcode.textedit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ZipEdit extends JFrame implements ActionListener, DocumentListener {
    private JTextArea area;
    private JFrame frame;
    private String filename = "untitled.txt";

    private boolean isOnSavedFile = false;
    private boolean isEdited = false;
    private File onEditingFile = null;
    // --------------------------------------------------------------------------
    public ZipEdit() {  }
    public static void main(String[] args) {
        ZipEdit runner = new ZipEdit();
        runner.run();
    }
    public void run() {
        frame = new JFrame(frameTitle());

        // Set the look-and-feel (LNF) of the application
        // Try to default to whatever the host system prefers
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ZipEdit.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Set attributes of the app window
        area = new JTextArea();
        area.getDocument().addDocumentListener(this); // added listener for document

        //Border blackline = BorderFactory.createLineBorder(Color.black);
        //area.setBorder(blackline);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        area.setText("");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(area);
        frame.setLocationRelativeTo(null);
        frame.setSize(640, 480);

        // Build the menu
        JMenuBar menus = new JMenuBar();

        JMenu menu_file = new JMenu("File");

        JMenuItem menuitem_new = new JMenuItem("New");
        JMenuItem menuitem_open = new JMenuItem("Open");
        JMenuItem menuitem_save = new JMenuItem("Save");
        JMenuItem menuitem_quit = new JMenuItem("Quit");

        menuitem_new.addActionListener(this);
        menuitem_open.addActionListener(this);
        menuitem_save.addActionListener(this);
        menuitem_quit.addActionListener(this);

        menus.add(menu_file);

        menu_file.add(menuitem_new);
        menu_file.add(menuitem_open);
        menu_file.add(menuitem_save);
        menu_file.add(menuitem_quit);

        frame.setJMenuBar(menus);

        // Build the edit menu

        JMenu edit_file = new JMenu("Edit");

        JMenuItem editMenu_cut = new JMenuItem(new DefaultEditorKit.CutAction());
        editMenu_cut.setText("Cut");

        JMenuItem editMenu_copy = new JMenuItem(new DefaultEditorKit.CopyAction());
        editMenu_copy.setText("Copy");

        JMenuItem editMenu_paste = new JMenuItem(new DefaultEditorKit.PasteAction());
        editMenu_paste.setText("Paste");

        JMenuItem editMenu_find = new JMenuItem("Find");

        editMenu_cut.addActionListener(this);
        editMenu_copy.addActionListener(this);
        editMenu_paste.addActionListener(this);
        editMenu_find.addActionListener(this);

        menus.add(edit_file);

        edit_file.add(editMenu_cut);
        edit_file.add(editMenu_copy);
        edit_file.add(editMenu_paste);
        edit_file.add(editMenu_find);

        frame.setJMenuBar(menus);


        frame.setVisible(true);

    }

    public String frameTitle() {
        return "Zip Edit ("+this.filename+")";
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        String ingest = "";
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory()+"/Desktop");
        jfc.setDialogTitle("Choose destination.");
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        String ae = e.getActionCommand();

        // OPEN
        if (ae.equals("Open")) {
            if(isEdited){
                if(save(jfc)){
                    open(jfc,ingest);
                    isEdited = false;
                }
            } else {
                open(jfc,ingest);
                isEdited = false;
            }

        // SAVE
        } else if (ae.equals("Save")) {
            if(isOnSavedFile && onEditingFile !=null){
                writeToFile(jfc);
            } else {
                save(jfc);
                isEdited = false;
                isOnSavedFile = true;
            }

        // NEW
        } else if (ae.equals("New")) {
            if(isEdited){
                save(jfc);
                isEdited = false;
            }
            area.setText("");

        // QUIT
        } else if (ae.equals("Quit")) {
            if(isEdited){
                save(jfc);
                isEdited = false;
            }
            System.exit(0);
        }
    }

    public void open(JFileChooser jfc, String ingest){
        jfc.setDialogTitle("Choose a file to open");
        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            onEditingFile = new File(jfc.getSelectedFile().getAbsolutePath());
            this.filename = jfc.getSelectedFile().getName();
            this.frame.setTitle(this.frameTitle());
            try{
                FileReader read = new FileReader(onEditingFile);
                Scanner scan = new Scanner(read);
                while(scan.hasNextLine()){
                    String line = scan.nextLine() + "\n";
                    ingest = ingest + line;
                }
                area.setText(ingest);
            }
            catch ( FileNotFoundException ex) { ex.printStackTrace(); }
        }
    }
    public boolean save(JFileChooser jfc){
        jfc.setSelectedFile( new File(filename)); // added this line
        jfc.setDialogTitle("Save your changes");
        int returnValue = jfc.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String tempFileName = jfc.getSelectedFile().getName();
            if(!tempFileName.isBlank() && !tempFileName.isEmpty()){
                this.filename = tempFileName;
            }
            this.frame.setTitle(this.frameTitle());
            writeToFile(jfc);
            return true;
        }
        return false;
    }

    public void writeToFile(JFileChooser jfc){
        try {
            onEditingFile = new File(jfc.getSelectedFile().getAbsolutePath());
            FileWriter out = new FileWriter(onEditingFile);
            out.write(area.getText());
            out.close();
        } catch (FileNotFoundException ex) {
            Component f = null;
            JOptionPane.showMessageDialog(f,"File not found.");
        } catch (IOException ex) {
            Component f = null;
            JOptionPane.showMessageDialog(f,"Error.");
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        isEdited = true;
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        isEdited = true;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        isEdited = true;
    }
}


