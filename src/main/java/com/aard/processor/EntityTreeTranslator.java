package com.aard.processor;

import com.aard.processor.annotation.EntityType;
import com.aard.processor.serializable.*;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.aard.processor.util.ClassUtil;
import com.aard.processor.util.ObjectSerializableUtil;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

import java.util.*;


/**
 * Desc: 遍历抽象语法树
 */
public class EntityTreeTranslator extends TreeTranslator {

    private TreeMaker treeMaker;
    private Names names;
    private TreePath treesPath;
    private Map<String, Set<String>> classParentMap;
    private Messager messager;

    private JCClassDecl jcClassDecl;

    public EntityTreeTranslator(TreePath treesPath, TreeMaker treeMaker, Names names, Map<String, Set<String>> classParentMap, Messager messager) {
        this.treesPath = treesPath;
        this.treeMaker = treeMaker;
        this.names = names;
        this.classParentMap = classParentMap;
        this.messager = messager;
    }

    private String className;

    private Map<String, String> importMap = new HashMap<>();

    public static final Set<String> ENTITY_CLASS_NAME = new LinkedHashSet<>();
    public static final Map<String, EntityTypeValue> ENTITY_CLASS_TYPE_VALUE = new LinkedHashMap<>();

    private boolean isEntity;
    private boolean isParentEntity;

    private Map<String, JCTree> getterMethodFieldTypeMap = new LinkedHashMap<>();
    private Map<String, JCExpression> setterMethodFieldTypeMap = new LinkedHashMap<>();
    private Set<String> setterFields = new LinkedHashSet<>();
    private Set<String> getterFields = new LinkedHashSet<>();

    private Set<String> setterEntityFields = new LinkedHashSet<>();
    private Set<String> getterEntityFields = new LinkedHashSet<>();
    private Map<String, EntityTypeValue> getterEntityFieldEntityTypeValueMap = new LinkedHashMap<>();
    private Map<String, EntityTypeValue> setterEntityFieldEntityTypeValueMap = new LinkedHashMap<>();

    private Set<String> setterNotEntityFields = new LinkedHashSet<>();
    private Set<String> getterNotEntityFields = new LinkedHashSet<>();

    private Map<String, Type> fieldTypeMap = new LinkedHashMap<>();
    private Map<String, EntityTypeValue> fieldEntityTypeValueMap = new LinkedHashMap<>();

    /**
     * 遍历到类的时候执行
     */
    @Override
    public void visitClassDef(JCClassDecl jcClassDecl) {
        if (StringUtils.isEmpty(jcClassDecl.getSimpleName())) {
            super.visitClassDef(jcClassDecl);
            return;
        }
        if (StringUtils.isNotBlank(this.className)) {
            super.visitClassDef(jcClassDecl);
            return;
        }
        this.jcClassDecl = jcClassDecl;
        this.className = this.treesPath.getCompilationUnit().getPackageName().toString() + "." + jcClassDecl.getSimpleName();
        java.util.List<? extends ImportTree> imports = this.treesPath.getCompilationUnit().getImports();
        if (imports != null) {
            for (ImportTree anImport : imports) {
                Tree qualifiedIdentifier = anImport.getQualifiedIdentifier();
                if (qualifiedIdentifier != null) {
                    String imp = anImport.getQualifiedIdentifier().toString();
                    String key = StringUtils.substringAfterLast(imp, ".");
                    this.importMap.put(key, imp);
                }
            }
        }
        List<JCAnnotation> annotations = jcClassDecl.getModifiers().getAnnotations();
        EntityTypeValue entityTypeValue = getEntityTypeValue(annotations);
        if (entityTypeValue != null) {
            if (StringUtils.isBlank(entityTypeValue.classValue)) {
                entityTypeValue.classValue = this.className;
            }
            ENTITY_CLASS_NAME.add(entityTypeValue.classValue);
            ENTITY_CLASS_TYPE_VALUE.put(entityTypeValue.classValue, entityTypeValue);
            this.isParentEntity = entityTypeValue.parent;
        }
        this.isEntity = entityTypeValue != null;

        if (this.isEntity) {
            initFieldType();
        }
        super.visitClassDef(jcClassDecl);

        if (this.isEntity) {
            messager.printMessage(Diagnostic.Kind.NOTE, "process: " + this.className);

            JCTree.JCCompilationUnit imp = (JCTree.JCCompilationUnit) this.treesPath.getCompilationUnit();
            List<JCTree> preList = List.nil();
            List<JCTree> afterList = List.nil();

            for (JCTree def : imp.defs) {
                if (def instanceof JCClassDecl) {
                    afterList = afterList.append(def);
                    treeMaker.pos = def.pos;
                } else {
                    preList = preList.append(def);
                }
            }
            preList = preList.append(treeMaker.Import(treeMaker.Select(ident("com.aard.processor"), name("EntityService")), false));
            preList = preList.append(treeMaker.Import(treeMaker.Select(ident("com.aard.processor.function"), name("*")), false));
            preList = preList.append(treeMaker.Import(treeMaker.Select(ident("com.aard.processor.serializable"), name("*")), false));
            preList = preList.append(treeMaker.Import(treeMaker.Select(ident("com.aard.processor.util"), name("*")), false));
            preList = preList.append(treeMaker.Import(treeMaker.Select(ident("java.util"), name("*")), false));
            preList = preList.append(treeMaker.Import(treeMaker.Select(ident("io.netty.buffer"), name("*")), false));
            preList = preList.append(treeMaker.Import(treeMaker.Select(ident("java.nio"), name("ByteBuffer")), false));
            imp.defs = preList;
            imp.defs = imp.defs.appendList(afterList);
            jcClassDecl.implementing = jcClassDecl.implementing.append(treeMaker.Ident(name("EntityService")));

            addSuppressWarnings();

            for (JCTree def : jcClassDecl.defs) {
                treeMaker.pos = def.pos;
            }
            createStaticGetterFieldFun();
            createStaticSetterFieldFun();

            createStaticGetterField();
            createStaticGetterNotEntityField();
            createStaticGetterEntityField();
            createStaticSetterField();
            createStaticSetterNotEntityField();
            createStaticSetterEntityField();

            createGetterMethod();
            createGetterNotEntityMethod();
            createGetterEntityMethod();
            createSetterMethod();
            createSetterNotEntityMethod();
            createSetterEntityMethod();

            createGetterFuncMethod();
            createGetterNotEntityFuncMethod();
            createGetterEntityFuncMethod();
            createSetterFuncMethod();
            createSetterNotEntityFuncMethod();
            createSetterEntityFuncMethod();

            createGetterFieldFuncMethod();
            createGetterNotEntityFieldFuncMethod();
            createGetterEntityFieldFuncMethod();
            createSetterFieldFuncMethod();
            createSetterNotEntityFieldFuncMethod();
            createSetterEntityFieldFuncMethod();

            createGetterFieldFuncMethod2();
            createGetterNotEntityFieldFuncMethod2();
            createGetterEntityFieldFuncMethod2();
            createSetterFieldFuncMethod2();
            createSetterNotEntityFieldFuncMethod2();
            createSetterEntityFieldFuncMethod2();

            createHasGetterFieldMethod();
            createHasGetterNotEntityFieldMethod();
            createHasGetterEntityFieldMethod();
            createHasSetterFieldMethod();
            createHasSetterNotEntityFieldMethod();
            createHasSetterEntityFieldMethod();

            createHasGetterFieldMethod2();
            createHasGetterNotEntityFieldMethod2();
            createHasGetterEntityFieldMethod2();
            createHasSetterFieldMethod2();
            createHasSetterNotEntityFieldMethod2();
            createHasSetterEntityFieldMethod2();

            createInvokedGetterFieldMethod();
            createInvokedGetterNotEntityFieldMethod();
            createInvokedGetterEntityFieldMethod();
            createInvokedSetterFieldMethod();
            createInvokedSetterNotEntityFieldMethod();
            createInvokedSetterEntityFieldMethod();

            createInvokedGetterFieldMethod2();
            createInvokedGetterNotEntityFieldMethod2();
            createInvokedGetterEntityFieldMethod2();
            createInvokedSetterFieldMethod2();
            createInvokedSetterNotEntityFieldMethod2();
            createInvokedSetterEntityFieldMethod2();

            createByteLengthNotEntityMethod();
            createNotEntitySerializableMethod();
            createSerializableNotEntityMethod();
            createNotEntityDeserializationMethod();

            createCopyMethod();
            createCopyNotEntityMethod();

            //messager.printMessage(Diagnostic.Kind.NOTE, this.treesPath.getCompilationUnit().toString());
        }
        this.result = jcClassDecl;
    }

    @Override
    public void visitImport(JCTree.JCImport jcImport) {
        super.visitImport(jcImport);
    }

    /**
     * 遍历成员遍历，参数等等
     */
    @Override
    public void visitVarDef(JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);
    }

    @Override
    public void visitMethodDef(JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);
        if (!this.isEntity) {
            return;
        }
        Set<Modifier> flags = jcMethodDecl.getModifiers().getFlags();
        if (flags.contains(Modifier.STATIC)) {
            return;
        }
        if (!flags.contains(Modifier.PUBLIC)) {
            return;
        }
        String methodName = jcMethodDecl.name.toString();
        if (methodName.length() <= 3) {
            return;
        }
        char c4 = methodName.charAt(3);
        if (!(c4 >= 'A' && c4 <= 'Z')) {
            return;
        }
        String fieldName = StringUtils.right(methodName, methodName.length() - 3);
        fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
        EntityTypeValue entityTypeValue = getEntityTypeValue(jcMethodDecl.getModifiers().getAnnotations());
        if (entityTypeValue == null) {
            entityTypeValue = this.fieldEntityTypeValueMap.get(fieldName);
        }
        if (StringUtils.startsWith(methodName, "get") && jcMethodDecl.params.isEmpty()) {
            if (StringUtils.equals(jcMethodDecl.getReturnType().toString(), "void")) {
                return;
            }
            this.getterFields.add(fieldName);
            this.getterMethodFieldTypeMap.put(fieldName, jcMethodDecl.getReturnType());
            if (entityTypeValue != null || isEntityScope(jcMethodDecl.getReturnType().type)) {
                this.getterEntityFields.add(fieldName);
                if (entityTypeValue == null) {
                    entityTypeValue = new EntityTypeValue();
                }
                if (StringUtils.isBlank(entityTypeValue.classValue)) {
                    entityTypeValue.classValue = getEntityStr(jcMethodDecl.getReturnType().type);
                }
                this.getterEntityFieldEntityTypeValueMap.put(fieldName, entityTypeValue);
            } else {
                this.getterNotEntityFields.add(fieldName);
            }
        } else if (StringUtils.startsWith(methodName, "set") && jcMethodDecl.params.size() == 1) {
            this.setterFields.add(fieldName);
            this.setterMethodFieldTypeMap.put(fieldName, jcMethodDecl.params.get(0).vartype);

            Type type = jcMethodDecl.params.get(0).vartype.type;
            if (entityTypeValue != null || isEntityScope(type)) {
                this.setterEntityFields.add(fieldName);
                if (entityTypeValue == null) {
                    entityTypeValue = new EntityTypeValue();
                }
                if (StringUtils.isBlank(entityTypeValue.classValue)) {
                    entityTypeValue.classValue = getEntityStr(type);
                }
                this.setterEntityFieldEntityTypeValueMap.put(fieldName, entityTypeValue);
            } else {
                this.setterNotEntityFields.add(fieldName);
            }
        }
    }

    private EntityTypeValue getEntityTypeValue(List<JCAnnotation> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return null;
        }
        for (JCAnnotation annotation : annotations) {
            if (annotation.type == null) {
                continue;
            }
            if (EntityType.class.getName().equals(annotation.type.toString())) {
                EntityTypeValue entityTypeValue = new EntityTypeValue();
                List<JCExpression> arguments = annotation.getArguments();
                String classes = null;
                if (arguments != null) {
                    for (JCExpression argument : arguments) {
                        if (Class.class.getName().equals(argument.type.toString())) {
                            String classesStr = ((JCAssign) argument).getExpression().toString();
                            classesStr = StringUtils.removeEnd(classesStr, ".class");
                            if (!StringUtils.contains(classesStr, ".")) {
                                classes = this.importMap.get(classesStr);
                                if (classes == null) {
                                    classes = this.treesPath.getCompilationUnit().getPackageName().toString() + "." + classesStr;
                                }
                                entityTypeValue.classValue = classes;
                            } else {
                                entityTypeValue.classValue = classesStr;
                            }
                        } else if (boolean.class.getName().equals(argument.type.toString())) {
                            String value = ((JCAssign) argument).getExpression().toString();
                            entityTypeValue.parent = Boolean.valueOf(value);
                        }
                    }
                }
                return entityTypeValue;
            }
        }
        return null;
    }

    private void initFieldType() {
        List<JCTree> defs = this.jcClassDecl.defs;
        for (JCTree def : defs) {
            if (!(def instanceof JCVariableDecl)) {
                continue;
            }
            JCVariableDecl jcVariableDecl = (JCVariableDecl) def;
            JCTree.JCModifiers modifiers = jcVariableDecl.getModifiers();
            Set<Modifier> flags = modifiers.getFlags();
            if (flags.contains(Modifier.FINAL)) {
                continue;
            }
            if (flags.contains(Modifier.STATIC)) {
                continue;
            }
            if (flags.contains(Modifier.TRANSIENT)) {
                continue;
            }
            fieldTypeMap.put(jcVariableDecl.name.toString(), jcVariableDecl.vartype.type);
            EntityTypeValue entityTypeValue = getEntityTypeValue(jcVariableDecl.getModifiers().getAnnotations());
            if (entityTypeValue != null || isEntityScope(jcVariableDecl.vartype.type)) {
                if (entityTypeValue == null) {
                    entityTypeValue = new EntityTypeValue();
                }
                if (StringUtils.isBlank(entityTypeValue.classValue)) {
                    entityTypeValue.classValue = getEntityStr(jcVariableDecl.vartype.type);
                }
                fieldEntityTypeValueMap.put(jcVariableDecl.name.toString(), entityTypeValue);
            }
        }
    }

    /**
     * 获取Type 的 Entity str
     *
     * @param type 类型
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/7 19:46
     */
    private String getEntityStr(Type type) {
        String typeName = type.toString();
        typeName = StringUtils.removeEnd(typeName, "[]");
        if (isEntityScope(typeName)) {
            return typeName;
        } else {
            List<Type> typeArguments = type.getTypeArguments();
            if (typeArguments != null && !typeArguments.isEmpty()) {
                for (Type typeArgument : typeArguments) {
                    if (isEntityScope(typeArgument.toString())) {
                        return typeArgument.toString();
                    }
                }
            }
        }
        List<Type> typeArguments = type.getTypeArguments();
        if (typeArguments != null && !typeArguments.isEmpty()) {
            return typeArguments.get(typeArguments.size() - 1).toString();
        }
        return type.toString();
    }

    /**
     * 是否在Entity范围内
     *
     * @param type 类型
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/7 19:46
     */
    private boolean isEntityScope(Type type) {
        if (type == null) {
            return false;
        }
        String typeName = type.toString();
        typeName = StringUtils.removeEnd(typeName, "[]");
        if (isEntityScope(typeName)) {
            return true;
        } else {
            List<Type> typeArguments = type.getTypeArguments();
            boolean isArg = false;
            if (typeArguments != null && !typeArguments.isEmpty()) {
                for (Type typeArgument : typeArguments) {
                    if (isEntityScope(typeArgument.toString())) {
                        isArg = true;
                    }
                }
            }
            if (isArg) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否在Entity范围内
     *
     * @param className 类名
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/7 19:46
     */
    private boolean isEntityScope(String className) {
        if (StringUtils.isEmpty(className)) {
            return false;
        }
        if (ENTITY_CLASS_NAME.contains(className)) {
            return true;
        }
        try {
            Class<?> classes = Class.forName(className);
            if (ClassUtil.isAssignable(EntityService.class, classes)) {
                return true;
            }
        } catch (Exception e) {
        }
        return isParentEntityScope(className);
    }

    /**
     * 父级是否在Entity范围内
     *
     * @param className 类名
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/7 19:46
     */
    private boolean isParentEntityScope(String className) {
        if (ENTITY_CLASS_TYPE_VALUE.containsKey(className)) {
            if (ENTITY_CLASS_TYPE_VALUE.get(className).parent) {
                return true;
            }
        }
        Set<String> parents = this.classParentMap.get(className);
        if (parents != null && !parents.isEmpty()) {
            for (String parent : parents) {
                if (ENTITY_CLASS_NAME.contains(parent)) {
                    return true;
                }
            }
            return false;
        } else {
            try {
                Class<?> classes = Class.forName(className).getSuperclass();
                while (true) {
                    if (classes == Object.class) {
                        break;
                    }
                    if (ENTITY_CLASS_NAME.contains(classes.getName())) {
                        return true;
                    }
                    classes = classes.getSuperclass();
                }
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    private void addSuppressWarnings() {
        List<JCAnnotation> annotations = this.jcClassDecl.getModifiers().getAnnotations();
        if (annotations == null) {
            this.jcClassDecl.getModifiers().annotations = List.nil();
            annotations = this.jcClassDecl.getModifiers().getAnnotations();
        }
        for (JCAnnotation annotation : annotations) {
            if (SuppressWarnings.class.getName().equals(annotation.annotationType.toString())) {
                return;
            }
        }
        JCAnnotation suppressWarnings = treeMaker.Annotation(ident("SuppressWarnings"), List.of(treeMaker.Literal("all")));
        annotations = annotations.append(suppressWarnings);
        this.jcClassDecl.getModifiers().annotations = annotations;
    }

    private void addSuppressWarnings(JCVariableDecl tree) {
        List<JCAnnotation> annotations = tree.getModifiers().getAnnotations();
        if (annotations == null) {
            tree.getModifiers().annotations = List.nil();
            annotations = tree.getModifiers().getAnnotations();
        }
        for (JCAnnotation annotation : annotations) {
            if (SuppressWarnings.class.getName().equals(annotation.annotationType.toString())) {
                return;
            }
        }
        JCAnnotation suppressWarnings = treeMaker.Annotation(ident("SuppressWarnings"), List.of(treeMaker.Literal("all")));
        annotations = annotations.append(suppressWarnings);
        tree.getModifiers().annotations = annotations;
    }

    private void addSuppressWarnings(JCMethodDecl tree) {
        List<JCAnnotation> annotations = tree.getModifiers().getAnnotations();
        if (annotations == null) {
            tree.getModifiers().annotations = List.nil();
            annotations = tree.getModifiers().getAnnotations();
        }
        for (JCAnnotation annotation : annotations) {
            if (SuppressWarnings.class.getName().equals(annotation.annotationType.toString())) {
                return;
            }
        }
        JCAnnotation suppressWarnings = treeMaker.Annotation(ident("SuppressWarnings"), List.of(treeMaker.Literal("all")));
        annotations = annotations.append(suppressWarnings);
        tree.getModifiers().annotations = annotations;
    }

    private void createStaticGetterFieldFun() {
        for (String getterField : this.getterFields) {
            String fieldName = getterField + "_GETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            String className = getterField.substring(0, 1).toUpperCase() + getterField.substring(1) + "GetMethod";
            JCVariableDecl getterFieldFun = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC + Flags.FINAL)
                    , name(fieldName)
                    , type(className)
                    , treeMaker.NewClass(null, null, ident(className), List.nil(), null)
            );
            addSuppressWarnings(getterFieldFun);
            this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterFieldFun);
        }
        for (String getterField : this.getterFields) {
            String className = getterField.substring(0, 1).toUpperCase() + getterField.substring(1) + "GetMethod";
            String thisClassName = StringUtils.substringAfterLast(this.className, ".");
            String fieldClassName = getClassName(this.getterMethodFieldTypeMap.get(getterField));
            if (fieldClassName == null) {
                messager.printMessage(Diagnostic.Kind.NOTE, "getter field type is null !" + getterField);
                continue;
            }
            JCExpression select = type(fieldClassName);
            if (StringUtils.contains(fieldClassName, "[")) {
                select = treeMaker.TypeArray(type(StringUtils.substringBefore(fieldClassName, "[")));
            }
            List<JCTree.JCExpression> imp = List.of(treeMaker.TypeApply(select("GetMethod"), List.of(select(thisClassName), select)));

            JCMethodDecl initMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("<init>"),
                    // 方法返回的类型
                    null,
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.nil(),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Exec(treeMaker.Apply(List.nil(), ident("super"), List.nil())))),
                    // 默认值
                    null
            );
            JCMethodDecl implMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("get"),
                    // 方法返回的类型
                    select,
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("item"), ident(thisClassName), null)),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Return(treeMaker.Apply(List.nil(), select("item", "get" + toTitleCase(getterField)), List.nil())))),
                    // 默认值
                    null
            );
            addSuppressWarnings(implMethod);
            JCMethodDecl nameMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("name"),
                    // 方法返回的类型
                    ident("String"),
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.nil(),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Return(treeMaker.Literal("get" + toTitleCase(getterField))))),
                    // 默认值
                    null
            );
            addSuppressWarnings(nameMethod);
            JCMethodDecl fieldMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("field"),
                    // 方法返回的类型
                    ident("String"),
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.nil(),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Return(treeMaker.Literal(getterField)))),
                    // 默认值
                    null
            );
            addSuppressWarnings(fieldMethod);
            JCMethodDecl classesMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("classes"),
                    // 方法返回的类型
                    ident("Class"),
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.nil(),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Return(select(select, "class")))),
                    // 默认值
                    null
            );
            addSuppressWarnings(classesMethod);
            EntityTypeValue entityTypeValue = this.getterEntityFieldEntityTypeValueMap.get(getterField);
            JCTree.JCExpression literal = treeMaker.Literal(TypeTag.BOT, null);
            if (entityTypeValue != null) {
                literal = select(select(entityTypeValue.classValue), "class");
            } else if (this.fieldEntityTypeValueMap.containsKey(getterField)) {
                entityTypeValue = this.fieldEntityTypeValueMap.get(getterField);
                literal = select(select(entityTypeValue.classValue), "class");
            }
            JCMethodDecl entityClassesMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("entityClasses"),
                    // 方法返回的类型
                    ident("Class"),
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.nil(),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Return(literal))),
                    // 默认值
                    null
            );
            addSuppressWarnings(entityClassesMethod);

            JCClassDecl implClass = treeMaker.ClassDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC + Flags.FINAL), name(className), List.nil(), null, imp, List.of(initMethod, implMethod, nameMethod, fieldMethod, classesMethod, entityClassesMethod));
            this.jcClassDecl.defs = this.jcClassDecl.defs.append(implClass);
        }

    }

    private void createStaticSetterFieldFun() {
        for (String setterField : this.setterFields) {
            String fieldName = setterField + "_SETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            String className = setterField.substring(0, 1).toUpperCase() + setterField.substring(1) + "SetMethod";
            JCVariableDecl setterFieldFun = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC + Flags.FINAL)
                    , name(fieldName)
                    , select(className)
                    , treeMaker.NewClass(null, List.nil(), ident(className), List.nil(), null)
            );
            addSuppressWarnings(setterFieldFun);
            this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterFieldFun);
        }
        for (String setterField : this.setterFields) {
            String className = setterField.substring(0, 1).toUpperCase() + setterField.substring(1) + "SetMethod";
            String thisClassName = StringUtils.substringAfterLast(this.className, ".");
            String fieldClassName = getClassName(this.setterMethodFieldTypeMap.get(setterField));
            if (fieldClassName == null) {
                messager.printMessage(Diagnostic.Kind.NOTE, "setter field type is null !" + setterField);
                continue;
            }
            JCExpression select = type(fieldClassName);
            if (StringUtils.contains(fieldClassName, "[")) {
                select = treeMaker.TypeArray(type(StringUtils.substringBefore(fieldClassName, "[")));
            }
            List<JCTree.JCExpression> imp = List.of(treeMaker.TypeApply(ident("SetMethod"), List.of(select(thisClassName), select)));

            JCMethodDecl initMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("<init>"),
                    // 方法返回的类型
                    null,
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.nil(),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Exec(treeMaker.Apply(List.nil(), ident("super"), List.nil())))),
                    // 默认值
                    null
            );
            JCMethodDecl implMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("set"),
                    // 方法返回的类型
                    treeMaker.Type(new Type.JCVoidType()),
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("item"), ident(thisClassName), null)
                            , treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("value"), select, null)),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Exec(treeMaker.Apply(List.nil(), select("item", "set" + toTitleCase(setterField)), List.of(ident("value")))))),
                    // 默认值
                    null
            );
            addSuppressWarnings(implMethod);
            JCMethodDecl nameMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("name"),
                    // 方法返回的类型
                    ident("String"),
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.nil(),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Return(treeMaker.Literal("get" + toTitleCase(setterField))))),
                    // 默认值
                    null
            );
            addSuppressWarnings(nameMethod);
            JCMethodDecl fieldMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("field"),
                    // 方法返回的类型
                    ident("String"),
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.nil(),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Return(treeMaker.Literal(setterField)))),
                    // 默认值
                    null
            );
            addSuppressWarnings(fieldMethod);
            JCMethodDecl classesMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("classes"),
                    // 方法返回的类型
                    ident("Class"),
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.nil(),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Return(select(select, "class")))),
                    // 默认值
                    null
            );
            addSuppressWarnings(classesMethod);
            EntityTypeValue entityTypeValue = this.setterEntityFieldEntityTypeValueMap.get(setterField);
            JCTree.JCExpression literal = treeMaker.Literal(TypeTag.BOT, null);
            if (entityTypeValue != null) {
                literal = select(select(entityTypeValue.classValue), "class");
            } else if (this.fieldEntityTypeValueMap.containsKey(setterField)) {
                entityTypeValue = this.fieldEntityTypeValueMap.get(setterField);
                literal = select(select(entityTypeValue.classValue), "class");
            }
            JCMethodDecl entityClassesMethod = treeMaker.MethodDef(
                    // public方法
                    treeMaker.Modifiers(Flags.PUBLIC),
                    // 方法名称
                    name("entityClasses"),
                    // 方法返回的类型
                    ident("Class"),
                    // 泛型参数
                    List.nil(),
                    // 方法参数
                    List.nil(),
                    // throw表达式
                    List.nil(),
                    // 方法体
                    treeMaker.Block(0L, List.of(treeMaker.Return(literal))),
                    // 默认值
                    null
            );
            addSuppressWarnings(entityClassesMethod);

            JCClassDecl implClass = treeMaker.ClassDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC + Flags.FINAL), name(className), List.nil(), null, imp, List.of(initMethod, implMethod, nameMethod, fieldMethod, classesMethod, entityClassesMethod));
            this.jcClassDecl.defs = this.jcClassDecl.defs.append(implClass);
        }
    }

    private void createStaticGetterField() {
        List<JCExpression> elems = List.nil();
        for (String getterField : this.getterFields) {
            elems = elems.append(treeMaker.Literal(getterField));
        }
        JCVariableDecl jcVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("GETTER_FIELD")
                , treeMaker.TypeArray(ident("String"))
                , treeMaker.NewArray(ident("String"), List.nil(), elems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDecl);

        JCVariableDecl flagJCVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("GETTER_FIELD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJCVariableDecl);

        List<JCExpression> methodElems = List.nil();
        for (String getterField : this.getterFields) {
            String fieldName = getterField + "_GETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            methodElems = methodElems.append(ident(fieldName));
        }
        JCVariableDecl jcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("GETTER_METHOD")
                , treeMaker.TypeArray(ident("GetMethod"))
                , treeMaker.NewArray(ident("GetMethod"), List.nil(), methodElems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDeclMethod);
        JCVariableDecl flagJcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("GETTER_METHOD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJcVariableDeclMethod);


    }

    private void createStaticGetterNotEntityField() {
        List<JCExpression> elems = List.nil();
        for (String getterField : this.getterNotEntityFields) {
            elems = elems.append(treeMaker.Literal(getterField));
        }
        JCVariableDecl jcVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("GETTER_NOT_ENTITY_FIELD")
                , treeMaker.TypeArray(ident("String"))
                , treeMaker.NewArray(ident("String"), List.nil(), elems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDecl);

        JCVariableDecl flagJCVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("GETTER_NOT_ENTITY_FIELD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJCVariableDecl);

        List<JCExpression> methodElems = List.nil();
        for (String getterField : this.getterNotEntityFields) {
            String fieldName = getterField + "_GETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            methodElems = methodElems.append(ident(fieldName));
        }
        JCVariableDecl jcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("GETTER_NOT_ENTITY_METHOD")
                , treeMaker.TypeArray(ident("GetMethod"))
                , treeMaker.NewArray(ident("GetMethod"), List.nil(), methodElems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDeclMethod);
        JCVariableDecl flagJcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("GETTER_NOT_ENTITY_METHOD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJcVariableDeclMethod);
    }

    private void createStaticGetterEntityField() {
        List<JCExpression> elems = List.nil();
        for (String getterField : this.getterEntityFields) {
            elems = elems.append(treeMaker.Literal(getterField));
        }
        JCVariableDecl jcVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("GETTER_ENTITY_FIELD")
                , treeMaker.TypeArray(ident("String"))
                , treeMaker.NewArray(ident("String"), List.nil(), elems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDecl);

        JCVariableDecl flagJCVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("GETTER_ENTITY_FIELD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJCVariableDecl);

        List<JCExpression> methodElems = List.nil();
        for (String getterField : this.getterEntityFields) {
            String fieldName = getterField + "_GETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            methodElems = methodElems.append(ident(fieldName));
        }
        JCVariableDecl jcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("GETTER_ENTITY_METHOD")
                , treeMaker.TypeArray(ident("GetMethod"))
                , treeMaker.NewArray(ident("GetMethod"), List.nil(), methodElems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDeclMethod);
        JCVariableDecl flagJcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("GETTER_ENTITY_METHOD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJcVariableDeclMethod);


        List<JCExpression> methodElems2 = List.nil();
        for (String getterField : this.getterEntityFields) {
            String fieldName = getterField + "_GETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            EntityTypeValue entityTypeValue = this.getterEntityFieldEntityTypeValueMap.get(getterField);
            if (entityTypeValue == null) {
                continue;
            }
            JCTree.JCFieldAccess select = treeMaker.Select(select(entityTypeValue.classValue), name("class"));
            methodElems2 = methodElems2.append(treeMaker.NewArray(ident("Object"), List.nil(), List.of(select, treeMaker.Literal(getterField), ident(fieldName))));
        }

    }

    private void createStaticSetterField() {
        List<JCExpression> elems = List.nil();
        for (String getterField : this.setterFields) {
            elems = elems.append(treeMaker.Literal(getterField));
        }
        JCVariableDecl jcVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("SETTER_FIELD")
                , treeMaker.TypeArray(ident("String"))
                , treeMaker.NewArray(ident("String"), List.nil(), elems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDecl);

        JCVariableDecl flagJCVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("SETTER_FIELD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJCVariableDecl);

        List<JCExpression> methodElems = List.nil();
        for (String setterField : this.setterFields) {
            String fieldName = setterField + "_SETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            methodElems = methodElems.append(ident(fieldName));
        }
        JCVariableDecl jcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("SETTER_METHOD")
                , treeMaker.TypeArray(ident("SetMethod"))
                , treeMaker.NewArray(ident("SetMethod"), List.nil(), methodElems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDeclMethod);
        JCVariableDecl flagJcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("SETTER_METHOD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJcVariableDeclMethod);
    }

    private void createStaticSetterNotEntityField() {
        List<JCExpression> elems = List.nil();
        for (String getterField : this.setterNotEntityFields) {
            elems = elems.append(treeMaker.Literal(getterField));
        }
        JCVariableDecl jcVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("SETTER_NOT_ENTITY_FIELD")
                , treeMaker.TypeArray(ident("String"))
                , treeMaker.NewArray(ident("String"), List.nil(), elems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDecl);
        JCVariableDecl flagJCVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("SETTER_NOT_ENTITY_FIELD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJCVariableDecl);

        List<JCExpression> methodElems = List.nil();
        for (String setterField : this.setterNotEntityFields) {
            String fieldName = setterField + "_SETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            methodElems = methodElems.append(ident(fieldName));
        }
        JCVariableDecl jcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("SETTER_NOT_ENTITY_METHOD")
                , treeMaker.TypeArray(ident("SetMethod"))
                , treeMaker.NewArray(ident("SetMethod"), List.nil(), methodElems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDeclMethod);
        JCVariableDecl flagJcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("SETTER_NOT_ENTITY_METHOD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJcVariableDeclMethod);
    }

    private void createStaticSetterEntityField() {
        List<JCExpression> elems = List.nil();
        for (String getterField : this.setterEntityFields) {
            elems = elems.append(treeMaker.Literal(getterField));
        }
        JCVariableDecl jcVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("SETTER_ENTITY_FIELD")
                , treeMaker.TypeArray(ident("String"))
                , treeMaker.NewArray(ident("String"), List.nil(), elems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDecl);
        JCVariableDecl flagJCVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("SETTER_ENTITY_FIELD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJCVariableDecl);

        List<JCExpression> methodElems = List.nil();
        for (String setterField : this.setterEntityFields) {
            String fieldName = setterField + "_SETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            methodElems = methodElems.append(ident(fieldName));
        }
        JCVariableDecl jcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC)
                , name("SETTER_ENTITY_METHOD")
                , treeMaker.TypeArray(ident("SetMethod"))
                , treeMaker.NewArray(ident("SetMethod"), List.nil(), methodElems)
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(jcVariableDeclMethod);
        JCVariableDecl flagJcVariableDeclMethod = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC), name("SETTER_ENTITY_METHOD_FLAG"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Literal(false));
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(flagJcVariableDeclMethod);
    }

    private void createGetterMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("GETTER_FIELD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterFields")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("GETTER_FIELD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("String"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_FIELD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_FIELD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("GETTER_FIELD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("GETTER_FIELD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("GETTER_FIELD"));
        }
        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterFields"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("String")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createGetterNotEntityMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("GETTER_NOT_ENTITY_FIELD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterNotEntityFields")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("GETTER_NOT_ENTITY_FIELD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("String"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_NOT_ENTITY_FIELD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_NOT_ENTITY_FIELD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("GETTER_NOT_ENTITY_FIELD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("GETTER_NOT_ENTITY_FIELD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("GETTER_NOT_ENTITY_FIELD"));
        }
        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterNotEntityFields"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("String")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createGetterEntityMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("GETTER_ENTITY_FIELD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterEntityFields")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("GETTER_ENTITY_FIELD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("String"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_ENTITY_FIELD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_ENTITY_FIELD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("GETTER_ENTITY_FIELD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("GETTER_ENTITY_FIELD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("GETTER_ENTITY_FIELD"));
        }
        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterEntityFields"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("String")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createSetterMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("SETTER_FIELD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterFields")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("SETTER_FIELD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("String"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_FIELD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_FIELD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("SETTER_FIELD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("SETTER_FIELD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("SETTER_FIELD"));
        }
        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterFields"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("String")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createSetterNotEntityMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("SETTER_NOT_ENTITY_FIELD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterNotEntityFields")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("SETTER_NOT_ENTITY_FIELD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("String"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_NOT_ENTITY_FIELD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_NOT_ENTITY_FIELD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("SETTER_NOT_ENTITY_FIELD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("SETTER_NOT_ENTITY_FIELD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("SETTER_NOT_ENTITY_FIELD"));
        }
        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterNotEntityFields"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("String")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createSetterEntityMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("SETTER_ENTITY_FIELD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterEntityFields")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("String"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("SETTER_ENTITY_FIELD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("String")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("String"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_ENTITY_FIELD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_ENTITY_FIELD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("SETTER_ENTITY_FIELD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("SETTER_ENTITY_FIELD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("SETTER_ENTITY_FIELD"));
        }
        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterEntityFields"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("String")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createGetterFuncMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("GETTER_METHOD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("GetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterMethods")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("GetMethod"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("GetMethod"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("GETTER_METHOD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("GetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("GetMethod"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_METHOD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_METHOD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("GETTER_METHOD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("GETTER_METHOD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("GETTER_METHOD"));
        }
        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterMethods"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("GetMethod")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createGetterNotEntityFuncMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("GETTER_NOT_ENTITY_METHOD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("GetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterNotEntityMethods")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("GetMethod"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("GetMethod"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("GETTER_NOT_ENTITY_METHOD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("GetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("GetMethod"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_NOT_ENTITY_METHOD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_NOT_ENTITY_METHOD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("GETTER_NOT_ENTITY_METHOD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("GETTER_NOT_ENTITY_METHOD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("GETTER_NOT_ENTITY_METHOD"));
        }
        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterNotEntityMethods"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("GetMethod")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createGetterEntityFuncMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("GETTER_ENTITY_METHOD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("GetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterEntityMethods")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("GetMethod"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("GetMethod"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("GETTER_ENTITY_METHOD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("GetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("GetMethod"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_ENTITY_METHOD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("GETTER_ENTITY_METHOD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("GETTER_ENTITY_METHOD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("GETTER_ENTITY_METHOD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("GETTER_ENTITY_METHOD"));
        }
        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterEntityMethods"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("GetMethod")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createSetterFuncMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("SETTER_METHOD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("SetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterMethods")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("SetMethod"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("SetMethod"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("SETTER_METHOD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("SetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("SetMethod"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_METHOD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_METHOD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("SETTER_METHOD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("SETTER_METHOD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("SETTER_METHOD"));
        }
        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterMethods"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("SetMethod")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createSetterNotEntityFuncMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("SETTER_NOT_ENTITY_METHOD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("SetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterNotEntityMethods")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("SetMethod"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("SetMethod"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("SETTER_NOT_ENTITY_METHOD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("SetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("SetMethod"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_NOT_ENTITY_METHOD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_NOT_ENTITY_METHOD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("SETTER_NOT_ENTITY_METHOD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("SETTER_NOT_ENTITY_METHOD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("SETTER_NOT_ENTITY_METHOD"));
        }
        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterNotEntityMethods"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("SetMethod")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createSetterEntityFuncMethod() {
        JCTree.JCStatement statement = null;
        if (this.isParentEntity) {
            JCBlock exec1 = treeMaker.Block(0L, List.of(treeMaker.Return(ident("SETTER_ENTITY_METHOD"))));
            List<JCTree.JCStatement> block = List.nil();
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superFields"), treeMaker.TypeArray(ident("SetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterEntityMethods")), List.nil())));
            JCTree.JCMethodInvocation apply = treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("superFields")));
            JCTree.JCNewClass arrayList = treeMaker.NewClass(null, List.nil(), ident("ArrayList"), List.of(apply), null);
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList"), treeMaker.TypeApply(ident("List"), List.of(ident("SetMethod"))), arrayList));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fieldList2"), treeMaker.TypeApply(ident("List"), List.of(ident("SetMethod"))), treeMaker.Apply(List.nil(), select("Arrays", "asList"), List.of(ident("SETTER_ENTITY_METHOD")))));
            block = block.append(treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("addAll")), List.of(ident("fieldList2")))));
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("fields"), treeMaker.TypeArray(ident("SetMethod")), treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("toArray")), List.of(treeMaker.NewArray(ident("SetMethod"), List.of(treeMaker.Apply(List.nil(), treeMaker.Select(ident("fieldList"), name("size")), List.nil())), null)))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_ENTITY_METHOD"), ident("fields"))));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("SETTER_ENTITY_METHOD_FLAG"), treeMaker.Literal(true))));
            block = block.append(treeMaker.Return(ident("SETTER_ENTITY_METHOD")));
            JCBlock jcBlock = treeMaker.Block(0L, block);
            statement = treeMaker.If(ident("SETTER_ENTITY_METHOD_FLAG"), exec1, jcBlock);
        } else {
            statement = treeMaker.Return(ident("SETTER_ENTITY_METHOD"));
        }
        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterEntityMethods"),
                // 方法返回的类型
                treeMaker.TypeArray(ident("SetMethod")),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, List.of(statement)),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createGetterFieldFuncMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("getMethod"), type("GetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterMethod")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("getMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("getMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterFields) {
            String fieldName = getterField + "_GETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterMethod"),
                // 方法返回的类型
                type("GetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createGetterNotEntityFieldFuncMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("getMethod"), type("GetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterNotEntityMethod")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("getMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("getMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterNotEntityFields) {
            String fieldName = getterField + "_GETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterNotEntityMethod"),
                // 方法返回的类型
                type("GetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createGetterEntityFieldFuncMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("getMethod"), type("GetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterEntityMethod")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("getMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("getMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterEntityFields) {
            String fieldName = getterField + "_GETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterEntityMethod"),
                // 方法返回的类型
                type("GetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createSetterFieldFuncMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("setMethod"), type("SetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterMethod")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("setMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("setMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterFields) {
            String fieldName = setterField + "_SETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal(setterField), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterMethod"),
                // 方法返回的类型
                type("SetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createSetterNotEntityFieldFuncMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("setMethod"), type("SetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterNotEntityMethod")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("setMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("setMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterNotEntityFields) {
            String fieldName = setterField + "_SETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal(setterField), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterNotEntityMethod"),
                // 方法返回的类型
                type("SetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createSetterEntityFieldFuncMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("setMethod"), type("SetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterEntityMethod")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("setMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("setMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterEntityFields) {
            String fieldName = setterField + "_SETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal(setterField), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterEntityMethod"),
                // 方法返回的类型
                type("SetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createGetterFieldFuncMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("getMethod"), type("GetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterMethod2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("getMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("getMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterFields) {
            String fieldName = getterField + "_GETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal("get" + toTitleCase(getterField)), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterMethod2"),
                // 方法返回的类型
                type("GetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createGetterNotEntityFieldFuncMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("getMethod"), type("GetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterNotEntityMethod2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("getMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("getMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterNotEntityFields) {
            String fieldName = getterField + "_GETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal("get" + toTitleCase(getterField)), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterNotEntityMethod2"),
                // 方法返回的类型
                type("GetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createGetterEntityFieldFuncMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("getMethod"), type("GetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("getterEntityMethod2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("getMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("getMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterEntityFields) {
            String fieldName = getterField + "_GETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal("get" + toTitleCase(getterField)), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("getterEntityMethod2"),
                // 方法返回的类型
                type("GetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createSetterFieldFuncMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("setMethod"), type("SetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterMethod2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("setMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("setMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterFields) {
            String fieldName = setterField + "_SETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal("set" + toTitleCase(setterField)), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterMethod2"),
                // 方法返回的类型
                type("SetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createSetterNotEntityFieldFuncMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("setMethod"), type("SetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterNotEntityMethod2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("setMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("setMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterNotEntityFields) {
            String fieldName = setterField + "_SETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal(setterField), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterNotEntityMethod2"),
                // 方法返回的类型
                type("SetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createSetterEntityFieldFuncMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("setMethod"), type("SetMethod"), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("setterEntityMethod2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(treeMaker.Binary(Tag.NE, ident("setMethod"), treeMaker.Literal(TypeTag.BOT, null)), treeMaker.Return(ident("setMethod")), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterEntityFields) {
            String fieldName = setterField + "_SETTER_FIELD_FUN";
            fieldName = fieldName.toUpperCase();
            cases = cases.append(treeMaker.Case(treeMaker.Literal("set" + toTitleCase(setterField)), List.of(treeMaker.Return(ident(fieldName)))));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));

        JCMethodDecl setterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("setterEntityMethod2"),
                // 方法返回的类型
                type("SetMethod"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(setterMethodJcMethodDecl);
    }

    private void createHasGetterFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasGetterField")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.getterFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.getterFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasGetterField"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createHasGetterNotEntityFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasGetterNotEntityField")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.getterNotEntityFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.getterNotEntityFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasGetterNotEntityField"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createHasGetterEntityFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasGetterEntityField")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.getterEntityFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.getterEntityFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasGetterEntityField"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createHasSetterFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasSetterField")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.setterFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.setterFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasSetterField"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createHasSetterNotEntityFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasSetterNotEntityField")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.setterNotEntityFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.setterNotEntityFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasSetterNotEntityField"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createHasSetterEntityFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasSetterEntityField")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.setterEntityFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.setterEntityFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasSetterEntityField"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createHasGetterFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasGetterField2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.getterFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.getterFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal("get" + toTitleCase(getterField)), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasGetterField2"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createHasGetterNotEntityFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasGetterNotEntityField2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.getterNotEntityFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.getterNotEntityFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal("get" + toTitleCase(getterField)), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasGetterNotEntityField2"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createHasGetterEntityFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasGetterEntityField2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.getterEntityFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.getterEntityFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal("get" + toTitleCase(getterField)), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasGetterEntityField2"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createHasSetterFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasSetterField2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.setterFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.setterFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal("set" + toTitleCase(getterField)), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasSetterField2"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createHasSetterNotEntityFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasSetterNotEntityField2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.setterNotEntityFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.setterNotEntityFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal("set" + toTitleCase(getterField)), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasSetterNotEntityField2"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createHasSetterEntityFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();
        if (this.isParentEntity) {
            block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("superHas"), treeMaker.TypeIdent(TypeTag.BOOLEAN), treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("hasSetterEntityField2")), List.of(ident("field")))));
            block = block.append(treeMaker.If(ident("superHas"), treeMaker.Return(treeMaker.Literal(true)), null));
        }
        List<JCTree.JCCase> cases = List.nil();
        int index = 0;
        for (String getterField : this.setterEntityFields) {
            index++;
            List<JCTree.JCStatement> list = List.nil();
            if (index == this.setterEntityFields.size()) {
                list = list.append(treeMaker.Return(treeMaker.Literal(true)));
            }
            cases = cases.append(treeMaker.Case(treeMaker.Literal("set" + toTitleCase(getterField)), list));
        }
        cases = cases.append(treeMaker.Case(null, List.of(treeMaker.Return(treeMaker.Literal(false)))));
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("hasSetterEntityField2"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.BOOLEAN),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedGetterFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterFields) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("get" + toTitleCase(getterField))), List.nil());
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), List.of(treeMaker.Return(invocation))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedGetterField")), List.of(ident("field")));
            block = block.append(treeMaker.Return(invocation));
        } else {
            block = block.append(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedGetterField"),
                // 方法返回的类型
                ident("Object"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedGetterNotEntityFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterNotEntityFields) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("get" + toTitleCase(getterField))), List.nil());
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), List.of(treeMaker.Return(invocation))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedGetterNotEntityField")), List.of(ident("field")));
            block = block.append(treeMaker.Return(invocation));
        } else {
            block = block.append(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedGetterNotEntityField"),
                // 方法返回的类型
                ident("Object"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedGetterEntityFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterEntityFields) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("get" + toTitleCase(getterField))), List.nil());
            cases = cases.append(treeMaker.Case(treeMaker.Literal(getterField), List.of(treeMaker.Return(invocation))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedGetterEntityField")), List.of(ident("field")));
            block = block.append(treeMaker.Return(invocation));
        } else {
            block = block.append(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedGetterEntityField"),
                // 方法返回的类型
                ident("Object"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedSetterFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterFields) {
            JCTree.JCTypeCast castValue = treeMaker.TypeCast(this.setterMethodFieldTypeMap.get(setterField), treeMaker.Ident(name("value")));
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("set" + toTitleCase(setterField))), List.of(castValue));
            cases = cases.append(treeMaker.Case(treeMaker.Literal(setterField), List.of(treeMaker.Exec(invocation), treeMaker.Return(null))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedSetterField")), List.of(ident("field"), ident("value")));
            block = block.append(treeMaker.Exec(invocation));
            block = block.append(treeMaker.Return(null));
        } else {
            block = block.append(treeMaker.Return(null));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedSetterField"),
                // 方法返回的类型
                treeMaker.Type(new Type.JCVoidType()),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)
                        , treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("value"), ident("Object"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedSetterNotEntityFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterNotEntityFields) {
            JCTree.JCTypeCast castValue = treeMaker.TypeCast(this.setterMethodFieldTypeMap.get(setterField), treeMaker.Ident(name("value")));
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("set" + toTitleCase(setterField))), List.of(castValue));
            cases = cases.append(treeMaker.Case(treeMaker.Literal(setterField), List.of(treeMaker.Exec(invocation), treeMaker.Return(null))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedSetterNotEntityField")), List.of(ident("field"), ident("value")));
            block = block.append(treeMaker.Exec(invocation));
            block = block.append(treeMaker.Return(null));
        } else {
            block = block.append(treeMaker.Return(null));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedSetterNotEntityField"),
                // 方法返回的类型
                treeMaker.Type(new Type.JCVoidType()),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)
                        , treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("value"), ident("Object"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedSetterEntityFieldMethod() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterEntityFields) {
            JCTree.JCTypeCast castValue = treeMaker.TypeCast(this.setterMethodFieldTypeMap.get(setterField), treeMaker.Ident(name("value")));
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("set" + toTitleCase(setterField))), List.of(castValue));
            cases = cases.append(treeMaker.Case(treeMaker.Literal(setterField), List.of(treeMaker.Exec(invocation), treeMaker.Return(null))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedSetterEntityField")), List.of(ident("field"), ident("value")));
            block = block.append(treeMaker.Exec(invocation));
            block = block.append(treeMaker.Return(null));
        } else {
            block = block.append(treeMaker.Return(null));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedSetterEntityField"),
                // 方法返回的类型
                treeMaker.Type(new Type.JCVoidType()),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)
                        , treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("value"), ident("Object"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedGetterFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterFields) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("get" + toTitleCase(getterField))), List.nil());
            cases = cases.append(treeMaker.Case(treeMaker.Literal("get" + toTitleCase(getterField)), List.of(treeMaker.Return(invocation))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedGetterField2")), List.of(ident("field")));
            block = block.append(treeMaker.Return(invocation));
        } else {
            block = block.append(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedGetterField2"),
                // 方法返回的类型
                ident("Object"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedGetterNotEntityFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterNotEntityFields) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("get" + toTitleCase(getterField))), List.nil());
            cases = cases.append(treeMaker.Case(treeMaker.Literal("get" + toTitleCase(getterField)), List.of(treeMaker.Return(invocation))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedGetterNotEntityField2")), List.of(ident("field")));
            block = block.append(treeMaker.Return(invocation));
        } else {
            block = block.append(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedGetterNotEntityField2"),
                // 方法返回的类型
                ident("Object"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedGetterEntityFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String getterField : this.getterEntityFields) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("get" + toTitleCase(getterField))), List.nil());
            cases = cases.append(treeMaker.Case(treeMaker.Literal("get" + toTitleCase(getterField)), List.of(treeMaker.Return(invocation))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedGetterEntityField2")), List.of(ident("field")));
            block = block.append(treeMaker.Return(invocation));
        } else {
            block = block.append(treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedGetterEntityField2"),
                // 方法返回的类型
                ident("Object"),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedSetterFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterFields) {
            JCTree.JCTypeCast castValue = treeMaker.TypeCast(this.setterMethodFieldTypeMap.get(setterField), treeMaker.Ident(name("value")));
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("set" + toTitleCase(setterField))), List.of(castValue));
            cases = cases.append(treeMaker.Case(treeMaker.Literal("set" + toTitleCase(setterField)), List.of(treeMaker.Exec(invocation), treeMaker.Return(null))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedSetterField2")), List.of(ident("field"), ident("value")));
            block = block.append(treeMaker.Exec(invocation));
            block = block.append(treeMaker.Return(null));
        } else {
            block = block.append(treeMaker.Return(null));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedSetterField2"),
                // 方法返回的类型
                treeMaker.Type(new Type.JCVoidType()),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)
                        , treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("value"), ident("Object"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedSetterNotEntityFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterNotEntityFields) {
            JCTree.JCTypeCast castValue = treeMaker.TypeCast(this.setterMethodFieldTypeMap.get(setterField), treeMaker.Ident(name("value")));
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("set" + toTitleCase(setterField))), List.of(castValue));
            cases = cases.append(treeMaker.Case(treeMaker.Literal("set" + toTitleCase(setterField)), List.of(treeMaker.Exec(invocation), treeMaker.Return(null))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedSetterNotEntityField2")), List.of(ident("field"), ident("value")));
            block = block.append(treeMaker.Exec(invocation));
            block = block.append(treeMaker.Return(null));
        } else {
            block = block.append(treeMaker.Return(null));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedSetterNotEntityField2"),
                // 方法返回的类型
                treeMaker.Type(new Type.JCVoidType()),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)
                        , treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("value"), ident("Object"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createInvokedSetterEntityFieldMethod2() {
        List<JCTree.JCStatement> block = List.nil();

        List<JCTree.JCCase> cases = List.nil();
        for (String setterField : this.setterEntityFields) {
            JCTree.JCTypeCast castValue = treeMaker.TypeCast(this.setterMethodFieldTypeMap.get(setterField), treeMaker.Ident(name("value")));
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("set" + toTitleCase(setterField))), List.of(castValue));
            cases = cases.append(treeMaker.Case(treeMaker.Literal("set" + toTitleCase(setterField)), List.of(treeMaker.Exec(invocation), treeMaker.Return(null))));
        }
        block = block.append(treeMaker.Switch(treeMaker.Parens(ident("field")), cases));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("super"), name("invokedSetterEntityField2")), List.of(ident("field"), ident("value")));
            block = block.append(treeMaker.Exec(invocation));
            block = block.append(treeMaker.Return(null));
        } else {
            block = block.append(treeMaker.Return(null));
        }


        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("invokedSetterEntityField2"),
                // 方法返回的类型
                treeMaker.Type(new Type.JCVoidType()),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("field"), ident("String"), null)
                        , treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("value"), ident("Object"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createByteLengthNotEntityMethod() {
        List<JCTree.JCStatement> block = List.nil();
        block = block.append(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("len"), treeMaker.TypeIdent(TypeTag.INT), treeMaker.Literal(0)));
        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select("super", "byteLengthNotEntity"), List.nil());
            block = block.append(treeMaker.Exec(treeMaker.Assignop(JCTree.Tag.PLUS_ASG, ident("len"), invocation)));
        }
        for (Map.Entry<String, Type> entry : this.fieldTypeMap.entrySet()) {
            String field = entry.getKey();
            Type type = entry.getValue();
            if (this.fieldEntityTypeValueMap.containsKey(field)) {
                continue;
            }
            String serializableName = getSerializableName(type);
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select(serializableName, "valueByteLength"), List.of(select("this", field)));
            block = block.append(treeMaker.Exec(treeMaker.Assignop(JCTree.Tag.PLUS_ASG, ident("len"), invocation)));
        }
        block = block.append(treeMaker.Return(ident("len")));
        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("byteLengthNotEntity"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.INT),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.nil(),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);

    }

    private void createNotEntitySerializableMethod() {
        List<JCTree.JCStatement> block = List.nil();

        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select("super", "serializableNotEntity"), List.of(ident("buffer")));
            block = block.append(treeMaker.Exec(invocation));
        }
        for (Map.Entry<String, Type> entry : this.fieldTypeMap.entrySet()) {
            String field = entry.getKey();
            Type type = entry.getValue();
            if (this.fieldEntityTypeValueMap.containsKey(field)) {
                continue;
            }
            String serializableName = getSerializableName(type);
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select(serializableName, "serializableValue"), List.of(select("this", field), ident("buffer")));
            block = block.append(treeMaker.Exec(invocation));
        }

        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("serializableNotEntity"),
                // 方法返回的类型
                treeMaker.Type(new Type.JCVoidType()),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("buffer"), ident("ByteBuf"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createSerializableNotEntityMethod() {
        List<JCTree.JCStatement> block = List.nil();

        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select("super", "serializableNotEntity"), List.of(ident("bytes"), ident("index")));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("index"), invocation)));
        }

        for (Map.Entry<String, Type> entry : this.fieldTypeMap.entrySet()) {
            String field = entry.getKey();
            Type type = entry.getValue();
            if (this.fieldEntityTypeValueMap.containsKey(field)) {
                continue;
            }
            String serializableName = getSerializableName(type);
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select(serializableName, "serializableValue"), List.of(ident("bytes"), ident("index"), select("this", field)));
            block = block.append(treeMaker.Exec(treeMaker.Assign(ident("index"), invocation)));
        }
        block = block.append(treeMaker.Return(ident("index")));

        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("serializableNotEntity"),
                // 方法返回的类型
                treeMaker.TypeIdent(TypeTag.INT),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(
                        treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("bytes"), treeMaker.TypeArray(treeMaker.TypeIdent(TypeTag.BYTE)), null)
                        , treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("index"), treeMaker.TypeIdent(TypeTag.INT), null)
                ),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createNotEntityDeserializationMethod() {
        List<JCTree.JCStatement> block = List.nil();

        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select("super", "deserializationNotEntity"), List.of(ident("buffer")));
            block = block.append(treeMaker.Exec(invocation));
        }
        for (Map.Entry<String, Type> entry : this.fieldTypeMap.entrySet()) {
            String field = entry.getKey();
            Type type = entry.getValue();
            if (this.fieldEntityTypeValueMap.containsKey(field)) {
                continue;
            }
            String serializableName = getSerializableName(type);
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select(serializableName, "deserializationValue"), List.of(ident("buffer")));
            JCAssign assign = treeMaker.Assign(select("this", field), treeMaker.TypeCast(type, invocation));
            block = block.append(treeMaker.Exec(assign));
        }

        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("deserializationNotEntity"),
                // 方法返回的类型
                treeMaker.Type(new Type.JCVoidType()),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("buffer"), ident("ByteBuffer"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createCopyMethod() {
        List<JCTree.JCStatement> block = List.nil();

        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select("super", "copy"), List.of(ident("target")));
            block = block.append(treeMaker.Exec(invocation));
        }
        for (String getterField : this.getterFields) {
            if (!this.setterFields.contains(getterField)) {
                continue;
            }
            JCTree.JCTypeCast cast = treeMaker.TypeCast(treeMaker.Ident(this.jcClassDecl.name), ident("target"));
            JCTree.JCMethodInvocation getInvocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("get" + toTitleCase(getterField))), List.nil());
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select(cast, "set" + toTitleCase(getterField)), List.of(getInvocation));
            block = block.append(treeMaker.Exec(invocation));
        }

        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("copy"),
                // 方法返回的类型
                treeMaker.Type(new Type.JCVoidType()),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("target"), ident("Object"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private void createCopyNotEntityMethod() {
        List<JCTree.JCStatement> block = List.nil();

        if (this.isParentEntity) {
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select("super", "copyNotEntity"), List.of(ident("target")));
            block = block.append(treeMaker.Exec(invocation));
        }
        for (String getterField : this.getterNotEntityFields) {
            if (!this.setterNotEntityFields.contains(getterField)) {
                continue;
            }
            JCTree.JCTypeCast cast = treeMaker.TypeCast(treeMaker.Ident(this.jcClassDecl.name), ident("target"));
            JCTree.JCMethodInvocation getInvocation = treeMaker.Apply(List.nil(), treeMaker.Select(ident("this"), name("get" + toTitleCase(getterField))), List.nil());
            JCTree.JCMethodInvocation invocation = treeMaker.Apply(List.nil(), select(cast, "set" + toTitleCase(getterField)), List.of(getInvocation));
            block = block.append(treeMaker.Exec(invocation));
        }

        JCMethodDecl getterMethodJcMethodDecl = treeMaker.MethodDef(
                // public方法
                treeMaker.Modifiers(Flags.PUBLIC),
                // 方法名称
                name("copyNotEntity"),
                // 方法返回的类型
                treeMaker.Type(new Type.JCVoidType()),
                // 泛型参数
                List.nil(),
                // 方法参数
                List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), name("target"), ident("Object"), null)),
                // throw表达式
                List.nil(),
                // 方法体
                treeMaker.Block(0L, block),
                // 默认值
                null
        );
        this.jcClassDecl.defs = this.jcClassDecl.defs.append(getterMethodJcMethodDecl);
    }

    private JCTree.JCIdent ident(String name) {
        return treeMaker.Ident(name(name));
    }

    private Name name(String name) {
        return names.fromString(name);
    }

    private JCTree.JCFieldAccess select(String sel, String name) {
        return treeMaker.Select(ident(sel), name(name));
    }

    private JCTree.JCFieldAccess select(JCTree.JCExpression sel, String name) {
        return treeMaker.Select(sel, name(name));
    }

    private JCTree.JCExpression select(String sel) {
        if (!StringUtils.contains(sel, ".")) {
            return ident(sel);
        }
        return treeMaker.Select(ident(StringUtils.substringBeforeLast(sel, ".")), name(StringUtils.substringAfterLast(sel, ".")));
    }

    private JCTree.JCExpression type(String type) {
        switch (type) {
            case "boolean":
                return treeMaker.TypeIdent(TypeTag.BOOLEAN);
            case "byte":
                return treeMaker.TypeIdent(TypeTag.BYTE);
            case "char":
                return treeMaker.TypeIdent(TypeTag.CHAR);
            case "short":
                return treeMaker.TypeIdent(TypeTag.SHORT);
            case "int":
                return treeMaker.TypeIdent(TypeTag.INT);
            case "float":
                return treeMaker.TypeIdent(TypeTag.FLOAT);
            case "double":
                return treeMaker.TypeIdent(TypeTag.DOUBLE);
            case "long":
                return treeMaker.TypeIdent(TypeTag.LONG);
            case "void":
                return treeMaker.TypeIdent(TypeTag.VOID);
            default:
                return select(type);
        }
    }

    /**
     * 首字母大写
     */
    public String toTitleCase(String str) {
        char first = str.charAt(0);
        if (first >= 'a' && first <= 'z') {
            first -= 32;
        }
        return first + str.substring(1);
    }

    public static String getClassName(JCTree type) {
        if (type == null) {
            return null;
        }
        String classType = type.toString();
        if (classType.contains("<")) {
            classType = StringUtils.substringBefore(classType, "<");
        }
        switch (classType) {
            case "boolean":
                return "Boolean";
            case "byte":
                return "Byte";
            case "char":
                return "Character";
            case "short":
                return "Short";
            case "int":
                return "Integer";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "long":
                return "Long";
            default:
                return classType;
        }
    }

    public static String getSerializableName(Type type) {
        String classType = type.tsym.toString();
        switch (classType) {
            case "java.util.Collection":
                return CollectionSerializable.class.getSimpleName();
            case "java.util.List":
                return CollectionSerializable.class.getSimpleName();
            case "java.util.Arrays":
                return CollectionSerializable.class.getSimpleName();
            case "java.util.ArrayList":
                return CollectionSerializable.class.getSimpleName();
            case "java.util.LinkedList":
                return CollectionSerializable.class.getSimpleName();
            case "java.util.Set":
                return CollectionSerializable.class.getSimpleName();
            case "java.util.HashSet":
                return CollectionSerializable.class.getSimpleName();
            case "java.util.TreeSet":
                return CollectionSerializable.class.getSimpleName();
            case "java.util.LinkedHashSet":
                return CollectionSerializable.class.getSimpleName();
            case "java.util.Map":
                return MapSerializable.class.getSimpleName();
            case "java.util.TreeMap":
                return MapSerializable.class.getSimpleName();
            case "java.util.LinkedHashMap":
                return MapSerializable.class.getSimpleName();
            case "java.util.HashMap":
                return MapSerializable.class.getSimpleName();
            case "java.math.BigDecimal":
                return BigDecimalSerializable.class.getSimpleName();
            case "java.math.BigInteger":
                return BigIntegerSerializable.class.getSimpleName();
            case "java.lang.Boolean":
                return BooleanSerializable.class.getSimpleName();
            case "java.lang.Byte":
                return ByteSerializable.class.getSimpleName();
            case "java.lang.Character":
                return CharacterSerializable.class.getSimpleName();
            case "java.util.Date":
                return DateSerializable.class.getSimpleName();
            case "java.lang.Double":
                return DoubleSerializable.class.getSimpleName();
            case "java.lang.Float":
                return FloatSerializable.class.getSimpleName();
            case "java.lang.Integer":
                return IntegerSerializable.class.getSimpleName();
            case "java.lang.Long":
                return LongSerializable.class.getSimpleName();
            case "java.lang.Short":
                return ShortSerializable.class.getSimpleName();
            case "java.sql.Date":
                return SqlDateSerializable.class.getSimpleName();
            case "java.lang.StringBuffer":
                return StringBufferSerializable.class.getSimpleName();
            case "java.lang.StringBuilder":
                return StringBuilderSerializable.class.getSimpleName();
            case "java.lang.String":
                return StringSerializable.class.getSimpleName();
            case "java.sql.Timestamp":
                return TimestampSerializable.class.getSimpleName();
            case "boolean":
                return BoolSerializable.class.getSimpleName();
            case "byte":
                return BytSerializable.class.getSimpleName();
            case "char":
                return CharSerializable.class.getSimpleName();
            case "short":
                return ShoSerializable.class.getSimpleName();
            case "int":
                return IntSerializable.class.getSimpleName();
            case "float":
                return FloSerializable.class.getSimpleName();
            case "double":
                return DouSerializable.class.getSimpleName();
            case "long":
                return LonSerializable.class.getSimpleName();
            case "Array":
                String arrayType = type.toString();
                switch (arrayType) {
                    case "java.math.BigDecimal[]":
                        return BigDecimalArraySerializable.class.getSimpleName();
                    case "java.math.BigInteger[]":
                        return BigIntegerArraySerializable.class.getSimpleName();
                    case "java.lang.Boolean[]":
                        return BooleanArraySerializable.class.getSimpleName();
                    case "java.lang.Byte[]":
                        return ByteArraySerializable.class.getSimpleName();
                    case "java.lang.Character[]":
                        return CharacterArraySerializable.class.getSimpleName();
                    case "java.util.Date[]":
                        return DateArraySerializable.class.getSimpleName();
                    case "java.lang.Double[]":
                        return DoubleArraySerializable.class.getSimpleName();
                    case "java.lang.Float[]":
                        return FloatArraySerializable.class.getSimpleName();
                    case "java.lang.Integer[]":
                        return IntegerArraySerializable.class.getSimpleName();
                    case "java.lang.Long[]":
                        return LongArraySerializable.class.getSimpleName();
                    case "java.lang.Short[]":
                        return ShortArraySerializable.class.getSimpleName();
                    case "java.sql.Date[]":
                        return SqlDateArraySerializable.class.getSimpleName();
                    case "java.lang.StringBuffer[]":
                        return StringBufferArraySerializable.class.getSimpleName();
                    case "java.lang.StringBuilder[]":
                        return StringBuilderArraySerializable.class.getSimpleName();
                    case "java.lang.String[]":
                        return StringArraySerializable.class.getSimpleName();
                    case "java.sql.Timestamp[]":
                        return TimestampArraySerializable.class.getSimpleName();
                    case "boolean[]":
                        return BoolArraySerializable.class.getSimpleName();
                    case "byte[]":
                        return BytArraySerializable.class.getSimpleName();
                    case "char[]":
                        return CharArraySerializable.class.getSimpleName();
                    case "short[]":
                        return ShoArraySerializable.class.getSimpleName();
                    case "int[]":
                        return IntArraySerializable.class.getSimpleName();
                    case "float[]":
                        return FloArraySerializable.class.getSimpleName();
                    case "double[]":
                        return DouArraySerializable.class.getSimpleName();
                    case "long[]":
                        return LonArraySerializable.class.getSimpleName();
                    default:
                        return ObjectSerializableUtil.class.getSimpleName();
                }
            default:
                return ObjectSerializableUtil.class.getSimpleName();
        }
    }

    class EntityTypeValue {
        public EntityTypeValue() {
        }

        public EntityTypeValue(String classValue, boolean parent) {
            this.classValue = classValue;
            this.parent = parent;
        }

        public String classValue;
        public boolean parent = true;
    }
}
