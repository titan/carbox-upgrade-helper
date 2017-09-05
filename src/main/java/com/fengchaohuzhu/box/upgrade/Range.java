package com.fengchaohuzhu.box.upgrade;

/**
 * 文件内容 Range。根据 HTTP 协议规范 <a
 * href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35">https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35</a>
 * ，Range 是闭区间，start 是 first-byte-pos，stop 是 last-byte-pos。<br>
 *
 * 比如：<br>
 *
 * <ul>
 *   <li>The first 500 bytes: start = 0, stop = 499</li>
 *   <li>The second 500 bytes: start = 500, stop = 999</li>
 * </ul>
 */
public class Range {
  /**
   * 起始地址
   */
  public int start;
  /**
   * 结束地址
   */
  public int stop;
}
