package groupu.controller;

import groupu.model.Group;
import groupu.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 * Controller class that handles the business logic when creating a new group.
 *
 * @author ds-91
 * @author markopetrovic239
 */
public class CreateGroupController {

  private static final int maxNameLength = 50;
  private static final int maxDescriptionLength = 200;

  private String[] tags = new String[10];
  private int tagCount = 0;

  private Group group;

  @FXML private Button btnCancel;
  @FXML private TextField txtGroupName;
  @FXML private TextArea txtDescription;
  @FXML private TextField txtTag;

  Alert alert;

  /**
   * Method called when controller is initialized. Sets up listeners to detect enter keypress for
   * logging in and sets up text field prompt text.
   */
  @FXML
  void initialize() {
    txtGroupName.setPromptText("Group Name");
    txtDescription.setPromptText("200 character description of your group");

    txtTag.setOnKeyPressed(
        event -> {
          if (event.getCode() == KeyCode.ENTER) {
            actionAddTag();
            txtTag.clear();
          }
        });

    /** * block spaces in tag field** */
    txtTag
        .textProperty()
        .addListener(
            (observable, old_value, new_value) -> {
              if (new_value.contains(" ")) txtTag.setText(old_value);
            });
  }

  /**
   * Method called when user clicks create group. Checks if the group name and description are the
   * right length. Also checks for leading and trailing spaces in both.
   */
  public void actionCreateGroup() {
    if (txtGroupName.getLength() > 0 && txtGroupName.getLength() < maxNameLength) {
      if (txtDescription.getLength() > 0 && txtDescription.getLength() < maxDescriptionLength) {
        if (!(txtGroupName.getText().startsWith(" ") || txtGroupName.getText().endsWith(" "))) {
          if (!(txtDescription.getText().startsWith(" ")
              || txtDescription.getText().endsWith(" "))) {
            Group group =
                new Group(
                    txtGroupName.getText(),
                    txtDescription.getText(),
                    Session.getInstance("").getUserName(),
                    tags);
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setContentText("Successfully created group!");
            alert.showAndWait();
            actionCancel();
          } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("Description cannot start or end with a space!");
            alert.show();
          }
        } else {
          Alert alert = new Alert(AlertType.ERROR);
          alert.setContentText("Group name cannot start or end with a space!");
          alert.show();
        }
      } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(
            "Group description must be between 1 and " + maxDescriptionLength + "!");
        alert.show();
      }

    } else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setContentText("Group name must be between 1 and " + maxNameLength + "!");
      alert.show();
      // actionCancel(actionEvent);
    }
  }

  /**
   * Method called when user clicks cancel when creating a new group. Goes to the home scene if
   * cancelled.
   */
  public void actionCancel() {
    Utilities.nextScene(btnCancel, "home", "Home");
  }

  /** Method to add searchable tags to each group. */
  public void actionAddTag() {
    for (int i = 0; i < tagCount; i++)
      if (tags[i].equals(txtTag.getText())) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText("Duplicate tag!");
        alert.show();
        return;
      }

    if (txtTag.getLength() > 0 && txtTag.getLength() < 30 && tagCount < 10) {
      tags[tagCount] = txtTag.getText();
      tagCount++;
      txtTag.clear();
    } else if (tagCount >= 10) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setContentText("Tag limit reached!");
      alert.show();
    } else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setContentText("Tag is too long! (30 characters)");
      alert.show();
    }
  }
}
