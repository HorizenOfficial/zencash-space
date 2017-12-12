package com.vaklinov.zcashui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//import javax.swing.JOptionPane;
//import javax.swing.ProgressMonitorInputStream;
import javax.xml.bind.DatatypeConverter;

import com.vaklinov.zcashui.OSUtil.OS_TYPE;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig;


/**
 * Fetches the proving key.  Deliberately hardcoded.
 * @author zab
 */
public class ProvingKeyFetcher implements IConfig {
    
    private static final int PROVING_KEY_SIZE = 910173851;
    private static final String SHA256 = "8bc20a7f013b2b58970cddd2e7ea028975c88ae7ceb9259a5344a16bc2c0eef7";
    private static final String pathURL = "https://zensystem.io/downloads/sprout-proving.key";
    // TODO: add backups
    
    public void fetchIfMissing(/*final StartupProgressDialog parent*/) throws IOException {
        try {
            verifyOrFetch(/*parent*/);
        } catch (final InterruptedIOException iox) {
        	log.error("The ZENCash wallet cannot proceed without a proving key.", iox);
//        	Notification.show("The ZENCash wallet cannot proceed without a proving key.", Type.ERROR_MESSAGE);
        	throw iox;
//            System.exit(-3);
        }
    }
    
    private void verifyOrFetch(/*final StartupProgressDialog parent*/)
    	throws IOException
    {
    	final OS_TYPE ost = OSUtil.getOSType();
        
    	File zCashParams = null;
        // TODO: isolate getting ZcashParams in a utility method
        if (ost == OS_TYPE.WINDOWS)
        {
        	zCashParams = new File(System.getenv("APPDATA") + "/ZcashParams");
        } else if (ost == OS_TYPE.MAC_OS)
        {
        	final File userHome = new File(System.getProperty("user.home"));
        	zCashParams = new File(userHome, "Library/Application Support/ZcashParams");
        }
        
        zCashParams = zCashParams.getCanonicalFile();
        
        boolean needsFetch = false;
        if (!zCashParams.exists())
        {
            needsFetch = true;
            zCashParams.mkdirs();
        }
        
        // verifying key is small, always copy it
        final File verifyingKeyFile = new File(zCashParams,"sprout-verifying.key");
        final FileOutputStream fos = new FileOutputStream(verifyingKeyFile);
        InputStream is = ProvingKeyFetcher.class.getClassLoader().getResourceAsStream("keys/sprout-verifying.key");
        copy(is,fos);
        fos.close();
        is = null;
        
        File provingKeyFile = new File(zCashParams,"sprout-proving.key");
        provingKeyFile = provingKeyFile.getCanonicalFile();
        if (!provingKeyFile.exists())
        {
            needsFetch = true;
        } else if (provingKeyFile.length() != PROVING_KEY_SIZE)
        {
            needsFetch = true;
        }
        /*
         * We skip proving key verification every start - this is impractical.
         * If the proving key exists and is the correct size, then it should be OK.
        else
        {
            parent.setProgressText("Verifying proving key...");
            needsFetch = !checkSHA256(provingKeyFile,parent);
        }*/
        
        if (!needsFetch)
        {
            return;
        }
        
        log.info("The wallet needs to download the Z cryptographic proving key (approx. 900 MB)." +
            	"This will be done only once. Please be patient... Press OK to continue");
//    	Notification.show("The wallet needs to download the Z cryptographic proving key (approx. 900 MB).\n" +
//            	"This will be done only once. Please be patient... Press OK to continue", Type.HUMANIZED_MESSAGE);

//        parent.setProgressText("Downloading proving key...");
        provingKeyFile.delete();
        final OutputStream os = new BufferedOutputStream(new FileOutputStream(provingKeyFile));
        final URL keyURL = new URL(pathURL);
        final URLConnection urlc = keyURL.openConnection();
        urlc.setRequestProperty("User-Agent", "Wget/1.17.1 (linux-gnu)");
        
//        try
//        {
//        	is = urlc.getInputStream();
//            final ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(parent, "Downloading proving key", is);
//            pmis.getProgressMonitor().setMaximum(PROVING_KEY_SIZE);
//            pmis.getProgressMonitor().setMillisToPopup(10);
//
//            copy(pmis,os);
//            os.close();
//        } finally
//        {
//            try { if (is != null) {
//				is.close();
//			} } catch (final IOException ignore){}
//        }
//        parent.setProgressText("Verifying downloaded proving key...");
        
//        TODO LS check provingKey
//        if (!checkSHA256(provingKeyFile/*, parent*/))
//        {
//        	throw new IOException("Failed to download proving key properly. Cannot continue!");
////        	Notification.show("Failed to download proving key properly. Cannot continue!", Type.ERROR_MESSAGE);
////            System.exit(-4);
//        }
        
    }
            

    private static void copy(final InputStream is, final OutputStream os) throws IOException {
        final byte[] buf = new byte[0x1 << 13];
        int read;
        while ((read = is.read(buf)) >- 0) {
            os.write(buf,0,read);
        }
        os.flush();
    }
    
    private static boolean checkSHA256(final File provingKey/*, final Component parent*/) throws IOException {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException impossible) {
            throw new IOException(impossible);
        }
        try (InputStream is = new BufferedInputStream(new FileInputStream(provingKey))) {
//            final ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(parent,"Verifying proving key",is);
//            pmis.getProgressMonitor().setMaximum(PROVING_KEY_SIZE);
//            pmis.getProgressMonitor().setMillisToPopup(10);
//            final DigestInputStream dis = new DigestInputStream(pmis, sha256);
            final byte [] temp = new byte[0x1 << 13];
//            while(dis.read(temp) >= 0) {
//				;
//			}
            final byte [] digest = sha256.digest();
            return SHA256.equalsIgnoreCase(DatatypeConverter.printHexBinary(digest));
        }
    }
}
