package onion.node;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;

import javax.crypto.spec.IvParameterSpec;

import onion.node.upstream.NodeChannelInitializer;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;

import receiver.handler.upstream.ReceiverChannelInitializer;
import util.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Discards any incoming data.
 */
public class Node2 {

    private int port;
	private KeyFactory DH_KeyFac;
	private IvParameterSpec iv;
	private AsymmetricKeyParameter privateKey;

    public Node2(int port) throws IOException {
        this.port = port;
        
		byte[] keyBytes = Files.readAllBytes(Paths.get(".").resolve("privKey2.der"));
		privateKey  = PrivateKeyFactory.createKey(keyBytes);
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(3);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new NodeChannelInitializer(privateKey));
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
    	new Node2(12346).run();
    }
}