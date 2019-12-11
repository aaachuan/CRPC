package com.crpc.consumer;

import com.crpc.framework.CRpcFramework;
import com.crpc.provider.service.BinarySearch;

public class Consumer {

	public static void main(String[] args) {
		BinarySearch service = CRpcFramework.refer(BinarySearch.class, "127.0.0.1", 8080);
		int[] a = {1,2,3,4,5};
		int i = service.rank(6, a);
        System.out.println(i);
	}

}
