package com.futureconcepts.jupiter.util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class ParserUtils
{
    public static XmlPullParser createParserFrom(InputStream inputStream) throws IOException, XmlPullParserException
    {
        if (inputStream == null)
        {
            throw new IllegalArgumentException("inputStream");
        }
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser parser = factory.newPullParser();
        
        try
        {
            parser.setInput(inputStream, null);
        }
        catch (XmlPullParserException e)
        {
            throw new IOException(e.toString());
        }
        return parser;
    }

     public static String getMimeTypeForTag(XmlPullParser parser) throws IOException
     {
        int attributeCount = parser.getAttributeCount();
        String type = null;
        if (attributeCount == 0)
        {
            type = "text";
        }
        else if (attributeCount == 1)
        {
            type = parser.getAttributeValue(0);
        }
        else if (attributeCount > 1)
        {
            throw new IOException("Too many attributes found: Number = " + attributeCount);
        }
        return type;
    }

    public static String getAttributeByName(String name, XmlPullParser parser)
    {
        int attributeCount = parser.getAttributeCount();
        for (int i = 0; i < attributeCount; i++)
        {
            if (name.equals(parser.getAttributeName(i)))
            {
                return parser.getAttributeValue(i);
            }
        }
        return null;
    }
    
    /**
     * Skip sub tree that is currently porser positioned on.
     * <br>NOTE: parser must be on START_TAG and when funtion returns
     * parser will be positioned on corresponding END_TAG. 
     */

   //	Implementation copied from Alek's mail... 

   public static void skipSubTree(XmlPullParser parser) throws XmlPullParserException, IOException
   {
      parser.require(XmlPullParser.START_TAG, null, null); // See kXml
       int level = 1;
       while (level > 0)
       {
           int eventType = parser.next();
           if (eventType == XmlPullParser.END_TAG)
           {
               --level;
           }
           else if (eventType == XmlPullParser.START_TAG)
           {
               ++level;
           }
       }
   }
}
