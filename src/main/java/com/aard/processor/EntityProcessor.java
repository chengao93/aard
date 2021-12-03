package com.aard.processor;

import com.aard.processor.annotation.EntityType;
import com.google.auto.service.AutoService;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 注解处理类
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/7 18:29
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.aard.processor.annotation.*"})
public class EntityProcessor extends AbstractProcessor {

    /**
     * 语法树
     */
    private Trees trees;

    /**
     * 树节点创建工具类
     */
    private TreeMaker treeMaker;

    /**
     * 命名工具类
     */
    private Names names;

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.trees = Trees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            try {
                // 准备在gradle的控制台打印信息
                Messager messager = processingEnv.getMessager();
                messager.printMessage(Diagnostic.Kind.NOTE, "start process: " + this.getClass().getName());
                long start = System.currentTimeMillis();
                Set<RootElement> sortRootElements = getSortRootElements(roundEnv);
                Map<String, Set<String>> classParentMap = new LinkedHashMap<>();
                for (RootElement sortRootElement : sortRootElements) {
                    classParentMap.put(sortRootElement.currClass, sortRootElement.parentClasses);
                }
                for (RootElement sortRootElement : sortRootElements) {
                    Element element = sortRootElement.root;
                    TreePath treesPath = trees.getPath(element);
                    // 获取语法树
                    JCTree tree = (JCTree) trees.getTree(element);
                    // 使用TreeTranslator遍历
                    tree.accept(new EntityTreeTranslator(treesPath, treeMaker, names, classParentMap, messager));
                }
                messager.printMessage(Diagnostic.Kind.NOTE, "end process: " + this.getClass().getName() + ",time:" + (System.currentTimeMillis() - start) + "ms");
            } catch (Throwable e) {
                messager.printMessage(Diagnostic.Kind.NOTE, "process error! " + e.getMessage());
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    messager.printMessage(Diagnostic.Kind.NOTE, stackTraceElement.toString());
                }
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 获取排序后的 rootElements
     *
     * @param roundEnv roundEnv
     * @return Set
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/7 18:29
     */
    private Set<RootElement> getSortRootElements(RoundEnvironment roundEnv) {
        Set<? extends Element> rootElements = roundEnv.getRootElements();
        Set<RootElement> roots = new LinkedHashSet<>();
        for (Element element : rootElements) {
            if (element.getKind().isClass()) {
                RootElement root = new RootElement();
                root.root = element;
                JCTree tree = (JCTree) trees.getTree(element);
                JCTree.JCExpression extendsClause = ((JCTree.JCClassDecl) tree).getExtendsClause();
                if (extendsClause != null) {
                    root.parentClass = ((JCTree.JCClassDecl) tree).getExtendsClause().type.toString();
                } else {
                    root.parentClass = "";
                }
                TreePath treesPath = trees.getPath(element);
                String packageName = treesPath.getCompilationUnit().getPackageName().toString();
                Name simpleName = ((JCTree.JCClassDecl) tree).getSimpleName();
                root.currClass = packageName + "." + simpleName;
                roots.add(root);

                com.sun.tools.javac.util.List<JCTree.JCAnnotation> annotations = ((JCTree.JCClassDecl) tree).getModifiers().getAnnotations();
                if (annotations != null && !annotations.isEmpty()) {
                    for (JCTree.JCAnnotation annotation : annotations) {
                        if (EntityType.class.getName().equals(annotation.type.toString())) {
                            EntityTreeTranslator.ENTITY_CLASS_NAME.add(root.currClass);
                        }
                    }
                }
            }
        }
        Map<String, List<RootElement>> parentMap = roots.stream().collect(Collectors.groupingBy(item -> item.parentClass));
        Map<String, RootElement> currMap = roots.stream().collect(Collectors.toMap(item -> item.currClass, v -> v));
        Set<String> compClass = new HashSet<>();
        for (RootElement root : roots) {
            if (parentMap.containsKey(root.currClass)) {
                root.childrens = new LinkedHashSet<>();
                root.childrens.addAll(parentMap.get(root.currClass));
            }
            if (StringUtils.isNotEmpty(root.parentClass)) {
                root.parentClasses = new LinkedHashSet<>();
                String parentClass = root.parentClass;
                while (true) {
                    if (StringUtils.isEmpty(parentClass)) {
                        break;
                    }
                    if (root.parentClasses.contains(parentClass)) {
                        break;
                    }
                    root.parentClasses.add(parentClass);
                    if (currMap.containsKey(parentClass)) {
                        String currClass = currMap.get(parentClass).currClass;
                        compClass.add(parentClass);
                        if (StringUtils.equals(currClass, parentClass)) {
                            break;
                        }
                        parentClass = currMap.get(parentClass).currClass;
                    } else {
                        try {
                            Class<?> classes = Class.forName(parentClass);
                            parentClass = classes.getSuperclass().getName();
                            if (classes.getSuperclass() == Object.class) {
                                break;
                            }
                        } catch (ClassNotFoundException e) {
                            break;
                        }
                    }
                }
            }
        }
        roots.removeIf(item -> compClass.contains(item.parentClass));
        Set<RootElement> elements = new LinkedHashSet<>();
        expansion(roots, elements);
        return elements;
    }

    /**
     * 层级结构展开，最低层的排在后面
     *
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/7 19:20
     */
    private void expansion(Set<RootElement> roots, Set<RootElement> elements) {
        if (roots == null || roots.isEmpty()) {
            return;
        }
        for (RootElement root : roots) {
            elements.add(root);
            expansion(root.childrens, elements);
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    class RootElement {
        public String currClass;
        public String parentClass;
        public Element root;
        public Set<RootElement> childrens;
        public Set<String> parentClasses;
    }
}
