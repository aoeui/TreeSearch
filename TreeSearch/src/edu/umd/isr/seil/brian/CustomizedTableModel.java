package edu.umd.isr.seil.brian;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

// Customized table model
public class CustomizedTableModel extends DefaultTableModel{

	private static final long serialVersionUID = 1L;

	public CustomizedTableModel(){
		super(0,3);
	}
	
	@SuppressWarnings("rawtypes")
	public void setDataVector(Vector vector){
		dataVector = vector;
	}
	
	@SuppressWarnings("rawtypes")
	public Vector getColumnIdentifier(){
		return columnIdentifiers;
	}
	
	public Class<?> getColumnClass(int columnIndex){
		return String.class;
	}
	
	public boolean isCellEditable(int row, int col){
		if(col == 0){
			return false;
		}else{
			return true;
		}
	}
}