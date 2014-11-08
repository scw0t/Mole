package View;

import View.MainGUI;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.swing.filechooser.FileSystemView;
import org.controlsfx.control.CheckTreeView;

public class DirTreeView extends Stage {

    private CustomCheckTreeView cctv;

    public DirTreeView() {
        setResizable(false);
    }

    public void drawGUI() throws FileNotFoundException, IOException {
        cctv = buildFileSystemBrowser();
        
        //Кнопка Ok
        Button okButton = new Button("Ok");
        okButton.setMinWidth(60);
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Получаем список выбранных папок
                if (!MainGUI.dirList.isEmpty()) {
                    MainGUI.dirList.clear();
                }
                
                final Iterator<CustomCheckBoxTreeItem> iterator = cctv.getCheckModel().getCheckedItems().iterator();
                
                while (iterator.hasNext()) {
                    CustomCheckBoxTreeItem next = iterator.next();
                    MainGUI.dirList.add((File) next.getValue());
                }
                
                DirTreeView.this.close();
            }
        });

        //Кнопка Cancel
        Button cancelButton = new Button("Cancel");
        cancelButton.setMinWidth(60);
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirTreeView.this.close();
            }
        });
        
        HBox buttonBox = new HBox();
        //buttonBox.setStyle("-fx-border-color: black;");
        buttonBox.setPadding(new Insets(5));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(20);
        buttonBox.getChildren().addAll(okButton, cancelButton);

        VBox vBox = new VBox();
        //vBox.setStyle("-fx-border-color: red;");
        vBox.setMaxHeight(Double.MAX_VALUE);
        vBox.getChildren().addAll(cctv, buttonBox);
        
        BorderPane bp = new BorderPane(vBox);
        bp.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        //bp.setPrefSize(BorderPane.USE_COMPUTED_SIZE, BorderPane.USE_COMPUTED_SIZE);
        //bp.setScaleShape(true);
        
        Scene scene = new Scene(bp, 500, 700);
        setScene(scene);
        show();
    }
    
    //Возвращаем список локальных дисков
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

    //Формируем дерево папок
    private CustomCheckTreeView buildFileSystemBrowser() throws IOException {
        CheckBoxTreeItem<File> root = new CheckBoxTreeItem<>();

        for (File drive : findDrives()) {
            root.getChildren().add(createNode(drive, true));
        }
        
        return new CustomCheckTreeView(root);
    }

    //Формируем узлы дерева
    private CustomCheckBoxTreeItem createNode(final File f, boolean b) throws IOException {
        return new CustomCheckBoxTreeItem(f, b);
    }

    //Кастомный TreeView
    class CustomCheckTreeView extends CheckTreeView {

        public CustomCheckTreeView(CheckBoxTreeItem root) {
            super(root);
            setShowRoot(false);
            setPrefHeight(660);
            //Настраиваем отображаемые имена папок
            setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {

                @Override
                public TreeCell<File> call(TreeView<File> p) {
                    return new CheckBoxTreeCell<File>() {
                        @Override
                        public void updateItem(File item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setText(null);
                            } else {
                                FileSystemView fsv = FileSystemView.getFileSystemView();
                                String name = fsv.isDrive(item) ? fsv.getSystemDisplayName(item) : getItem().getName();
                                setText(getItem() == null ? "" : name);
                            }
                        }
                    };
                }
                
            });

            //initModel();
        }
        
        //Отлавливание изменения списка выбранных элементов
        //Может быть пригодится для потоковой фильтрации нужных папок
        private void initModel() {
            this.getCheckModel().getCheckedItems().addListener(new ListChangeListener<TreeItem<File>>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends TreeItem<File>> c) {
                    //System.out.println(getCheckModel().getCheckedItems());
                }
            });
        }
    }

    // Кастомизированный TreeViewItem
    class CustomCheckBoxTreeItem extends CheckBoxTreeItem {

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
                icon = new Image(new FileInputStream("hp_hdd.png"));
            } else {
                icon = new Image(new FileInputStream("opened_folder.png"));
            }
            if (icon != null) {
                imgView.setImage(icon);
                setGraphic(imgView);
            }
        }

        @Override
        public ObservableList<TreeItem<String>> getChildren() {
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

        private ObservableList<TreeItem> buildChildren(CustomCheckBoxTreeItem treeItem) throws IOException {
            File f = (File) treeItem.getValue();
            if (f != null && f.isDirectory()) {
                File[] files = f.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return new File(dir, name).isDirectory();
                    }
                });
                if (files != null) {
                    ObservableList<TreeItem> children = FXCollections.observableArrayList();
                    for (File childFile : files) {
                        children.add(createNode(childFile, false));
                    }
                    return children;
                }
            }
            return FXCollections.emptyObservableList();
        }

        public void setDir(File dir) {
            this.dir = dir;
        }

        public File getDir() {
            return dir;
        }

    } 
}
