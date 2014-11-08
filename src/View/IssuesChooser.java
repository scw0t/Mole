package View;

import Entities.Issue;
import Gears.Mole;
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class IssuesChooser extends Stage {

    private ArrayList<Issue> issues;
    private Button okButton;
    private Button cancelButton;
    private Issue selectedIssue;

    public IssuesChooser() {
        super(StageStyle.DECORATED);
        this.initModality(Modality.WINDOW_MODAL);
        this.initOwner(Mole.primaryStage);
    }

    public void init() {
        final ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                if (toggleGroup.getSelectedToggle() != null) {
                    selectedIssue = (Issue) toggleGroup.getSelectedToggle().getUserData();
                    System.out.println(getSelectedIssue().getIssueLabel());
                }
            }
        });
        okButton = new Button("OK");
        cancelButton = new Button("Cancel");
        
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(25);
        buttonsBox.getChildren().addAll(getOkButton(), getCancelButton());
        
        VBox vBox = new VBox(5);
        vBox.setPadding(new Insets(10));
        
        ScrollPane scrollPane = new ScrollPane();

        for (Issue issue : issues) {
            
            vBox.getChildren().add(new IssueHBox(5, issue, toggleGroup));
        }
        
        vBox.getChildren().add(buttonsBox);
        
        scrollPane.setContent(vBox);
        
        this.setScene(new Scene(scrollPane, 800, 600));
        this.setTitle("Choose Issue");
    }

    public void setIssues(ArrayList<Issue> issues) {
        this.issues = issues;
    }

    public Button getOkButton() {
        return okButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Issue getSelectedIssue() {
        return selectedIssue;
    }

    class IssueHBox extends HBox {

        Issue issue;
        ToggleGroup toggleGroup;

        public IssueHBox(double d, Issue issue, ToggleGroup toggleGroup) {
            super(d);
            this.issue = issue;
            this.toggleGroup = toggleGroup;
            this.init();
        }

        
        public void init() {
            VBox vBox = new VBox();
            HBox hBox1 = new HBox(5);
            HBox hBox2 = new HBox(5);
            hBox1.getChildren().addAll(new Label(issue.getIssueName()),
                    new Label(issue.getIssueAttributes()));
            hBox2.getChildren().addAll(new Label(issue.getIssueYear()),
                    new Label(issue.getIssueFormats()),
                    new Label(issue.getIssueLabel() + " / " + issue.getCatNumber()),
                    new Label(issue.getIssueCountries()));
            vBox.getChildren().addAll(hBox1, hBox2);
            IssueRadioButton radioButton = new IssueRadioButton(null, issue);
            radioButton.setUserData((Issue) issue);
            radioButton.setToggleGroup(toggleGroup);
            this.getChildren().addAll(radioButton, vBox);
        }
    }

    class IssueRadioButton extends RadioButton {

        Issue issue;

        public IssueRadioButton(String string, Issue issue) {
            super(string);
            this.issue = issue;
        }
    }
}
