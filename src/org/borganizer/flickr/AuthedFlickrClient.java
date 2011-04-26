package org.borganizer.flickr;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.people.User;

import org.xml.sax.SAXException;

/**
 * @author Utkarsh Srivastava
 */
public class AuthedFlickrClient {
  private Flickr flickr;
  private User currentUser;
  private String token;

  public AuthedFlickrClient(String apiKey, String secret, String token) throws IOException, SAXException,
      FlickrException, ParserConfigurationException {

    flickr = new Flickr(apiKey, secret, new REST());
    this.token = token;
    Flickr.debugRequest = false;
    Flickr.debugStream = false;

    currentUser = initializeCurrentUser();
  }

  public void setCurrentThreadAuth() {
    Auth auth = new Auth();
    auth.setPermission(Permission.WRITE);
    auth.setToken(token);
    RequestContext.getRequestContext().setAuth(auth);
  }

  private User initializeCurrentUser() throws IOException, SAXException,
      FlickrException {
    setCurrentThreadAuth();
    return flickr.getAuthInterface().checkToken(token).getUser();
  }

  public Flickr getFlickr() {
    return flickr;
  }

  public User getCurrentUser() {
    return currentUser;
  }
}
