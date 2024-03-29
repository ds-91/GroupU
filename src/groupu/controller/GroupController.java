package groupu.controller;

import groupu.model.Friend;
import groupu.model.Group;
import groupu.model.Message;
import groupu.model.Post;
import groupu.model.Report;
import groupu.model.Session;

import java.util.ArrayList;

import groupu.model.User;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;

/**
 * Controller that handles the logic for group management.
 *
 * @author markopetrovic239
 * @author ds-91
 */
public class GroupController {

  @FXML private Button btnBack;
  @FXML private Button btnPost;
  @FXML private Button btnLeaveGroup;
  @FXML private Button btnJoinGroup;
  @FXML private Button btnDeleteGroup;
  @FXML private TextArea txtPostBody;
  @FXML private ListView listPosts;
  @FXML private TabPane tabPane;
  @FXML private Tab tabAdmin;
  @FXML private Tab tabEvent;
  @FXML private Label labelEventName;
  @FXML private Label labelEventDescription;
  @FXML private Label labelEventDate;
  @FXML private TextArea txtEventDesc;
  @FXML private TextArea txtEventDate;
  @FXML private TextArea txtEventTitle;
  @FXML private Label labelGroupName;
  @FXML private Label labelGroupDescription;
  @FXML private ListView listMemberList;
  @FXML private ListView listMemberListUser;
  @FXML private TextArea txtGroupDescription;
  @FXML private TextField txtGroupTags;
  @FXML private ListView listReportList;
  @FXML private ListView listRSVP;
  @FXML private CheckBox chkRSVP;
  @FXML private AnchorPane anchorpaneInfo;

  private String groupName;
  private ArrayList<String> postList;
  private ObservableList<String> posts;
  private ObservableList<String> userList;

  private Group g = new Group();
  private Post p = new Post();

  /** */
  @FXML
  public void initialize() {
    groupName = HomeController.GroupSelect;

    setupPlaceholders();
    setupRSVPList();
    updateUserMemberList();
    updateTabsAndButtons();
    updateGroupInfo();
    updateListOfPosts();
    updateListOfUsers();
    updateListOfReports();
    updateEventInfo();
  }

  /** initialize the RSVP list and checkbox */
  public void setupRSVPList() {
    if (!g.isUserInGroup(Session.getInstance("").getUserName(), groupName)) {
      chkRSVP.setDisable(true);
    }
    if (g.isRSVP(groupName, Session.getInstance("").getUserName())) {
      chkRSVP.setSelected(true);
    }

    ObservableList<String> RSVPlist = FXCollections.observableArrayList();
    ObservableList<String> userList = g.getAllUsers(groupName);

    for (int i = 0; i < userList.size(); i++) {
      if (g.isRSVP(groupName, userList.get(i))) RSVPlist.add(userList.get(i));
    }

    listRSVP.setItems(RSVPlist);
  }

  /** Method to set placeholder text for empty list views. */
  public void setupPlaceholders() {
    listMemberListUser.setPlaceholder(new Label("No content"));
    listMemberList.setPlaceholder(new Label("No content"));
    listPosts.setPlaceholder(new Label("No content"));
    listReportList.setPlaceholder(new Label("No content"));
    listRSVP.setPlaceholder(new Label("No content"));
  }

  /** Method to update the event information labels with the current event. */
  public void updateEventInfo() {
    labelEventName.setText(g.getEventTitle(HomeController.GroupSelect));
    labelEventDescription.setText(g.getEventDescription(HomeController.GroupSelect));
    labelEventDate.setText(g.getEventDate(HomeController.GroupSelect));
  }

  /** Method that updates the listview of the currently joined members. */
  public void updateUserMemberList() {
    setupMessageContextMenu();

    ObservableList<String> userMemberList = g.getAllUsers(groupName);
    listMemberListUser.setItems(userMemberList);
  }

  /** Method that populates the reports listview with all of the current reports. */
  public void updateListOfReports() {
    ObservableList<String> reportList = Report.getAllGroupReports(groupName);
    listReportList.setItems(reportList);
  }

  public void updateTabsAndButtons() {
    if (!Session.getInstance("").getUserName().equals(g.getGroupAdmin(groupName))) {
      tabPane.getTabs().remove(tabAdmin);
      tabPane.getTabs().remove(tabEvent);
    }

    if (!g.isUserInGroup(Session.getInstance("").getUserName(), groupName)) {
      btnLeaveGroup.setDisable(true);
      btnJoinGroup.setDisable(false);

      if (Session.getInstance("").getUserName().equals(g.getGroupAdmin(groupName))) {
        btnJoinGroup.setDisable(true);
      }
    } else {
      btnLeaveGroup.setDisable(false);
      btnJoinGroup.setDisable(true);
    }
  }

  /** Method that populates the member listview with all current joined members. */
  public void updateListOfUsers() {
    ObservableList<String> userList = g.getAllUsers(groupName);
    listMemberList.setItems(userList);
  }

  /** Method that updates the labels for group name and group description on the group home page. */
  public void updateGroupInfo() {
    labelGroupName.setText(groupName);
    labelGroupDescription.setText(g.getGroupDescription(groupName));
  }

  /** Method that updates the list of all posts on the group home page. */
  public void updateListOfPosts() {
    setupPostContextMenu();

    postList = p.getPostsByGroupName(groupName);
    posts = FXCollections.observableArrayList();
    for (String s : postList) {
      posts.add(s);
    }
    listPosts.setItems(posts);
  }

  /** Method that sets up the context menu (right click) for the member list listview. */
  public void setupMessageContextMenu() {
    ContextMenu cm = new ContextMenu();
    MenuItem itemSendMessage = new MenuItem("Send Message");
    MenuItem itemAddFriend = new MenuItem("Add Friend");
    cm.getItems().add(itemSendMessage);
    cm.getItems().add(itemAddFriend);
    listMemberListUser.setContextMenu(cm);

    User u = new User();

    itemSendMessage.setOnAction(
        event -> {
          String user;
          try {
            user = listMemberListUser.getSelectionModel().getSelectedItem().toString();
          } catch (Exception e) {
            user = null;
          }

          if (user != null) {
            if (u.checkUserExists(user)) {
              TextInputDialog dialog = new TextInputDialog("");
              dialog.setTitle("Send Message");
              dialog.setHeaderText("Message user " + user);
              dialog.setContentText("Enter your message: ");

              Optional<String> messageContent = dialog.showAndWait();
              if (messageContent.isPresent()) {
                if (!messageContent.get().isEmpty()) {
                  Message m = new Message(user, messageContent.get());
                  m.sendPrivateMessage();
                  // something to confirm
                } else {
                  Alert alert = new Alert(AlertType.ERROR);
                  alert.setContentText("Can't send an empty message!");
                  alert.show();
                }
              }
            }
          } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("No user selected!");
            alert.show();
          }
        });

    itemAddFriend.setOnAction(
        event -> {
          String userToAdd = null;
          Friend friend = new Friend();
          ArrayList<String> friendsList = friend.getFriends();
          try {
            userToAdd = listMemberListUser.getSelectionModel().getSelectedItem().toString();
          } catch (Exception e) {
            userToAdd = null;
          }
          if (userToAdd != null) {
            if (u.checkUserExists(userToAdd)) {
              if (!userToAdd.equals(Session.getInstance("").getUserName())) {
                if (!friendsList.contains(userToAdd)) {
                  Friend f = new Friend(userToAdd);
                  f.addFriend();
                  // something to confirm
                } else {
                  Alert alert = new Alert(AlertType.ERROR);
                  alert.setContentText("That person is already on your friends list!");
                  alert.show();
                }
              } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setContentText("You can't add yourself as a friend!");
                alert.show();
              }
            } else {
              Alert alert = new Alert(AlertType.ERROR);
              alert.setContentText("User does not exist!?");
              alert.show();
            }
          } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("No user selected!");
            alert.show();
          }
        });
  }

  /**
   * Method that sets up the context menu (right click) for the list of posts on the group home
   * page.
   */
  public void setupPostContextMenu() {
    if (Session.getInstance("").getUserName().equals(g.getGroupAdmin(groupName))) {
      ContextMenu cm = new ContextMenu();
      MenuItem item = new MenuItem("Delete");
      cm.getItems().add(item);

      listPosts.setContextMenu(cm);

      item.setOnAction(
          event -> {
            String[] selectionText =
                listPosts.getSelectionModel().getSelectedItem().toString().split("\\: ");
            String poster = selectionText[0];
            String data = selectionText[1];

            Post post = new Post(data, poster, groupName);
            post.deletePost();

            posts.remove(poster + ": " + data);
            listPosts.refresh();
          });
    }
  }

  /** Method called when a user tries to post a message to the group. */
  public void actionPost() {
    if (txtPostBody.getText().length() > 0 && txtPostBody.getText().length() <= 300) {
      String poster = Session.getInstance("").getUserName();
      String data = txtPostBody.getText();

      p.createPost(data, poster, groupName);

      posts.add(poster + ": " + data);
      listPosts.refresh();
      txtPostBody.clear();
    }
  }

  /** Method called when user tries to join a group. */
  public void actionJoinGroup() {
    User user = new User();
    Group group = new Group(groupName);

    if (!group.getGroupAdmin(group.toString()).equals(Session.getInstance("").getUserName())) {
      user.joinGroup(group);
    }
    chkRSVP.setDisable(false);
    updateTabsAndButtons();
  }

  /** Method called when a user reports a group. */
  public void actionReportGroup() {
    Alert alert;
    Report r;

    TextInputDialog input = new TextInputDialog();
    input.setTitle("Report Group");
    input.setHeaderText("Please enter your report");

    Optional<String> report = input.showAndWait();
    if (report.isPresent()) {
      if (report.get().length() > 0 && report.get().length() <= 200) {
        r = new Report(groupName, report.get());
        r.reportGroup();

        alert = new Alert(AlertType.CONFIRMATION);
        alert.setContentText("Successfully reported!");
        alert.show();

        updateListOfReports();
      } else {
        alert = new Alert(AlertType.ERROR);
        alert.setContentText("Report length must be between 1 and 200!");
        alert.show();
      }
    }
  }

  /** Method to send the user to the previous page. */
  public void actionBack() {
    Utilities.nextScene(btnBack, "home", "Home - " + Session.getInstance("").getUserName());
  }

  /** Method called when the group admin kicks a member from the group. */
  public void actionKickMember() {
    String username = listMemberList.getSelectionModel().getSelectedItem().toString();

    g.removeMember(username, groupName);
    updateListOfUsers();
    updateUserMemberList();
  }

  /** Method called when the group admin removes a report from the report list. */
  public void actionRemoveReport() {
    Report.removeReport(groupName, listReportList.getSelectionModel().getSelectedItem().toString());

    updateListOfReports();
  }

  /** Method called when the group admin clicks the delete group button. */
  public void actionDeleteGroup() {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setHeaderText("Delete Group");
    alert.setContentText("Are you sure you want to delete this group?");

    Optional<ButtonType> result = alert.showAndWait();
    if (!result.isPresent()) {
      System.out.println("XXXXX - user exited with X");
    } else if (result.get() == ButtonType.OK) {
      g.deleteGroup(groupName);
      Utilities.nextScene(
          btnDeleteGroup, "home", "Home - " + Session.getInstance("").getUserName());
    } else if (result.get() == ButtonType.CANCEL) {
      System.out.println("XXXXX - user canceled");
    }
  }

  /** Method for the group admin to update the text of the group description. */
  public void actionUpdateDescription() {
    Alert alert;

    if (txtGroupDescription.getLength() > 0 && txtGroupDescription.getLength() <= 200) {
      if (!(txtGroupDescription.getText().startsWith(" ")
          || txtGroupDescription.getText().endsWith(" "))) {
        g.setDescription(txtGroupDescription.getText(), groupName);

        alert = new Alert(AlertType.CONFIRMATION);
        alert.setContentText("Updated group description!");
        alert.show();

        txtGroupDescription.clear();
        updateGroupInfo();
      } else {
        alert = new Alert(AlertType.ERROR);
        alert.setContentText("Description cannot start or end with a space!");
        alert.show();
      }
    } else {
      alert = new Alert(AlertType.ERROR);
      alert.setContentText("Description must be between 1 and 200 characters!");
      alert.show();
      ;
    }
  }

  /** Method called when the group admin updates the groups search tags. */
  public void actionSaveTags() {
    Alert alert;

    // Each tag is up to 30 characters, up to maybe 5? at a time? Minus commas
    if (txtGroupTags.getLength() > 0 && txtGroupTags.getLength() <= 146) {
      boolean tagsAreRightLength = true;

      String userInput = txtGroupTags.getText().trim().replaceAll("\\s", "");
      List<String> separatedInput = Arrays.asList(userInput.split("\\s*,\\s*"));

      for (String s : separatedInput) {
        if (s.length() > 30) {
          tagsAreRightLength = false;
          alert = new Alert(AlertType.ERROR);
          alert.setContentText("Tags can only be 30 characters max! (" + s + ")");
          alert.show();
        }
      }

      if (tagsAreRightLength) {
        g.updateGroupTags(groupName, separatedInput);
        alert = new Alert(AlertType.CONFIRMATION);
        alert.setContentText("Updated group tags!");
        alert.show();

        txtGroupTags.clear();
      }
    } else {
      alert = new Alert(AlertType.ERROR);
      alert.setContentText("Tag length total must be less than 146 characters!");
      alert.show();
    }
  }

  /** Method called when the user clicks the leave group button. */
  public void actionLeaveGroup() {
    String username = Session.getInstance("").getUserName();
    if (g.isUserInGroup(username, groupName)) {
      g.removeMember(username, groupName);
      Utilities.nextScene(btnLeaveGroup, "home", "Home - " + username);
    }
  }

  /** Method called when the group admin edits the groups current event description. */
  public void actionEventDesc() {
    Alert alert;

    if (txtEventDesc.getText().length() < 100) {
      g.setEventDescription(txtEventDesc.getText(), HomeController.GroupSelect);
      labelEventDescription.setText(txtEventDesc.getText());
      txtEventDesc.clear();
      alert = new Alert(AlertType.CONFIRMATION);
      alert.setContentText("Event description modified!");
      alert.show();
    } else {
      alert = new Alert(AlertType.ERROR);
      alert.setContentText("Event description can be no longer than 100 char!");
      alert.show();
    }
  }

  /** Method for editing the groups current event title. */
  public void actionNameEvent() {
    Alert alert;

    if (txtEventTitle.getText().length() < 80) {
      g.setEventTitle(txtEventTitle.getText(), HomeController.GroupSelect);
      labelEventName.setText(txtEventTitle.getText());
      txtEventTitle.clear();
      alert = new Alert(AlertType.CONFIRMATION);
      alert.setContentText("Event Title modified!");
      alert.show();
    } else {
      alert = new Alert(AlertType.ERROR);
      alert.setContentText("Event title can be no longer than 80 char!");
      alert.show();
    }
  }

  /** Method for editing the groups current event date. */
  public void actionEventDate() {
    Alert alert;

    if (txtEventDate.getText().length() < 50) {
      g.setEventDate(txtEventDate.getText(), HomeController.GroupSelect);
      labelEventDate.setText(txtEventDate.getText());
      txtEventDate.clear();
      alert = new Alert(AlertType.CONFIRMATION);
      alert.setContentText("Event meeting times/date modified!");
      alert.show();
    } else {
      alert = new Alert(AlertType.ERROR);
      alert.setContentText("Event date can be no longer than 50 char!");
      alert.show();
    }
  }

  /** Method to RSVP for group events. */
  public void actionRSVP() {
    System.out.println("check");
    if (!g.isRSVP(groupName, Session.getInstance("").getUserName())) {
      g.setRSVP(groupName);
      setupRSVPList();
    }

    if (!chkRSVP.isSelected()) {
      g.removeRSVP(groupName);
      setupRSVPList();
    }
  }
}
