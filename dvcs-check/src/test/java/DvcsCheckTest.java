import org.jnotary.client.DvcsCheck;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class DvcsCheckTest {
	private String configFile;
	private String srcFile;
	private String dvcsFile;
	
	@Before
	public void before() {
		String keyPath = getClass().getClassLoader().getSystemResource("Client1.p12").getPath();
		keyPath = keyPath.replace("/Client1.p12", "");
		System.setProperty("user.dir", keyPath);
		configFile = getClass().getClassLoader().getSystemResource("myKey.properties").getPath();
		srcFile = getClass().getClassLoader().getSystemResource("txtfile.in").getPath();
		dvcsFile = getClass().getClassLoader().getSystemResource("cpd-file.out").getPath();
	}

	@Test
	public void checkCpd() throws Exception {

		String[] args = {
				"-k", configFile,
				srcFile, 
				dvcsFile};
		DvcsCheck.main(args);
	}
}
