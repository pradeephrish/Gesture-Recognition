package com.asu.mwdb.matlab;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

public class MatlabObject {
	
	private static MatlabProxy _instance = null;
	
	private MatlabObject() {
	}

	public static MatlabProxy getInstance() throws MatlabConnectionException {
		MatlabProxyFactory factory = new MatlabProxyFactory();
		if(_instance == null) {
			_instance = factory.getProxy();
		}
		return _instance;
	}
}
