package issuer.handler.upstream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASObject;
import protocol.PEASResponse;
import server.dao.Query;
import util.Encryption;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;

public class QueryHandler extends SimpleChannelInboundHandler<PEASObject> {


	private static final int KEY_SIZE = 16;
	private PKCS1Encoding RSAdecipher;
	private IvParameterSpec iv;
	private SecretKey currentKey;

	public QueryHandler() throws IOException {
        byte[] ivBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        iv = new IvParameterSpec(ivBytes);
        
        byte[] keyBytes = Files.readAllBytes(Paths.get(".").resolve("privKey2.der"));
        AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(keyBytes);
        
        RSAdecipher = new PKCS1Encoding(new RSAEngine());
        RSAdecipher.init(false, privateKey);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext arg0, Throwable arg1) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASObject obj) throws Exception {
		if (obj.getHeader().getCommand().equals("QUERY")) {
			System.out.println("query received");
			// TODO: decrypt query field and body, and dispatch http or forward to another issuer
			String query = getQueryFromQueryField(obj.getHeader().getQuery());
			System.out.println("q: " + query);
			
			String content = new String(Encryption.AESdecrypt(obj.getBody().getBody().array(), currentKey, iv));
			System.out.println("c: " + content);
			
			
			// simulate search engine request#
			int size = 8000;
			PEASHeader header = new PEASHeader();
			header.setCommand("RESPONSE");
			header.setIssuer(obj.getHeader().getIssuer());
			header.setStatus("100");
			header.setProtocol("HTTP");
			
			byte[] b = new byte[size];
			new Random().nextBytes(b);
			b = Encryption.AESencrypt(b, currentKey, iv);
			
			header.setBodyLength(b.length);
			PEASBody body = new PEASBody(b.length);
			body.getBody().writeBytes(b);
			
			PEASResponse res = new PEASResponse(header, body);
			
			// send response back
            ChannelFuture f = ctx.writeAndFlush(res);
            
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                    	System.out.println("return query successful");
                    } else {
                        System.out.println("return query failed");
                        future.channel().close();
                    }
                }
            });
		}
	}
	
	private String getQueryFromQueryField(String field) throws InvalidCipherTextException {
		byte[] decoded = Base64.decodeBase64(field);
		ByteBuf keyAndQuery = Unpooled.wrappedBuffer(decoded);
		
		String query;
	    if (keyAndQuery.capacity() <= 128) {
	        // CASE 1: {K | Q}_RSA
	    	// E(K + Q1)
			keyAndQuery = RSAdecrypt(keyAndQuery);
			// Extract the Symmetric Key
	    	currentKey = extractSecretKey(keyAndQuery);
	    	
	        query = new String(keyAndQuery.readerIndex(KEY_SIZE).readBytes(keyAndQuery.capacity() - KEY_SIZE).array());
	    } else {
	    	// CASE 2: {K | Q_1}_RSA | {Q_2}_AES
	        // Decipher the first part
	    	// K + Q1
			ByteBuf firstPartDecrypted = RSAdecrypt(keyAndQuery.readBytes(128));
			// Extract the Symmetric Key
	    	currentKey = extractSecretKey(firstPartDecrypted);

	    	// Q1
	        ByteBuf partOne = firstPartDecrypted.readerIndex(KEY_SIZE).readBytes(117 - KEY_SIZE); // 128 because RSA output when using 1024bit key is 128 bytes long

	        // Q2
	        ByteBuf partTwo = Unpooled.wrappedBuffer(Encryption.AESdecrypt(keyAndQuery.readerIndex(128).readBytes(keyAndQuery.capacity() - 128).array(), currentKey, iv)); // read the rest

	        ByteBuf queryConcat = Unpooled.buffer(partOne.capacity() + partTwo.capacity());
	        queryConcat.writeBytes(partOne);
	        queryConcat.writeBytes(partTwo);

	        query = new String(queryConcat.array());
	    }
	    
	    return query;
	}
	
	private ByteBuf RSAdecrypt(ByteBuf encrypted) throws InvalidCipherTextException {
      	byte[] decrypted = RSAdecipher.processBlock(encrypted.array(), 0, encrypted.array().length);
      	return Unpooled.wrappedBuffer(decrypted);
	}
	
    private SecretKey extractSecretKey(ByteBuf concat) {  
    	ByteBuf key = concat.readerIndex(0).readBytes(KEY_SIZE);
        return new SecretKeySpec(key.array(), "AES");         
    }
	

}
