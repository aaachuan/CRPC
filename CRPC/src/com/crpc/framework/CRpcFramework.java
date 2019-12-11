package com.crpc.framework;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;



public class CRpcFramework {

	public static void export(final Object service, int port) throws Exception {  
    
        System.out.println("Export service " + service.getClass().getName() + " on port " + port);  
        
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        for(;;) {  
            try {  
                SocketChannel socketChannel = serverSocketChannel.accept();
                new Thread(() -> {    
                        try {  
                            try {  
                                ObjectInputStream input = new ObjectInputStream(socketChannel.socket().getInputStream());  
                                try {  
                                    String methodName = input.readUTF();  
                                    Class<?>[] parameterTypes = (Class<?>[])input.readObject();  
                                    Object[] arguments = (Object[])input.readObject();  
                                    ObjectOutputStream output = new ObjectOutputStream(socketChannel.socket().getOutputStream());  
                                    try {  
                                        Method method = service.getClass().getMethod(methodName, parameterTypes);  
                                        Object result = method.invoke(service, arguments);  
                                        output.writeObject(result);  
                                    } catch (Throwable t) {  
                                        output.writeObject(t);  
                                    } finally {  
                                        output.close();  
                                    }  
                                } finally {  
                                    input.close();  
                                }  
                            } finally {  
                            	socketChannel.close();  
                            }  
                        } catch (Exception e) {  
                            e.printStackTrace();  
                        }  
                      
                }).start();  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
    }  
	
	@SuppressWarnings("unchecked")
	public static <T> T refer(final Class<T> interfaceClass, final String host, final int port) {
		System.out.println("Get remote service " + interfaceClass.getName() + " from server " + host + ":" + port);  
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] {interfaceClass}, new InvocationHandler() {  
            public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {  
            	
            	SocketChannel socketChannel = SocketChannel.open();
    			socketChannel.connect(new InetSocketAddress(host, port));
    			
                try {  
                    ObjectOutputStream output = new ObjectOutputStream(socketChannel.socket().getOutputStream());  
                    try {  
                        output.writeUTF(method.getName());  
                        output.writeObject(method.getParameterTypes());  
                        output.writeObject(arguments);  
                        ObjectInputStream input = new ObjectInputStream(socketChannel.socket().getInputStream());  
                        try {  
                            Object result = input.readObject();  
                            if (result instanceof Throwable) {  
                                throw (Throwable) result;  
                            }  
                            return result;  
                        } finally {  
                            input.close();  
                        }  
                    } finally {  
                        output.close();  
                    }  
                } finally {  
                    socketChannel.close();  
                }  
            }  
        });  
		
	}
}
