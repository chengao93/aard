# aard
一种高效的序列化方式，AST编译时期生成字节码，未雨绸缪（against a rainy day）

# 使用示例

## 创建User class
```
@EntityType(parent = false)
public class User {
    private String name;
    @EntityType
    private List<User> users;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
```
##   main
```
public static void main(String[] args) {
        User user = new User();
        user.setName("chengao");

        EntityService entityService = (EntityService) user;
        GetMethod[] getMethods = entityService.getterMethods();
        for (GetMethod getMethod : getMethods) {
            System.out.println(getMethod.get(user));
        }
        byte[] bytes = AardSerializer.toBytesNotDepth(user);
        User parse = ZzSerializer.parse(bytes);
}
```
##   maven config
```$xslt
<dependency>
    <groupId>io.github.chengao93</groupId>
    <artifactId>aard</artifactId>
    <version>1.2.4</version>
</dependency>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.0</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <forceJavacCompilerUse>true</forceJavacCompilerUse>

        <annotationProcessorPaths>
            <path>
                <groupId>io.github.chengao93</groupId>
                <artifactId>aard</artifactId>
                <version>1.2.4</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```


