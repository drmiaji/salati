package com.salati

import android.content.Context
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class XmlFileHandler(private val context: Context) {

    private val fileName = "mfz.xml"

    // Function to check or create the XML file in app-specific storage
    fun checkOrCreateXmlFile() {
        val xmlFile = File(context.filesDir, fileName)

        if (!xmlFile.exists()) {
            // If the file doesn't exist, create it and write default content if necessary
            Log.i("XMLFileHandler", "$fileName not found, creating a new file.")
            xmlFile.writeText("<root></root>") // default XML structure
        } else {
            Log.i("XMLFileHandler", "$fileName found, ready for parsing.")
        }
    }

    // Function to parse the XML file
    fun parseXmlFile() {
        val xmlFile = File(context.filesDir, fileName)

        try {
            FileInputStream(xmlFile).use { inputStream ->
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(inputStream, "UTF-8")

                // Parsing logic
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            Log.d("XMLParsing", "Start tag ${parser.name}")
                            // Process the start tag and its attributes
                        }

                        XmlPullParser.TEXT -> {
                            Log.d("XMLParsing", "Text ${parser.text}")
                            // Process text inside a tag
                        }

                        XmlPullParser.END_TAG -> {
                            Log.d("XMLParsing", "End tag ${parser.name}")
                            // Process end tag
                        }
                    }
                    eventType = parser.next()
                }
            }
            Log.i("XMLParsing", "$fileName parsed successfully.")
        } catch (e: FileNotFoundException) {
            Log.e("XMLParsing", "$fileName file not found.")
        } catch (e: Exception) {
            Log.e("XMLParsing", "Error parsing $fileName: ${e.message}")
        }
    }
}
