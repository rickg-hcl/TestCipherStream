package sample.lotus.com.testcipherstream;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;



public class Tester {

    static int lastAlgorithmIndex=0;
    String results;
    public static final byte[] ONE_HUNDRED="1234567890123456789112345678921234567893123456789412345678951234567896123456789712345678981234567890".getBytes();

    public int hundredCount=10;
    public int thousandCount=5;
    public static final String initResult="Test in progress";
    private boolean useCipher;

    private byte[] rawKey;

    private String algorithm;


    public Tester(boolean useCipher, int thousandCount) {
        this.thousandCount=thousandCount;
        this.useCipher=useCipher;
        rawKey=getRawKey();
        algorithm=getAlgorithm();
    }

    public void runTest(Context context) throws IOException {


        results = initResult;

        File testFile = new File(context.getFilesDir(),"testFile.txt");
        if(testFile.exists()) {
            testFile.delete();
        }
        try {
            OutputStream os = getCipherOutputStream(new FileOutputStream(testFile));
            byte[] startBuffer = new byte[ONE_HUNDRED.length * hundredCount];

            for (int i = 0; i < hundredCount; i++) {
                System.arraycopy(ONE_HUNDRED, 0, startBuffer, i * (ONE_HUNDRED.length), ONE_HUNDRED.length);
            }

            for (int count = 0; count < thousandCount; count++) {
                os.write(startBuffer);
            }
            os.close();

            long fileSize = testFile.length();
            long expectedFileSize=new Long((ONE_HUNDRED.length * hundredCount) * thousandCount).longValue();

            if(((ONE_HUNDRED.length * hundredCount) * thousandCount) <= fileSize) {
                // test passed so far
            } else {
                results = "file size was too small, expected "+expectedFileSize+", but size was "+fileSize;
            }

            InputStream fis = new BufferedInputStream(new FileInputStream(testFile));

            InputStream is = getCipherInputStream(fis);
            byte[] readBuffer = new byte[startBuffer.length];

            for (int count = 0; count < thousandCount; count++) {
                if(!readFully(readBuffer,is)) break;
                if(!(new String(startBuffer).equals(new String(readBuffer)))) {
                    results = "read buffer didn't match write buffer ";
                }
            }

            int lastByte = is.read();
            if(lastByte != -1) {
                results = "file is longer than expected";
            }
        } finally {
            if(testFile.exists()) {
                testFile.delete();
            }
        }

        if(initResult.equals(results)) {
            results="test passed with "+algorithm;
        }
    }

    private boolean readFully(byte[] buffer, InputStream stream) throws IOException {
        int offset=0;
        int remain=buffer.length;
        int read_len=0;
        while(remain >0 && read_len >=0 ) {
            read_len=stream.read(buffer,offset,remain);
            remain-=read_len;
            offset+=read_len;
        }

        if(remain >0) {
            results = "premature end of file, expected "+buffer.length+", but only read "+offset;
            return false;
        }
        return true;
    }

    private static final String[] CTR_ALGORITHMS = { "AES/SIC/PKCS7Padding", "AES/CTR/PKCS7Padding",
            "AES/SIC/PKCS5Padding", "AES/CTR/PKCS5Padding",
            "AES/SIC/NoPadding", "AES/CTR/NoPadding" }; //for random access data (e.g., files)


    private String getAlgorithm() {
        int size=CTR_ALGORITHMS.length;
        String rv=null;
        int current=(lastAlgorithmIndex+1) % size;
        while(rv ==null  && current != lastAlgorithmIndex) {
            try {
                Cipher.getInstance(CTR_ALGORITHMS[current]);
                lastAlgorithmIndex=current;
                return CTR_ALGORITHMS[current];
            } catch (NoSuchAlgorithmException e) {
                //
            } catch (NoSuchPaddingException e) {
                //
            }
            current++;
        }
        throw new RuntimeException(" No algorithm available");
    }

    private OutputStream getCipherOutputStream(OutputStream outputStream) {
        if(!useCipher) return outputStream;

        IvParameterSpec iv = new IvParameterSpec("0123456789abcdef".getBytes());
        try {
            Cipher streamCipher = Cipher.getInstance(algorithm);
            SecretKeySpec sKeySpec = new SecretKeySpec(rawKey, "AES");
            streamCipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);
            return new CipherOutputStream(outputStream, streamCipher);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InputStream getCipherInputStream(InputStream inputStream) {
        if(!useCipher) return inputStream;

        IvParameterSpec iv = new IvParameterSpec("0123456789abcdef".getBytes());
        try {
            Cipher inCipher = Cipher.getInstance(algorithm);
            SecretKeySpec sKeySpec = new SecretKeySpec(rawKey, "AES");
            inCipher.init(Cipher.DECRYPT_MODE, sKeySpec,iv);
            return new CipherInputStream(inputStream, inCipher);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }


    static byte[] getRawKey()  {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = new SecureRandom();
            keyGenerator.init(256, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getResults() {
        return results;
    }

}


