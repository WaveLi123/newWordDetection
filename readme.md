# 新词发现毕业设计

# 1. 问题
- unicode中非utf8字符，可能有bug
- 低频词和数据稀疏性
- 按照自己的语料训练，可能繁体字和半角符号不知道会怎样。
- crf_test需要自己准备

# 2. 计划
- [ ] 5.10论文初稿
- [x] 字母词和数字去掉
- [x] BEMS变成分词文件和新词
- [x] 原始文件变成分词bems文件（单字特征
- [ ] 测试分词准确率
- [ ] 数据预处理

# 3.想法
- [x] 将所有标点符号当做断句的东西，而不只是句号
