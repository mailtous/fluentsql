# fluent-sql
Lambda 风格的流式 SQL 查询工具,支持多表 lefjoin,from(子表),sum case , iif ,group by(子表属性)...

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
