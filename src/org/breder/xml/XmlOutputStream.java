package org.breder.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Escreve objeto no formato xml
 * 
 * 
 * @author Tecgraf
 */
public class XmlOutputStream {

  /** Delega a saída */
  private final OutputStream output;
  /** Unidade em Segundos */
  private static final long SECOND_UNIT = 1000;
  /** Unidade em Minutos */
  private static final long MINUTE_UNIT = 60 * SECOND_UNIT;
  /** Unidade em Hora */
  private static final long HOUR_UNIT = 60 * MINUTE_UNIT;
  /** Unidade em Dia */
  private static final long DAY_UNIT = 24 * HOUR_UNIT;
  /** Unidade em Mês */
  private static final long MONTH_UNIT = 31 * DAY_UNIT;
  /** Unidade em Ano */
  private static final long YEAR_UNIT = 12 * MONTH_UNIT;

  /**
   * Construtor
   * 
   * @param output
   */
  public XmlOutputStream(OutputStream output) {
    this.output = output;
  }

  /**
   * Escreve um objeto
   * 
   * @param object
   * @throws IOException
   */
  public void writeObject(Object object) throws IOException {
    if (object == null) {
      this.writeNull();
    }
    else if (object instanceof String) {
      this.writeString((String) object);
    }
    else if (object instanceof Boolean) {
      this.writeBoolean((Boolean) object);
    }
    else if (object instanceof Integer) {
      this.writeInteger((Integer) object);
    }
    else if (object instanceof Long) {
      this.writeLong((Long) object);
    }
    else if (object instanceof Float) {
      this.writeFloat((Float) object);
    }
    else if (object instanceof Double) {
      this.writeDouble((Double) object);
    }
    else if (object instanceof Number) {
      this.writeNumber((Number) object);
    }
    else if (object instanceof Date) {
      this.writeDate((Date) object);
    }
    else if (object instanceof Throwable) {
      this.writeThrowable((Throwable) object);
    }
    else if (object instanceof ArrayList<?>) {
      this.writeArrayList((ArrayList<?>) object);
    }
    else if (object instanceof Collection<?>) {
      this.writeCollection((Collection<?>) object);
    }
    else if (object instanceof Enum<?>) {
      this.writeEnum((Enum<?>) object);
    }
    else {
      this.writeStruct(object);
    }
  }

  /**
   * Escreve uma estrutura de dados qualquer
   * 
   * @param object
   * @throws IOException
   * @throws IllegalArgumentException
   */
  public void writeStruct(Object object) throws IOException {
    output.write('<');
    {
      String name = object.getClass().getSimpleName();
      int length = name.length();
      for (int m = 0; m < length; m++) {
        output.write(name.charAt(m));
      }
    }
    Collection<Field> fieldSet = createFieldSet(object);
    Map<String, Object> attributes = createAttributes(object, fieldSet);
    Collection<Object> children = createChildren(object, fieldSet);
    for (Entry<String, Object> entry : attributes.entrySet()) {
      String name = entry.getKey();
      Object value = entry.getValue();
      output.write(' ');
      int length = name.length();
      for (int m = 0; m < length; m++) {
        output.write(name.charAt(m));
      }
      output.write('=');
      this.writeObject(value);
    }
    if (children.isEmpty()) {
      output.write('/');
    }
    output.write('>');
    if (!children.isEmpty()) {
      for (Object entry : children) {
        this.writeObject(entry);
      }
      output.write('<');
      output.write('/');
      {
        String name = object.getClass().getSimpleName();
        int length = name.length();
        for (int m = 0; m < length; m++) {
          output.write(name.charAt(m));
        }
      }
      output.write('>');
    }
  }

  /**
   * Cria os atributos
   * 
   * @param object
   * @param fieldSet
   * @return atributos
   */
  private Map<String, Object> createAttributes(Object object,
    Collection<Field> fieldSet) {
    Map<String, Object> map = new TreeMap<String, Object>();
    for (Field field : fieldSet) {
      try {
        int modifiers = field.getModifiers();
        if (!Modifier.isTransient(modifiers) && !Modifier.isStatic(modifiers)) {
          if (isPrimitive(field)) {
            field.setAccessible(true);
            Object value = field.get(object);
            if (value != null) {
              map.put(field.getName(), value);
            }
          }
        }
      }
      catch (Exception e) {
      }
    }
    return map;
  }

  /**
   * Cria os atributos
   * 
   * @param object
   * @param fieldSet
   * @return atributos
   */
  private Collection<Object> createChildren(Object object,
    Collection<Field> fieldSet) {
    Set<Object> map = new TreeSet<Object>(new Comparator<Object>() {
      @Override
      public int compare(Object o1, Object o2) {
        return o1.getClass().getSimpleName().compareTo(
          o2.getClass().getSimpleName());
      }
    });
    for (Field field : fieldSet) {
      try {
        int modifiers = field.getModifiers();
        if (!Modifier.isTransient(modifiers) && !Modifier.isStatic(modifiers)) {
          if (!isPrimitive(field)) {
            field.setAccessible(true);
            Object value = field.get(object);
            if (value != null) {
              if (value instanceof Collection<?>
                && ((Collection<?>) value).isEmpty()) {
                continue;
              }
              map.add(value);
            }
          }
        }
      }
      catch (Exception e) {
      }
    }
    return map;
  }

  /**
   * Indica que é um primitivo
   * 
   * @param field
   * @return primitivo
   */
  private boolean isPrimitive(Field field) {
    Class<?> type = field.getType();
    return type.isPrimitive() || type == String.class || type == Date.class
      || type.isEnum();
  }

  /**
   * Cria os campos baseado nos objetos
   * 
   * @param object
   * @return coleção de campos
   */
  private Collection<Field> createFieldSet(Object object) {
    Collection<Field> set = new TreeSet<Field>(new Comparator<Field>() {
      @Override
      public int compare(Field o1, Field o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    Class<?> c = object.getClass();
    while (c != Object.class) {
      Field[] fields = c.getDeclaredFields();
      for (int n = 0; n < fields.length; n++) {
        try {
          Field field = fields[n];
          int modifiers = field.getModifiers();
          if (!Modifier.isTransient(modifiers) && !Modifier.isStatic(modifiers)) {
            set.add(field);
          }
        }
        catch (Exception e) {
        }
      }
      c = c.getSuperclass();
    }
    return set;
  }

  /**
   * Imprime o null
   * 
   * @throws IOException
   */
  public void writeNull() throws IOException {
    output.write('\"');
    output.write('n');
    output.write('u');
    output.write('l');
    output.write('l');
    output.write('\"');
  }

  /**
   * Escreve uma String
   * 
   * @param text
   * @throws IOException
   */
  public void writeString(String text) throws IOException {
    output.write('\"');
    int length = text.length();
    for (int n = 0; n < length; n++) {
      char c = text.charAt(n);
      if (c == '\\') {
        output.write('\\');
        output.write('\\');
      }
      else if (c == '\"') {
        output.write('\\');
        output.write('\"');
      }
      else if (c == '\r') {
        output.write('\\');
        output.write('r');
      }
      else if (c == '\n') {
        output.write('\\');
        output.write('n');
      }
      else if (c == '\t') {
        output.write('\\');
        output.write('t');
      }
      else if (c == '\b') {
        output.write('\\');
        output.write('b');
      }
      else if (c == '\f') {
        output.write('\\');
        output.write('f');
      }
      else if (c <= 0x7F) {
        output.write(c);
      }
      else if (c <= 0x7FF) {
        output.write(((c >> 6) & 0x1F) + 0xC0);
        output.write((c & 0x3F) + 0x80);
      }
      else {
        output.write(((c >> 12) & 0xF) + 0xE0);
        output.write(((c >> 6) & 0x3F) + 0x80);
        output.write((c & 0x3F) + 0x80);
      }
    }
    output.write('\"');
  }

  /**
   * Escreve True
   * 
   * @throws IOException
   */
  private void writeTrue() throws IOException {
    output.write('t');
    output.write('r');
    output.write('u');
    output.write('e');
  }

  /**
   * Escreve false
   * 
   * @throws IOException
   */
  private void writeFalse() throws IOException {
    output.write('f');
    output.write('a');
    output.write('l');
    output.write('s');
    output.write('e');
  }

  /**
   * Escreve um Boolean
   * 
   * @param flag
   * @throws IOException
   */
  public void writeBoolean(Boolean flag) throws IOException {
    output.write('\"');
    if (flag) {
      this.writeTrue();
    }
    else {
      this.writeFalse();
    }
    output.write('\"');
  }

  /**
   * Escreve um Boolean
   * 
   * @param flag
   * @throws IOException
   */
  public void writeBoolean(boolean flag) throws IOException {
    output.write('\"');
    if (flag) {
      this.writeTrue();
    }
    else {
      this.writeFalse();
    }
    output.write('\"');
  }

  /**
   * Escreve um Integer
   * 
   * @param number
   * @throws IOException
   */
  public void writeInteger(Integer number) throws IOException {
    output.write('\"');
    output.write(number.toString().getBytes());
    output.write('\"');
  }

  /**
   * Escreve um int
   * 
   * @param number
   * @throws IOException
   */
  public void writeInteger(int number) throws IOException {
    output.write('\"');
    output.write(Integer.toString(number).getBytes());
    output.write('\"');
  }

  /**
   * Escreve um Long
   * 
   * @param number
   * @throws IOException
   */
  public void writeLong(Long number) throws IOException {
    output.write('\"');
    output.write(number.toString().getBytes());
    output.write('\"');
  }

  /**
   * Escreve um Long
   * 
   * @param number
   * @throws IOException
   */
  public void writeLong(long number) throws IOException {
    output.write('\"');
    output.write(Long.toString(number).getBytes());
    output.write('\"');
  }

  /**
   * Escreve um Float
   * 
   * @param number
   * @throws IOException
   */
  public void writeFloat(Float number) throws IOException {
    output.write('\"');
    output.write(number.toString().getBytes());
    output.write('\"');
  }

  /**
   * Escreve um float
   * 
   * @param number
   * @throws IOException
   */
  public void writeFloat(float number) throws IOException {
    output.write('\"');
    output.write(Float.toString(number).getBytes());
    output.write('\"');
  }

  /**
   * Escreve um Double
   * 
   * @param number
   * @throws IOException
   */
  public void writeDouble(Double number) throws IOException {
    output.write('\"');
    output.write(number.toString().getBytes());
    output.write('\"');
  }

  /**
   * Escreve um Double
   * 
   * @param number
   * @throws IOException
   */
  public void writeDouble(double number) throws IOException {
    output.write('\"');
    output.write(Double.toString(number).getBytes());
    output.write('\"');
  }

  /**
   * Escreve um número
   * 
   * @param number
   * @throws IOException
   */
  public void writeNumber(Number number) throws IOException {
    output.write('\"');
    output.write(number.toString().getBytes());
    output.write('\"');
  }

  /**
   * Escreve uma data
   * 
   * @param object
   * @throws IOException
   */
  public void writeDate(Date object) throws IOException {
    output.write('\"');
    Calendar c = Calendar.getInstance();
    c.setTime(object);
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH) + 1;
    int day = c.get(Calendar.DAY_OF_MONTH);
    int hour = c.get(Calendar.HOUR_OF_DAY);
    int minute = c.get(Calendar.MINUTE);
    int second = c.get(Calendar.SECOND);
    int milisecond = c.get(Calendar.MILLISECOND);
    if (year < 1000) {
      output.write('0');
    }
    else {
      int value = year / 1000;
      output.write(value + '0');
      year -= value * 1000;
    }
    if (year < 100) {
      output.write('0');
    }
    else {
      int value = year / 100;
      output.write(value + '0');
      year -= value * 100;
    }
    if (year < 10) {
      output.write('0');
    }
    else {
      int value = year / 10;
      output.write(value + '0');
      year -= value * 10;
    }
    output.write(year + '0');
    output.write('-');
    if (month < 10) {
      output.write('0');
    }
    else {
      int value = month / 10;
      output.write(value + '0');
      month -= value * 10;
    }
    output.write(month + '0');
    output.write('-');
    if (day < 10) {
      output.write('0');
    }
    else {
      int value = day / 10;
      output.write(value + '0');
      day -= value * 10;
    }
    output.write(day + '0');
    output.write('T');
    if (hour < 10) {
      output.write('0');
    }
    else {
      int value = hour / 10;
      output.write(value + '0');
      hour -= value * 10;
    }
    output.write(hour + '0');
    output.write(':');
    if (minute < 10) {
      output.write('0');
    }
    else {
      int value = minute / 10;
      output.write(value + '0');
      minute -= value * 10;
    }
    output.write(minute + '0');
    output.write(':');
    if (second < 10) {
      output.write('0');
    }
    else {
      int value = second / 10;
      output.write(value + '0');
      second -= value * 10;
    }
    output.write(second + '0');
    output.write('.');
    if (milisecond < 100) {
      output.write('0');
    }
    else {
      int value = milisecond / 100;
      output.write(value + '0');
      milisecond -= value * 100;
    }
    if (milisecond < 10) {
      output.write('0');
    }
    else {
      int value = milisecond / 10;
      output.write(value + '0');
      milisecond -= value * 10;
    }
    output.write(milisecond + '0');
    output.write('Z');
    output.write('\"');
  }

  /**
   * Converte o date para um long que corresponde a data em milisegundos
   * 
   * @param object
   * @return long representativo da data
   */
  private static long convertToLong(Date object) {
    Calendar c = Calendar.getInstance();
    c.setTime(object);
    long year = c.get(Calendar.YEAR);
    long month = c.get(Calendar.MONTH);
    int day = c.get(Calendar.DAY_OF_MONTH) - 1;
    int hour = c.get(Calendar.HOUR_OF_DAY);
    int minute = c.get(Calendar.MINUTE);
    int second = c.get(Calendar.SECOND);
    int milisecond = c.get(Calendar.MILLISECOND);
    long timer =
      milisecond
        + (SECOND_UNIT * second + (MINUTE_UNIT * minute + (HOUR_UNIT * hour + (DAY_UNIT
          * day + (MONTH_UNIT * month + (YEAR_UNIT * year))))));
    return timer;
  }

  /**
   * Escreve um erro
   * 
   * @param object
   * @throws IOException
   */
  public void writeThrowable(Throwable object) throws IOException {
    output.write(("{\"type\":\"exception\",\"message\":\""
      + object.getMessage() + "\"}").getBytes());
  }

  /**
   * Escreve uma lista
   * 
   * @param list
   * @throws IOException
   */
  public void writeArrayList(ArrayList<?> list) throws IOException {
    int size = list.size();
    for (int n = 0; n < size; n++) {
      this.writeObject(list.get(n));
      if (n != size - 1) {
      }
    }
  }

  /**
   * Escreve uma coleção
   * 
   * @param list
   * @throws IOException
   */
  public void writeCollection(Collection<?> list) throws IOException {
    Iterator<?> iterator = list.iterator();
    while (iterator.hasNext()) {
      this.writeObject(iterator.next());
      if (iterator.hasNext()) {
      }
    }
  }

  /**
   * Escreve um enum
   * 
   * @param object
   * @throws IOException
   */
  public void writeEnum(Enum<?> object) throws IOException {
    output.write('\"');
    String name = object.name();
    int length = name.length();
    for (int m = 0; m < length; m++) {
      output.write(name.charAt(m));
    }
    output.write('\"');
  }

}
