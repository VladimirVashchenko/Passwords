package passwordProtector.dataProcessing;

import java.util.concurrent.Callable;

public class DecryptionTask implements Callable<String> {
    String data;
    String key;

    public DecryptionTask(String data, String key) {
        this.data = data;
        this.key = key;
    }

    @Override
    public String call() throws Exception {
        return AESDecryption.decrypt(data, key);
    }
}
