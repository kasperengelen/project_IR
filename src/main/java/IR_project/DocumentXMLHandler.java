package IR_project;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that provides a handler for the SAX XML parsing library.
 * This handler will parse documents as specified in the project requirements.
 */
public class DocumentXMLHandler extends DefaultHandler
{
    private String m_title = "";
    private String m_question = "";
    private String m_tags = "";
    private List<String> m_answers = new ArrayList<>();

    private boolean m_inDocument = false;
    private boolean m_inQuestion = false;
    private boolean m_inAnswer = false;
    private boolean m_inTitle = false;
    private boolean m_inBody = false;
    private boolean m_inTags = false;

    private StringBuilder chars = new StringBuilder();

    /**
     * Retrieve the title of the parsed document.
     */
    public String getTitle()
    {
        return m_title;
    }

    /**
     * Retrieve the question of the parsed document.
     */
    public String getQuestion()
    {
        return m_question;
    }

    /**
     * Retrieve a list of answers of the parsed document.
     */
    public List<String> getAnswers()
    {
        return m_answers;
    }

    /**
     * Retrieve the tags of the parsed document.
     */
    public String getTags() {
        return m_tags;
    }

    /**
     * Notify that an opening tag has been encountered.
     *
     * @param uri ?
     * @param localName ?
     * @param qName The name of the tag.
     * @throws SAXException
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        String lowercase_tagname = qName.toLowerCase();

        switch(lowercase_tagname)
        {
            case "document":
                m_inDocument = true;
                break;
            case "question":
                m_inQuestion = true;
                break;
            case "answer":
                m_inAnswer = true;
                break;
            case "title":
                chars.setLength(0);
                m_inTitle = true;
                break;
            case "body":
                chars.setLength(0);
                m_inBody = true;
                break;
            case "tags":
                chars.setLength(0);
                m_inTags = true;
                break;
            default:
                throw new DocumentXMLHandlerException(String.format("Invalid tag '%s'", qName));
        }
    }

    /**
     * Notify that a closing tag has been encountered.
     *
     * @param uri ?
     * @param localName ?
     * @param qName The name of the tag.
     * @throws SAXException
     */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if(m_inDocument && m_inQuestion && m_inTitle) {
            m_title = chars.toString();
        } else if(m_inDocument && m_inQuestion && m_inBody) {
            m_question = chars.toString();
        } else if(m_inDocument && m_inQuestion && m_inTags) {
            m_tags = chars.toString();
        } else if (m_inDocument && m_inAnswer && m_inBody) {
            m_answers.add(chars.toString());
        } // other cases are not useful

        String lowercase_tagname = qName.toLowerCase();

        switch(lowercase_tagname)
        {
            case "document":
                m_inDocument = false;
                break;
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
                throw new DocumentXMLHandlerException(String.format("Invalid tag '%s'", qName));
        }
    }

    /**
     * Read characters contained in an element.
     *
     * @param ch A character array that represents the encountered characters.
     * @param start The index where the text begins.
     * @param length The amount of characters that are relevant here.
     * @throws SAXException
     */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        // Need to use string builder here, see:
        // https://stackoverflow.com/questions/13336140/sax-parsing-and-special-characters
        chars.append(ch, start, length);
    }

    /**
     * Replace HTML codes for special characters with the actual special characters.
     */
    private static String replaceHTMLCodes(String input)
    {
        // Left this function for now, but I think we can remove it later
        input = input.replace("&gt;", ">");
        input = input.replace("&lt;", "<");
        input = input.replace("&amp;", "&");
        input = input.replace("&frasl;", "/");
        input = input.replace("<code>", "");

        return input;
    }

    /**
     * Exception that can be thrown when things go wrong here.
     */
    public static class DocumentXMLHandlerException extends SAXException
    {
        public DocumentXMLHandlerException(String message)
        {
            super(message);
        }
    }
}
