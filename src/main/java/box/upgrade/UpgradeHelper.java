package box.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 升级助手<br>
 *
 * <pre>
 * 使用方法：
 * <code>
 * Index index = UpgradeHelper.parseIndex(indexfile); // 解析索引文件
 * List&lt;Range&gt; ranges = UpgradeHelper.verifyIndex(index, basedir); // 验证下载文件的完整性
 *
 * if (ranges.size() &gt; 0) {
 *   // 文件下载失败
 *   List&lt;Range&gt; mergedRanges = UpgradeHelper.mergeRange(ranges); // 合并无效范围
 *   // 重新下载无效的文件范围
 *   ...
 * } else {
 *   // 文件下载成功
 *   ...
 * }
 * </code>
 * </pre>
 */
public class UpgradeHelper {
  private static final char [] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
  /**
   * 解析升级索引文件。
   *
   * @param file 索引文件
   * @return 索引信息, null 表示文件读取错误
   */
  public static Index parseIndex(File file) {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      byte [] buf = new byte[(int)file.length()];
      fis.read(buf);
      Index index = parseIndex(new String(buf));
      return index;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * 解析升级索引文件内容。文件内容为多行文本格式。每一行由三个部分组成，每个部
   * 分之间用"|"进行分割。第一行是切片合并后的文件名称，签名和文件大小。之后的每
   * 一行是切片的范围，签名和文件大小。
   *
   * <br>
   * <br>
   * 比如(行号忽略不计)：
   * <br>
   * 1:文件链接|checksum|size<br>
   * 2:起始地址1,结束地址1|checksum1|size1<br>
   * 3:起始地址2,结束地址2|checksum2|size2<br>
   *
   * @param content 索引文件内容
   * @return 索引信息, null 表示解析错误
   */
  public static Index parseIndex(String content) {
    boolean firstLine = true;
    String[] lines = content.split("\n");
    Index idx = new Index();
    idx.slices = new ArrayList<IndexItem>(lines.length - 1);
    for (String line: lines) {
      if (line != null && !line.equals("")) {
        String[] tokens = line.split("\\|");
        if (firstLine) {
          firstLine = false;
          idx.url = tokens[0];
          idx.checksum = tokens[1];
          idx.size = Integer.parseInt(tokens[2]);
          int pos = idx.url.lastIndexOf("/");
          if (pos != -1) {
            idx.filename = idx.url.substring(pos + 1);
          } else {
            idx.filename = idx.url;
          }
        } else {
          IndexItem item = new IndexItem();
          item.checksum = tokens[1];
          item.size = Integer.parseInt(tokens[2]);
          String [] ptrs = tokens[0].split(",");
          item.range = new Range();
          item.range.start = Integer.parseInt(ptrs[0]);
          item.range.stop = Integer.parseInt(ptrs[1]);
          idx.slices.add(item);
        }
      }
    }
    return idx;
  }

  /**
   * 验证索引文件的内容是否全部已经正确下载，返回需要重新下载的切片范围列表。如
   * 果所有切片完整，则返回空列表。
   *
   * @param index 索引文件
   * @param basedir 下载文件存放的路径
   * @return 无效切片范围列表
   */
  public static List<Range> verifyIndex(File index, File basedir) {
    Index idx = parseIndex(index);
    return verifyIndex(idx, basedir);
  }

  /**
   * 验证索引文件的内容是否全部已经正确下载，返回需要重新下载的切片范围列表。如
   * 果所有切片完整，则返回空列表。
   *
   * @param idx 解析后的 Index 对象
   * @param basedir 下载文件存放的路径
   * @return 无效切片范围列表
   */
  public static List<Range> verifyIndex(Index idx, File basedir) {
    List<Range> invalids = new ArrayList<Range>(idx.slices.size());
    int maxsize = 0;
    for (IndexItem item: idx.slices) {
      if (maxsize < item.size) {
        maxsize = item.size;
      }
    }
    byte [] buf = new byte[maxsize];
    try {
      File file = new File(basedir, idx.filename);
      FileInputStream fis = new FileInputStream(file);
      for (IndexItem item: idx.slices) {
        try {
          int r = fis.read(buf, 0, item.size);
          if (r != -1) {
            if (!item.checksum.equalsIgnoreCase(md5(buf, 0, r))) {
              invalids.add(item.range);
            }
          } else {
            invalids.add(item.range);
          }
        } catch (Exception e) {
          invalids.add(item.range);
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return invalids;
  }

  /**
   * 合并 Range，将多个首尾相连的 Range 合并成一个 Range
   *
   * @param ranges Range 列表
   * @return 合并后的 Range 列表
   */
  public static List<Range> mergeRange(List<Range> ranges) {
    List<Range> newranges = new ArrayList<Range>(ranges.size());
    Range last = null;
    for (Range range: ranges) {
      if (last == null) {
        last = range;
      } else {
        if (last.stop + 1 == range.start) {
          // they are continued
          last.stop = range.stop;
        } else {
          newranges.add(last);
          last = range;
        }
      }
    }
    if (last != null) {
      newranges.add(last);
    }
    return newranges;
  }

  static String md5(String content) {
    return md5(content.getBytes());
  }

  static String md5(byte [] bytes) {
    return md5(bytes, 0, bytes.length);
  }

  static String md5(byte [] bytes, int off, int len) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(bytes, off, len);
      byte [] digest = md.digest();
      return toHex(digest);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  static String md5(File file) {
    byte [] buf = new byte[1024];
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      FileInputStream fis = new FileInputStream(file);
      while (fis.read(buf) != -1) {
        md.update(buf);
      }
      fis.close();
      byte [] digest = md.digest();
      return toHex(digest);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  static String toHex(byte [] buf) {
    StringBuilder builder = new StringBuilder(buf.length * 2);
    for (int i = 0, len = buf.length; i < len; i ++) {
      builder.append(HEX[(buf[i] >> 4) & 0x0F]);
      builder.append(HEX[buf[i] & 0x0F]);
    }
    return builder.toString();
  }
}
