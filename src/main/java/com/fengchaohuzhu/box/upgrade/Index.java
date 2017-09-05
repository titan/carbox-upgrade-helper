package com.fengchaohuzhu.box.upgrade;

import java.util.List;

/**
 * 切片索引
 */

public class Index {

  /**
   * 目标文件的文件名称
   */
  public String filename;

  /**
   * 目标文件的下载链接
   */
  public String url;

  /**
   * 基于 MD5 的文件签名
   */
  public String checksum;

  /**
   * 目标文件大小
   */
  public int size;

  /**
   * 切片索引条目列表
   */
  public List<IndexItem> slices;
}
