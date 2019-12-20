package groupu.controller;

import groupu.model.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/** Controller class that handles the logic for registering a new user. */
public class RegisterController {

  private static final int minUsernameSize = 3;
  private static final int maxUsernameSize = 20;
  private static final int minPassSize = 6;
  private static final int maxPassSize = 30;

  @FXML private Button btnBack;
  @FXML private TextField txtUsername;
  @FXML private TextField txtPassword;

  /** Method that is called when clicking the register button. */
  public void actionRegister() {
    User u = new User();
    Alert alert;

    boolean exists = u.checkUserExists(txtUsername.getText());

    if (!exists) {
      if (txtUsername.getLength() >= minUsernameSize
          && txtUsername.getLength() <= maxUsernameSize) {
        if (txtPassword.getLength() >= minPassSize && txtPassword.getLength() <= maxPassSize) {
          if (!txtUsername.getText().contains(" ")) {
            if (!txtPassword.getText().contains(" ")) {
              User user = new User(txtUsername.getText(), txtPassword.getText());
              user.createUser();
              actionBack();
            } else {
              alert = new Alert(AlertType.ERROR);
              alert.setContentText("Password cannot have spaces!");
              alert.show();
            }
          } else {
            alert = new Alert(AlertType.ERROR);
            alert.setContentText("Username cannot have spaces!");
            alert.show();
          }
        } else {
          alert = new Alert(AlertType.ERROR);
          alert.setContentText("Password must be between 6 and 30!");
          alert.show();
        }
      } else {
        alert = new Alert(AlertType.ERROR);
        alert.setContentText("Username must between 3 and 20!");
        alert.show();
      }
    } else {
      alert = new Alert(AlertType.ERROR);
      alert.setContentText("Username already exists!");
      alert.show();
    }
  }

  /** Method that transitions to the previous scene (login). */
  public void actionBack() {
    Utilities.nextScene(btnBack, "login", "Login");
  }
}
