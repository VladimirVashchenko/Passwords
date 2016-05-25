package passwordProtector.dataProcessing;

import java.util.concurrent.Callable;

public class EncryptionTask implements Callable<String> {
    String data;
    String key;

    public EncryptionTask(String data, String key) {
        this.data = data;
        this.key = key;
    }

    @Override
    public String call() throws Exception {
        return AESEncryption.getEncrypted(data, key) ;
    }
}
