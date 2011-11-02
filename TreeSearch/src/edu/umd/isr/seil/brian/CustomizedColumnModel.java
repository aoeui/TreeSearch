package edu.umd.isr.seil.brian;

import java.util.Vector;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class CustomizedColumnModel extends DefaultTableColumnModel{
	private static final long serialVersionUID = 1L;
	private String[] column = new String[]{"NO", "Func Name", "Parameters"};
	
	public CustomizedColumnModel(){
		super();
		for(int i = 0; i < column.length; i++){
			TableColumn tableColumn = new TableColumn(i);
			tableColumn.setHeaderValue(column[i]);
			addColumn(tableColumn);
		}
	}
	
	public Vector<TableColumn> getTableColumns(){
		return tableColumns;
	}
	
	public void setTableColumns(Vector<TableColumn> tableColumns){
		this.tableColumns = tableColumns;
	}
}
