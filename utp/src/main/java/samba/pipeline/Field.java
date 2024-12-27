package samba.pipeline;

public class Field<T> {

  public static final Field<Object> INCOMING = new Field<>("INCOMING");

  private final String name;

  public Field(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Field[" + name + ']';
  }
}
