package RDownload;

import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ProgressRenderer extends JProgressBar implements TableCellRenderer{
	public ProgressRenderer(int min,int max){
		super(min,max);
	}
	public Component getTableCellRendererComponent(JTable table, Object value , boolean isSelected, boolean hasFocus,int row , int column){
		 //% completed value
		setValue((int)((Float) value).floatValue());
		return this;
	}
}
