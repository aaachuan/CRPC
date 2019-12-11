# CRPC

看完Dubbo作者梁飞老师的一篇关于RPC简易框架的博客随手写的demo，萌生的想基于此重新写个特定语言RPC框架的想法。

一开始，服务远程调用很容易想到用反射和动态代理来实现，梁飞老师写的demo里通信方式采用的Socket是基于BIO实现的，IO效率不高。而java本身的序列化方案也不是很好，序列化的开源机制有很多，Hadoop Avro与Google protobuf等。服务注册可以使用Zookeeper，应用也更稳定。

首先一步一步来，先从IO网络模型开始，主要是想要使用动态代理就难避免`Object*stream`的方案，这样很容易限制了IO选择，写demo时用动态代理来`readObject`是个很好的示例，也有足够的灵活性，比如调用时传参和方法等这些就不需要自己重新封装一个类来实现序列化，服务端动态代理从服务端得到的这些都已经自实现了序列化方案，所以就不需要考虑序列化和反序列的问题。主要还是序列化本身的性能问题。所以一开始还是准备先保留这个动态代理的方案。

接着从NIO开始入手，NIO的优势就是非阻塞，但是有些场景下也不比BIO性能来得好，NIO的重点是Selector，单线程来轮询处理多个Channel，因为为了提高读写效率，另一个特色便是Buffer，大多数场景是使用ByteBuffer，为能拎装上NIO的一系列特性，接下来的想法是先封装一个ByteBuffer与object之间的工具类。NIO的内容还没完整消化完，主要是不知道对应的应用场景及动机。现在的RPC基本都是基于Netty框架来完成，但是Netty也是NIO的一种完美实现，所以Netty就先放一下。Java的IO方案实在是令人头疼，主要的亮点是装饰者模式，感觉就是用一系列类来攻克整个IO的问题，但也不知道这样的灵活性是好是坏......

其实NIO的模型也一定上影响着线程的方案，Java好像没有真正的异步IO方案，比较出名的应该是NodeJS的异步回调了。有想着实现个服务端线程池来支撑多并发场景。

demo可学习的点太多了：
- 尽早失败（断言）设计
```
  if (interfaceClass == null)
            throw new IllegalArgumentException("Interface class == null");
        if (! interfaceClass.isInterface())
            throw new IllegalArgumentException("The " + interfaceClass.getName() + " must be interface class!");
        if (host == null || host.length() == 0)
            throw new IllegalArgumentException("Host == null!");
        if (port <= 0 || port > 65535)
            throw new IllegalArgumentException("Invalid port " + port);
```
- 变量尽量不可变（Immutable Class）
```
public static <T> T refer(final Class<T> interfaceClass, final String host, final int port) throws Exception {
```
- 防御性异常规范
```
  try {
            final Socket socket = server.accept();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        try {
                            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                            try {
                                String methodName = input.readUTF();
                                Class<?>[] parameterTypes = (Class<?>[])input.readObject();
                                Object[] arguments = (Object[])input.readObject();
                                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                                try {

```

关于RPC协议规范这方面的还没考虑到，之后做完足够的工作再来重新弄一番吧。

边写边学。
