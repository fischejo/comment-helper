import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

public class LeafTreeSelectionModel extends DefaultTreeSelectionModel {
	private static final long serialVersionUID = 1L;

	@Override
    public void setSelectionPath(TreePath path) {
        if (canPathBeAdded(path)) {
            super.setSelectionPath(path);
        }
    }

    @Override
    public void setSelectionPaths(TreePath[] paths) {
        paths = getFilteredPaths(paths);
        super.setSelectionPaths(paths);
    }

    @Override
    public void addSelectionPath(TreePath path) {
        if (canPathBeAdded(path)) {
            super.addSelectionPath(path);
        }
    }

    @Override
    public void addSelectionPaths(TreePath[] paths) {
        paths = getFilteredPaths(paths);
        super.addSelectionPaths(paths);
    }
    
    private boolean canPathBeAdded(TreePath treePath) {
    	DefaultMutableTreeNode node = 
    			(DefaultMutableTreeNode) treePath.getLastPathComponent();
        return node.isLeaf();
    }
    
    private TreePath[] getFilteredPaths(TreePath[] paths) {
        List<TreePath> returnedPaths = new ArrayList<TreePath>(paths.length);
        for (TreePath treePath : paths) {
            if (canPathBeAdded(treePath)) {
                returnedPaths.add(treePath);
            }
        }
        return returnedPaths.toArray(new TreePath[returnedPaths.size()]);
    }
}