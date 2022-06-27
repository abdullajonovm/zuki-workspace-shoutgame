package com.meinc.commons.postoffice.directory;

import java.io.File;

public class EmailFile {
  File _file;
  String _emailText;
  
  public String getEmailText() {
    return _emailText;
  }

  @Override
  public String toString() {
    return "[EmailFile] " + _file.getPath() + ": " + _emailText;
  }
}
