import org.testng.annotations.*;

import org.testng.*;
import com.fengchaohuzhu.box.upgrade.*;

public class IndexTest {
  @Test()
  public void testParseIndex() {
    String content = "test-file.apk|md5-0|0\nhttp://test.com/test-file.apk.patch1|md5-1|1\ntest-file.apk.patch2|md5-2|2\n";
    Index idx = UpgradeHelper.parseIndex(content);
    Assert.assertNotNull(idx);
    Assert.assertEquals(idx.target.filename, "test-file.apk");
    Assert.assertEquals(idx.target.checksum, "md5-0");
    Assert.assertEquals(idx.target.size, 0);
    Assert.assertNull(idx.target.url);
    Assert.assertEquals(idx.slices.size(), 2);
    Assert.assertEquals(idx.slices.get(0).url, "http://test.com/test-file.apk.patch1");
    Assert.assertEquals(idx.slices.get(0).checksum, "md5-1");
    Assert.assertEquals(idx.slices.get(0).size, 1);
    Assert.assertEquals(idx.slices.get(0).filename, "test-file.apk.patch1");
    Assert.assertEquals(idx.slices.get(1).url, "test-file.apk.patch2");
    Assert.assertEquals(idx.slices.get(1).checksum, "md5-2");
    Assert.assertEquals(idx.slices.get(1).size, 2);
    Assert.assertEquals(idx.slices.get(1).filename, "test-file.apk.patch2");
  }
}
