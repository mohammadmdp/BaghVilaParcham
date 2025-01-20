package mdp.baghvilaparchammanagementapp;

import java.util.ArrayList;

public class DBTable {
	private String name,createStatement;
	private ArrayList<Field> fields;
	public static class Field{
		public String name,type;
		public Field(String name, String type){
			this.name = name;
			this.type = type;
		}
	}
	public DBTable(String name, String createStatement, ArrayList<Field> fields) {
		setName(name);
		setCreateStatement(createStatement);
		setFields(fields);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCreateStatement() {
		return createStatement;
	}
	public void setCreateStatement(String createStatement) {
		this.createStatement = createStatement;
	}
	public ArrayList<Field> getFields() {
		return fields;
	}
	public void setFields(ArrayList<Field> fields) {
		this.fields = fields;
	}
	public String getFieldNamesStr(){
		StringBuilder sb = new StringBuilder();
		for(Field f:fields){
			sb.append(f.name+",");
		}
		return sb.substring(0, sb.length()-1);
	}
	public String[] getFieldNames(){
		String[] str = new String[fields.size()];
		int i=0;
		for(Field f:fields){
			str[i++] = f.name;
		}
		return str;
	}
	public String getFilteredFieldNamesStr(ArrayList<String> filter){
		StringBuilder sb = new StringBuilder();
		for(Field f:fields){
			if(!filter.contains(f.name)){
				sb.append(f.name+",");
			}
		}
		return sb.substring(0, sb.length()-1);
	}
	public String getAddedFieldNamesStr(ArrayList<String> addedFields){
		StringBuilder sb = new StringBuilder();
		for(Field f:fields){
			sb.append(f.name+",");
		}
		for(String af:addedFields){
			boolean hasIt = false;
			for(Field f:fields){
				if(f.name.equals(af)){
					hasIt = true;
				}
			}
			if(!hasIt){
				sb.append(af+",");
			}
		}
		return sb.substring(0, sb.length()-1);
	}
	public String getRetypedFieldNamesStr(ArrayList<String> retypedFields){
		StringBuilder sb = new StringBuilder();
		for(Field f:fields){
			if(retypedFields.contains(f.name)){
				sb.append("CAST("+f.name+" AS "+f.type+"),");
			} else {
				sb.append(f.name+",");
			}
		}
		return sb.substring(0, sb.length()-1);
	}
}
