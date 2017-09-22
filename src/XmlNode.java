package org.breder.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Estrutura simples de Xml para fazer parse. O parse pode ser feito pelo
 * construtor da classe.
 * 
 * @author bernardobreder
 * 
 */
public class XmlNode {

  /** Node pai */
  private XmlNode parent;
  /** Nome da tag */
  private String name;
  /** Attributos */
  private Map<String, String> attributes;
  /** Filhos */
  private List<XmlNode> nodes;
  /** Conteúdo */
  private String content;

  /**
   * Construtor
   * 
   * @param name
   */
  public XmlNode(String name) {
    this.name = name;
  }

  /**
   * Construtor
   * 
   * @param input
   * @throws ParseException
   * @throws IOException
   */
  public XmlNode(InputStream input) throws ParseException, IOException {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource source = new InputSource(input);
      source.setEncoding("utf-8");
      Document document = db.parse(source);
      if (document.getFirstChild() instanceof Element) {
        Element element = (Element) document.getFirstChild();
        this.read(element);
      }
    }
    catch (ParserConfigurationException e) {
      throw new ParseException(e.getMessage(), 0);
    }
    catch (SAXException e) {
      throw new ParseException(e.getMessage(), 0);
    }
  }

  /**
   * Construtor
   * 
   * @param element
   */
  private XmlNode(Element element) {
    this.read(element);
  }

  /**
   * Realiza a leitura da tag
   * 
   * @param root
   */
  private void read(Element root) {
    this.name = root.getNodeName();
    NamedNodeMap atts = root.getAttributes();
    if (atts != null) {
      int size = atts.getLength();
      if (size > 0) {
        this.attributes = new HashMap<String, String>(size);
      }
      for (int n = 0; n < size; n++) {
        Node node = atts.item(n);
        if (node instanceof Attr) {
          Attr attr = (Attr) node;
          this.attributes.put(attr.getName(), attr.getValue());
        }
      }
    }
    String content = root.getTextContent().trim();
    if (content.length() > 0) {
      this.content = content;
    }
    NodeList nodes = root.getChildNodes();
    if (nodes != null) {
      int size = nodes.getLength();
      for (int n = 0; n < size; n++) {
        Node node = nodes.item(n);
        if (node instanceof Element) {
          Element element = (Element) node;
          if (this.nodes == null) {
            this.nodes = new ArrayList<XmlNode>();
          }
          XmlNode child = new XmlNode(element);
          child.parent = this;
          this.nodes.add(child);
        }
      }
    }
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the attributes
   */
  public Map<String, String> getAttributes() {
    return attributes;
  }

  /**
   * Adiciona um estilo html
   * 
   * @param key
   * @param value
   * @return this
   */
  public XmlNode addHtmlStyle(String key, String value) {
    if (this.attributes == null) {
      this.attributes = new HashMap<String, String>();
    }
    String styleValue = this.attributes.get("style");
    if (styleValue == null) {
      styleValue = "";
    }
    else {
      styleValue += ";";
    }
    styleValue += key + ":" + value;
    this.attributes.put("style", styleValue);
    return this;
  }

  /**
   * Adiciona um estilo html
   * 
   * @param className
   * @return this
   */
  public XmlNode addHtmlClass(String className) {
    if (this.attributes == null) {
      this.attributes = new HashMap<String, String>();
    }
    String value = this.attributes.get("class");
    if (value == null) {
      value = "";
    }
    else {
      value += " ";
    }
    value += className;
    this.attributes.put("class", value);
    return this;
  }

  /**
   * Indica se tem o atributo
   * 
   * @param attribute
   * @return tem o atributo
   */
  public boolean hasAttribute(String attribute) {
    if (this.attributes == null) {
      return false;
    }
    return this.attributes.get(attribute) != null;
  }

  /**
   * Indica se tem o atributo
   * 
   * @param attribute
   * @return tem o atributo
   */
  public String getAttribute(String attribute) {
    if (this.attributes == null) {
      return null;
    }
    return this.attributes.get(attribute);
  }

  /**
   * Indica se tem o atributo
   * 
   * @param attribute
   * @param defaultValue
   * @return tem o atributo
   */
  public Integer getAttribute(String attribute, Integer defaultValue) {
    if (this.attributes == null) {
      return defaultValue;
    }
    try {
      return Integer.valueOf(this.attributes.get(attribute));
    }
    catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Indica se tem o atributo
   * 
   * @param attribute
   * @param defaultValue
   * @return tem o atributo
   */
  public String getAttribute(String attribute, String defaultValue) {
    if (this.attributes == null) {
      return defaultValue;
    }
    return this.attributes.get(attribute);
  }

  /**
   * Indica se tem o atributo
   * 
   * @param attribute
   * @param defaultValue
   * @return tem o atributo
   */
  public Double getAttribute(String attribute, Double defaultValue) {
    if (this.attributes == null) {
      return defaultValue;
    }
    try {
      return Double.valueOf(this.attributes.get(attribute));
    }
    catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Indica se tem o atributo
   * 
   * @param attribute
   * @param defaultValue
   * @return tem o atributo
   */
  public Long getAttribute(String attribute, Long defaultValue) {
    if (this.attributes == null) {
      return defaultValue;
    }
    try {
      return Long.valueOf(this.attributes.get(attribute));
    }
    catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Indica se tem o atributo
   * 
   * @param attribute
   * @param defaultValue
   * @return tem o atributo
   */
  public Float getAttribute(String attribute, Float defaultValue) {
    if (this.attributes == null) {
      return defaultValue;
    }
    try {
      return Float.valueOf(this.attributes.get(attribute));
    }
    catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Indica se tem o atributo
   * 
   * @param attribute
   * @param defaultValue
   * @return tem o atributo
   */
  public Boolean getAttribute(String attribute, Boolean defaultValue) {
    if (this.attributes == null) {
      return defaultValue;
    }
    return Boolean.valueOf(this.attributes.get(attribute));
  }

  /**
   * Adiciona ou modifica um atributo
   * 
   * @param key
   * @param value
   * @return owner
   */
  public XmlNode setAttribute(String key, String value) {
    if (this.attributes == null) {
      this.attributes = new HashMap<String, String>();
    }
    this.attributes.put(key, value);
    return this;
  }

  /**
   * @param name the name to set
   * @return this
   */
  public XmlNode setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @param content the content to set
   * @return this
   */
  public XmlNode setContent(String content) {
    this.content = content;
    return this;
  }

  /**
   * @param input the content to set
   * @return this
   * @throws IOException
   */
  public XmlNode setContent(InputStream input) throws IOException {
    StringBuilder sb = new StringBuilder();
    StringInputStream in = new StringInputStream(input);
    try {
      for (int n; ((n = in.read()) != -1);) {
        sb.append((char) n);
      }
    }
    finally {
      in.close();
    }
    return this.setContent(sb.toString());
  }

  /**
   * @return the nodes
   */
  public List<XmlNode> getNodes() {
    return nodes;
  }

  /**
   * @return the nodes
   */
  public int getNodeCount() {
    return nodes == null ? 0 : nodes.size();
  }

  /**
   * Indica se tem filho
   * 
   * @return se tem filho
   */
  public boolean hasNodes() {
    return this.nodes != null;
  }

  /**
   * Adiciona uma tag
   * 
   * @param node
   * @return owner
   */
  public XmlNode addNode(XmlNode node) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<XmlNode>();
    }
    node.parent = this;
    this.nodes.add(node);
    return this;
  }

  /**
   * Adiciona varios nodes
   * 
   * @param list
   * @return this
   */
  public XmlNode addNodes(Collection<XmlNode> list) {
    if (list != null) {
      if (this.nodes == null) {
        this.nodes = new ArrayList<XmlNode>(list);
      }
      else {
        this.nodes.addAll(list);
      }
    }
    return this;
  }

  /**
   * Adiciona uma tag
   * 
   * @param node
   * @param index
   * @return owner
   */
  public XmlNode addNode(XmlNode node, int index) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<XmlNode>();
    }
    node.parent = this;
    this.nodes.add(index, node);
    return this;
  }

  /**
   * @param name
   * @return the nodes
   */
  public List<XmlNode> getNodesByTagName(String name) {
    List<XmlNode> list = new ArrayList<XmlNode>();
    if (this.nodes != null) {
      for (XmlNode node : this.nodes) {
        if (node.name.equals(name)) {
          list.add(node);
        }
      }
    }
    return list;
  }

  /**
   * @param attribute
   * @param value
   * @return the nodes
   */
  public List<XmlNode> getNodesByAttributeValue(String attribute, String value) {
    List<XmlNode> list = new ArrayList<XmlNode>();
    if (this.nodes != null) {
      for (XmlNode node : this.nodes) {
        if (node.attributes != null) {
          String attValue = node.attributes.get(attribute);
          if (attValue != null && attValue.equals(value)) {
            list.add(node);
          }
        }
      }
    }
    return list;
  }

  /**
   * @param attribute
   * @param value
   * @return the nodes
   */
  public List<XmlNode> getNodesByAttributeKey(String attribute, String value) {
    List<XmlNode> list = new ArrayList<XmlNode>();
    if (this.nodes != null) {
      for (XmlNode node : this.nodes) {
        if (node.attributes != null) {
          String attValue = node.attributes.get(attribute);
          if (attValue != null) {
            list.add(node);
          }
        }
      }
    }
    return list;
  }

  /**
   * @param attribute
   * @param value
   * @return the nodes
   */
  public List<XmlNode> getNodesByAttributeContainValue(String attribute,
    String value) {
    List<XmlNode> list = new ArrayList<XmlNode>();
    if (this.nodes != null) {
      for (XmlNode node : this.nodes) {
        if (node.attributes != null) {
          String attValue = node.attributes.get(attribute);
          if (attValue != null && attValue.contains(value)) {
            list.add(node);
          }
        }
      }
    }
    return list;
  }

  /**
   * @param attribute
   * @param value
   * @return the nodes
   */
  public List<XmlNode> getNodesByAttributeMatchValue(String attribute,
    String value) {
    List<XmlNode> list = new ArrayList<XmlNode>();
    if (this.nodes != null) {
      for (XmlNode node : this.nodes) {
        if (node.attributes != null) {
          String attValue = node.attributes.get(attribute);
          if (attValue != null && attValue.matches(value)) {
            list.add(node);
          }
        }
      }
    }
    return list;
  }

  /**
   * @param name
   * @return the nodes
   */
  public XmlNode getNodeByTagName(String name) {
    if (this.nodes != null) {
      for (XmlNode node : this.nodes) {
        if (node.name.equals(name)) {
          return node;
        }
      }
    }
    return null;
  }

  /**
   * @param attribute
   * @param value
   * @return the nodes
   */
  public XmlNode getNodeByAttributeValue(String attribute, String value) {
    if (this.nodes != null) {
      for (XmlNode node : this.nodes) {
        if (node.attributes != null) {
          String attValue = node.attributes.get(attribute);
          if (attValue != null && attValue.equals(value)) {
            return node;
          }
        }
      }
    }
    return null;
  }

  /**
   * @param attribute
   * @param value
   * @return the nodes
   */
  public XmlNode getNodeByAttributeKey(String attribute, String value) {
    if (this.nodes != null) {
      for (XmlNode node : this.nodes) {
        if (node.attributes != null) {
          String attValue = node.attributes.get(attribute);
          if (attValue != null) {
            return node;
          }
        }
      }
    }
    return null;
  }

  /**
   * @param attribute
   * @param value
   * @return the nodes
   */
  public XmlNode getNodeByAttributeContainValue(String attribute, String value) {
    if (this.nodes != null) {
      for (XmlNode node : this.nodes) {
        if (node.attributes != null) {
          String attValue = node.attributes.get(attribute);
          if (attValue != null && attValue.contains(value)) {
            return node;
          }
        }
      }
    }
    return null;
  }

  /**
   * @param attribute
   * @param value
   * @return the nodes
   */
  public XmlNode getNodeByAttributeMatchValue(String attribute, String value) {
    if (this.nodes != null) {
      for (XmlNode node : this.nodes) {
        if (node.attributes != null) {
          String attValue = node.attributes.get(attribute);
          if (attValue != null && attValue.matches(value)) {
            return node;
          }
        }
      }
    }
    return null;
  }

  /**
   * @return the parent
   */
  public XmlNode getParent() {
    return parent;
  }

  /**
   * @return the content
   */
  public String getContent() {
    return content;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder atts = new StringBuilder();
    if (this.attributes != null) {
      for (String key : this.attributes.keySet()) {
        atts.append(' ');
        atts.append(key);
        atts.append('=');
        atts.append('\"');
        atts.append(this.attributes.get(key));
        atts.append('\"');
      }
    }
    StringBuilder list = new StringBuilder();
    if (this.nodes != null) {
      for (XmlNode node : this.nodes) {
        list.append(node.toString());
      }
    }
    else if (this.content != null) {
      list.append(this.content);
    }
    return "<" + this.name + atts + ">" + list + "</" + this.name + ">";
  }

  /**
   * Recupera os bytes do xml
   * 
   * @return bytes
   * @throws IOException
   */
  public byte[] getBytes() throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    StringOutputStream string = new StringOutputStream(output);
    getBytes(string, this);
    return output.toByteArray();
  }

  /**
   * Recupera os bytes do xml
   * 
   * @param output
   * @param root
   * @throws IOException
   */
  private static void getBytes(StringOutputStream output, XmlNode root)
    throws IOException {
    output.write('<');
    output.append(root.name);
    if (root.attributes != null) {
      for (String key : root.attributes.keySet()) {
        output.write(' ');
        output.append(key);
        output.write('=');
        output.write('\"');
        output.append(root.attributes.get(key));
        output.write('\"');
      }
    }
    output.write('>');
    if (root.nodes != null) {
      for (XmlNode node : root.nodes) {
        getBytes(output, node);
      }
    }
    else if (root.content != null) {
      output.append(root.content);
    }
    output.write('<');
    output.write('/');
    output.append(root.name);
    output.write('>');
  }

  /**
   * Escreve no output
   * 
   * @param output
   * @throws IOException
   */
  public void write(OutputStream output) throws IOException {
    StringOutputStream out = new StringOutputStream(output);
    getBytes(out, this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
      prime * result + ((attributes == null) ? 0 : attributes.hashCode());
    result = prime * result + ((content == null) ? 0 : content.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    XmlNode other = (XmlNode) obj;
    if (attributes == null) {
      if (other.attributes != null) {
        return false;
      }
    }
    else if (!attributes.equals(other.attributes)) {
      return false;
    }
    if (content == null) {
      if (other.content != null) {
        return false;
      }
    }
    else if (!content.equals(other.content)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    }
    else if (!name.equals(other.name)) {
      return false;
    }
    if (nodes == null) {
      if (other.nodes != null) {
        return false;
      }
    }
    else if (!nodes.equals(other.nodes)) {
      return false;
    }
    return true;
  }

  /**
   * String para output stream
   * 
   * @author bernardobreder
   * 
   */
  private static class StringOutputStream extends OutputStream {

    /** Saída */
    private final OutputStream output;

    /**
     * Construtor
     * 
     * @param output
     */
    public StringOutputStream(OutputStream output) {
      this.output = output;
    }

    /**
     * Acrescenta uma string
     * 
     * @param text
     * @throws IOException
     */
    public void append(String text) throws IOException {
      int size = text.length();
      for (int n = 0; n < size; n++) {
        char c = text.charAt(n);
        if (c <= 0x7F) {
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int n) throws IOException {
      output.write(n);
    }

  }

  /**
   * Leitor de String em UTF8
   * 
   * 
   * @author Tecgraf
   */
  public static class StringInputStream extends InputStream {

    /** Stream */
    private final InputStream input;

    /**
     * @param input
     */
    public StringInputStream(InputStream input) {
      this.input = input;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
      int c = this.input.read();
      if (c <= 0x7F) {
        return c;
      }
      else if ((c >> 5) == 0x6) {
        int i2 = this.input.read();
        return ((c & 0x1F) << 6) + (i2 & 0x3F);
      }
      else {
        int i2 = this.input.read();
        int i3 = this.input.read();
        return ((c & 0xF) << 12) + ((i2 & 0x3F) << 6) + (i3 & 0x3F);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException {
      return this.input.available();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
      this.input.close();
    }

  }

  /**
   * Handler para contar as linhas
   * 
   * @author Tecgraf
   */
  private static class MyHandler extends DefaultHandler {

    /** Documento */
    private final Document doc;
    /** Locator */
    private Locator locator;
    /** Pilha */
    private final Stack<Element> elementStack = new Stack<Element>();
    /** Texto */
    private final StringBuilder textBuffer = new StringBuilder();

    /**
     * Construtor
     * 
     * @param doc
     */
    public MyHandler(Document doc) {
      this.doc = doc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName,
      final String qName, final Attributes attributes) throws SAXException {
      addTextIfNeeded();
      final Element el = doc.createElement(qName);
      for (int i = 0; i < attributes.getLength(); i++) {
        el.setAttribute(attributes.getQName(i), attributes.getValue(i));
      }
      el.setUserData("line", String.valueOf(this.locator.getLineNumber()), null);
      elementStack.push(el);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String localName,
      final String qName) {
      addTextIfNeeded();
      final Element closedEl = elementStack.pop();
      if (elementStack.isEmpty()) {
        doc.appendChild(closedEl);
      }
      else {
        final Element parentEl = elementStack.peek();
        parentEl.appendChild(closedEl);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(final char ch[], final int start, final int length)
      throws SAXException {
      textBuffer.append(ch, start, length);
    }

    /**
     * Adiciona o texto se precisar
     */
    private void addTextIfNeeded() {
      if (textBuffer.length() > 0) {
        final Element el = elementStack.peek();
        final Node textNode = doc.createTextNode(textBuffer.toString());
        el.appendChild(textNode);
        textBuffer.delete(0, textBuffer.length());
      }
    }
  }

}
