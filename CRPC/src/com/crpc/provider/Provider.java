package com.crpc.provider;

import com.crpc.framework.CRpcFramework;
import com.crpc.provider.service.BinarySearch;
import com.crpc.provider.service.BinarySearchImpl;

public class Provider {

	public static void main(String[] args) throws Throwable {
		BinarySearch service = new BinarySearchImpl();
		CRpcFramework.export(service, 8080);

	}

}
