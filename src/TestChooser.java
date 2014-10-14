import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TestChooser extends Stage {

    private ArrayList<String> issues;
    private final StackPane issuesLayout;

    public TestChooser() {
        super(StageStyle.DECORATED);
        this.initModality(Modality.WINDOW_MODAL);
        this.initOwner(Mole.primaryStage);
        issuesLayout = new StackPane();
        issuesLayout.setPadding(new Insets(10));
    }

    public void init() {
        final ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                if (toggleGroup.getSelectedToggle() != null) {
                    System.out.println(toggleGroup.getSelectedToggle().getUserData().toString());
                }
            }
        });
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(25);
        buttonsBox.getChildren().addAll(okButton, cancelButton);
        
        VBox vBox = new VBox(5);
        vBox.setPadding(new Insets(10));

        for (int i = 0; i < 5; i++) {
            String str = "iss" + i;
            vBox.getChildren().add(new IssueHBox(5, str, toggleGroup));
        }
        
        /*vBox.getChildren().addAll(new RadioButton("1"),
                new RadioButton("2"),
                new RadioButton("3"));*/
        
        vBox.getChildren().add(buttonsBox);
        
        //issuesLayout.getChildren().addAll(vBox, buttonsBox);
        
        this.setScene(new Scene(vBox, 400, 250));
        this.setTitle("Choose Issue");

        show();

    }

    public void setIssues(ArrayList<String> issues) {
        this.issues = issues;
    }

    class IssueHBox extends HBox {

        String issue;
        ToggleGroup toggleGroup;

        public IssueHBox(double d, String issue, ToggleGroup toggleGroup) {
            super(d);
            this.issue = issue;
            this.toggleGroup = toggleGroup;
            this.init();
        }

        public void init() {
            VBox vBox = new VBox();
            HBox hBox1 = new HBox(5);
            HBox hBox2 = new HBox(5);
            hBox1.getChildren().addAll(new Label("Name"),
                    new Label("Attributes"));
            hBox2.getChildren().addAll(new Label("Year"),
                    new Label("Formats"),
                    new Label("Label"),
                    new Label("Country"));
            vBox.getChildren().addAll(hBox1, hBox2);
            IssueRadioButton radioButton = new IssueRadioButton(null, issue);
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData((VBox) vBox);
            this.getChildren().addAll(radioButton);
        }
    }

    class IssueRadioButton extends RadioButton {

        String issue;

        public IssueRadioButton(String string, String issue) {
            super(string);
            this.issue = issue;
        }
    }
}
