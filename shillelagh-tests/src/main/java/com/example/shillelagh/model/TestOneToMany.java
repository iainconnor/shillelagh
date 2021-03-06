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

package com.example.shillelagh.model;

import java.util.List;

import shillelagh.Field;
import shillelagh.Id;
import shillelagh.OrmOnly;
import shillelagh.Table;

@Table public class TestOneToMany {
  @Id long id;
  @Field String someValue;
  @Field List<Child> children;

  @OrmOnly TestOneToMany() {}

  public TestOneToMany(String someValue, List<Child> children) {
    this.someValue = someValue;
    this.children = children;
  }

  public void setSomeValue(String someValue) {
    this.someValue = someValue;
  }

  public List<Child> getChildren() {
    return children;
  }

  @Table public static class Child {
    @Id long id;
    @Field String testString;
    @Field int testInt;

    @OrmOnly Child() { }

    public Child(String testString, int testInt) {
      this.testString = testString;
      this.testInt = testInt;
    }

    public void setTestString(String testString) {
      this.testString = testString;
    }

    @Override public String toString() {
      return "Child{" +
          "id=" + id +
          ", testString='" + testString + '\'' +
          ", testInt=" + testInt +
          '}';
    }
  }
}
