package com.ice2systems.voices.srt;

import java.util.regex.Pattern;

import java.util.regex.Matcher;


public class XmlHelper {
	/**
	 * return true if the String passed in is something like XML
	 *
	 *
	 * @param inString a string that might be XML
	 * @return true of the string is XML, false otherwise
	 */
	private XmlHelper() {}
	
	
	public static XmlData getXMLData(String inXMLStr) {
	
		  XmlData result = XmlData.notXML();
	    Pattern pattern;
	    Matcher matcher;
	
	    // REGULAR EXPRESSION TO SEE IF IT AT LEAST STARTS AND ENDS
	    // WITH THE SAME ELEMENT
	    final String XML_PATTERN_STR = "<(\\S+?)(.*?)>(.*?)</\\1>";
	
	    // IF WE HAVE A STRING
	    if (inXMLStr != null && inXMLStr.trim().length() > 0) {
	
	    			inXMLStr = inXMLStr.trim();
	        // IF WE EVEN RESEMBLE XML
	        if (inXMLStr.startsWith("<")) {
	
	            pattern = Pattern.compile(XML_PATTERN_STR,
	            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
	
	            // RETURN TRUE IF IT HAS PASSED BOTH TESTS
	            matcher = pattern.matcher(inXMLStr);
	            
	            if(matcher.matches()) {// full string is an XML fragment
	            		result = new XmlData(matcher.group(1), matcher.group(3), XmlData.XMLState.FULL );
	            }    
	            else if(inXMLStr.contains("</") && inXMLStr.contains(">")) {
	        	    		final String CLOSE_XML_PATTERN_STR = "</(\\S+?)>";	 	    
		        	    	pattern = Pattern.compile(CLOSE_XML_PATTERN_STR,
		      	            Pattern.CASE_INSENSITIVE | Pattern.DOTALL );
	        	    	
		        	    	matcher = pattern.matcher(inXMLStr);
		        	    	
		        	    	if(matcher.matches()) {// string contains closing tag
		            		result = new XmlData(matcher.group(1), "", XmlData.XMLState.CLOSE_TAG);
		        	    	}
	            }
	            else if(inXMLStr.contains(">")) {// no closing tag, but possible just opening tag
		        	    	final String OPEN_XML_PATTERN_STR = "<(\\S+)>(.*?)";	  
		        	    	pattern = Pattern.compile(OPEN_XML_PATTERN_STR,
		      	            Pattern.CASE_INSENSITIVE | Pattern.DOTALL );
		        	    	
		        	    	matcher = pattern.matcher(inXMLStr);
		        	    	
		        	    	if(matcher.matches()) {// string contains opening tag, expect closing tag later
		            		result = new XmlData(matcher.group(1), matcher.group(2), XmlData.XMLState.OPEN_TAG );
		        	    	}
	            }
	        }
          else if(inXMLStr.contains("</") && inXMLStr.contains(">") ) {
		  	    		final String CLOSE_XML_PATTERN_STR = "(.*?)</(\\S+?)>";	 	    
		    	    	pattern = Pattern.compile(CLOSE_XML_PATTERN_STR,
		  	            Pattern.CASE_INSENSITIVE | Pattern.DOTALL );
		  	    	
		    	    	matcher = pattern.matcher(inXMLStr);
		    	    	
		    	    	if(matcher.matches()) {// string contains closing tag
		        		result = new XmlData(matcher.group(2), matcher.group(1), XmlData.XMLState.CLOSE_TAG);
		    	    	}
          }	        

	    }
	
	    return result;
	}
	
	/*
	public static void main(String[] args) {
		XmlData data = isXMLLike("<i>dfgfdsdfg</i>");
		System.out.println("^^^^^^^^^^^^^^^^");
		
		System.out.println("state = " + data.state);	
		
		if(data.state != XmlData.XMLState.NOT_XML) {
			System.out.println("tag = " + data.tag);	
		}
		if(data.content != null) {
			System.out.println("content = "+data.content);	
		}
	}
*/
}