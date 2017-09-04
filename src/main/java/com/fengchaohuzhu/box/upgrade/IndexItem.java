package com.fengchaohuzhu.box.upgrade;

/**
 * 切片索引条目
 */

public class IndexItem {
  /**
   * 切片合并后的文件名称或切片文件名称
   */
  public String filename;
  /**
   * 切片下载链接
   */
  public String url;
  /**
   * md5签名
   */
  public String checksum;
  /**
   * 文件大小
   */
  public int size;
}
