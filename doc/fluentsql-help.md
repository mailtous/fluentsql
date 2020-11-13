# 约定: 
1. 数据库字段默认风格是全小写加下划线分隔的 underline 风格
2. 使用 Attr.Property类型入参 fun(x::getMethod) 代表是使用默认的主表,
3. 使用 new Attr<>(x::getMethod) 入参,则使用你指定的 x 做为从表.
4. 不指定 spell 默认是把字段 AS 为驼峰格式
5. 如果选择的字段出现在子表查询里则要指定拼写为 Spell.UNDERLINE(受限于第一条约定)

# MAVEN 引用:
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

# 语法示例:
```sql
SELECT user.user_name AS userName,dept.dept_name AS deptName FROM `user` AS user  
LEFT JOIN `dept` ON user.dept_id = dept.id 
WHERE (LOCATE (user.user_name,'jack')>0)
GROUP BY user.dept_id HAVING (user.dept_id>0) 
ORDER BY user.dept_id ASC,user.user_name DESC

```
等同与:

```java
String sql = new Lq<User>(User.class) // 指定要查询的表
                .select(User::getUserName) // 选择要返回的字段(可以多个字段)
                .select(new Attr<>(Dept::getDeptName)) // 选择关联表的字段
                .leftJoin(Dept.class)                  // 关联表
                .andLike(User::getUserName, "jack")    // like 条件(userName为空,则会自动忽略本条件,有值则自动优化成 LOCATE 语句)
                .group(User::getDeptId)                // 分组查询
                .having(User::getDeptId, Qe.Opt.GT, 0) // 分组的条件
                .asc(User::getDeptId)                  // 升序
                .desc(User::getUserName)               // 降序
                .build();                              // 生成 SQL
```

# CRUD 示例
(DBProvider, 为底层的 ORM , 目前支持 JdbcTemplate , SQL2O )

一 . 查询:

1. 查询单个对象

```sql
SELECT * FROM `user` where id = 1;

```
==
```java

User user = new Lq<User>(User.class,DBProvider)
.andEq(User::getId,1)
.to();

```

2. 列表查询(可指定返回的字段)

```sql
SELECT user_name ,age FROM `user` where age > 18;

```
==

```java

List<User> userList = new Lq<User>(User.class,DBProvider)
.select(User::getUserName,User:getAge)
.andGt(User::getAge,18)
.toList();

```

3. 分页查询
```java
page.setPageNumber(1);
page.setPageSize(10);
Page<User> userPage = new Lq<User>(User.class,DBProvider)
.andGt(User::getAge,18)
.toPage(page);

```

二. 新增修改
```sql
 INSERT INTO `user` SET user_name = 'apple' ;
 UPDATE `user` SET user_name = 'apple' WHERE id = 1 ;
```
==

```java
ADD:
User user = new User();
user.setUserName("apple");
new Lq<User>(User.class ,DBProvider).toSave(user);

UPDATE:
User user = findUserById(1);
user.setUserName("apple");
new Lq<User>(User.class ,DBProvider).toUpdate(user);

```

三. 删除

```sql
 DELETE FROM `user` WHERE id = 1 ;
```
==

```java
new Lq<User>(User.class ,DBProvider).andEq(User::getId,1).toDel();

```

# 聚集查询

```sql
 SELECT COUNT(1) FROM `user` WHERE age > 18;

```


```java
new Lq<User>(User.class ,DBProvider)
.andGt(User::getAge,18)
.toCount();
```