package groovy.servlet

import groovy.servlet.AbstractHttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletContext
import javax.servlet.ServletConfig
import java.net.URL

/**
* This test case tests the AbstractHttpServlet class. It uses a test
* specific subclass called ConcreteHttpServlet to test the abstract
* class in isolation from any implementations. 
* 
* @author Hamlet D'Arcy
*/ 
class AbstractHttpServletTest extends GroovyTestCase {

	def servlet; 
	
	void setUp() {
		super.setUp()
		servlet = new ConcreteHttpServlet()
	}
	
	/**
	* getScriptUri() concatenates the servlet path and path info 
	* attributes if attributes exist on the http request. 
	*/
	void testGetScriptUri_AllAttributesExist() {

		//just return whatever attributes were requested
		def request = {attribute -> attribute} 

		assert servlet.getScriptUri(request as HttpServletRequest) ==
			AbstractHttpServlet.INC_SERVLET_PATH + AbstractHttpServlet.INC_PATH_INFO
	}

	/**
	* getScriptUri() returns the servlet path if the http request
	* contains path but no path info attribute.  
	*/
	void testGetScriptUri_NoPathInfoAttribute() {

		//just return whatever attributes were requested, except for path info attribute
		def request = {attribute -> 
			if (attribute == AbstractHttpServlet.INC_PATH_INFO) {
				return null 
			}
			attribute
		} 

		assert servlet.getScriptUri(request as HttpServletRequest) ==
			AbstractHttpServlet.INC_SERVLET_PATH
	}
	
	/**
	* Tests getScriptUri when no attributes exist, but servletPath and 
	* pathInfo methods return data. 
	*/
	void testGetScriptUri_NoAttributesPathInfoExists() {
		def request = [ 
			getAttribute: {null}, 
		    getServletPath: {"servletPath"}, 
		    getPathInfo: {"pathInfo"}] as HttpServletRequest

		def servlet = new ConcreteHttpServlet()
		assert servlet.getScriptUri(request) == "servletPathpathInfo"
		
	}

	/**
	* Tests getScriptUri when no attributes exist, no path info exists, 
	* but servletPath returns data. 
	*/
	void testGetScriptUri_NoAttributesPathInfoMissing() {
		def request = [ 
			getAttribute: {null}, 
		    getServletPath: {"servletPath"}, 
		    getPathInfo: {null}] as HttpServletRequest

		def servlet = new ConcreteHttpServlet()
		assert servlet.getScriptUri(request) == "servletPath"
		
	}
	
	/**
	* Tests getting URIs as files. 
	*/ 
	void testGetScriptURIasFile() {

		def request = [ 
		    getAttribute: {null}, 
		    getServletPath: {"servletPath"}, 
		    getPathInfo: {"pathInfo"}] as HttpServletRequest

		def servletContext = [
		    getRealPath: { arg-> "realPath" + arg}] as ServletContext

		def servletConfig = [
		    getServletContext: {servletContext}, 
		    getInitParameter: {null}] as ServletConfig

		servlet.init(servletConfig)
		def file = servlet.getScriptUriAsFile(request)
		assert file.getName() == "realPathservletPathpathInfo"		
	}
	
	/**
	* Tests that exception is thrown when resource is not found. 
	*/ 
	void testGetResourceConnection_MissingResource() {
		def servletContext = [
		    getRealPath: {arg-> "realPath" + arg}, 
		    getResource: {arg -> null} ] as ServletContext

		def servletConfig = [
		    getServletContext: {servletContext}, 
		    getInitParameter: {null}] as ServletConfig

		//servlet config is used to find resources
		servlet.init(servletConfig)    

		shouldFail(groovy.util.ResourceException) {
		    servlet.getResourceConnection("someresource")
		}
	}

	/**
	* Tests finding resource. 
	*/ 
	public void testGetResourceConnection_FoundInCurrentDir() {
		def urlStub = new java.net.URL("http://foo")
		def servletContext = [
		    getRealPath: { arg-> "realPath" + arg}, 
		    getResource: {arg -> 
				if (arg == "/someresource") return urlStub
				else return null
			} ] as ServletContext

		def servletConfig = [
		    getServletContext: {servletContext}, 
		    getInitParameter: {null}] as ServletConfig

		//servlet config is used to find resources
		servlet.init(servletConfig)    

		def connection = servlet.getResourceConnection("someresource")

		assert connection.getURL() == urlStub		
	}

	/**
	* Tests finding resource in web-inf directory. 
	*/ 
	public void testGetResourceConnection_FoundInWebInf() {
		def urlStub = new java.net.URL("http://foo")
		def servletContext = [
		    getRealPath: { arg-> "realPath" + arg}, 
		    getResource: {arg -> 
				if (arg == "/WEB-INF/groovy/someresource") return urlStub
				else return null
			} ] as ServletContext

		def servletConfig = [
		    getServletContext: {servletContext}, 
		    getInitParameter: {null}] as ServletConfig

		//servlet config is used to find resources
		servlet.init(servletConfig)    

		def connection = servlet.getResourceConnection("someresource")

		assert connection.getURL() == urlStub		
	}
	
	/**
	* Tests regex style resource replacement for first occurence. 
	*/ 
	public void testGetResourceConnection_Replace1stFooWithBar() {
		def servletContext = [
		    getRealPath: {arg -> "realPath" + arg}, 
		    getResource: {arg -> 
		      if (arg.startsWith("//")) arg=arg.substring(2)
		      new URL("http://" + arg)}
		] as ServletContext

		def servletConfig = [
		    getServletContext: {servletContext}, 
		    getInitParameter: {arg ->
		        //replace first occurence of foo resources with bar resources
		        if (arg == "resource.name.regex") return "foo"
		        else if (arg == "resource.name.replacement") return "bar" 
		        else if (arg == "resource.name.replace.all") return "false"
		        else  return null
		    }] as ServletConfig

		//servlet config is used to find resources
		servlet.init(servletConfig)    

		//replace first foo with bar in resources
		def connection = servlet.getResourceConnection("/somefoo/foo")
		//expecting http://somebar/foo
		def actual = connection.getURL().toExternalForm() 
		def expected = new URL("http://somebar/foo").toExternalForm()
		assert actual == expected		
	}

	/**
	* Tests regex style resource replacement for all occurences. 
	*/ 
	public void testGetResourceConnection_ReplaceAllFooWithBar() {
		def servletContext = [
		    getRealPath: {arg -> "realPath" + arg}, 
		    getResource: {arg -> 
		      if (arg.startsWith("//")) arg=arg.substring(2)
		      new URL("http://" + arg)
		}] as ServletContext

		def servletConfig = [
		    getServletContext: {servletContext}, 
		    getInitParameter: {arg ->
		        //replace all occurences of foo resources with bar resources
		        if (arg == "resource.name.regex") return "foo"
		        else if (arg == "resource.name.replacement") return "bar" 
		        else if (arg == "resource.name.replace.all") return "true"
		        else  return null
		    }] as ServletConfig

		//servlet config is used to find resources
		servlet.init(servletConfig)    

		//replace first foo with bar in resources
		def connection = servlet.getResourceConnection("/somefoo/foo")
		//expecting http://somebar/foo
		def actual = connection.getURL().toExternalForm() 
		def expected = new URL("http://somebar/bar").toExternalForm()
		assert actual == expected		
	}
}

//test specific subclass
class ConcreteHttpServlet extends AbstractHttpServlet {}

