# PastMap

> 支持设置过期的Map类工具
> support setting expiring map utils

- 此Map中的数据操作是线程安全的

- 内部使用了两个Map来实现数据与过期时间绑定

- 采用懒清理的方式存储数据，当数据被获取时再判断是否过期

  判断为过期则清理该数据，否则返回该数据

- 性能测试 : 循环 10000000 条数据 100 次

  - 全部插入 : 平均 1.71 s
  - forEach：平均 1.46 s

- 简易用法：

  ```xml
  // 在pom.xml中引用
  <dependency>
      <groupId>io.github.dawnfz</groupId>
      <artifactId>PastMap</artifactId>
      <version>0.1.0</version>
  </dependency>
  ```
  
  ```java
  // expiring 单位为毫秒
  
  // [1] 默认创建，在外部使用定时器执行定时懒清理
  PastMap<K,V> pastMap = new PastMap<>();
  pastMap.put(key,value,expiring);
  // 定时清理过期数据
  TimerTask task = new TimerTask() {
      public void run() {pastMap.clearPastValue();}
  };
  Timer timer = new Timer();
  // 十分钟执行一次过期数据的清理
  timer.schedule(600000);
  ```

  ```java	
  // [2] 创建时启用内部定时器[推荐] - 600秒自动执行一次清理
  PastMap<K,V> pastMap = new PastMap<K,V>().timingClear(600);
  pastMap.put(key,value,expiring);
  
  // 当PastMap对象不再使用时需要手动关闭内部的定时器
  pastMap.closeTiming();
  ```
  
  ```java
  // [3] 不进行过期数据自动清理(数据少且PastMap生命周期短时使用)[不建议]
  PastMap<K,V> pastMap = new PastMap<>();
  pastMap.put(key,value,expiring);
  // 在过期数据被pastMap对象获取之前，pastMap中的过期数据会一直存在
  
  ```



# Engish - Google Translate

- Data operations in this Map are thread-safe

- Two Maps are used internally to bind data and expiration time

- Use lazy clear to store data, and then judge whether it is expired when the data is acquired

  If it is judged to be expired, the data will be cleared, otherwise the data will be returned

- Performance test: loop 10000000 pieces of data 100 times

  - Insert all : Average 1.71 s
  - forEach: average 1.46 s

- Simple usage:
  ```xml
  // import in pom.xml
  <dependency>
      <groupId>io.github.dawnfz</groupId>
      <artifactId>PastMap</artifactId>
      <version>0.1.0</version>
  </dependency>
  ```
  ```java
  // expiring in millis
  
  // [1] Use timers externally to perform scheduled lazy clear
  PastMap<K,V> pastMap = new PastMap<>();
  pastMap.put(key,value,expiring);
  // Periodically clear expired data
  TimerTask task = new TimerTask() {
      public void run() {pastMap.clearPastValue();}
  };
  Timer timer = new Timer();
  // Clean up expired data every ten minutes
  timer.schedule(600000);
  ```

  ```java	
  // [2] Enable internal timer on creation [recommended] - automatically perform a cleanup every 600 seconds
  PastMap<K,V> pastMap = new PastMap<K,V>().timingClear(600);
  pastMap.put(key,value,expiring);
  
  // When the PastMap object is no longer used, the internal timer needs to be manually closed
  pastMap.closeTiming();
  ```

  ```java
  // Do not automatically clean up expired data (used when there is little data and the PastMap life cycle is short) [not recommended]
  PastMap<K,V> pastMap = new PastMap<>();
  pastMap.put(key,value,expiring);
  // The expired data in the pastMap will always exist until the expired data is acquired by the pastMap object
  
  ```
  

