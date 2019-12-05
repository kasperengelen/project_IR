import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class that provides a handler for the SAX XML parsing library.
 * This handler will parse documents as specified in the project requirements.
 */
public class DocumentHandler extends DefaultHandler
{
    String m_title;
    String m_question;
    String m_tags;

    // concatenation of all answers
    String m_answers;

    boolean m_inQuestion = false;
    boolean m_inAnswer = false;
    boolean m_inTitle = false;
    boolean m_inBody = false;
    boolean m_inTags = false;

    /**
     * Notify that an opening tag has been encountered.
     *
     * @param uri
     * @param localName
     * @param qName The name of the tag.
     * @throws SAXException
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        String lowercase_tagname = qName.toLowerCase();

        switch(lowercase_tagname)
        {
            case "question":
                m_inQuestion = true;
                break;
            case "answer":
                m_inAnswer = true;
                break;
            case "title":
                m_inTitle = true;
                break;
            case "body":
                m_inBody = true;
                break;
            case "tags":
                m_inTags = true;
                break;
            default:
                throw new SAXException(String.format("Invalid tag '%s'", qName));
        }
    }

    /**
     * Notify that a closing tag has been encountered.
     *
     * @param uri
     * @param localName
     * @param qName The name of the tag.
     * @throws SAXException
     */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        String lowercase_tagname = qName.toLowerCase();

        switch(lowercase_tagname)
        {
            case "question":
                m_inQuestion = false;
                break;
            case "answer":
                m_inAnswer = false;
                break;
            case "title":
                m_inTitle = false;
                break;
            case "body":
                m_inBody = false;
                break;
            case "tags":
                m_inTags = false;
                break;
            default:
                throw new SAXException(String.format("Invalid tag '%s'", qName));
        }
    }

    /**
     * Read characters contained in an element.
     *
     * @param ch A character array that represents the encountered characters.
     * @param start
     * @param length
     * @throws SAXException
     */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if(m_inQuestion && m_inTitle) {
            m_title = new String(ch);
        } else if(m_inQuestion && m_inBody) {
            m_question = new String(ch);
        } else if(m_inQuestion && m_inTags) {
            m_tags = new String(ch);
        } else if (m_inAnswer && m_inBody) {
            m_answers += " " + new String(ch);
        } else {
            throw new SAXException(String.format(
                    "Invalid state: inQuestion='%b', inAnswer='%b', inBody='%b', inTags='%b', inTitle='%b'",
                    m_inQuestion, m_inAnswer, m_inBody, m_inTags, m_inTitle
            ));
        }
    }
}
