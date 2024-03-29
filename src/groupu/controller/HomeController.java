package groupu.controller;

import groupu.model.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicReference;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
/**
 * * THis produces the gui for the home screen
 *
 * @author markopetrovic239
 * @author ds-91 *
 */
public class HomeController {

  static String GroupSelect;

  @FXML private TextField txtSearchGroups;
  @FXML private TextField txtTag;
  @FXML private Button btnInfo;
  @FXML private Button btnCreateGroup;
  @FXML private Button btnLogout;
  @FXML private Button btnSendMessage;
  @FXML private Button btnDeleteMessage;
  @FXML private TextField txtMessageTo;
  @FXML private TextField txtReplyText;
  @FXML private TextArea txtMessageBody;
  @FXML private TableView tableview;
  @FXML private TableColumn colName;
  @FXML private TableColumn colDescription;
  @FXML private ListView listviewAdmin;
  @FXML private ListView listviewJoined;
  @FXML private ListView listMessageList;
  @FXML private ListView listMessageConversation;
  @FXML private ListView listFriendsList;
  @FXML private HBox tagBox1;

  private ObservableList<ObservableList> TableViewData;
  private ObservableList<String> messageFromList;
  private ObservableList<String> messageBodyList;
  private Object select;
  private String[] tags = new String[10];
  private int tagCount = 0;

  private Group group = new Group();
  private ResultSet allGroups = group.getGroups();

  @FXML
  void initialize() {
    updateGroupTable(allGroups);
    updateFriendsList();
    updateMessageList();
    updateMyGroupsTables();

    setupPlaceholders();
    setupFriendsListContextMenu();
    setupTextFieldListeners();
    setupGroupSelectListeners();
    setupGroupContextMenu();
  }

  /** Method to create context menus for right clicking and choosing to leave the selected group. */
  public void setupGroupContextMenu() {
    ContextMenu cmUser = new ContextMenu();
    MenuItem itemLeaveGroup = new MenuItem("Leave Group");
    cmUser.getItems().add(itemLeaveGroup);
    listviewJoined.setContextMenu(cmUser);

    itemLeaveGroup.setOnAction(
        event -> {
          String selectedGroup = null;
          try {
            selectedGroup = listviewJoined.getSelectionModel().getSelectedItem().toString();
          } catch (NullPointerException e) {
            System.out.println("no group selected");
          }
          group.removeMember(Session.getInstance("").getUserName(), selectedGroup);
          updateMyGroupsTables();
        });
  }

  /** Method to set up ChangeListeners when selecting a group from the groups list. */
  private void setupGroupSelectListeners() {
    listviewAdmin.getSelectionModel().getSelectedItem();
    listviewAdmin
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            new ChangeListener<String>() {
              @Override
              public void changed(
                  ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                  select = newValue;
                  listviewJoined.getSelectionModel().select(null);
                }
              }
            });

    listviewJoined.getSelectionModel().getSelectedItem();
    listviewJoined
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            new ChangeListener<String>() {
              @Override
              public void changed(
                  ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                  select = newValue;
                  listviewAdmin.getSelectionModel().clearSelection();
                }
              }
            });
    ObservableList<TablePosition> selectedCells = tableview.getSelectionModel().getSelectedCells();
    selectedCells.addListener(
        (ListChangeListener.Change<? extends TablePosition> change) -> {
          if (selectedCells.size() > 0) {
            TablePosition selectedCell = selectedCells.get(0);
            int rowIndex = selectedCell.getRow();
            select = colName.getCellObservableValue(rowIndex).getValue();
          }
        });
  }

  /**
   * Method to set up textfield listeners on the search textfield to respond when Enter is pressed.
   */
  private void setupTextFieldListeners() {
    String[] tags = new String[20];
    txtSearchGroups.setOnKeyPressed(
        event -> {
          if (event.getCode() == KeyCode.ENTER) {
            actionSearch();
            txtSearchGroups.clear();
          }
        });

    txtTag.setOnKeyPressed(
        event -> {
          if (event.getCode() == KeyCode.ENTER) {
            actionTagSearch();
            txtTag.clear();
          }
        });
  }

  /** Method that sets up placeholder text for empty listviews on the home page. */
  public void setupPlaceholders() {
    listviewJoined.setPlaceholder(new Label("No content"));
    listviewAdmin.setPlaceholder(new Label("No content"));
    listMessageConversation.setPlaceholder(new Label("No content"));
    listMessageList.setPlaceholder(new Label("No content"));
    listFriendsList.setPlaceholder(new Label("No content"));
  }

  /** Method that updates friends list. Used when adding or removing friends. */
  public void updateFriendsList() {
    Friend f = new Friend();
    ObservableList<String> friends = FXCollections.observableArrayList();
    ArrayList<String> friendsList = f.getFriends();

    friends.addAll(friendsList);

    listFriendsList.setItems(friends);
  }

  /** Method that updates message list that is used when receiving or sending a message. */
  public void updateMessageList() {
    Message m = new Message();
    messageFromList = FXCollections.observableArrayList();
    Set<String> userSet = new LinkedHashSet<String>();
    ArrayList<String> messages = m.getAllMessagesFromUsers();

    userSet.addAll(messages);
    messageFromList.addAll(userSet);

    listMessageList.setItems(messageFromList);
  }

  /**
   * Method that populates the conversation textview when user clicks on a friend if they have
   * messages.
   */
  public void actionMessagesClicked() {
    Message m = new Message();
    ArrayList<String> messages = new ArrayList<String>();
    try {
      String clickedUser = listMessageList.getSelectionModel().getSelectedItem().toString();
      messageBodyList = m.getMessagesFromUser(clickedUser);

      listMessageConversation.setItems(messageBodyList);
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  /** Method to update the list of groups the current user has created. */
  public void updateMyGroupsTables() {
    User user = new User();

    try {
      ResultSet rsUserGroups = group.getUserGroups();
      listviewAdmin.getItems().clear();
      while (rsUserGroups.next()) {
        String current = rsUserGroups.getString("name");
        ObservableList<String> list = FXCollections.observableArrayList(current);
        listviewAdmin.getItems().addAll(list);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.out.println("Error on Building user groups table");
    }
    // Data added to joined group list
    try {
      ResultSet rUserGroups = user.getJoinedGroups();
      listviewJoined.getItems().clear();
      while (rUserGroups.next()) {
        String current2 = rUserGroups.getString("name");
        ObservableList<String> list2 = FXCollections.observableArrayList(current2);
        listviewJoined.getItems().addAll(list2);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.out.println("Error on Building user groups table");
    }
  }

  /**
   * Method that updates the users group table of their joined groups.
   *
   * @param rsGroups ResultSet of the users joined groups.
   */
  private void updateGroupTable(ResultSet rsGroups) {
    // Populating TableView from ResultSet
    TableViewData = FXCollections.observableArrayList();
    try {
      colName.setCellValueFactory(
          new Callback<
              TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(
                TableColumn.CellDataFeatures<ObservableList, String> param) {
              return new SimpleStringProperty(param.getValue().get(0).toString());
            }
          });
      colDescription.setCellValueFactory(
          new Callback<
              TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(
                TableColumn.CellDataFeatures<ObservableList, String> param) {
              return new SimpleStringProperty(param.getValue().get(1).toString());
            }
          });
      while (rsGroups.next()) {
        // Iterate Row
        ObservableList<String> row = FXCollections.observableArrayList();
        for (int i = 1; i <= rsGroups.getMetaData().getColumnCount(); i++) {
          // Iterate Column
          row.add(rsGroups.getString(i));
        }
        TableViewData.add(row);
      }
      tableview.setItems(TableViewData);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error on Building group table");
    }
  }

  /**
   * Method called when user clicks to create a new group. Opens up the scene to input group
   * details.
   */
  public void actionCreateGroup() {
    Utilities.nextScene(btnCreateGroup, "creategroup", "Create New Group");
  }

  /**
   * Method called when user wants to view group information. Uses the selected group in the table
   * and sends the user to that groups scene.
   */
  public void actionOpenGroup() {
    if (select != null) {
      GroupSelect = select.toString();
      System.out.println("view group pressed" + GroupSelect);
      Utilities.nextScene(btnInfo, "group", GroupSelect);
    }
  }

  /** Method called when users opens one of their created groups. */
  public void actionOpenUserGroups() {
    System.out.println("view group pressed");
    if (select != null) {
      GroupSelect = select.toString();
      Utilities.nextScene(btnInfo, "group", GroupSelect);
    }
  }

  /** Method called when user searches for a group based on text. */
  public void actionSearch() {
    ResultSet rsGroups = group.getSearch(txtSearchGroups.getText());
    updateGroupTable(rsGroups);
  }

  /** Method called when user clicks the log out button. */
  public void actionLogout() {
    Session.getInstance("").cleanUserSession();
    Utilities.nextScene(btnLogout, "login", "Login");
  }

  /** Method called when sending a message to another user. */
  public void actionSendMessage() {
    User u = new User();
    String toUser = txtMessageTo.getText();
    String messageBody = txtMessageBody.getText();

    if (u.checkUserExists(toUser)) {
      if (!Session.getInstance("").getUserName().equals(toUser)) {
        Message m = new Message(toUser, messageBody);
        m.sendPrivateMessage();

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setContentText("Message sent!");
        alert.show();

        txtMessageTo.clear();
        txtMessageBody.clear();
        updateMessageList();
      } else {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setContentText("You can't send a message to yourself!");
        alert.show();
      }
    } else {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setContentText("Username doesn't exist!");
      alert.show();
    }
  }

  /** Method to delete a conversation with another user from the home page. */
  public void actionDeleteMessage() {
    String username = getSelectedFromConversationList();
    Message m = new Message(username, null);
    m.deleteAllMessages();

    updateMessageList();
    listMessageConversation.getItems().clear();
  }

  /** Method to reply to a message from another user. */
  public void actionReply() {
    String toUser = listMessageList.getSelectionModel().getSelectedItem().toString();
    String body = txtReplyText.getText();

    if (!body.isEmpty()) {
      if (body.length() > 0 && body.length() <= 300) {
        Message m = new Message(toUser, body);
        m.sendPrivateMessage();

        txtReplyText.clear();
        actionMessagesClicked();
      } else {
        Alert a = new Alert(AlertType.ERROR);
        a.setContentText("Message must be between 1 and 300!");
        a.show();
      }
    } else {
      Alert a = new Alert(AlertType.ERROR);
      a.setContentText("Empty message body!");
      a.show();
    }
  }

  /** Method called when removing a friend from users friends list. */
  public void actionRemoveFriend() {
    String username = getSelectedFromFriendsList();
    if (username != null) {
      Friend f = new Friend(username);
      f.removeFriend();

      updateFriendsList();
    }
  }

  /** Method that sets up the context menu for friends list. */
  public void setupFriendsListContextMenu() {
    ContextMenu cm = new ContextMenu();
    MenuItem itemRemoveFriend = new MenuItem("Remove Friend");
    cm.getItems().add(itemRemoveFriend);
    listFriendsList.setContextMenu(cm);

    itemRemoveFriend.setOnAction(
        event -> {
          actionRemoveFriend();
        });
  }

  /**
   * Method that gets the selected friend from friends list.
   *
   * @return The selected friend from the friends listview.
   */
  public String getSelectedFromFriendsList() {
    try {
      return listFriendsList.getSelectionModel().getSelectedItem().toString();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Method that gets the selected conversation from conversation list.
   *
   * @return The selected conversation from the listview.
   */
  public String getSelectedFromConversationList() {
    try {
      return listMessageList.getSelectionModel().getSelectedItem().toString();
    } catch (Exception e) {
      return null;
    }
  }

  /** Method called to search for groups by search tags on the home page. */
  public void actionTagSearch() {
    if (tagCount < 10) {
      tags[tagCount] = txtTag.getText();

      Button btn = new Button(txtTag.getText());
      btn.setBackground(Background.EMPTY);
      btn.setOpacity(tagCount + 1);
      btn.setOnMouseClicked(
          new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
              tags[(int) btn.getOpacity() - 1] = "null";
              tagBox1.getChildren().remove(btn);
              tagCount--;
              System.out.println(tagCount);
              ResultSet tagSearch = group.tagSearch(tags);
              if (tagSearch != null) updateGroupTable(tagSearch);
              if (tagCount == 0) updateGroupTable(group.getGroups());
            }
          });

      tagBox1.getChildren().add(btn);

      ResultSet tagSearch = group.tagSearch(tags);
      if (tagSearch != null) updateGroupTable(tagSearch);

      tagCount++;
      txtTag.clear();
    }
  }
}
