import java.io.IOException;
import java.text.ParseException;

import org.breder.xml.XmlNode;
import org.junit.Test;


public class ReaderXmlTest {

  @Test
  public void test() throws IOException, ParseException {
    System.out.println(new XmlNode(this.getClass().getResourceAsStream(
      "/test.xml")));
  }

}
