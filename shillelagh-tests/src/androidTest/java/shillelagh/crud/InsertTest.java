/*
 * Copyright 2014 Andrew Reitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shillelagh.crud;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;

import com.example.shillelagh.TestSQLiteOpenHelper;
import com.example.shillelagh.model.TestBlobs;
import com.example.shillelagh.model.TestBoxedPrimitivesTable;
import com.example.shillelagh.model.TestJavaObjectsTable;
import com.example.shillelagh.model.TestNotTableObject;
import com.example.shillelagh.model.TestOneToMany;
import com.example.shillelagh.model.TestOneToOne;
import com.example.shillelagh.model.TestPrimitiveTable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Date;

import shillelagh.Shillelagh;

import static org.fest.assertions.api.Assertions.assertThat;
import static shillelagh.Shillelagh.getTableName;

public class InsertTest extends AndroidTestCase {

  private SQLiteOpenHelper sqliteOpenHelper;
  private Shillelagh shillelagh;

  @Override protected void setUp() throws Exception {
    super.setUp();

    sqliteOpenHelper = new TestSQLiteOpenHelper(getContext());
    shillelagh = new Shillelagh(sqliteOpenHelper);
  }

  @Override protected void tearDown() throws Exception {
    getContext().deleteDatabase(TestSQLiteOpenHelper.DATABASE_NAME);
    super.tearDown();
  }

  public void testInsertPrimitives() {
    // Arrange
    double expectedDouble = 2342342.2323;
    float expectedFloat = 4.0f;
    long expectedLong = 10000;
    int expectedInt = 23;
    short expectedShort = 234;

    TestPrimitiveTable row = new TestPrimitiveTable();
    row.setaBoolean(true);
    row.setaDouble(expectedDouble);
    row.setaFloat(expectedFloat);
    row.setaLong(expectedLong);
    row.setAnInt(expectedInt);
    row.setaShort(expectedShort);

    // Act
    shillelagh.insert(row);

    // Assert
    Cursor cursor = sqliteOpenHelper.getReadableDatabase().rawQuery(
        "SELECT * FROM " + getTableName(TestPrimitiveTable.class), null);

    assertThat(cursor.getCount()).isEqualTo(1);

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getLong(0)).isEqualTo(1); // id column
    assertThat(cursor.getShort(1)).isEqualTo(expectedShort); // aShort
    assertThat(cursor.getInt(2)).isEqualTo(expectedInt); // anInt
    assertThat(cursor.getLong(3)).isEqualTo(expectedLong); // aLong
    assertThat(cursor.getFloat(4)).isEqualTo(expectedFloat); // aFloat
    assertThat(cursor.getDouble(5)).isEqualTo(expectedDouble); // aDouble
    assertThat(cursor.getInt(6)).isEqualTo(1); // aBoolean, true maps to 1

    assertThat(cursor.moveToNext()).isFalse();
    cursor.close();
  }

  public void testInsertBoxedPrimitives() {
    // Arrange
    double expectedDouble = 10000.55;
    float expectedFloat = 2.0f;
    long expectedLong = 200000;
    int expectedInt = 42;
    short expectedShort = 2;

    TestBoxedPrimitivesTable row = new TestBoxedPrimitivesTable();
    row.setaBoolean(false);
    row.setaDouble(expectedDouble);
    row.setaFloat(expectedFloat);
    row.setAnInteger(expectedInt);
    row.setaLong(expectedLong);
    row.setaShort(expectedShort);

    // Act
    shillelagh.insert(row);

    // Assert
    Cursor cursor = sqliteOpenHelper.getReadableDatabase().rawQuery(
        "SELECT * FROM " + getTableName(TestBoxedPrimitivesTable.class), null);

    assertThat(cursor.getCount()).isEqualTo(1);

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getLong(0)).isEqualTo(1); // id column
    assertThat(cursor.getInt(1)).isEqualTo(0); // aBoolean
    assertThat(cursor.getDouble(2)).isEqualTo(expectedDouble); // aDouble
    assertThat(cursor.getFloat(3)).isEqualTo(expectedFloat); // aFloat
    assertThat(cursor.getInt(4)).isEqualTo(expectedInt); // anInteger
    assertThat(cursor.getLong(5)).isEqualTo(expectedLong); // aLong
    assertThat(cursor.getShort(6)).isEqualTo(expectedShort); // aShort

    assertThat(cursor.moveToNext()).isFalse();
    cursor.close();
  }

  public void testInsertJavaObjects() {
    // Arrange
    final Date now = new Date();
    final String expected = "TestString";
    TestJavaObjectsTable row = new TestJavaObjectsTable();
    row.setaDate(now);
    row.setaString(expected);

    // Act
    shillelagh.insert(row);

    // Assert
    Cursor cursor = sqliteOpenHelper.getReadableDatabase().rawQuery(
        "SELECT * FROM " + getTableName(TestJavaObjectsTable.class), null);

    assertThat(cursor.getCount()).isEqualTo(1);

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getLong(0)).isEqualTo(1);
    assertThat(cursor.getString(1)).isEqualTo(expected);
    assertThat(cursor.getLong(2)).isEqualTo(now.getTime());

    assertThat(cursor.moveToNext()).isFalse();
    cursor.close();
  }

  public void testInsertBlobs() throws IOException, ClassNotFoundException {
    // Arrange
    final Byte[] expectedByteArray = new Byte[5];
    for (byte i = 0; i < expectedByteArray.length; i++) {
      expectedByteArray[i] = i;
    }
    final byte[] expectedOtherByteArray = new byte[10];
    for (byte i = 0; i < expectedOtherByteArray.length; i++) {
      expectedOtherByteArray[i] = i;
    }
    final TestBlobs.TestBlobObject expectedBlobObject = new TestBlobs.TestBlobObject();
    expectedBlobObject.testString = "this is expected string";
    TestBlobs row = new TestBlobs();
    row.setaByteArray(expectedByteArray);
    row.setAnotherByteArray(expectedOtherByteArray);
    row.setaTestBlobObject(expectedBlobObject);

    // Act
    shillelagh.insert(row);

    // Assert
    Cursor cursor = sqliteOpenHelper.getReadableDatabase().rawQuery(
        "SELECT * FROM " + getTableName(TestBlobs.class), null);

    assertThat(cursor.getCount()).isEqualTo(1);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getLong(0)).isEqualTo(1);
    assertThat(this.<Byte[]>deserialize(cursor.getBlob(1))).isEqualTo(expectedByteArray);
    assertThat(cursor.getBlob(2)).isEqualTo(expectedOtherByteArray);
    TestBlobs.TestBlobObject resultBlob = deserialize(cursor.getBlob(3));
    assertThat(resultBlob).isEqualsToByComparingFields(expectedBlobObject);

    assertThat(cursor.moveToNext()).isFalse();
    cursor.close();
  }

  public void testOneToOneInsertion() {
    // Arrange
    final String expected = "TEST STRING";
    final TestOneToOne.Child expectedChild = new TestOneToOne.Child(expected);
    final TestOneToOne expectedOneToOne = new TestOneToOne(expectedChild);

    // Act
    shillelagh.insert(expectedOneToOne);

    // Assert
    Cursor cursor = sqliteOpenHelper.getReadableDatabase().rawQuery(
        "SELECT * FROM " + getTableName(TestOneToOne.class), null);

    assertThat(cursor.getCount()).isEqualTo(1);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getLong(0)).isEqualTo(1);
    assertThat(cursor.getLong(1)).isEqualTo(1);

    assertThat(cursor.moveToNext()).isFalse();
    cursor.close();

    cursor = sqliteOpenHelper.getReadableDatabase().rawQuery(
        "SELECT * FROM " + getTableName(TestOneToOne.Child.class), null);

    assertThat(cursor.getCount()).isEqualTo(1);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getLong(0)).isEqualTo(1);
    assertThat(cursor.getString(1)).isEqualTo(expected);

    assertThat(cursor.moveToNext()).isFalse();
    cursor.close();

  }

  public void testOneToManyInsertion() {
    // Arrange
    String childExpectedString1 = "some test string";
    int childExpectedInt1 = 123;
    TestOneToMany.Child child1 = new TestOneToMany.Child(childExpectedString1, childExpectedInt1);

    String childExpectedString2 = "some other string";
    int childExpectedInt2 = -1;
    TestOneToMany.Child child2 = new TestOneToMany.Child(childExpectedString2, childExpectedInt2);

    String someValueExpected = "some value";
    TestOneToMany testOneToMany = new TestOneToMany(someValueExpected,
        Arrays.asList(child1, child2));

    // Act
    shillelagh.insert(testOneToMany);

    // Assert
    Cursor cursor = sqliteOpenHelper.getReadableDatabase().rawQuery(
        "SELECT * FROM " + getTableName(TestOneToMany.class), null);

    assertThat(cursor.getCount()).isEqualTo(1);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getLong(0)).isEqualTo(1);
    assertThat(cursor.getString(1)).isEqualTo(someValueExpected);

    assertThat(cursor.moveToNext()).isFalse();
    cursor.close();

    cursor = sqliteOpenHelper.getReadableDatabase().rawQuery(
        "SELECT * FROM " + getTableName(TestOneToMany.Child.class), null);

    assertThat(cursor.getCount()).isEqualTo(2);

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getLong(0)).isEqualTo(1);
    assertThat(cursor.getString(1)).isEqualTo(childExpectedString1);
    assertThat(cursor.getInt(2)).isEqualTo(childExpectedInt1);
    assertThat(cursor.getLong(3)).isEqualTo(1);

    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getLong(0)).isEqualTo(2);
    assertThat(cursor.getString(1)).isEqualTo(childExpectedString2);
    assertThat(cursor.getInt(2)).isEqualTo(childExpectedInt2);
    assertThat(cursor.getLong(3)).isEqualTo(1);
  }

  public void testInsertShouldFailWhenNotAnnotated() {
    // Arrange
    TestNotTableObject row = new TestNotTableObject();
    row.setName("some text");
    row.setValue(6);

    // Act
    try {
      shillelagh.insert(row);
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).isEqualTo("Unable to insert into com.example.shillelagh.model." +
          "TestNotTableObject. Did you forget to call Shillelagh.createTable or are you missing " +
          "@Table annotation?");
      return;
    }

    // Assert
    throw new AssertionError("Expected Exception Not Thrown");
  }

  @SuppressWarnings("unchecked")
  private <K> K deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
    return (K) objectInputStream.readObject();
  }

  // TODO Tests for null values
}
