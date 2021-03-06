package com.generallycloud.nio.balancing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.component.DefaultNIOContext;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.SessionEventListener;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;
import com.generallycloud.nio.component.concurrent.SingleEventLoopGroup;
import com.generallycloud.nio.component.protocol.ProtocolFactory;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class FrontServerBootStrap {
	
	private ProtocolFactory frontProtocolFactory;
	private ProtocolFactory frontReverseProtocolFactory;
	private ServerConfiguration frontServerConfiguration;
	private ServerConfiguration frontReverseServerConfiguration;
	private List<SessionEventListener> frontSessionEventListeners;
	private List<SessionEventListener> frontReverseSessionEventListeners;
	
	public void startup() throws IOException{
		
		FrontFacadeAcceptor frontFacadeAcceptor = new FrontFacadeAcceptor();
		
		FrontContext frontContext = new FrontContext(frontFacadeAcceptor);

		NIOContext frontNIOContext = getFrontNIOContext(frontContext, frontServerConfiguration, frontProtocolFactory);
		
		NIOContext frontReverseNIOContext = getFrontReverseNIOContext(frontContext, frontReverseServerConfiguration, frontReverseProtocolFactory);
		
		frontFacadeAcceptor.start(frontContext,frontNIOContext,frontReverseNIOContext);
	}
	
	private NIOContext getFrontNIOContext(FrontContext frontContext,ServerConfiguration configuration,ProtocolFactory protocolFactory){
		
		EventLoopGroup eventLoopGroup = new SingleEventLoopGroup(
				"IOEvent", 
				configuration.getSERVER_CHANNEL_QUEUE_SIZE(),
				configuration.getSERVER_CORE_SIZE());
		
		NIOContext context = new DefaultNIOContext(configuration,eventLoopGroup);

		context.setIOEventHandleAdaptor(frontContext.getFrontFacadeAcceptorHandler());

		context.addSessionEventListener(frontContext.getFrontFacadeAcceptorSEListener());
		
		context.setProtocolFactory(protocolFactory);
		
		if (frontSessionEventListeners != null) {
			addSessionEventListener2Context(context, frontSessionEventListeners);
		}
		
		return context;
	}
	
	private NIOContext getFrontReverseNIOContext(FrontContext frontContext,ServerConfiguration configuration,ProtocolFactory protocolFactory){
		
		EventLoopGroup eventLoopGroup = new SingleEventLoopGroup(
				"IOEvent", 
				configuration.getSERVER_CHANNEL_QUEUE_SIZE(),
				configuration.getSERVER_CORE_SIZE());
		
		NIOContext context = new DefaultNIOContext(configuration,eventLoopGroup);

		context.setIOEventHandleAdaptor(frontContext.getFrontReverseAcceptorHandler());

		context.addSessionEventListener(frontContext.getFrontReverseAcceptorSEListener());
		
		context.setProtocolFactory(protocolFactory);
		
		if (frontReverseSessionEventListeners != null) {
			addSessionEventListener2Context(context, frontReverseSessionEventListeners);
		}
		
		return context;
	}

	public ProtocolFactory getFrontProtocolFactory() {
		return frontProtocolFactory;
	}

	public void setFrontProtocolFactory(ProtocolFactory frontProtocolFactory) {
		this.frontProtocolFactory = frontProtocolFactory;
	}

	public ProtocolFactory getFrontReverseProtocolFactory() {
		return frontReverseProtocolFactory;
	}

	public void setFrontReverseProtocolFactory(ProtocolFactory frontReverseProtocolFactory) {
		this.frontReverseProtocolFactory = frontReverseProtocolFactory;
	}

	public ServerConfiguration getFrontServerConfiguration() {
		return frontServerConfiguration;
	}

	public void setFrontServerConfiguration(ServerConfiguration frontServerConfiguration) {
		this.frontServerConfiguration = frontServerConfiguration;
	}

	public ServerConfiguration getFrontReverseServerConfiguration() {
		return frontReverseServerConfiguration;
	}

	public void setFrontReverseServerConfiguration(ServerConfiguration frontReverseServerConfiguration) {
		this.frontReverseServerConfiguration = frontReverseServerConfiguration;
	}
	
	public void addFrontSessionEventListener(SessionEventListener listener){
		if (frontSessionEventListeners == null) {
			frontSessionEventListeners = new ArrayList<SessionEventListener>();
		}
		frontSessionEventListeners.add(listener);
	}
	
	public void addFrontReverseSessionEventListener(SessionEventListener listener){
		if (frontReverseSessionEventListeners == null) {
			frontReverseSessionEventListeners = new ArrayList<SessionEventListener>();
		}
		frontReverseSessionEventListeners.add(listener);
	}
	
	private void addSessionEventListener2Context(NIOContext context,List<SessionEventListener> listeners){
		for(SessionEventListener l : listeners){
			context.addSessionEventListener(l);
		}
	}
	
}
