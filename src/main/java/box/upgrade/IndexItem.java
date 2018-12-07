package box.upgrade;

/**
 * 切片索引条目
 */

public class IndexItem {
  /**
   * md5签名
   */
  public String checksum;
  /**
   * 文件大小
   */
  public int size;
  /**
   * 切片地址范围
   */
  public Range range;
}
