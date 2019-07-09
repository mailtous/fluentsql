# Fluent-SQL [![](https://jitpack.io/v/mailtous/fluentsql.svg)](https://jitpack.io/#mailtous/fluentsql)
我们不生产SQL,我们只是SQL的搬运工!

Lambda 风格的流式 SQL 查询工具,支持多表 lefjoin,from(子表),sum case , iif ,group by(子表属性)...
集成了SQL2O,JDBCTEMPLATE实现CRUD,分页查询等. 自我感觉良好,有了这个小工具,可以忘却 Hibernate,Mybatis...
Fluent-SQL 优势所在:
1. 无须编写实体类,您只需创建简单的 POJO 一一对应数据表字段,即可.
2. 您无须关心各种多对多,一对多等各种 ORM 关联配置,您懂得使用 Left-Join 即可以关联任意数据表.
3. Fluent-SQL 自带了防 SQL 注入.
4. 在不考虑缓存的情况下,有那一种 ORM 方案会比原生 SQL 快呢? 请告诉我让我学习进步一下,可好?

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
# MAVEN 
1. 在您的 POM 文件增加新的仓库 jitpack.io
2. 增加对本项目的依赖,您可以只选择对子项目的依赖 fluentsql-sql2o | fluentsql-jdbctemplate
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
	<dependency>
	    <groupId>com.github.mailtous</groupId>
	    <artifactId>fluentsql</artifactId>
	    <version>0.0.1-snapshot</version>
	</dependency>
```
