package box.upgrade;

import java.util.*;

import org.testng.annotations.*;

import org.testng.*;

public class IndexTest {
  @Test()
  public void testParseIndex() {
    String content = "http://test.com/test-file.apk|md5-0|11\n0,10|md5-1|10\n10,11|md5-2|1\n";
    Index idx = UpgradeHelper.parseIndex(content);
    Assert.assertNotNull(idx);
    Assert.assertEquals(idx.filename, "test-file.apk");
    Assert.assertEquals(idx.url, "http://test.com/test-file.apk");
    Assert.assertEquals(idx.checksum, "md5-0");
    Assert.assertEquals(idx.size, 11);
    Assert.assertEquals(idx.slices.size(), 2);
    Assert.assertNotNull(idx.slices.get(0).range);
    Assert.assertEquals(idx.slices.get(0).checksum, "md5-1");
    Assert.assertEquals(idx.slices.get(0).size, 10);
    Assert.assertEquals(idx.slices.get(0).range.start, 0);
    Assert.assertEquals(idx.slices.get(0).range.stop, 10);
    Assert.assertNotNull(idx.slices.get(1).range);
    Assert.assertEquals(idx.slices.get(1).checksum, "md5-2");
    Assert.assertEquals(idx.slices.get(1).size, 1);
    Assert.assertEquals(idx.slices.get(1).range.start, 10);
    Assert.assertEquals(idx.slices.get(1).range.stop, 11);
  }

  @Test()
  public void testMergeRange() {
    List<Range> origin = new ArrayList<Range>();
    Range range1 = new Range();
    range1.start = 0;
    range1.stop = 1023;
    origin.add(range1);
    Range range2 = new Range();
    range2.start = 1024;
    range2.stop = 2047;
    origin.add(range2);
    List<Range> ranges = UpgradeHelper.mergeRange(origin);
    Assert.assertNotNull(ranges);
    Assert.assertEquals(ranges.size(), 1);
    Assert.assertEquals(ranges.get(0).start, 0);
    Assert.assertEquals(ranges.get(0).stop, 2047);

    origin = new ArrayList<Range>();
    range1 = new Range();
    range1.start = 0;
    range1.stop = 1023;
    origin.add(range1);
    range2 = new Range();
    range2.start = 2048;
    range2.stop = 3000;
    origin.add(range2);
    ranges = UpgradeHelper.mergeRange(origin);
    Assert.assertNotNull(ranges);
    Assert.assertEquals(ranges.size(), 2);
    Assert.assertEquals(ranges.get(0).start, 0);
    Assert.assertEquals(ranges.get(0).stop, 1023);
    Assert.assertEquals(ranges.get(1).start, 2048);
    Assert.assertEquals(ranges.get(1).stop, 3000);

    origin = new ArrayList<Range>();
    range1 = new Range();
    range1.start = 0;
    range1.stop = 1023;
    origin.add(range1);
    range2 = new Range();
    range2.start = 2048;
    range2.stop = 3000;
    origin.add(range2);
    Range range3 = new Range();
    range3.start = 3001;
    range3.stop = 4000;
    origin.add(range3);
    ranges = UpgradeHelper.mergeRange(origin);
    Assert.assertNotNull(ranges);
    Assert.assertEquals(ranges.size(), 2);
    Assert.assertEquals(ranges.get(0).start, 0);
    Assert.assertEquals(ranges.get(0).stop, 1023);
    Assert.assertEquals(ranges.get(1).start, 2048);
    Assert.assertEquals(ranges.get(1).stop, 4000);

    origin = new ArrayList<Range>();
    range1 = new Range();
    range1.start = 0;
    range1.stop = 1023;
    origin.add(range1);
    range2 = new Range();
    range2.start = 1024;
    range2.stop = 2047;
    origin.add(range2);
    range3 = new Range();
    range3.start = 3001;
    range3.stop = 4000;
    origin.add(range3);
    ranges = UpgradeHelper.mergeRange(origin);
    Assert.assertNotNull(ranges);
    Assert.assertEquals(ranges.size(), 2);
    Assert.assertEquals(ranges.get(0).start, 0);
    Assert.assertEquals(ranges.get(0).stop, 2047);
    Assert.assertEquals(ranges.get(1).start, 3001);
    Assert.assertEquals(ranges.get(1).stop, 4000);

    origin = new ArrayList<Range>();
    range1 = new Range();
    range1.start = 0;
    range1.stop = 1023;
    origin.add(range1);
    range2 = new Range();
    range2.start = 1024;
    range2.stop = 2047;
    origin.add(range2);
    range3 = new Range();
    range3.start = 2048;
    range3.stop = 4000;
    origin.add(range3);
    ranges = UpgradeHelper.mergeRange(origin);
    Assert.assertNotNull(ranges);
    Assert.assertEquals(ranges.size(), 1);
    Assert.assertEquals(ranges.get(0).start, 0);
    Assert.assertEquals(ranges.get(0).stop, 4000);
  }
}
