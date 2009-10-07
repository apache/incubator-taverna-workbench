package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.User;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;



/*
 * @author Sergejs Aleksejevs
 */
public class MyStuffSidebarPanel extends JPanel implements ActionListener {
  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;

  // main components of the SidebarPanel
  private JPanel jpMyProfileBox;
  private JPanel jpMyFriendsBox;
  private JPanel jpMyGroupsBox;
  private JPanel jpMyFavouritesBox;
  private JPanel jpMyTagsBox;
  private JButton bLogout;
  protected JButton bRefreshMyStuff;

  // icons which are used in several places in the sidebar
  private ImageIcon iconUser;
  private ImageIcon iconLogout;

  public MyStuffSidebarPanel(MainComponent component,
	  MyExperimentClient client, Logger logger) {
	super();

	// set main variables to ensure access to myExperiment, logger and the
	// parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	// prepare icons
	iconUser = new ImageIcon(MyExperimentPerspective
		.getLocalIconURL(Resource.USER));
	iconLogout = new ImageIcon(MyExperimentPerspective
		.getLocalResourceURL("logout_icon"));

	// add elements of the sidebar
	this.setLayout(new GridBagLayout());
	GridBagConstraints gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.NORTHWEST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
	gbConstraints.weightx = 1;
	gbConstraints.gridx = 0;

	gbConstraints.gridy = 0;
	jpMyProfileBox = createMyProfileBox();
	this.add(jpMyProfileBox, gbConstraints);

	gbConstraints.gridy = 1;
	jpMyFriendsBox = createMyFriendsBox();
	this.add(jpMyFriendsBox, gbConstraints);

	gbConstraints.gridy = 2;
	jpMyGroupsBox = createMyGroupsBox();
	this.add(jpMyGroupsBox, gbConstraints);

	gbConstraints.gridy = 3;
	jpMyFavouritesBox = createMyFavouritesBox();
	repopulateFavouritesBox();
	this.add(jpMyFavouritesBox, gbConstraints);

	gbConstraints.gridy = 4;
	jpMyTagsBox = createMyTagsBox();
	this.add(jpMyTagsBox, gbConstraints);

	// report that this component has been loaded
	pluginMainComponent.getMyStuffTab().cdlComponentLoadingDone.countDown();
  }

  // creates a JPanel displaying the currently logged in user, logout button,
  // etc
  private JPanel createMyProfileBox() {
	JPanel jpProfile = new JPanel();
	jpProfile.setMaximumSize(new Dimension(1024, 0)); // HACK: this is to make
													  // sure that profile box
													  // won't be stretched
	jpProfile.setLayout(new BoxLayout(jpProfile, BoxLayout.X_AXIS));

	JPanel jpAvatar = new JPanel();
	jpAvatar.setLayout(new BoxLayout(jpAvatar, BoxLayout.Y_AXIS));

	User currentUser = this.myExperimentClient.getCurrentUser();
	ImageIcon userAvatar = currentUser.getAvatar();
	JLabel jlUserAvatar = new JLabel("No Profile Picture Found");
	if (userAvatar != null)
	  jlUserAvatar = new JLabel(Util.getResizedImageIcon(userAvatar, 80, 80));

	jlUserAvatar.setAlignmentX(LEFT_ALIGNMENT);
	jpAvatar.add(jlUserAvatar);

	JClickableLabel jclUserName = new JClickableLabel(currentUser.getName(),
		"preview:" + Resource.USER + ":" + currentUser.getURI(),
		pluginMainComponent.getPreviewBrowser(), this.iconUser);
	jclUserName.setAlignmentX(LEFT_ALIGNMENT);
	jpAvatar.add(jclUserName);

	jpProfile.add(jpAvatar);
	
	bLogout = new JButton("Logout", iconLogout);
	bLogout.addActionListener(this);

	bRefreshMyStuff = new JButton("Refresh", WorkbenchIcons.refreshIcon);
	bRefreshMyStuff.addActionListener(this.pluginMainComponent.getMyStuffTab());

	JPanel jpButtons = new JPanel();
	jpButtons.add(bLogout);
	jpButtons.add(bRefreshMyStuff);

	jpProfile.add(jpButtons);

	jpProfile.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
			" My Profile "), BorderFactory.createEmptyBorder(1, 8, 8, 5)));

	return (jpProfile);
  }

  // creates a JPanel that displays a list of all friends of the current user
  private JPanel createMyFriendsBox() {
	JPanel jpFriends = new JPanel();
	jpFriends.setLayout(new BoxLayout(jpFriends, BoxLayout.Y_AXIS));

	// iterate through all friends and add all to the panel
	Iterator<HashMap<String, String>> iFriends = this.myExperimentClient
		.getCurrentUser().getFriends().iterator();
	if (iFriends.hasNext()) {
	  while (iFriends.hasNext()) {
		HashMap<String, String> hmCurFriend = iFriends.next();
		jpFriends.add(new JClickableLabel(hmCurFriend.get("name"), "preview:"
			+ Resource.USER + ":" + hmCurFriend.get("uri"), pluginMainComponent
			.getPreviewBrowser(), this.iconUser));
	  }
	} else {
	  // known not to have any friends
	  JLabel lNone = new JLabel("None");
	  lNone.setFont(lNone.getFont().deriveFont(Font.ITALIC));
	  lNone.setForeground(Color.GRAY);
	  jpFriends.add(lNone);
	}

	jpFriends.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
			" My Friends "), BorderFactory.createEmptyBorder(1, 8, 8, 5)));

	return (jpFriends);
  }

  // generates a JPanel that displays a list of groups for current user
  // when they are logged in
  private JPanel createMyGroupsBox() {
	JPanel jpGroups = new JPanel();

	jpGroups.setLayout(new BoxLayout(jpGroups, BoxLayout.Y_AXIS));

	// prepare the icon for groups
	ImageIcon iconGroup = new ImageIcon(MyExperimentPerspective
		.getLocalIconURL(Resource.GROUP));

	// iterate through all groups and add all to the panel
	Iterator<HashMap<String, String>> iGroups = this.myExperimentClient
		.getCurrentUser().getGroups().iterator();
	if (iGroups.hasNext()) {
	  while (iGroups.hasNext()) {
		HashMap<String, String> hmCurGroup = iGroups.next();
		jpGroups.add(new JClickableLabel(hmCurGroup.get("name"), "preview:"
			+ Resource.GROUP + ":" + hmCurGroup.get("uri"), pluginMainComponent
			.getPreviewBrowser(), iconGroup));
	  }
	} else {
	  // known not to have any groups
	  JLabel lNone = new JLabel("None");
	  lNone.setFont(lNone.getFont().deriveFont(Font.ITALIC));
	  lNone.setForeground(Color.GRAY);
	  jpGroups.add(lNone);
	}

	jpGroups.setBorder(BorderFactory.createCompoundBorder(BorderFactory
		.createTitledBorder(BorderFactory.createEtchedBorder(), " My Groups "),
		BorderFactory.createEmptyBorder(1, 8, 8, 5)));

	return (jpGroups);
  }

  // generates a JPanel that displays a list of favourite items for current user
  // when they are logged in
  private JPanel createMyFavouritesBox() {
	JPanel jpFavourites = new JPanel();

	jpFavourites.setLayout(new BoxLayout(jpFavourites, BoxLayout.Y_AXIS));
	jpFavourites.setBorder(BorderFactory.createCompoundBorder(BorderFactory
		.createTitledBorder(BorderFactory.createEtchedBorder(),
			" My Favourites "), BorderFactory.createEmptyBorder(1, 8, 8, 5)));

	return (jpFavourites);
  }

  public void repopulateFavouritesBox() {
	this.jpMyFavouritesBox.removeAll();

	// iterate through all favourites and add all to the panel
	Iterator<Resource> iFavourites = this.myExperimentClient.getCurrentUser()
		.getFavourites().iterator();
	if (iFavourites.hasNext()) {
	  while (iFavourites.hasNext()) {
		Resource rFavourite = iFavourites.next();
		this.jpMyFavouritesBox.add(new JClickableLabel(rFavourite.getTitle(),
			"preview:" + rFavourite.getItemType() + ":" + rFavourite.getURI(),
			pluginMainComponent.getPreviewBrowser(), new ImageIcon(
				MyExperimentPerspective.getLocalIconURL(rFavourite
					.getItemType()))));
	  }
	} else {
	  // known not to have any favourites
	  JLabel lNone = new JLabel("None");
	  lNone.setFont(lNone.getFont().deriveFont(Font.ITALIC));
	  lNone.setForeground(Color.GRAY);
	  this.jpMyFavouritesBox.add(lNone);
	}
  }

  // creates a Panel that shows all tags of the current user
  private JPanel createMyTagsBox() {
	JPanel jpTags = new JPanel();
	jpTags.setLayout(new BoxLayout(jpTags, BoxLayout.Y_AXIS));

	// prepare the icon for tags
	ImageIcon iconTag = new ImageIcon(MyExperimentPerspective
		.getLocalIconURL(Resource.TAG));

	// iterate through all tags and add all to the panel
	Iterator<HashMap<String, String>> iTags = this.myExperimentClient
		.getCurrentUser().getTags().iterator();
	if (iTags.hasNext()) {
	  while (iTags.hasNext()) {
		String strCurTag = iTags.next().get("name");
		jpTags.add(new JClickableLabel(strCurTag, "tag:" + strCurTag,
			pluginMainComponent.getPreviewBrowser(), iconTag));
	  }
	} else {
	  // known not to have any tags
	  JLabel lNone = new JLabel("None");
	  lNone.setFont(lNone.getFont().deriveFont(Font.ITALIC));
	  lNone.setForeground(Color.GRAY);
	  jpTags.add(lNone);
	}

	jpTags.setBorder(BorderFactory.createCompoundBorder(BorderFactory
		.createTitledBorder(BorderFactory.createEtchedBorder(), " My Tags "),
		BorderFactory.createEmptyBorder(1, 8, 8, 5)));

	return (jpTags);
  }

  public JPanel getMyProfileBox() {
	return (this.jpMyProfileBox);
  }

  // listener of button clicks in the sidebar
  public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(bLogout)) {
	  // logout button was clicked

	  try {
		// "forget" login details
		this.myExperimentClient.doLogout();
	  } catch (Exception ex) {
		logger
			.error("Error while trying to logout from myExperiment, exception:\n"
				+ ex);
	  }

	  // repaint "myStuff" tab to display the login box again
	  this.pluginMainComponent.getStatusBar().setStatus(
		  this.pluginMainComponent.getMyStuffTab().getClass().getName(),
		  "Logging out");
	  this.pluginMainComponent.getMyStuffTab()
		  .createAndInitialiseInnerComponents();
	  this.pluginMainComponent.getMyStuffTab().revalidate();
	  this.pluginMainComponent.getMyStuffTab().repaint();
	  this.pluginMainComponent.getStatusBar().setStatus(
		  this.pluginMainComponent.getMyStuffTab().getClass().getName(), null);
	  this.pluginMainComponent.getStatusBar().setCurrentUser(null);

	  // remove "My Tags" from the tags browser tab and rerun last searches (tag
	  // & keyword)
	  // so that any "private" search results won't get shown anymore
	  this.pluginMainComponent.getTagBrowserTab().setMyTagsShown(false);
	  this.pluginMainComponent.getTagBrowserTab().getTagSearchResultPanel()
		  .clear();
	  this.pluginMainComponent.getTagBrowserTab().rerunLastTagSearch();

	  this.pluginMainComponent.getSearchTab().getSearchResultPanel().clear();
	  this.pluginMainComponent.getSearchTab().rerunLastSearch();

	  // also, update another tabs, so that they don't display any 'private'
	  // content?
	  // TODO
	}
  }

}
