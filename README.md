# fluent-sql
Lambda 风格的流式 SQL 查询工具,支持多表 lefjoin,from(子表),sum case , iif ,group by(子表属性)...

# 约定: 
1. 数据库字段默认风格是全小写加下划线分隔的 underline 风格
2. 使用 Attr.Property类型入参 fun(x::getMethod) 代表是使用默认的主表,
3. 使用 new Attr<>(x::getMethod) 入参,则使用你指定的 x 做为从表.
4. 不指定 spell 默认是把字段 AS 为驼峰格式
5. 如果选择的字段出现在子表查询里则要指定拼写为 Spell.UNDERLINE(受限于第一条约定)
