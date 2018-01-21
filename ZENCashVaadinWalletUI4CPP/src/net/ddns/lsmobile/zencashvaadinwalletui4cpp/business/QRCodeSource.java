package net.ddns.lsmobile.zencashvaadinwalletui4cpp.business;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vaadin.server.StreamResource.StreamSource;

public class QRCodeSource implements StreamSource, IConfig {

	private final String data;
	private final int width;
	private final int height;
	private ByteArrayOutputStream imagebuffer = null;

	public QRCodeSource(final String data, final int width, final int height) {
		super();
		this.data = data;
		this.width = width;
		this.height = height;
	}

	@Override
	public InputStream getStream() {

		try {
			final QRCodeWriter qrCodeWriter = new QRCodeWriter();
			final BitMatrix bitMatrix = qrCodeWriter.encode(this.data, BarcodeFormat.QR_CODE, this.width, this.height);
			this.imagebuffer = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(bitMatrix, "PNG", this.imagebuffer);
			return new ByteArrayInputStream(this.imagebuffer.toByteArray());
		} catch (final Exception e) {
			log.error(e, e);
			return null;
		}
	}
}
