package onion.node;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import onion.node.upstream.NodeChannelInitializer;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;





import util.Config;
import util.Observer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * Discards any incoming data.
 */
public class Node {

    private int port;
	private AsymmetricKeyParameter privateKey;
	private Executor executor = Executors.newSingleThreadExecutor();

    public Node(int port) throws IOException, URISyntaxException {
        this.port = port;
        
        if (Config.getInstance().getValue("MEASURE_SERVER_STATS").equals("on")) {
        	executor.execute(new StatsWriter());
        }
        
		//byte[] keyBytes = Files.readAllBytes(Paths.get("./resources/").resolve("privKey2.der"));
		//privateKey  = PrivateKeyFactory.createKey(keyBytes);
        String jarPath = new File(Node.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath();
        InputStream inputStream = new FileInputStream(new File(jarPath + "/resources/privKey2.der"));
        //InputStream inputStream = Node.class.getClassLoader().getResourceAsStream("privKey2.der");
        byte[] keyBytes = IOUtils.toByteArray(inputStream);
        
        privateKey  = PrivateKeyFactory.createKey(keyBytes);
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(Integer.parseInt(Config.getInstance().getValue("BOSS_CORES")));
        EventLoopGroup workerGroup = new NioEventLoopGroup(Integer.parseInt(Config.getInstance().getValue("WORKER_CORES")));
        //EventLoopGroup bossGroup = new NioEventLoopGroup(); 
        //EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new NodeChannelInitializer(privateKey))
             .option(ChannelOption.SO_BACKLOG, 2000);
             //.option(ChannelOption.SO_BACKLOG, 128);        
             //.childOption(ChannelOption.AUTO_READ, true)
             //.childOption(ChannelOption.SO_KEEPALIVE, true); 

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); 

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        //new RecieverServer(Integer.parseInt(Config.getInstance().getValue("port"))).run();
    	new Node(12345).run();
    }
    
    private class StatsWriter implements Runnable {

        private long interval;

        public StatsWriter() {
            interval = System.nanoTime();
        }

        @Override
        public void run() {
        	while(true) {
	        	// every second print stats
	        	if ((System.nanoTime() - interval) / 1e6 >= 1000) {
	        		Observer.getInstance().printResultsToFile();
	        		Observer.getInstance().reset();
	        		interval = System.nanoTime();
	        	}
        	}
        }
    }
}