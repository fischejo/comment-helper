import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jsoup.Jsoup;

public class JWindow extends JFrame implements TreeSelectionListener, KeyEventDispatcher {
	private static final long serialVersionUID = 1L;
	
	private JTextPane textPane;
	private JTree tree;
	private ArrayList<String> selection;
	private Clipboard clipboard;
	
	private File[] files;
	DefaultMutableTreeNode rootNode;
	
	public JWindow(File[] files) {
		super();
		this.files = files;
		
		// create title
		setTitle(createTitle(files));
		
		selection = new ArrayList<String>();
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		// Create the nodes.
		rootNode = new DefaultMutableTreeNode();
		createNodes(rootNode, files);

		// Create a tree that allows multiple selection at a time.
		tree = new JTree(rootNode);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		tree.setRootVisible(false);
		tree.setRowHeight(20);
		tree.setBorder(new EmptyBorder(5,5, 5, 5));

		tree.setSelectionModel( new LeafTreeSelectionModel());
		tree.setToggleClickCount(0); // disable collapse nodes

		// expand all
		for (int i = 0; i < tree.getRowCount(); i++) {
		    tree.expandRow(i);
		}
	    
		// Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);

		 //Create the text viewing pane. 
		textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setEditable(false);
		
		// Create the text scroll pane.
		JScrollPane textView = new JScrollPane(textPane);

		// Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(treeView);
		splitPane.setBottomComponent(textView);

		Dimension minimumSize = new Dimension(400, 100);
		textView.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(500);
		splitPane.setPreferredSize(new Dimension(800, 600));

		// Add the split pane to this panel.
		add(splitPane);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		// register keyboard listener
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(this);
	}


	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node;
		for(TreePath path : e.getPaths()) {
			node = (DefaultMutableTreeNode) path.getLastPathComponent();
			
			// node is null or a parent -> next path
			if(node == null || !node.isLeaf()) 
				continue;
			
			String line = (String) node.getUserObject();
			
			if(e.isAddedPath(path)) {
				selection.add(line);
			} else {
				selection.remove(line);
			}
		}
		
		
    	// create string from selection
		TransferDocument doc = new TransferDocument();
        for (String line : selection) {
        	doc.append(line);
        }
    
        // publish in text panel.
        textPane.setText( doc.html());
        
        // put to clipboard
		clipboard.setContents (doc, null);
	}

	private String createTitle(File[] files) {
		String title = "Comment-Helper ";
		for(File file : files) {
			title +="["+file.getName() +"]";
		}
		return title;
	}
	

	private void createNodes(DefaultMutableTreeNode top, File[] files) {
		// actual opened category
		DefaultMutableTreeNode category = null; 
		
		// actual child
		DefaultMutableTreeNode child = null;
		
		// hashmap of all categories to void duplicates.
		HashMap<String,DefaultMutableTreeNode> categories = 
				new HashMap<String,DefaultMutableTreeNode>();

		
		for (File file : files) {
    		// load items
            try {
            	BufferedReader br = new BufferedReader(new FileReader(file));
                String line = null;
                while ((line = br.readLine()) != null) {
                	// remove empty lines
                	if (!line.replaceAll("\\s+","").isEmpty()) {
                		
                		String trim = line.trim();
                		if (trim.startsWith("[") && trim.endsWith("]")) {
                			// line is a category header
                			String title = line.substring(1, line.length()-1);
                			
                			if(categories.containsKey(title)) {
                				// reuse old category
                				category = categories.get(title);
                			} else {
                				// create new category
                				category = new DefaultMutableTreeNode(title);
                				top.add(category);
                				categories.put(title, category);
                			}
                		} else {
                			// convert line to html
                			line = Jsoup.parse(line).html();
                		
                			// new child
                			child = new DefaultMutableTreeNode(line);
                			if(category != null) {
                				category.add(child);
                			} else {
                				top.add(child);
                			}
                		}
                	}
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
	}


	
	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Create file chooser
				JFileChooser chooser = new JFileChooser(new File("."));
				chooser.setFileFilter(new FileNameExtensionFilter(".txt-Datein", "txt"));
				chooser.setMultiSelectionEnabled(true);
			
				// Show dialog
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					// Create and set up the window.
					File[] files = chooser.getSelectedFiles();
					JWindow frame = new JWindow(files);
					frame.pack();
					frame.setVisible(true);
				}
			}
		});
	}


	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// F5 -> update nodes
		if(event.getKeyCode() == KeyEvent.VK_F5 && event.getID() == KeyEvent.KEY_RELEASED) {
			// remove all children
			rootNode.removeAllChildren();
			
			// recreate nodes
			createNodes(rootNode, files);
			
			// reload nodes
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			model.reload();
			
			// expand all rows again
			for (int i = 0; i < tree.getRowCount(); i++) {
			    tree.expandRow(i);
			}
			return true;
		}
		return false;
	}
}