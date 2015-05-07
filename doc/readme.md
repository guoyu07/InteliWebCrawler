1. 使用java.util.concurrent.Executors 保证运行的thread数目不减少，尤其是parserThread异常中止后能重启。
2. 保存已经读取的url到文件，保证系统异常退出后能继续之前的作业
3. 已经读取的url保存的是md5之后的值
4. 设计一种保存运行状态“url-to”方法，保存那些将要爬取得数据。用于下一次继续运行