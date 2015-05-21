package onion.node.upstream;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASObject;
import protocol.PEASResponse;
import util.Encryption;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HandshakeHandler extends SimpleChannelInboundHandler<PEASObject> {
	
	private NodeChannelInitializer initializer;

	public HandshakeHandler(NodeChannelInitializer initializer) {
		this.initializer = initializer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASObject obj) throws Exception {
		if (obj.getHeader().getCommand().equals("HANDSHAKE")) {
			// TODO: check if forward or adresses to this
			if (obj.getHeader().getForward() == null) {
	            // Decrypt the payload in output
				ByteBuf decrypted = decryptHandshake(obj.getBody().getBody());

	            // Compute DH Key Aggreement and save the result in msg
	            ByteBuf key = dhKeyAggreement(decrypted);
	            
	            PEASHeader header = new PEASHeader();
	            header.setCommand("RESPONSE");
	            header.setIssuer(obj.getHeader().getIssuer());
	            header.setStatus("100");
	            
	            //byte[] encKey = initializer.getAEScipher().doFinal(key.array());
	            header.setBodyLength(key.capacity());
	            
	            PEASBody body = new PEASBody(key);
	            
	            PEASResponse res = new PEASResponse(header, body);

	            ChannelFuture f = ctx.writeAndFlush(res);
	            
	            f.addListener(new ChannelFutureListener() {
	                @Override
	                public void operationComplete(ChannelFuture future) {
	                    if (future.isSuccess()) {
	                    	System.out.println("return handshake successful");
	                    } else {
	                        System.out.println("return handshake failed");
	                        future.channel().close();
	                    }
	                }
	            });
			} else {
				ctx.fireChannelRead(obj);
			}  
		} else {
			ctx.fireChannelRead(obj);
		}

	}

	private static final byte ModulusBytes[] = {(byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xC9, (byte) 0x0F, (byte) 0xDA, (byte) 0xA2,
        (byte) 0x21, (byte) 0x68, (byte) 0xC2, (byte) 0x34, (byte) 0xC4,
        (byte) 0xC6, (byte) 0x62, (byte) 0x8B, (byte) 0x80, (byte) 0xDC,
        (byte) 0x1C, (byte) 0xD1, (byte) 0x29, (byte) 0x02, (byte) 0x4E,
        (byte) 0x08, (byte) 0x8A, (byte) 0x67, (byte) 0xCC, (byte) 0x74,
        (byte) 0x02, (byte) 0x0B, (byte) 0xBE, (byte) 0xA6, (byte) 0x3B,
        (byte) 0x13, (byte) 0x9B, (byte) 0x22, (byte) 0x51, (byte) 0x4A,
        (byte) 0x08, (byte) 0x79, (byte) 0x8E, (byte) 0x34, (byte) 0x04,
        (byte) 0xDD, (byte) 0xEF, (byte) 0x95, (byte) 0x19, (byte) 0xB3,
        (byte) 0xCD, (byte) 0x3A, (byte) 0x43, (byte) 0x1B, (byte) 0x30,
        (byte) 0x2B, (byte) 0x0A, (byte) 0x6D, (byte) 0xF2, (byte) 0x5F,
        (byte) 0x14, (byte) 0x37, (byte) 0x4F, (byte) 0xE1, (byte) 0x35,
        (byte) 0x6D, (byte) 0x6D, (byte) 0x51, (byte) 0xC2, (byte) 0x45,
        (byte) 0xE4, (byte) 0x85, (byte) 0xB5, (byte) 0x76, (byte) 0x62,
        (byte) 0x5E, (byte) 0x7E, (byte) 0xC6, (byte) 0xF4, (byte) 0x4C,
        (byte) 0x42, (byte) 0xE9, (byte) 0xA6, (byte) 0x37, (byte) 0xED,
        (byte) 0x6B, (byte) 0x0B, (byte) 0xFF, (byte) 0x5C, (byte) 0xB6,
        (byte) 0xF4, (byte) 0x06, (byte) 0xB7, (byte) 0xED, (byte) 0xEE,
        (byte) 0x38, (byte) 0x6B, (byte) 0xFB, (byte) 0x5A, (byte) 0x89,
        (byte) 0x9F, (byte) 0xA5, (byte) 0xAE, (byte) 0x9F, (byte) 0x24,
        (byte) 0x11, (byte) 0x7C, (byte) 0x4B, (byte) 0x1F, (byte) 0xE6,
        (byte) 0x49, (byte) 0x28, (byte) 0x66, (byte) 0x51, (byte) 0xEC,
        (byte) 0xE6, (byte) 0x53, (byte) 0x81, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF,};
    private static final BigInteger Modulus = new BigInteger(1, ModulusBytes);
    private static final BigInteger Base = BigInteger.valueOf(2);
    private final static int KEY_SIZE = 16; // Size of the key in bytes.
    public final static int BUFFER_SIZE = 55000;
    private static final byte[] ivBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final IvParameterSpec iv = new IvParameterSpec(ivBytes);
    
    /**
     * Decrypts the request bytes and extracts the public key send by the client for the handshake
     * 
     * @param RSAdecipher
     * @param input
     * @return
     * @throws InvalidCipherTextException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */
    public ByteBuf decryptHandshake(ByteBuf input) throws InvalidCipherTextException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, ShortBufferException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException {
        ByteBuf output;		
        if (input.capacity() <= 128) {
            // CASE 1: {responseBytes}_RSA
            output = RSAdecrypt(initializer.getRSAdecipher(), input);
        } else {
            // CASE 2: {K | responseBytes_1}_RSA | {responseBytes2}_AES
            ByteBuf firstPartDecrypted = RSAdecrypt(initializer.getRSAdecipher(), input.readBytes(128));
			// Extract the Symmetric Key
	    	SecretKey key = extractSecretKey(firstPartDecrypted);

	    	// K1
	        ByteBuf partOneOfPublicKey = firstPartDecrypted.readerIndex(KEY_SIZE).readBytes(117 - KEY_SIZE); // 128 because RSA output when using 1024bit key is 128 bytes long

	        // K2
	        ByteBuf partTwoOfPublicKey = Unpooled.wrappedBuffer(Encryption.AESdecrypt(input.readerIndex(128).readBytes(input.capacity() - 128).array(), key, iv)); // read the rest

	        output = Unpooled.buffer(partOneOfPublicKey.capacity() + partTwoOfPublicKey.capacity());
	        output.writeBytes(partOneOfPublicKey);
	        output.writeBytes(partTwoOfPublicKey);
	        
        }
        return output;
        
    }
    
    private SecretKey extractSecretKey(ByteBuf concat) {  
    	ByteBuf key = concat.readerIndex(0).readBytes(KEY_SIZE);
        return new SecretKeySpec(key.array(), "AES");         
    }

	private ByteBuf RSAdecrypt(AsymmetricBlockCipher RSAdecipher, ByteBuf encrypted) throws InvalidCipherTextException {
      	byte[] decrypted = RSAdecipher.processBlock(encrypted.array(), 0, encrypted.array().length);
      	return Unpooled.wrappedBuffer(decrypted);
	}
    
    public ByteBuf dhKeyAggreement(ByteBuf input) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException {
    	// Load DH half key
    	DHPublicKeySpec dhPublicKeySpec = new DHPublicKeySpec(new BigInteger(input.array()), Modulus, Base);
        PublicKey clientPubKey = KeyFactory.getInstance("DH").generatePublic(dhPublicKeySpec);
    	
        // Load DH parameters
        DHParameterSpec dhParamSpec = new DHParameterSpec(Modulus, Base);
        KeyPairGenerator KpairGen = KeyPairGenerator.getInstance("DH");
        KpairGen.initialize(dhParamSpec);
        
        // Generate the DH half key
        KeyPair Kpair = KpairGen.generateKeyPair();
        KeyAgreement KeyAgree = KeyAgreement.getInstance("DH");
        KeyAgree.init(Kpair.getPrivate());
        
        // Create the symmetric key
        KeyAgree.doPhase(clientPubKey, true);
        byte[] sharedSecret = KeyAgree.generateSecret();
        SecretKey symmetricKey = new SecretKeySpec(sharedSecret, 0, 16, "AES");
        System.out.println("skn " + Encryption.bytesToHex(symmetricKey.getEncoded()));
        // Initiate AES cipher and decipher
        Cipher AEScipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        AEScipher.init(Cipher.ENCRYPT_MODE, symmetricKey, iv);
        initializer.setAEScipher(AEScipher);
        Cipher AESdecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        AESdecipher.init(Cipher.DECRYPT_MODE, symmetricKey, iv);
        initializer.setAESdecipher(AESdecipher);
        
        byte[] keyBytes = ((DHPublicKey) Kpair.getPublic()).getY().toByteArray();
        return Unpooled.wrappedBuffer(keyBytes);

    }

}
