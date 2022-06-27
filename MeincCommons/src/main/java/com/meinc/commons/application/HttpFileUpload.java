package com.meinc.commons.application;

import java.io.File;
import java.io.Serializable;

public class HttpFileUpload implements Serializable {
	
	private static final long serialVersionUID = -2661183435941378604L;
	
	public File savedFile;
	public String originalFileName;
	public String requestParameterKey;
}
