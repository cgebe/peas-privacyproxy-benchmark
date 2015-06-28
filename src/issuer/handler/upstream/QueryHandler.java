package issuer.handler.upstream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASMessage;
import util.Config;
import util.Encryption;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;

public class QueryHandler extends SimpleChannelInboundHandler<PEASMessage> {


	private static final int KEY_SIZE = 16;
	private PKCS1Encoding RSAdecipher;
	private IvParameterSpec iv;
	private SecretKey currentKey;

	public QueryHandler() throws IOException, URISyntaxException {
        byte[] ivBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        iv = new IvParameterSpec(ivBytes);
        
        //byte[] keyBytes = Files.readAllBytes(Paths.get("./resources/").resolve("privKey2.der"));
        String jarPath = new File(QueryHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath();
        InputStream inputStream = new FileInputStream(new File(jarPath + "/resources/privKey2.der"));
        //InputStream inputStream = QueryHandler.class.getClassLoader().getResourceAsStream("privKey2.der");
        byte[] keyBytes = IOUtils.toByteArray(inputStream);
        AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(keyBytes);
        
        RSAdecipher = new PKCS1Encoding(new RSAEngine());
        RSAdecipher.init(false, privateKey);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext arg0, Throwable arg1) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASMessage obj) throws Exception {
		if (obj.getHeader().getCommand().equals("QUERY")) {

			String query = getQueryFromQueryField(obj.getHeader().getQuery());
			System.out.println("q: " + query);
			
			String content = new String(Encryption.AESdecrypt(obj.getBody().getContent().array(), currentKey, iv));
			System.out.println("c: " + content);
			
			// simulate search engine request#
			int size = Integer.parseInt(Config.getInstance().getValue("TEST_PAYLOAD_SIZE"));
			PEASHeader header = new PEASHeader();
			header.setCommand("RESPONSE");
			header.setIssuer(obj.getHeader().getIssuer());
			header.setReceiverID(obj.getHeader().getReceiverID());
			header.setStatus("100");
			header.setProtocol("HTTP");
			
			byte[] b = new byte[size];
			//new Random().nextBytes(b);
			byte[] enc = Encryption.AESencrypt(b, currentKey, iv);
			
			header.setContentLength(enc.length);
			PEASBody body = new PEASBody(enc);
			
			PEASMessage res = new PEASMessage(header, body);
			if (Config.getInstance().getValue("MEASURE_PROCESS_TIME").equals("on")) {
				res.setCreationTime(obj.getCreationTime());
			}
			// send response back
            ChannelFuture f = ctx.writeAndFlush(res);
            
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                    	System.out.println("return query successful");
                    } else {
                        System.out.println("return query failed");
                    }
                    f.channel().close();
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
