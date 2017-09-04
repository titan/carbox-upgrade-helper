package com.fengchaohuzhu.box.upgrade;

import java.util.List;

/**
 * 切片索引
 */

public class Index {
  /**
   * 切片合并后文件的索引信息
   */
  public IndexItem target;

  /**
   * 切片索引条目列表
   */
  public List<IndexItem> slices;
}
