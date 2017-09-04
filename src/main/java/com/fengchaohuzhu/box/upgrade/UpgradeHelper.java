package com.fengchaohuzhu.box.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
 * if (UpgradeHelper.verifyIndex(index, basedir).size() == 0) {
 *   UpgradeHelper.merge(index, basedir, basedir); // merge slices
 * } else {
 *   System.out.println("Errors in verifying downloaded slices");
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
   * 一行是切片的下载链接，签名和文件大小。
   * <br>
   * <br>
   * 比如(行号忽略不计)：
   * <br>
   * 1:文件名|checksum|size<br>
   * 2:链接1|checksum1|size1<br>
   * 3:链接2|checksum2|size2<br>
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
        IndexItem item = new IndexItem();
        item.checksum = tokens[1];
        item.size = Integer.parseInt(tokens[2]);
        if (firstLine) {
          firstLine = false;
          item.filename = tokens[0];
          idx.target = item;
        } else {
          item.url = tokens[0];
          idx.slices.add(item);
        }
      }
    }
    return idx;
  }

  /**
   * 验证索引文件的内容是否全部已经正确下载，返回需要重新下载的切片索引列表。如
   * 果所有切片完整，则返回空列表。
   *
   * @param index 索引文件
   * @param basedir 下载切片存放的路径
   * @return 无效切片列表
   */
  public static List<IndexItem> verifyIndex(File index, File basedir) {
    Index idx = parseIndex(index);
    List<IndexItem> invalids = new ArrayList<IndexItem>(idx.slices.size());
    for (IndexItem item: idx.slices) {
      int pos = item.url.lastIndexOf("/");
      String filename = null;
      if (pos != -1) {
        filename = item.url.substring(pos + 1);
      } else {
        filename = item.url;
      }
      try {
        File file = new File(basedir, filename);
        if (!item.checksum.equalsIgnoreCase(md5(file))) {
          invalids.add(item);
        }
      } catch (Exception e) {
        e.printStackTrace();
        invalids.add(item);
      }
    }
    return invalids;
  }

  /**
   * 合并切片文件<br>
   *
   * 注意，merge 方法不检查切片文件的完整性。
   * @param index 索引文件
   * @param basedir 切片文件存放路径
   * @param destdir 合并后文件存放路径
   * @return 合并后的文件，如果合并失败则为 null
   */
  public static File merge(File index, File basedir, File destdir) {
    byte [] buf = new byte[1024];
    Index idx = parseIndex(index);
    if (idx != null && idx.target != null) {
      FileOutputStream fos = null;
      try {
        File target = new File(destdir, idx.target.filename);
        fos = new FileOutputStream(target);
        for (IndexItem item: idx.slices) {
          int pos = item.url.lastIndexOf("/");
          String filename = null;
          if (pos != -1) {
            filename = item.url.substring(pos + 1);
          } else {
            filename = item.url;
          }
          File slice = new File(basedir, filename);
          FileInputStream fis = new FileInputStream(slice);
          int r = 0;
          while ((r = fis.read(buf)) != -1) {
            fos.write(buf, 0, r);
          }
          fis.close();
        }
        fos.close();
        if (idx.target.checksum.equalsIgnoreCase(md5(target))) {
          return target;
        } else {
          return null;
        }
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      } finally {
        if (fos != null) {
          try {
            fos.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    } else {
      return null;
    }
  }

  private static String md5(String content) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(content.getBytes());
      byte [] digest = md.digest();
      return toHex(digest);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private static String md5(File file) {
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

  private static String toHex(byte [] buf) {
    StringBuilder builder = new StringBuilder(buf.length * 2);
    for (int i = 0, len = buf.length; i < len; i ++) {
      builder.append(HEX[(buf[i] >> 4) & 0x0F]);
      builder.append(HEX[buf[i] & 0x0F]);
    }
    return builder.toString();
  }
}
