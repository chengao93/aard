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


