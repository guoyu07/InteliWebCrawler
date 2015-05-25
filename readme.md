### 简介 ###
一个使用多线程爬取网页内容的程序，提供动态的配置重新载入。
根据校园网络环境，提供一个自动ip切换与校园网帐号登入模块。

### TODO ###
1. 定义登录一面的刷选规则
2. 定义根据页面内容进行刷选的正则规则
3. 使用java.util.concurrent.Executors 保证运行的thread数目不减少，尤其是parserThread异常中止后能重启。（abandoned）
4. 保存已经读取的url到文件，保证系统异常退出后能继续之前的作业（done）
5. 已经读取的url保存的是md5之后的值（done）
6. 设计一种保存运行状态“url-to”方法，保存那些将要爬取得数据。用于下一次继续运行（done）
7. 动态配置重载
8. 处理同一个url不同hashtag（#）导致的页面重复
9. 使用一个高性能集合库trove或者使用内存数据库来替代现在使用的集合
10. 使用分批校验去重

### NOTICE ###
1. 现在的resume操作再次爬取的数量也是在配置里面设置的数量，而没有计算之前爬取的数量，所以如果是增量爬取需要手动修改配置
2. 如果使用resume的话需要保证url-to.txt文件有内容，否则不能呢个继续爬取，也不会使用seed配置

### FIX ###
1. 解析线程异常退出，爬取线程工作正常但是解析线程已经结束


### Contributor ###
wuxu92@gmail.com