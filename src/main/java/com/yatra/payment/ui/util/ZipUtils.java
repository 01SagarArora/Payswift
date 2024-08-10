package com.yatra.payment.ui.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

import org.apache.log4j.Logger;
import org.castor.util.Base64Decoder;
import org.castor.util.Base64Encoder;


public class ZipUtils {

	public static Logger logger = Logger.getLogger(ZipUtils.class);

    public static String getZipped(String str)
    {
    	if (str == null || str.length() == 0) {
            return str;
        }
    	
    	String retStr=str;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
        GZIPOutputStream zip;
		try {
			zip = new GZIPOutputStream(out);
			IO.dump(in, zip);
	        zip.close();
	        retStr  = new String(Base64Encoder.encode(out.toByteArray()));
		} catch (IOException e) {
			logger.error(e.getStackTrace());
		}
		
		return retStr;
    }
    
    public static String getUnZipped(InputStream in)
    {
    	String retStr="";
		try {
			InputStream gzipStream = new GZIPInputStream(in);
			Reader decoder = new InputStreamReader(gzipStream);
			BufferedReader buffered = new BufferedReader(decoder);
			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = buffered.readLine()) != null) {
				sb.append(line);
			}
			retStr = sb.toString();
		} catch (IOException e) {
			logger.error(e.getStackTrace());
		}
		return retStr;
    }
    
    public static String getUnZippedBase64(String str){
    	if(str.length()>1)
    		return getUnZipped(new ByteArrayInputStream(Base64Decoder.decode(str)));
    	else return str;
	}
	    
	public static byte[] compressByteArray(byte[] data) throws IOException {
		Deflater deflater = new Deflater();
		deflater.setInput(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
				data.length);

		deflater.finish();
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer); // returns the generated
													// code... index
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		byte[] output = outputStream.toByteArray();

		deflater.end();
		return output;
	}
    
	public static byte[] decompressByteArray(byte[] data) throws IOException,
			DataFormatException {
		Inflater inflater = new Inflater();
		inflater.setInput(data);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
				data.length);
		byte[] buffer = new byte[1024];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		byte[] output = outputStream.toByteArray();

		inflater.end();
		return output;
	}
}
