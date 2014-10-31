import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.swing.filechooser.FileSystemView;
import org.controlsfx.control.CheckTreeView;

public class DirTreeView extends Stage {

    private CustomCheckTreeView cctv;

    //private CustomCheckTreeView checkTreeView = null;
    public DirTreeView() {
    }

    public void drawGUI() throws FileNotFoundException, IOException {
        cctv = buildFileSystemBrowser();
        VBox vBox = new VBox();
        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(5));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(20);
        Button okButton = new Button("Ok");
        okButton.setMinWidth(60);
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println(cctv.getCheckModel().getCheckedItems());
                DirTreeView.this.close();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setMinWidth(60);
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                DirTreeView.this.close();
            }
        });

        buttonBox.getChildren().addAll(okButton, cancelButton);

        vBox.getChildren().addAll(cctv, buttonBox);

        setScene(new Scene(vBox));
        show();
    }

    private CustomCheckTreeView buildFileSystemBrowser() throws IOException {
        CheckBoxTreeItem<File> root = new CheckBoxTreeItem<>();

        for (File drive : findDrives()) {
            root.getChildren().add(createNode(drive, true));
        }

        return new CustomCheckTreeView(root);
    }

    private CustomCheckBoxTreeItem createNode(final File f, boolean b) throws IOException {
        return new CustomCheckBoxTreeItem(f, b);
    }

    /*public CustomCheckBoxTreeItem initCheckTreeItems() throws FileNotFoundException, IOException {
     CustomCheckBoxTreeItem root = createNode(new File("/"), false);
     //root.setExpanded(true);
     for (File drive : findDrives()) {
     root.getChildren().add(createNode(drive, true));
     }
     return root;
     }*/
    private ArrayList<File> findDrives() {
        File[] drives = File.listRoots();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        ArrayList<File> drivesList = new ArrayList<>();
        if (drives != null && drives.length > 0) {
            for (File aDrive : drives) {
                if (fsv.getSystemTypeDescription(aDrive).equals("Локальный диск")
                        || fsv.getSystemTypeDescription(aDrive).equals("Local drive")) {
                    drivesList.add(aDrive);
                }
            }
        }
        return drivesList;
    }

    class CustomCheckTreeView extends CheckTreeView {

        public CustomCheckTreeView() {
        }

        public CustomCheckTreeView(CheckBoxTreeItem root) {
            super(root);
            setShowRoot(false);
            initModel();
        }

        private void initModel() {
            this.getCheckModel().getCheckedItems().addListener(new ListChangeListener<TreeItem<File>>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends TreeItem<File>> c) {
                    //System.out.println(getCheckModel().getCheckedItems());
                }
            });
        }
    }

    class CustomCheckBoxTreeItem extends CheckBoxTreeItem<File> {

        private File dir;
        private boolean isLeaf;

        private boolean isFirstTimeChildren = true;
        private boolean isFirstTimeLeaf = true;

        public CustomCheckBoxTreeItem() {
            super();

        }

        public CustomCheckBoxTreeItem(File value, boolean isDrive) throws FileNotFoundException {
            super(value);
            ImageView imgView = new ImageView();
            Image icon = null;
            if (isDrive) {
                icon = new Image(new FileInputStream("hdd.png"));
            } else {
                icon = new Image(new FileInputStream("folder.png"));
            }
            if (icon != null) {
                imgView.setImage(icon);
                setGraphic(imgView);
            }

            dir = (File) value;
            //setTitle(value.getName());
        }

        @Override
        public ObservableList<TreeItem<File>> getChildren() {
            if (isFirstTimeChildren) {
                try {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                } catch (IOException ex) {
                    Logger.getLogger(DirTreeView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return super.getChildren();
        }

        @Override
        public boolean isLeaf() {
            if (isFirstTimeLeaf) {
                isFirstTimeLeaf = false;
                File f = (File) getValue();
                isLeaf = f.isFile();
            }

            return isLeaf;
        }

        private ObservableList<TreeItem<File>> buildChildren(CustomCheckBoxTreeItem treeItem) throws IOException {
            File f = treeItem.getValue();
            if (f != null && f.isDirectory()) {
                File[] files = f.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return new File(dir, name).isDirectory();
                    }
                });
                if (files != null) {
                    ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
                    //FileSystemView fsv = FileSystemView.getFileSystemView();
                    for (File childFile : files) {

                            //if (fsv.isDrive(childFile)) {
                        // children.add(createNode(childFile, true));
                        //} else {
                        children.add(createNode(childFile, false));
                            //}

                    }

                    return children;
                }
            }
            return FXCollections.emptyObservableList();
        }

    }
    /*
     private CustomCheckBoxTreeItem createNode(final File f, boolean isDrive) throws IOException {
     return new CustomCheckBoxTreeItem(f, isDrive) {
     private boolean isLeaf;
     private boolean isFirstTimeChildren;
     private boolean isFirstTimeLeaf;

     @Override
     public ObservableList<TreeItem<File>> getChildren() {
     if (isFirstTimeChildren) {
     isFirstTimeChildren = false;
     try {
     super.getChildren().setAll(buildChildren(this));
     } catch (IOException ex) {
     Logger.getLogger(DirTreeView.class.getName()).log(Level.SEVERE, null, ex);
     }
     }
     return super.getChildren();
     }

     @Override
     public boolean isLeaf() {
     if (isFirstTimeLeaf) {
     isFirstTimeLeaf = false;
     File f = (File) getValue();
     isLeaf = f.isFile();
     }
     return super.isLeaf();
     }

     private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) throws IOException {
     File f = TreeItem.getValue();
     if (f != null && f.isDirectory()) {
     File[] files = f.listFiles();
     if (files != null) {
     ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();

     for (File childFile : files) {
     children.add(createNode(childFile, false));
     }

     return children;
     } else {
     return FXCollections.emptyObservableList();
     }
     } else {
     return FXCollections.emptyObservableList();
     }

                
     }

     };
     }*/
}
