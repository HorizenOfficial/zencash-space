
package net.ddns.lsmobile.zencashvaadinwalletui4cpp.ui.tablet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.xdev.ui.XdevUI;
import com.xdev.ui.navigation.XdevNavigator;

@Push(value = PushMode.MANUAL, transport = Transport.LONG_POLLING)
@Theme("ZENCashVaadinWalletUI4CPP")
public class TabletUI extends XdevUI {
	public TabletUI() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(VaadinRequest request) {
		this.initUI();
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated
	 * by the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.navigator = new XdevNavigator(this, this);

		this.navigator.addView("", MainView.class);

		this.setSizeFull();
	} // </generated-code>

	// <generated-code name="variables">
	private XdevNavigator navigator; // </generated-code>
}