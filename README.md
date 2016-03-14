# CollectionManager

Sql query is formed by the following rules, presented in the section [sql rule](#sqlrule).
This utility is used to retrieve data from a collection by sql query.
A collection is a list of map tables, each map table is a separate row in this list
and displays a set of columns in key-value format,
where the key - the column name, value - the value of the column.

-----
### Feature:

1. Implemented a distinct method.
2. Left, Right, Outer join.
3. Sort rows.

-----
### <a name="sqlrule"></a>Sql rule:

* **select:**<br/>
select *column-expr* from *from-expr* [[where *where-expr*] | [order-by *order-expr*]]<br/>
* **column-expr:**<br/>
table_name.column_name | table_alias.column_name | column_name<br/>
column-expr [, column-expr]<br/>
* **from-expr:**<br/>
table-expr [, table-expr]<br/>
* **table-expr:**<br/>
table_name | table_name table_alias<br/>
* **where-expr:**<br/>
[(] where-expr1 [ or|and where-expr1] [)]<br/>
* **where-expr1:**<br/>
[(] column-expr*[+]* (<|<=|>|>=) column-expr*[+]* [)]<br/>
* **order-expr:**<br/>
column-expr asc|desc [, order-expr]<br/>

-----

### Requirements and restrictions:
* This application was compiled by java of the version 1.8.
* The value can be a null in map table (e.g. in column but not key) and all row with the value is null will be excluded from output data if they located in expression after "where".

-----
### Example:
Consider some examples to explain the work program.
More detailed information can look for in source of test files.<pre><code>
`List<Map<String, Object>> table1 = new ArrayList<>();
Map<String, Object> row = new HashMap<>();
table1.add(row);
row.put("c1", 1);
row.put("c2", 1);
... fill table1 ...
row = new HashMap<>();
table1.add(row);
row.put("c1", 2);
row.put("c2", 2);
...
List<Map<String, Object>> table2 = new ArrayList<>();
... fill table2 ...
    LEFT join:
List<Object> parameters = new ArrayList<>();
CollectionManager processor = new CollectionManager();
processor.setSqlExpression("select t1.c1, t2.c1 from table1 t1, table2 t2 where t2.c1 = t1.c1[+]);
processor.addTable("table1", table1);
processor.addTable("table2", table2);
List<Map<String, Object>> result = processor.perform();
    RIGHT join:
List<Object> parameters = new ArrayList<>();
CollectionManager processor = new CollectionManager();
processor.setSqlExpression("select t1.c1, t2.c1 from table1 t1, table2 t2 where t2.c1[+] = t1.c1);
processor.addTable("table1", table1);
processor.addTable("table2", table2);
List<Map<String, Object>> result = processor.perform();
    OUTER join:
List<Object> parameters = new ArrayList<>();
CollectionManager processor = new CollectionManager();
processor.setSqlExpression("select t1.c1, t2.c1 from table1 t1, table2 t2 where t2.c1[+] = t1.c1[+]);
processor.addTable("table1", table1);
processor.addTable("table2", table2);
List<Map<String, Object>> result = processor.perform();
    DISTINCT:
List<Object> parameters = new ArrayList<>();
CollectionManager processor = new CollectionManager();
processor.setSqlExpression("select distinct t1.c1 from table1 t1);
processor.addTable("table1", table1);
List<Map<String, Object>> result = processor.perform();
    ORDER BY:
List<Object> parameters = new ArrayList<>();
parameters.add(1);
CollectionManager processor = new CollectionManager();
processor.setSqlExpression("select t1.c1, t2.c1 from table1 t1, table2 t2 where t2.c1 = t1.c1 and t1.c2 = ?0 order by t1.c1 asc, t2.c2 desc);
processor.setParameters(parameters);
processor.addTable("table1", table1);
processor.addTable("table2", table2);
List<Map<String, Object>> result = processor.perform();`
</code></pre>