package test.fixedlength;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.nio.component.protocol.fixedlength.future.FixedLengthReadFuture;
import com.generallycloud.nio.component.protocol.fixedlength.future.FixedLengthReadFutureImpl;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;

public class TestClient {

	public static void main(String[] args) throws Exception {

		PropertiesLoader.setBasepath("nio");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {

				FixedLengthReadFuture f = (FixedLengthReadFuture) future;
				System.out.println();
				System.out.println("____________________"+f.getText());
				System.out.println();
			}
		};

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);

		connector.getContext().setProtocolFactory(new FixedLengthProtocolFactory());

		connector.connect();

		Session session = connector.getSession();

		ReadFuture future = new FixedLengthReadFutureImpl(session);

		future.write("hello server !");

		session.flush(future);
		
//		session.flush(new FLBeatFutureFactory().createBeatPacket(session));

		ThreadUtil.sleep(100);

		CloseUtil.close(connector);

	}
}
