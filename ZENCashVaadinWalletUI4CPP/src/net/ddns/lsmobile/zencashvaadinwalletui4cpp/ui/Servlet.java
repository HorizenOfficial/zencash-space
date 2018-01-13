
package net.ddns.lsmobile.zencashvaadinwalletui4cpp.ui;

import javax.servlet.annotation.WebServlet;

import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;
import com.xdev.communication.XdevServlet;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.ZenNode;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.ui.desktop.DesktopUI;

@WebServlet(value = "/*", asyncSupported = true)
public class Servlet extends XdevServlet implements IConfig {

	public ZenNode zenNode = new ZenNode ();

	public Servlet() throws Exception {
		super();
		
		this.zenNode.connect();
	}

	@Override
	protected void initSession(final SessionInitEvent event) {
		super.initSession(event);

		event.getSession().addUIProvider(new ServletUIProvider());
	}

	/**
	 * UIProvider which provides different UIs depending on the caller's device.
	 */
	private static class ServletUIProvider extends UIProvider {
		@Override
		public Class<? extends UI> getUIClass(final UIClassSelectionEvent event) {
//			final ClientInfo client = ClientInfo.getCurrent();
//			if (client != null) {
//				if (client.isMobile()) {
//					return PhoneUI.class;
//				}
//				if (client.isTablet()) {
//					return TabletUI.class;
//				}
//			}
			return DesktopUI.class;
		}
	}
}