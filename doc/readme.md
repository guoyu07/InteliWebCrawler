1. 使用java.util.concurrent.Executors 保证运行的thread数目不减少，尤其是parserThread异常中止后能重启。
2. 保存已经读取的url到文件，保证系统异常退出后能继续之前的作业
3. 