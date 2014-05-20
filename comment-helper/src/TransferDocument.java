import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.jsoup.nodes.Document;


public class TransferDocument extends Document implements Transferable {
	private static final DataFlavor[] supportedFlavors;
	
	static {
        try {
            supportedFlavors = new DataFlavor[]{
                    new DataFlavor("text/html;class=java.lang.String"),
                    new DataFlavor("text/plain;class=java.lang.String")
            };
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
	
	public TransferDocument() {
		super("");
	}
	
	
	public DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor supportedFlavor : supportedFlavors) {
            if (supportedFlavor == flavor) {
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(supportedFlavors[0])) {
            return html();
        }
        if (flavor.equals(supportedFlavors[1])) {
            return text();
        }
        throw new UnsupportedFlavorException(flavor);
    }
}
