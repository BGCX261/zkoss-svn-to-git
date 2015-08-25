package org.zerokode.designer.model;

import java.io.FileOutputStream;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.zkoss.idom.transform.Transformer;



/**
 * Object that provides static methods
 * for writing a ZUML Model to a specified
 * File through a stream.
 * @author chris.spiliotopoulos
 *
 */
public class ModelWriter
{
	/**
	 * Write the model to the file in a standard manner 
	 */
	public static final int MODE_NORMAL = 0;	
	
	/**
	 * Write the model to the file silently, i.e. do not display
	 * MessageBoxes or other UI elements 
	 */
	public static final int MODE_SILENT = 1;
	
	/**
	 * Writes a given ZUML model to a specified
	 * file, using a file stream object.
	 * @param model The ZUML model to be written 
	 * @param sFilename The full filename
	 */
	public static void writeModelToFile(ZUMLModel model, 
										String sFilename, 
										int nMode) throws Exception
	{
		if ((model == null) || (StringUtils.isEmpty(sFilename)))
			return;
		
		// file output stream object
        FileOutputStream streamOut = null; 	

        try
        {
    	    // create a new file output stream
    		streamOut = new FileOutputStream(sFilename);

    		// print the ZUML Document structure to the stream 
    		ModelWriter.writeModelToStream(model, streamOut);

    		// close the stream
            streamOut.close();
        }
        catch (Exception e)
        {
        	if (streamOut != null)
        		streamOut.close();
 
        	throw e;
        }
	}
	
	
	/**
	 * Writes a ZUML model to the specified
	 * FileStream.
	 * @param writer The FileStream object
	 * @throws TransformerException 
	 */
	public static void writeModelToStream(ZUMLModel model, 
								  		  FileOutputStream stream) throws TransformerException
	{
		if ((model == null) || (stream == null))
			return;
		
		// create a new iDOM Transformer object 
		Transformer transformer = new Transformer();
		
		// set the output formatting style of the document
		transformer.setOutputProperty("encoding", "ISO-8859-7");
		transformer.setOutputProperty("indent", "yes");
		transformer.setOutputProperty("omit-xml-declaration", "yes");
		transformer.setOutputProperty("method", "xml");
		
		// transform the Document using the specified style 
		// and print it to the specified FileStream
		transformer.transform(model.getZUMLDocument(), new StreamResult(stream));
	}
}
