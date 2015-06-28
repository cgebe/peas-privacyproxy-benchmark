package onion.node.upstream;

import io.netty.channel.Channel;

import javax.crypto.Cipher;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class NodeChannelState {

	private AsymmetricBlockCipher RSAdecipher;
	private Cipher AEScipher;
	private Cipher AESdecipher;
	private Channel outboundChannel;
	
	public NodeChannelState(AsymmetricKeyParameter key) {
		RSAdecipher = new PKCS1Encoding(new RSAEngine());
        RSAdecipher.init(false, key);
	}
	
	public synchronized AsymmetricBlockCipher getRSAdecipher() {
		return RSAdecipher;
	}

	public synchronized void setRSAdecipher(AsymmetricBlockCipher RSAdecipher) {
		this.RSAdecipher = RSAdecipher;
	}

	public synchronized Cipher getAEScipher() {
		return AEScipher;
	}

	public synchronized void setAEScipher(Cipher aEScipher) {
		AEScipher = aEScipher;
	}

	public synchronized Cipher getAESdecipher() {
		return AESdecipher;
	}

	public synchronized void setAESdecipher(Cipher aESdecipher) {
		AESdecipher = aESdecipher;
	}
	
	public synchronized void setOutboundChannel(Channel channel) {
		this.outboundChannel = channel;
	}
	
	public synchronized Channel getOutboundChannel() {
		return outboundChannel;
	}

}
