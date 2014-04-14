package shillelagh.internal;

import java.util.Iterator;

import shillelagh.SqliteType;

public class ShillelaghInjector {

  /**
   * Internal class function names
   */
  public static final String CREATE_TABLE_FUNCTION = "getCreateTableSql";
  public static final String DROP_TABLE_FUNCTION = "getDropTableSql";
  public static final String INSERT_OBJECT_FUNCTION = "getInsertSql";
  public static final String UPDATE_OBJECT_FUNCTION = "getUpdateSql";
  public static final String UPDATE_ID_FUNCTION = "updateColumnId";

  /**
   * SQL statement to select the id of the last inserted row. Does not end with ; in order to be
   * used with {@link android.database.sqlite.SQLiteDatabase#rawQuery(String, String[])}
   */
  private static final String GET_ID_OF_LAST_INSERTED_ROW_SQL = "SELECT ROWID FROM %s ORDER BY ROWID DESC LIMIT 1";

  private final String classPackage;
  private final String className;
  private final String targetClass;
  private final ShillelaghLogger logger;

  private TableObject tableObject;

  ShillelaghInjector(String classPackage, String className, String targetClass, ShillelaghLogger logger) {
    this.classPackage = classPackage;
    this.className = className;
    this.targetClass = targetClass;
    this.logger = logger;
  }

  public void setTable(TableObject tableObject) {
    this.tableObject = tableObject;
  }

  /** Get the fully qualified class name */
  String getFqcn() {
    return classPackage + "." + className;
  }

  /** Create the java functions required for the internal class */
  String brewJava() {
    StringBuilder builder = new StringBuilder();
    builder.append("// Generated code from Shillelagh. Do not modify!\n");
    builder.append("package ").append(classPackage).append(";\n\n");
    builder.append("import android.database.Cursor;\n");
    builder.append("import android.database.sqlite.SQLiteDatabase;\n\n");
    builder.append("public class ").append(className).append(" {\n");
    emmitCreateTableSql(builder);
    builder.append("\n");
    emmitDropTableSql(builder);
    builder.append("\n");
    emmitInsertObject(builder);
    builder.append("\n");
    emmitUpdateObject(builder);
    builder.append("\n");
    emmitUpdateColumnId(builder);
    builder.append("}\n");
    return builder.toString();
  }

  /** Creates the function for getting the create sql string */
  private void emmitCreateTableSql(StringBuilder builder) {
    builder.append("  public static String ").append(CREATE_TABLE_FUNCTION).append("() {\n");
    builder.append("    return \"").append(tableObject.getSchema()).append("\";\n");
    builder.append("  }\n");
  }

  /** Creates the function for getting the drop sql string */
  private void emmitDropTableSql(StringBuilder builder) {
    builder.append("  public static String ").append(DROP_TABLE_FUNCTION).append("() {\n");
    builder.append("    return \"DROP TABLE IF EXISTS ").append(tableObject.getTableName()).append("\";\n");
    builder.append("  }\n");
  }

  /** Creates the function for getting the insert sql string to insert a new value into the database */
  private void emmitInsertObject(StringBuilder builder) {
    builder.append("  public static String ").append(INSERT_OBJECT_FUNCTION).append("(").append(targetClass).append(" element) {\n");
    builder.append("    return \"INSERT INTO ").append(tableObject.getTableName());
    builder.append(" (");

    StringBuilder columns = new StringBuilder();
    StringBuilder values = new StringBuilder();
    Iterator<TableColumn> iterator = tableObject.getColumns().iterator();
    while (iterator.hasNext()) {
      TableColumn column = iterator.next();
      columns.append(column.getColumnName());
      if (column.getType() == SqliteType.TEXT) {
        // TODO Checks to make sure elements are accessible
        values.append("'\" + element.").append(column.getColumnName()).append(" + \"'");
      } else if (column.isDate()) {
        values.append("\" + element.").append(column.getColumnName()).append(".getTime() + \"");
      } else {
        values.append("\" + element.").append(column.getColumnName()).append(" + \"");
      }
      if (iterator.hasNext()) {
        columns.append(", ");
        values.append(", ");
      }
    }

    builder.append(columns.toString()).append(")");
    builder.append(" VALUES (");
    builder.append(values.toString()).append(");\";\n");
    builder.append("  }\n");
  }

  /** Updates the id of the object to the last insert */
  private void emmitUpdateColumnId(StringBuilder builder) {
    // Updates the column id for the last inserted row
    builder.append("  public static void ").append(UPDATE_ID_FUNCTION).append("(").append(targetClass).append(" element, SQLiteDatabase db) {\n");
    builder.append("    final Cursor cursor = db.rawQuery(\"").append(String.format(GET_ID_OF_LAST_INSERTED_ROW_SQL, tableObject.getTableName())).append("\", null);\n");
    builder.append("    cursor.moveToFirst();\n");
    builder.append("    long id = cursor.getLong(0);\n");
    builder.append("    element.").append(tableObject.getIdColumnName()).append(" = id;\n");
    builder.append("  }\n");
  }

  /** Creates the function for getting the update sql statement */
  private void emmitUpdateObject(StringBuilder builder) {
    builder.append("  public static String ").append(UPDATE_OBJECT_FUNCTION).append("(").append(targetClass).append(" element) {\n");
    builder.append("    return \"UPDATE ").append(tableObject.getTableName()).append(" SET ");

    StringBuilder columnUpdates = new StringBuilder();
    Iterator<TableColumn> iterator = tableObject.getColumns().iterator();
    while (iterator.hasNext()) {
      TableColumn column = iterator.next();
      columnUpdates.append(column.getColumnName()).append(" = \" + element.").append(column.getColumnName()).append(" + \"");
      if (iterator.hasNext()) {
        columnUpdates.append(", ");
      }
    }

    builder.append(columnUpdates.toString());
    builder.append(" WHERE ").append(tableObject.getIdColumnName()).append(" = \" + element.").append(tableObject.getIdColumnName()).append(" + \"");
    builder.append("\";\n");
    builder.append("  }\n");
  }
}
