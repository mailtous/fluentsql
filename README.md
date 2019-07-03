# fluent-sql
我们不创建SQL,我们只是SQL的搬运工!

Lambda 风格的流式 SQL 查询工具,支持多表 lefjoin,from(子表),sum case , iif ,group by(子表属性)...
集成了SQL2O,JDBCTEMPLATE实现CRUD,分页查询等. 自我感觉良好,有了这个小工具,可以忘却 Hibernate,Mybatis...

# 开发思历
世面上有的 ORM 我基本上都用过,用得最多的是 Hibernate,Mybatis. 
日子久了我就在想有没有一种 ORM 既易用而又高性能的呢?鱼与掌真的不能兼得吗? 要说高性能,原生SQL肯定是性能最高,可是原生SQL却有硬代码,并且因为前端的查询条件的可选性,自己手动拼接查询条件时,必然会有许许多多的 IF(null!=xx){sql}... , 这个缺点 Hibernate,Mybatis 一样也会有.
这催发了我 开发出 fluent-sql 这个项目, 这只是一个工具类,绝对会让你大叫: SHUANG! SHUANG! SHUANG!

fluent-sql + sql2o ,就是性能与易用的经典示范!

SQL2O: https://github.com/aaberg/sql2o

# 约定: 
1. 数据库字段默认风格是全小写加下划线分隔的 underline 风格
2. 使用 Attr.Property类型入参 fun(x::getMethod) 代表是使用默认的主表,
3. 使用 new Attr<>(x::getMethod) 入参,则使用你指定的 x 做为从表.
4. 不指定 spell 默认是把字段 AS 为驼峰格式
5. 如果选择的字段出现在子表查询里则要指定拼写为 Spell.UNDERLINE(受限于第一条约定)

# 语法示例:
```sql
SELECT user.user_name AS userName,dept.dept_name AS deptName FROM `user` AS user  
LEFT JOIN `dept` ON user.dept_id = dept.id 
WHERE ((LOCATE (user.user_name,'jack')>0) ) 
GROUP BY user.dept_id HAVING (user.dept_id>0) 
ORDER BY user.dept_id ASC,user.user_name DESC

```
```java
String sql = new Lq<>(User.class)
                .select(User::getUserName)
                .select(new Attr<>(Dept::getDeptName))
                .leftJoin(Dept.class)
                .andLike(User::getUserName, "jack")
                .asc(User::getDeptId)
                .desc(User::getUserName)
                .group(User::getDeptId)
                .having(User::getDeptId, Qe.Opt.GT, 0)
                .build();
```
