package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.future.IOReadFuture;

public interface TCPEndPoint extends EndPoint{
	
	public abstract boolean enableWriting(long sessionID);

	public abstract void setWriting(long sessionID);
	
	public abstract void setCurrentWriter(IOWriteFuture writer);
	
	public abstract boolean isOpened();
	
	public abstract boolean isNetworkWeak();
	
	public abstract void attackNetwork(int length);
	
	public abstract void flushWriters() throws IOException ;
	
	public abstract Session getSession(byte sessionID) throws IOException;
	
	public abstract IOReadFuture getReadFuture();
	
	public abstract void setReadFuture(IOReadFuture future);
	
	public abstract void incrementWriter();
	
	public abstract void decrementWriter();
	
	public abstract ByteBuffer read(int limit) throws IOException;
	
	public abstract EndPointWriter getEndPointWriter();
	
}