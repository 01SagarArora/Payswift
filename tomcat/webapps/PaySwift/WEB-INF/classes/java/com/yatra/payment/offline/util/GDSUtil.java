/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yatra.payment.offline.util;

import com.yatra.payment.offline.beans.ClientFileDisplayMods;
import com.yatra.payment.offline.beans.ClientFileMaintenanceMods;
import com.yatra.payment.offline.beans.ClientFileMods;
import com.yatra.payment.offline.beans.GDSCardInfo;
import com.yatra.payment.ui.controller.RuntimeDBLookUpController;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author YATRAONLINE\rohit.lohia
 */
public class GDSUtil {
        public static X509TrustManager s_x509TrustManager = null;
	public static SSLSocketFactory s_sslSocketFactory = null;
        
        private static Logger logger = Logger.getLogger(RuntimeDBLookUpController.class);

        private static MessageFactory messageFactory;
        
        static {
            s_x509TrustManager = new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[] {};
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
                    }
            };

            java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            try {
                    messageFactory = MessageFactory.newInstance();
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, new X509TrustManager[] { s_x509TrustManager }, null);
                    s_sslSocketFactory = context.getSocketFactory();
            } catch (Exception e) {
                    logger.error(e.getStackTrace());
                    throw new RuntimeException("Some Error Occured 1");
            }
	}
        
        private static Document convertStringToDocument(String xmlStr) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           try {
                factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
                logger.info("SUCCESSFUL in setting  http://javax.xml.XMLConstants/feature/secure-processing for DocumentBuilderFactory to prevent XXE attacks");
            }catch (Exception e){
                logger.fatal("Exception in setting http://javax.xml.XMLConstants/feature/secure-processing for DocumentBuilderFactory \n CAUTION : CODE PRONE TO XXE Attacks\n Read More About XXE Attacks https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet");
            }
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
                return doc;
            } catch (Exception e) {
                logger.error(e.getStackTrace());
            }
            return null;
        }
        
        private static String convertObjectToXml(ClientFileMods clientFileMods) throws JAXBException {
            Class c;
            if (clientFileMods instanceof ClientFileDisplayMods) {
                c = ClientFileDisplayMods.class;
            } else {
                c = ClientFileMaintenanceMods.class;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(c);
            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(clientFileMods, out);
            String responseXML = new String(out.toByteArray());
            return responseXML;
        }
        
        private static Document generateClientFileMaintenanceModsDocument(JSONObject data) throws JSONException, JAXBException {
            ClientFileMods mods = new ClientFileMaintenanceMods();
            mods.setBusinessTitle(data.getString("businessTitle"));
            mods.setCrsId(data.getString("crsId"));
            mods.setPersonalTitle(data.getString("personalTitle"));
            mods.setPcc(data.getString("pcc"));
            mods.setFileInd(data.getString("fileInd"));
            String xml = convertObjectToXml(mods);
            return convertStringToDocument(xml);
        }
        
        private static void generateClientFileFixedLineDataDocumentDelete(JSONObject data, SOAPElement parent, SOAPEnvelope envelope) throws JSONException, JAXBException, SOAPException {
            SOAPElement clientFileFixedLineData = parent.addChildElement(envelope.createName("ClientFileFixedLineData"));
            SOAPElement lineNo = clientFileFixedLineData.addChildElement(envelope.createName("LineNum"));
            lineNo.addTextNode(data.getString("lineNumber"));
            SOAPElement moveInd = clientFileFixedLineData.addChildElement(envelope.addChildElement("MoveInd"));
            moveInd.addTextNode(data.getString("moveInd"));
            clientFileFixedLineData.addChildElement(envelope.createName("ClientInd"));
            clientFileFixedLineData.addChildElement(envelope.createName("SecondaryInd"));
            clientFileFixedLineData.addChildElement(envelope.createName("TertiaryInd"));
            clientFileFixedLineData.addChildElement(envelope.createName("DataType"));
        }
        
        private static void generateClientFileFixedLineDataDocumentAdd(JSONObject data, SOAPElement parent, SOAPEnvelope envelope) throws JSONException, JAXBException, SOAPException {
            SOAPElement clientFileFixedLineData = parent.addChildElement(envelope.createName("ClientFileFixedLineData"));
            SOAPElement lineNo = clientFileFixedLineData.addChildElement(envelope.createName("LineNum"));
            lineNo.addTextNode(data.getString("lineNumber"));
            SOAPElement moveInd = clientFileFixedLineData.addChildElement(envelope.addChildElement("MoveInd"));
            moveInd.addTextNode(data.getString("moveInd"));
            clientFileFixedLineData.addChildElement(envelope.createName("ClientInd"));
            clientFileFixedLineData.addChildElement(envelope.createName("SecondaryInd"));
            clientFileFixedLineData.addChildElement(envelope.createName("TertiaryInd"));
            SOAPElement dataType = clientFileFixedLineData.addChildElement(envelope.createName("DataType"));
            dataType.addTextNode("0021"); 
        }
        
        private static String createTextNodeCardDetails(JSONObject data) throws JSONException {
            StringBuilder builder = new StringBuilder();
            builder.append(data.getString("cardVendor"));
            builder.append(data.getString("cardNumber"));
            builder.append("/D");
            builder.append(data.getString("expiryMonth"));
            builder.append(data.getString("expiryYear"));
            return builder.toString();
        }
        
        private static void generateClientFileVariableLineDataDocument(JSONObject data, SOAPElement parent, SOAPEnvelope envelope) throws JSONException, JAXBException, SOAPException {
            SOAPElement clientFileVariableLineData = parent.addChildElement(envelope.createName("ClientFileVariableLineData"));
            SOAPElement cardData = clientFileVariableLineData.addChildElement(envelope.createName("Data"));
            cardData.addTextNode(createTextNodeCardDetails(data));
        }
        
        private static Document generateClientFileDisplayModsDocument(JSONObject data) throws JSONException, JAXBException {
            ClientFileMods mods = new ClientFileDisplayMods();
            mods.setBusinessTitle(data.getString("businessTitle"));
            mods.setCrsId(data.getString("crsId"));
            mods.setPersonalTitle(data.getString("personalTitle"));
            mods.setMergeInd(data.getString("mergeInd"));
            mods.setPcc(data.getString("pcc"));
            mods.setFileInd(data.getString("fileInd"));
            String xml = convertObjectToXml(mods);
            return convertStringToDocument(xml);
        }
        
        private static String generateRequestXmlForDisplayPARSoap(JSONObject data) throws SOAPException, IOException, JSONException, JAXBException {
            SOAPMessage soapMessage = messageFactory.createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPBody soapBody = envelope.getBody();
            
            SOAPBodyElement submitXml = soapBody.addBodyElement(envelope.createName("SubmitXml"));
            SOAPElement profile = envelope.addChildElement("Profile");
            profile.addTextNode(data.getString("profile"));
            submitXml.appendChild(profile);
            SOAPElement request = submitXml.addChildElement(envelope.addChildElement("Request"));
            SOAPElement clientFile = envelope.addChildElement("ClientFile_2");
            request.appendChild(clientFile);
            SOAPElement clientFileMods = envelope.addChildElement("ClientFileMods");
            clientFile.appendChild(clientFileMods);
            SOAPElement filter = envelope.addChildElement("Filter");
            filter.appendChild(envelope.addChildElement("_"));
            
            Document dataDocument = generateClientFileDisplayModsDocument(data);
            NodeList list = dataDocument.getElementsByTagName("ClientFileDisplayMods");
            org.w3c.dom.Node node = list.item(0);
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            clientFileMods.appendChild(request.getOwnerDocument().importNode(node, true));
            
            submitXml.addChildElement(filter);
            soapMessage.writeTo(out1);
            String result = new String(out1.toByteArray());
            return result;
        }
        
        private static String generateRequestXmlForDeleteCardSoap(JSONObject data) throws SOAPException, IOException, JSONException, JAXBException {
            SOAPMessage soapMessage = messageFactory.createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPBody soapBody = envelope.getBody();
            
            SOAPBodyElement submitXml = soapBody.addBodyElement(envelope.createName("SubmitXml"));
            SOAPElement profile = envelope.addChildElement("Profile");
            profile.addTextNode(data.getString("profile"));
            submitXml.appendChild(profile);
            SOAPElement request = submitXml.addChildElement(envelope.addChildElement("Request"));
            SOAPElement clientFile = envelope.addChildElement("ClientFile_2");
            request.appendChild(clientFile);
            SOAPElement clientFileMods1 = envelope.addChildElement("ClientFileMods");
            SOAPElement clientFileMods2= envelope.addChildElement("ClientFileMods");
            clientFile.appendChild(clientFileMods1);
            clientFile.appendChild(clientFileMods2);
            SOAPElement filter = envelope.addChildElement("Filter");
            SOAPElement filter2 = envelope.addChildElement("Filter");
            filter.appendChild(envelope.addChildElement("_"));
            filter2.appendChild(envelope.addChildElement("_"));
            
            Document displayModsDocument = generateClientFileDisplayModsDocument(data);
            Document maintenanceModsDocument = generateClientFileMaintenanceModsDocument(data);
            org.w3c.dom.Node displayNode = displayModsDocument.getElementsByTagName("ClientFileDisplayMods").item(0);
            org.w3c.dom.Node maintenanceNode = maintenanceModsDocument.getElementsByTagName("ClientFileMaintenanceMods").item(0);
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            clientFileMods1.appendChild(request.getOwnerDocument().importNode(displayNode, true));
            clientFileMods2.appendChild(request.getOwnerDocument().importNode(maintenanceNode, true));
            generateClientFileFixedLineDataDocumentDelete(data, clientFileMods2, envelope);
            request.appendChild(filter2);
            submitXml.addChildElement(filter);
            soapMessage.writeTo(out1);
            String result = new String(out1.toByteArray());
            return result;
        }
        
        private static String generateRequestXmlForAddCardSoap(JSONObject data) throws SOAPException, IOException, JSONException, JAXBException {
            SOAPMessage soapMessage = messageFactory.createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPBody soapBody = envelope.getBody();
            
            SOAPBodyElement submitXml = soapBody.addBodyElement(envelope.createName("SubmitXml"));
            SOAPElement profile = envelope.addChildElement("Profile");
            profile.addTextNode(data.getString("profile"));
            submitXml.appendChild(profile);
            SOAPElement request = submitXml.addChildElement(envelope.addChildElement("Request"));
            SOAPElement clientFile = envelope.addChildElement("ClientFile_2");
            request.appendChild(clientFile);
            SOAPElement clientFileMods1 = envelope.addChildElement("ClientFileMods");
            SOAPElement clientFileMods2= envelope.addChildElement("ClientFileMods");
            clientFile.appendChild(clientFileMods1);
            clientFile.appendChild(clientFileMods2);
            SOAPElement filter = envelope.addChildElement("Filter");
            SOAPElement filter2 = envelope.addChildElement("Filter");
            filter.appendChild(envelope.addChildElement("_"));
            filter2.appendChild(envelope.addChildElement("_"));
            
            Document displayModsDocument = generateClientFileDisplayModsDocument(data);
            Document maintenanceModsDocument = generateClientFileMaintenanceModsDocument(data);
            org.w3c.dom.Node displayNode = displayModsDocument.getElementsByTagName("ClientFileDisplayMods").item(0);
            org.w3c.dom.Node maintenanceNode = maintenanceModsDocument.getElementsByTagName("ClientFileMaintenanceMods").item(0);
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            clientFileMods1.appendChild(request.getOwnerDocument().importNode(displayNode, true));
            clientFileMods2.appendChild(request.getOwnerDocument().importNode(maintenanceNode, true));
            generateClientFileFixedLineDataDocumentAdd(data, clientFileMods2, envelope);
            generateClientFileVariableLineDataDocument(data, clientFileMods2, envelope);
            request.appendChild(filter2);
            submitXml.addChildElement(filter);
            soapMessage.writeTo(out1);
            String result = new String(out1.toByteArray());
            return result;
        }
        
        public static GDSCardInfo parseCardData(String s) {
            GDSCardInfo gdsCardInfo = new GDSCardInfo();
            String[] cardInfo = s.split("/D");
            String[] cardData = cardInfo[0].split("\\.");
            gdsCardInfo.setCardBrandCode(cardData[1].substring(0, 2));
            gdsCardInfo.setCardNumber(cardData[1].substring(2,cardData[1].length()));
            gdsCardInfo.setExpiryMonth(cardInfo[1].substring(0, 2));
            gdsCardInfo.setExpiryYear(cardInfo[1].substring(2, cardInfo[1].length()));
            return gdsCardInfo;
        }
        
        public static SOAPBody sendPostCallToGalileo(JSONObject data, String requestAction) throws SOAPException, IOException, ParserConfigurationException, TransformerException, TransformerConfigurationException, JSONException, JAXBException, Exception {
            String requestBody = "";
            try {    
                if ("DisplayPAR".equalsIgnoreCase(requestAction)) {
                    data.put("mergeInd", "N");
                    data.put("fileInd", "P");
                    requestBody = generateRequestXmlForDisplayPARSoap(data);
                } else if ("AddCard".equalsIgnoreCase(requestAction)){
                    data.put("moveInd", "Y");
                    data.put("mergeInd", "Y");
                    data.put("fileInd", "P");
                    requestBody = generateRequestXmlForAddCardSoap(data);
                } else if ("DeleteCard".equalsIgnoreCase(requestAction)) {
                    data.put("moveInd", "X");
                    data.put("mergeInd", "Y");
                    data.put("fileInd", "P");
                    requestBody = generateRequestXmlForDeleteCardSoap(data);
                }
                String response = curlCallToGalileo(data, requestBody);
                InputStream is = new ByteArrayInputStream(response.getBytes(Charset.forName("UTF-8")));
                SOAPMessage message = MessageFactory.newInstance().createMessage(null, is);
                SOAPPart soapPart = message.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		SOAPBody soapBody = envelope.getBody();
                SOAPFault fault = soapBody.getFault();
                if (fault != null) {
                    throw new Exception("fault details: " + fault.getFaultString() + " " + " " + fault.getFaultCode());
                }
                NodeList errorNodes = soapBody.getElementsByTagName("ErrText");
                if (errorNodes.getLength() != 0) {
                    Node errorText = errorNodes.item(0);
                    Node errorDetails = errorText.getLastChild();
                    throw new Exception(errorDetails.getTextContent());
                }
                return soapBody;
            } catch (Exception ex) {
                logger.info("Galileo API failed for: " + requestAction, ex);
            }
            return null;
        }
        
        private static String curlCallToGalileo(JSONObject data, String body) throws SOAPException, MalformedURLException, IOException, Exception {
            String username = data.getString("username");
            String password = data.getString("password");
            String urlString = data.getString("url");
            URL url = new URL(urlString);
            String authString = username + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            
            InputStream is;
            OutputStream os;

            Socket s = new Socket(url.getHost(), 443);
            SSLSocket ssl = (SSLSocket) s_sslSocketFactory.createSocket(s, urlString, 443, true);
            ssl.setSoTimeout(3600000);
            ssl.startHandshake();
            os = ssl.getOutputStream();
            is = ssl.getInputStream();
            logger.info("Hitting galileo api.");
            long startTime = System.currentTimeMillis();
            String req = "POST /B2BGateway/service/XMLSelect HTTP/1.0\r\n" + "User-Agent: HTTP Client\r\n" +"Authorization: Basic " + authStringEnc +"\r\n" + "Content-Type: text/xml\r\n" + "Content-Length: " + body.length() + "\r\n\r\n"
                            + body;

            os.write(req.getBytes());
            String res = new String(readAll(is));
            int resIndex = res.indexOf("\r\n\r\n");
            String responseBody = res.substring(resIndex + 4, res.length());
            logger.info("Response received from Galileo: " + responseBody + " in " +(System.currentTimeMillis() - startTime)+" milliseconds");
            return responseBody;
        }

        private static byte[] readAll(InputStream is) throws IOException {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];

            while (true) {
                    int len = is.read(buf);
                    if (len < 0) {
                            break;
                    }
                    baos.write(buf, 0, len);
            }
            return baos.toByteArray();
	}
}
