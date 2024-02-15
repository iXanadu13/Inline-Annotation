package com.github.ixanadu13.annotation.processor;

import com.github.ixanadu13.annotation.InlineAt;
import com.github.ixanadu13.annotation.processor.metadata.Context;
import com.github.ixanadu13.annotation.processor.metadata.XMethodSymbol;
import com.github.ixanadu13.annotation.processor.metadata.XTypeSymbol;
import com.github.ixanadu13.annotation.processor.shade.lombok.javac.JavacAST;
import com.github.ixanadu13.annotation.processor.shade.lombok.javac.JavacNode;
import com.github.ixanadu13.annotation.processor.shade.lombok.javac.JavacResolution;
import com.google.auto.service.AutoService;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.github.ixanadu13.annotation.Inline;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class InlineProcessor extends BaseProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(roundEnv.processingOver()) return false;
        //获取被inline注解标记的元素
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Inline.class);
        Map<XMethodSymbol, JCTree.JCMethodDecl> mp = new ConcurrentHashMap<>();
        java.util.List<JCTree.JCMethodDecl> methodDecls = new ArrayList<>();
        for(Element element : set){
            if(element instanceof Symbol.MethodSymbol){
                TypeElement class_element = (TypeElement) element.getEnclosingElement();
                String class_name = class_element.getQualifiedName().toString();
                //TreePath path = trees.getPath(element);
                //System.out.println("方法所在路径为："+path);

                //获取当前元素的JCTree对象
                JCTree jcTree = trees.getTree(element);
                //JCTree利用的是访问者模式，将数据与数据的处理进行解耦，TreeTranslator就是访问者，这里我们重写访问类时的逻辑
                jcTree.accept(new TreeTranslator(){
                    @Override
                    public void visitMethodDef(JCTree.JCMethodDecl jcMethod){
                        super.visitMethodDef(jcMethod);
                        //System.out.println("测试"+jcMethod.sym);//(java.lang.String...)
                        XMethodSymbol symbol = new XMethodSymbol(class_name,jcMethod);
                        //System.out.println("XMethod测试: "+symbol);
                        mp.put(symbol,jcMethod);
                        methodDecls.add(jcMethod);
                        //System.out.println("获取到方法："+jcMethod.name+", class: "+class_name);
                    }
                });
            }
        }
        //对所有注解为@Inline的jcMethod进行处理
        // TODO - 如果发现递归取消inline
        // TODO - 目前遇到递归会因为标签冲突而直接编译失败
        for(JCTree.JCMethodDecl jcMethod : methodDecls){
            //JCTree.JCBlock copy = treeCopier.copy(code_block);
            //List<JCTree.JCStatement> statements = copy.getStatements();
            transJCMethodBlock(jcMethod);
        }
        //获取所有语法树的根节点
        Set<? extends Element> rts = roundEnv.getRootElements();
        for(Element element : rts){
            if(element.getKind() == ElementKind.CLASS){
//                TypeElement class_element = (TypeElement) element;
//                List<? extends Element> elements = class_element.getEnclosedElements();
//                for(Element e : elements){
//                    if(e.getKind() == ElementKind.METHOD && e instanceof Symbol.MethodSymbol){
//
//                    }
//                }
                JCTree jcTree = trees.getTree(element);
                TypeElement class_element = (TypeElement) element;
                String enclosing_class_name = class_element.getQualifiedName().toString();
                java.util.List<String> prefix = new ArrayList<>();
                prefix.add(enclosing_class_name);
                //方法所在类中的所有import
                JCTree.JCCompilationUnit compilationUnit = toUnit(element);
                if (compilationUnit==null) continue;
                List<JCTree.JCImport> imports = compilationUnit.getImports();
                imports.forEach(jcImport -> {
                    //java.util.*
                    //java.util.ArrayList
                    if (!jcImport.staticImport) prefix.add(jcImport.qualid.toString());
                });

                jcTree.accept(new TreeTranslator(){
                    @Override
                    public void visitBlock(JCTree.JCBlock jcBlock){
                        super.visitBlock(jcBlock);
                        Context context = new Context(element,prefix,mp);
                        System.out.println("开始转换代码块："+jcBlock);
                        transJCMethodInvocationBlock(jcBlock,context);
                        System.out.println("转换后的代码块："+jcBlock);
                    }
//                    @Override
//                    public void visitNewArray(JCTree.JCNewArray jcNewArray){
//                        super.visitNewArray(jcNewArray);
//                        System.out.println("elemtype: "+jcNewArray.elemtype);
//                        System.out.println("dims: "+jcNewArray.dims);
//                        if (jcNewArray.dims!=null){
//                            jcNewArray.dims.forEach(jcExpression -> System.out.println(jcExpression.getClass()));
//                        }
//                        System.out.println("elems: "+jcNewArray.elems);
//                    }

//                    @Override
//                    public void visitApply(JCTree.JCMethodInvocation methodInvocation){
//                        super.visitApply(methodInvocation);
////                        TreePath path = trees.getPath(compilationUnit,methodInvocation);
////                        TreePath fa_path = path.getParentPath();
////                        Symbol symbol = trees.getElement(fa_path);
////                        System.out.println("父节点symbol="+symbol);
//
////                        System.out.println("发现方法调用：");
////                        System.out.println("方法调用中的import：");
////                        imports.forEach(jcImport -> {
////                            JCTree jcTree1 = jcImport.getQualifiedIdentifier();
////                            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) jcTree1;
////                            System.out.println(fieldAccess.selected);
////                            System.out.println(fieldAccess.name);
////                            //System.out.println(fieldAccess.sym);
////                        });
//
//                        //构建AST，使用get(JCTree)返回JavacNode
//                        JavacAST ast = JavacAST.instance((JavacProcessingEnvironment) processingEnv,toUnit(element));
//                        JavacNode methodCallNode = ast.get(methodInvocation);
//                        Map<JCTree, JCTree> resolution = new JavacResolution(methodCallNode.getContext()).resolveMethodMember(methodCallNode);
//                        JCTree.JCExpression receiver = receiverOf(methodInvocation,methodCallNode);
//                        //JCTree resolvedMethodCall = resolution.get(methodInvocation);
//                        JCTree resolvedReceiver = resolution.get(receiver);
//                        System.out.println("解析后的receiver："+resolvedReceiver);
//                        //methodInvocation.meth 调用方法为static时为JCIdent，调用成员方法时为JCFieldAccess
//                        Symbol symbol = null;
//                        String class_name = null;
//                        if(resolvedReceiver instanceof JCTree.JCIdent){
//                            symbol = ((JCTree.JCIdent)resolvedReceiver).sym;
//                            class_name = symbol.getQualifiedName().toString();
//                        }
//                        else if(resolvedReceiver instanceof JCTree.JCFieldAccess){
//                            symbol = ((JCTree.JCFieldAccess)resolvedReceiver).sym;
//                            Symbol.VarSymbol varSymbol = (Symbol.VarSymbol) symbol;
//                            class_name = varSymbol.type.toString();
//                        }
//                        else if(resolvedReceiver instanceof JCTree.JCNewClass){
//                            Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) ((JCTree.JCIdent)((JCTree.JCNewClass)resolvedReceiver).clazz).sym;
//                            symbol = classSymbol;
//                            class_name = classSymbol.getQualifiedName().toString();
//                        }
//                        else{
//                            messager.printMessage(Diagnostic.Kind.WARNING,"有未分类的resolvedReceiver: "+resolvedReceiver);
//                        }
//                        System.out.println("解析后的sym："+symbol);
//                        //resolvedMethodCall 能掏出点有用信息来吗
//                        //TreePathScanner
//                        //com.sun.tools.javac.comp.Attr;//能否用于解析sym？
//                        //com.sun.source.util.TreeScanner;//可传递上下文信息的树扫描方式
//
//                        if(class_name != null){
//                            System.out.println("解析得到的类："+class_name);
//                        }
//                        //使用import查询大法
//                        else{
//                            //definitely a static method
//                            if(methodInvocation.meth instanceof JCTree.JCIdent){
//                                JCTree.JCIdent jcIdent = (JCTree.JCIdent)methodInvocation.meth;
//                                System.out.println("调用的方法为："+jcIdent.name);
//                                List<JCTree.JCExpression> args = methodInvocation.getArguments();
//                                args.forEach(jcExpression -> {
//                                    System.out.println("表达式："+jcExpression);
//                                    System.out.println("表达式类型："+jcExpression.getClass());
//                                    JCTree.JCExpression res = (JCTree.JCExpression) resolution.get(jcExpression);
//                                    XTypeSymbol xTypeSymbol = getReturnType(res);
//                                    if(xTypeSymbol != null) System.out.println("表达式数据类型解析结果："+xTypeSymbol);
//                                    else {
//                                        messager.printMessage(Diagnostic.Kind.WARNING,"表达式数据类型解析失败:"+jcExpression);
//                                    }
//                                    //else throw new RuntimeException("表达式数据类型解析失败: "+jcExpression);
//
//                                });
//                            }
//                            //Class.method(static) or Instance.method
//                            else if(methodInvocation.meth instanceof JCTree.JCFieldAccess){
//                                JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess)methodInvocation.meth;
//                                System.out.println("方法执行对象: "+fieldAccess.selected);
//                                System.out.println("调用的方法为："+fieldAccess.name);
//                                System.out.println("fieldAccess.sym:"+fieldAccess.sym);
//                                //sym 为null，如果需要获取需要先解析
//                            }
//                        }
//                        System.out.println("调用语句："+methodInvocation);
//                        System.out.println("expression: "+methodInvocation.getMethodSelect());
//                        System.out.println("Type："+methodInvocation.varargsElement);
//                        JCTree.JCExpression expression = methodInvocation.getMethodSelect();
//                        System.out.println("isPoly?"+expression.isPoly()+" isStandalone?"+expression.isStandalone());
//                    }

                });
            }
        }
        // 注意由于是处理了 * 所以需要返回 false，否则其它 APT 无法执行
        return false;
    }
    private JCTree.JCMethodDecl trackMethodDecl(JCTree.JCMethodInvocation jcMethodInvocation, Context context,String owner){
        //解析后的方法调用
        JCTree.JCMethodInvocation resolvedMethodCall;
        Type type;
        {
            //解析一下，拿到methodInvocation调用的方法返回类型
            JavacAST ast = JavacAST.instance((JavacProcessingEnvironment) processingEnv,toUnit(context.ancestor));
            JavacNode methodCallNode = ast.get(jcMethodInvocation);
            Map<JCTree, JCTree> resolution = new JavacResolution(methodCallNode.getContext()).resolveMethodMember(methodCallNode);
            resolvedMethodCall = (JCTree.JCMethodInvocation) resolution.get(jcMethodInvocation);
            type = resolvedMethodCall.type;
        }
        String meth = resolvedMethodCall.meth.toString();
        String[] splits = meth.split("\\.");
        //目前只解析指定类名的调用，即CLASS_NAME.method(...)或pkg.CLASS_NAME.method(...)
        //或者在@InlineAt中指明方法申明所在类的全限定名
        if (splits.length<=1) return null;
        String method_name = splits[splits.length-1];
        //可能为class的SimpleName，也有可能是全限定名
        final String simple_name = meth.substring(0,meth.length()-method_name.length()-1);
        //用于兜底的情形，即这里的simple_name实际上已经是全限定名
        String res = simple_name;
        boolean found = false;
        //如果指明了所在类的全限定名，直接用这个
        if (owner!=null && !"".equals(owner)){
            res = owner;
            found = true;
        }
        //尝试分析是SimpleName的情形
        //导入了类
        if (!found){
            for (String maybe : context.prefix) {
                String[] names = maybe.split("\\.");
                if (!Objects.equals(names[names.length - 1], "*")){
                    if (Objects.equals(names[names.length - 1], simple_name)){
                        res = maybe;
                        found = true;
                        break;
                    }
                }
            }
        }
        //通配符
        if (!found){
            label1:
            for (String maybe : context.prefix){
                String[] names = maybe.split("\\.");
                if (Objects.equals(names[names.length - 1], "*")){
                    try{
                        Class<?> clazz = Class.forName(maybe.replace("*",simple_name));
                        for (Method method : clazz.getMethods()){
                            //目前只处理static方法
                            if (Modifier.isStatic(method.getModifiers())){
                                if (method.getName().equals(method_name)){
                                    res = maybe.replace("*",simple_name);
                                    break label1;
                                }
                            }
                        }
                    }catch (ClassNotFoundException ignored){}
                }
            }
        }
        for (Map.Entry<XMethodSymbol, JCTree.JCMethodDecl> entry : context.mp.entrySet()){
            XMethodSymbol methodSymbol = entry.getKey();
            //检查类名和方法名
            if (Objects.equals(methodSymbol.class_name, res) && methodSymbol.meth_name.equals(method_name)){
                XTypeSymbol typeSymbol = new XTypeSymbol(treeMaker.Type(resolvedMethodCall.type));
                //检查返回类型
                // TODO: 检查参数是否匹配
                //if (typeSymbol.equals(methodSymbol.ret)){
                    //找到了实际调用的方法
                    return entry.getValue();
                //}
            }
        }
        return null;
    }
    private XTypeSymbol getReturnType(JCTree.JCExpression jcExpression){
        if(jcExpression instanceof JCTree.JCIdent){
            JCTree.JCIdent jcIdent = (JCTree.JCIdent) jcExpression;
            try{
                Symbol.VarSymbol varSymbol = (Symbol.VarSymbol) jcIdent.sym;
                return XTypeSymbol.fromType(varSymbol.type);
            }catch (Throwable throwable){
                messager.printMessage(Diagnostic.Kind.WARNING,"类型解析失败："+jcIdent);
                messager.printMessage(Diagnostic.Kind.WARNING,""+throwable);
            }
        }
        else if(jcExpression instanceof JCTree.JCFieldAccess){//Foo.class in parameter
            JCTree.JCFieldAccess jcFieldAccess = (JCTree.JCFieldAccess) jcExpression;
            try{
                //System.out.println(jcFieldAccess.type.getClass());
                return XTypeSymbol.fromType(jcFieldAccess.type);
            }catch (Throwable throwable){
                messager.printMessage(Diagnostic.Kind.WARNING,"类型解析失败："+jcExpression);
                messager.printMessage(Diagnostic.Kind.WARNING,""+throwable);
            }
        }
        else if(jcExpression instanceof JCTree.JCLiteral || jcExpression instanceof JCTree.JCTypeCast ||
                jcExpression instanceof JCTree.JCUnary || jcExpression instanceof JCTree.JCBinary ||
                jcExpression instanceof JCTree.JCMethodInvocation) {
            try{
                return XTypeSymbol.fromType(jcExpression.type);
            }catch (Throwable throwable){
                messager.printMessage(Diagnostic.Kind.WARNING,"类型解析失败："+jcExpression);
                messager.printMessage(Diagnostic.Kind.WARNING,""+throwable);
            }
        }
        return null;
    }
    private boolean isTempDecl(JCTree.JCStatement statement){
        return statement instanceof JCTree.JCVariableDecl && ((JCTree.JCVariableDecl) statement).name.toString().equals("$$$temp$$$");
    }
    private JCTree getParent(final JCTree.JCCompilationUnit compilationUnit,final JCTree cur){
        try{
            TreePath parentPath = TreePath.getPath(compilationUnit,cur).getParentPath();
            return (JCTree) parentPath.getParentPath().getLeaf();
            //System.out.println("父节点类型测试："+parentNode.getClass());
        }catch (Throwable throwable){
            throwable.printStackTrace();
            messager.printMessage(Diagnostic.Kind.WARNING,"获取父节点失败 loc="+cur);
            return null;
        }
    }
    private JCTree.JCExpression receiverOf(final JCTree.JCMethodInvocation methodCall,final JavacNode annotationNode) {
        if (methodCall.meth instanceof JCTree.JCIdent) {
            return annotationNode.getTreeMaker().Ident(annotationNode.toName("this"));
        } else {
            return ((JCTree.JCFieldAccess) methodCall.meth).selected;
        }
    }
    private JCTree.JCCompilationUnit toUnit(Element element) {
        TreePath path = null;
        if (trees != null) {
            try {
                path = trees.getPath(element);
            } catch (NullPointerException ignore) {
                // Happens if a package-info.java doesn't contain a package declaration.
                // We can safely ignore those, since they do not need any processing
            }
        }
        if (path == null) return null;
        return (JCTree.JCCompilationUnit) path.getCompilationUnit();
    }

    private void transJCMethodInvocationBlock(JCTree.JCBlock jcBlock,Context context){
        try{
            handleInvocationStat(jcBlock,context);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }

    //ignored:
    // JCTree.JCAssert, JCTree.JCBreak, JCTree.JCClassDecl, JCTree.JCContinue,
    // JCTree.JCExpressionStatement, JCTree.JCReturn, JCTree.JCSkip, JCTree.JCThrow,
    //handled:
    // JCTree.JCBlock, JCTree.JCCase, JCTree.JCDoWhileLoop, JCTree.JCEnhancedForLoop,
    // JCTree.JCForLoop, JCTree.JCIf, JCTree.JCLabeledStatement, JCTree.JCSwitch,
    // JCTree.JCSynchronized, JCTree.JCTry, JCTree.JCVariableDecl, JCTree.JCWhileLoop

    private java.util.List<JCTree.JCStatement> handleInvocationStat(JCTree.JCStatement jcStatement,Context context){
        if (jcStatement instanceof JCTree.JCBlock){
            JCTree.JCBlock jcBlock = (JCTree.JCBlock) jcStatement;
            ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
            for (JCTree.JCStatement statement : jcBlock.stats){
                java.util.List<JCTree.JCStatement> handled = handleInvocationStat(statement,context);
                if (handled==null) listBuffer.append(statement);
                else {
                    handled.forEach(listBuffer::append);
                }
            }
            jcBlock.stats = listBuffer.toList();
            return null;
        }
        if (jcStatement instanceof JCTree.JCWhileLoop){
            JCTree.JCWhileLoop jcWhileLoop = (JCTree.JCWhileLoop) jcStatement;
            java.util.List<JCTree.JCStatement> update = handleInvocationStat(jcWhileLoop.body,context);
            if (update!=null){
                ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
                update.forEach(listBuffer::append);
                jcWhileLoop.body = treeMaker.Block(0,listBuffer.toList());
            }
            return null;
        }
        if (jcStatement instanceof JCTree.JCSynchronized){
            JCTree.JCSynchronized jcSynchronized = (JCTree.JCSynchronized) jcStatement;
            handleInvocationStat(jcSynchronized.body,context);
            return null;
        }
        if (jcStatement instanceof JCTree.JCDoWhileLoop){
            JCTree.JCDoWhileLoop jcDoWhileLoop = (JCTree.JCDoWhileLoop) jcStatement;
            java.util.List<JCTree.JCStatement> update = handleInvocationStat(jcDoWhileLoop.body,context);
            if (update!=null){
                ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
                update.forEach(listBuffer::append);
                jcDoWhileLoop.body = treeMaker.Block(0,listBuffer.toList());
            }
            return null;
        }
        //handle the body only
        if (jcStatement instanceof JCTree.JCForLoop){
            JCTree.JCForLoop jcForLoop = (JCTree.JCForLoop) jcStatement;
            java.util.List<JCTree.JCStatement> update = handleInvocationStat(jcForLoop.body,context);
            if (update!=null){
                ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
                update.forEach(listBuffer::append);
                jcForLoop.body = treeMaker.Block(0,listBuffer.toList());
            }
            return null;
        }
        //handle the body only
        if (jcStatement instanceof JCTree.JCEnhancedForLoop){
            JCTree.JCEnhancedForLoop jcEnhancedForLoop = (JCTree.JCEnhancedForLoop) jcStatement;
            java.util.List<JCTree.JCStatement> update = handleInvocationStat(jcEnhancedForLoop.body,context);
            if (update!=null){
                ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
                update.forEach(listBuffer::append);
                jcEnhancedForLoop.body = treeMaker.Block(0,listBuffer.toList());
            }
            return null;
        }
        if (jcStatement instanceof JCTree.JCTry){
            JCTree.JCTry jcTry = (JCTree.JCTry) jcStatement;
            handleInvocationStat(jcTry.body,context);
            for (JCTree.JCCatch jcCatch : jcTry.catchers){
                handleInvocationStat(jcCatch.body,context);
            }
            handleInvocationStat(jcTry.finalizer,context);
            return null;
        }
        if (jcStatement instanceof JCTree.JCLabeledStatement){
            JCTree.JCLabeledStatement jcLabeledStatement = (JCTree.JCLabeledStatement) jcStatement;
            java.util.List<JCTree.JCStatement> update = handleInvocationStat(jcLabeledStatement.body,context);
            if (update!=null){
                ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
                update.forEach(listBuffer::append);
                jcLabeledStatement.body = treeMaker.Block(0,listBuffer.toList());
            }
            return null;
        }
        if (jcStatement instanceof JCTree.JCIf){
            JCTree.JCIf jcIf = (JCTree.JCIf) jcStatement;
            {
                JCTree.JCStatement then = jcIf.getThenStatement();
                java.util.List<JCTree.JCStatement> update = handleInvocationStat(then,context);
                if (update != null) {
                    ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
                    update.forEach(listBuffer::append);
                    jcIf.thenpart = treeMaker.Block(0,listBuffer.toList());
                }
            }
            {
                JCTree.JCStatement els = jcIf.getElseStatement();
                java.util.List<JCTree.JCStatement> update = handleInvocationStat(els,context);
                if (update != null){
                    ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
                    update.forEach(listBuffer::append);
                    jcIf.elsepart = treeMaker.Block(0,listBuffer.toList());
                }
            }
            return null;
        }
        if (jcStatement instanceof JCTree.JCSwitch){
            JCTree.JCSwitch jcSwitch = (JCTree.JCSwitch) jcStatement;
            for (JCTree.JCCase jcCase : jcSwitch.getCases()){
                handleInvocationStat(jcCase,context);
            }
            return null;
        }
        if (jcStatement instanceof JCTree.JCCase){
            JCTree.JCCase jcCase = (JCTree.JCCase) jcStatement;
            ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
            for (JCTree.JCStatement statement : jcCase.getStatements()){
                java.util.List<JCTree.JCStatement> handled = handleInvocationStat(statement,context);
                if (handled==null) listBuffer.append(statement);
                else handled.forEach(listBuffer::append);
            }
            jcCase.stats = listBuffer.toList();
            return null;
        }
        if (jcStatement instanceof JCTree.JCVariableDecl){
            JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) jcStatement;
            //解析一下，拿到有上下文的变量定义
            JavacAST ast = JavacAST.instance((JavacProcessingEnvironment) processingEnv,toUnit(context.ancestor));
            JavacNode varDeclNode = ast.get(variableDecl);
            Map<JCTree, JCTree> resolution = new JavacResolution(varDeclNode.getContext()).resolveMethodMember(varDeclNode);
            JCTree.JCVariableDecl resolved = (JCTree.JCVariableDecl) resolution.get(variableDecl);
            Element element = resolved.sym;
            //一般来说不会是null，保险起见还是判断一下
            if (element == null) return null;
            InlineAt[] inlineAts = element.getAnnotationsByType(InlineAt.class);
            if (inlineAts.length==0) return null;
            InlineAt at = inlineAts[0];
            JCTree.JCExpression init = variableDecl.init;
            if (init instanceof JCTree.JCMethodInvocation){
                JCTree.JCMethodInvocation jcMethodInvocation = (JCTree.JCMethodInvocation) init;
                //init不需要在这里解析，因为trackMethodDecl方法内部已经解析了
                JCTree.JCMethodDecl decl = trackMethodDecl(jcMethodInvocation,context,at.value());
                //System.out.println("decl is null? "+decl);
                if (decl!=null){
                    JCTree.JCCompilationUnit compilationUnit_invocation = toUnit(context.ancestor);
                    JCTree.JCCompilationUnit compilationUnit_decl = toUnit(decl.sym);
                    //将方法申明所在类的import都加进来
                    if(compilationUnit_decl!=null){
                        JCTree.JCClassDecl decl_class = null;
                        for (JCTree jcTree : compilationUnit_decl.defs){
                            if (jcTree instanceof JCTree.JCClassDecl) {
                                //找到方法申明所在的外部类
                                decl_class = (JCTree.JCClassDecl) jcTree;
                                break;
                            }
                        }
                        //总是成立
                        if (decl_class != null) {
                            //import p.k.g.MethodDeclClassName;
                            importClass(context.ancestor,decl_class.sym.fullname.toString());
                            // TODO: add import static p.k.g.MethodDeclClassName.*; ?
                        }
                        ListBuffer<JCTree> imports = new ListBuffer<>();
                        compilationUnit_decl.getImports().forEach(jcImport -> {
                            imports.append(treeCopier.copy(jcImport));
                        });
                        if (compilationUnit_invocation != null) {
                            compilationUnit_invocation.defs.forEach(imports::append);
                            compilationUnit_invocation.defs = imports.toList();
                        }
                    }
                    if (decl.restype.type.getTag()==TypeTag.VOID){
                        ListBuffer<JCTree.JCStatement> code = parseArguments(jcMethodInvocation,decl);
                        for (JCTree.JCStatement stat : decl.body.stats) {
                            //不能直接把表达式塞进去，必须拷贝
                            code.append(treeCopier.copy(stat));
                        }
                        JCTree.JCBlock block = treeMaker.Block(0,code.toList());
                        return Collections.singletonList(block);
                    }
                    else{
                        ListBuffer<JCTree.JCStatement> code = parseArguments(jcMethodInvocation,decl);
                        for (JCTree.JCStatement stat : decl.body.stats) {
                            //不能直接把表达式塞进去，必须拷贝
                            //最后一句return改成向辅助变量赋值
                            if (stat instanceof JCTree.JCReturn) {
                                JCTree.JCReturn jcReturn = (JCTree.JCReturn) stat;
                                JCTree.JCExpression result = treeCopier.copy(jcReturn.expr);
                                code.append(
                                        treeMaker.Exec(treeMaker.Assign(treeMaker.Ident(variableDecl.name),result))
                                );
                            }
                            else code.append(treeCopier.copy(stat));
                        }
                        JCTree.JCBlock later = treeMaker.Block(0,code.toList());
                        variableDecl.init = null;
                        java.util.List<JCTree.JCStatement> all = new ArrayList<>();
                        all.add(variableDecl);
                        all.add(later);
                        return all;
                    }
                }
            }
        }
        return null;
    }
    private void importClass(Element element,String fullName){
        JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) trees.getPath(element).getCompilationUnit();
        String className = fullName.substring(fullName.lastIndexOf(".") + 1);
        String packageName = fullName.substring(0, fullName.lastIndexOf("."));
        JCTree.JCFieldAccess fieldAccess = treeMaker.Select(treeMaker.Ident(names.fromString(packageName)),
                names.fromString(className));
        JCTree.JCImport jcImport = treeMaker.Import(fieldAccess, false);
        ListBuffer<JCTree> imports = new ListBuffer<>();
        imports.append(jcImport);
        for (int i = 0; i < compilationUnit.defs.size(); i++) {
            imports.append(compilationUnit.defs.get(i));
        }
        compilationUnit.defs = imports.toList();
    }
    private ListBuffer<JCTree.JCStatement> parseArguments(JCTree.JCMethodInvocation jcMethodInvocation, JCTree.JCMethodDecl jcMethodDecl){
        ListBuffer<JCTree.JCStatement> res = new ListBuffer<>();
        final Iterator<JCTree.JCExpression> iterator = jcMethodInvocation.args.iterator();
        int size = jcMethodDecl.getParameters().length(),i=0;
        for (JCTree.JCVariableDecl jcVariableDecl : jcMethodDecl.params){
            JCTree.JCVariableDecl copy = treeCopier.copy(jcVariableDecl);
            XTypeSymbol xTypeSymbol = new XTypeSymbol(jcVariableDecl.vartype);
            //如果方法定义中最后一个参数为Object...
            if (++i==size && xTypeSymbol.type == XTypeSymbol.Type.ARRAY && jcVariableDecl.toString().split(" ")[0].endsWith("...")){
                JCTree.JCArrayTypeTree jcArrayTypeTree = (JCTree.JCArrayTypeTree) jcVariableDecl.vartype;
                ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
                while (iterator.hasNext())
                    listBuffer.append(treeCopier.copy(iterator.next()));
                copy.init = treeMaker.NewArray(treeCopier.copy(jcArrayTypeTree.elemtype),List.nil(),listBuffer.toList());
            }
            else copy.init = treeCopier.copy(iterator.next());
            copy.mods = treeMaker.Modifiers(0);
            res.append(copy);
        }
        return res;
    }
    private void transJCMethodBlock(JCTree.JCMethodDecl jcMethod){
        boolean isVoid = false;
        //如果为方法返回类型void则不添加临时变量
        if(jcMethod.restype instanceof JCTree.JCPrimitiveTypeTree){
            JCTree.JCPrimitiveTypeTree typeTree = (JCTree.JCPrimitiveTypeTree) jcMethod.restype;
            if(typeTree.typetag == TypeTag.VOID) isVoid = true;
        }
        ListBuffer<JCTree.JCStatement> res = new ListBuffer<>();
        //增加一个temp临时变量
        if(!isVoid){
            res.append(treeMaker.VarDef(
                    treeMaker.Modifiers(0),names.fromString("$$$temp$$$"),treeCopier.copy(jcMethod.restype),null)
            );
        }
        //处理方法内的return
        handleJCStatement(jcMethod.body);
        //使用代码块嵌套修改后的代码
        res.append(treeMaker.Labelled(names.fromString("$$$label$$$"),treeMaker.Block(0,jcMethod.body.stats)));
        jcMethod.body.stats = res.toList();
        //在尾部添加return $$$temp$$$;
        if(!isVoid){
            ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
            for(JCTree.JCStatement statement : jcMethod.body.stats){
                listBuffer.append(statement);
            }
            listBuffer.append(treeMaker.Return(treeMaker.Ident(names.fromString("$$$temp$$$"))));
            jcMethod.body.stats = listBuffer.toList();
        }
    }
    //ignored:
    // JCTree.JCAssert, JCTree.JCBreak, JCTree.JCClassDecl, JCTree.JCContinue,
    // JCTree.JCExpressionStatement, JCTree.JCSkip, JCTree.JCThrow,
    // JCTree.JCVariableDecl,
    //handled:
    // JCTree.JCBlock, JCTree.JCCase, JCTree.JCDoWhileLoop, JCTree.JCEnhancedForLoop,
    // JCTree.JCForLoop, JCTree.JCIf, JCTree.JCLabeledStatement, JCTree.JCReturn,
    // JCTree.JCSwitch, JCTree.JCSynchronized, JCTree.JCTry, JCTree.JCWhileLoop,

    private JCTree.JCStatement handleJCStatement(JCTree.JCStatement statement){
        if(statement == null) return null;
        if(statement instanceof JCTree.JCBlock){
            JCTree.JCBlock block = (JCTree.JCBlock) statement;
            ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
            for(JCTree.JCStatement statement1 : block.getStatements()){
                JCTree.JCStatement update = handleJCStatement(statement1);
                if(update == null) listBuffer.append(statement1);
                else listBuffer.append(update);
            }
            block.stats = listBuffer.toList();
            return null;
        }
        if (statement instanceof JCTree.JCLabeledStatement){
            JCTree.JCLabeledStatement jcLabeledStatement = (JCTree.JCLabeledStatement) statement;
            JCTree.JCStatement update = handleJCStatement(jcLabeledStatement.body);
            if (update != null) jcLabeledStatement.body = update;
            return null;
        }
        if (statement instanceof JCTree.JCTry){
            JCTree.JCTry jcTry = (JCTree.JCTry) statement;
            handleJCStatement(jcTry.body);
            for (JCTree.JCCatch jcCatch : jcTry.catchers){
                handleJCStatement(jcCatch.body);
            }
            handleJCStatement(jcTry.finalizer);
            return null;
        }
        if (statement instanceof JCTree.JCWhileLoop){
            JCTree.JCWhileLoop jcWhileLoop = (JCTree.JCWhileLoop) statement;
            JCTree.JCStatement update = handleJCStatement(jcWhileLoop.body);
            if (update != null) jcWhileLoop.body = update;
            return null;
        }
        if (statement instanceof JCTree.JCDoWhileLoop){
            JCTree.JCDoWhileLoop jcDoWhileLoop = (JCTree.JCDoWhileLoop) statement;
            JCTree.JCStatement update = handleJCStatement(jcDoWhileLoop.body);
            if (update != null) jcDoWhileLoop.body = update;
            return null;
        }
        if (statement instanceof JCTree.JCForLoop){
            JCTree.JCForLoop jcForLoop = (JCTree.JCForLoop) statement;
            JCTree.JCStatement update = handleJCStatement(jcForLoop.body);
            if (update != null) jcForLoop.body = update;
            return null;
        }
        if(statement instanceof JCTree.JCEnhancedForLoop){
            JCTree.JCEnhancedForLoop jcEnhancedForLoop = (JCTree.JCEnhancedForLoop) statement;
            JCTree.JCStatement update = handleJCStatement(jcEnhancedForLoop.body);
            if (update != null) jcEnhancedForLoop.body = update;
            return null;
        }
        if(statement instanceof JCTree.JCIf){
            JCTree.JCIf jcIf = (JCTree.JCIf) statement;
            JCTree.JCStatement then = jcIf.getThenStatement();
            {
                JCTree.JCStatement update = handleJCStatement(then);
                if(update != null) jcIf.thenpart = update;
            }
            JCTree.JCStatement els  = jcIf.getElseStatement();
            {
                JCTree.JCStatement update = handleJCStatement(els);
                if(update != null) jcIf.elsepart = update;
            }
            return null;
        }
        if(statement instanceof JCTree.JCReturn){
            JCTree.JCReturn jcReturn = (JCTree.JCReturn) statement;
            ListBuffer<JCTree.JCStatement> res = new ListBuffer<>();
            //jcReturn.expr==null -> return;
            if(jcReturn.expr != null){
                res.append(treeMaker.Exec(treeMaker.Assign(treeMaker.Ident(names.fromString("$$$temp$$$")),jcReturn.expr)));
            }
            res.append(treeMaker.Break(names.fromString("$$$label$$$")));
            return treeMaker.Block(0,res.toList());
        }
        if(statement instanceof JCTree.JCCase){
            JCTree.JCCase jcCase = (JCTree.JCCase) statement;
            ListBuffer<JCTree.JCStatement> listBuffer = new ListBuffer<>();
            for(JCTree.JCStatement statement1 : jcCase.getStatements()){
                JCTree.JCStatement update = handleJCStatement(statement1);
                if(update == null) listBuffer.append(statement1);
                else listBuffer.append(update);
            }
            jcCase.stats = listBuffer.toList();
            return null;
        }
        if(statement instanceof JCTree.JCSwitch){
            JCTree.JCSwitch jcSwitch = (JCTree.JCSwitch) statement;
            for(JCTree.JCCase jcCase : jcSwitch.getCases()){
                handleJCStatement(jcCase);
            }
            return null;
        }
        if(statement instanceof JCTree.JCSynchronized){
            JCTree.JCSynchronized jcSynchronized = (JCTree.JCSynchronized) statement;
            handleJCStatement(jcSynchronized.body);
            return null;
        }

        return null;
    }

    private Symbol.MethodSymbol findMethod(Symbol.ClassSymbol var1, Name var2, List<Type> var3){
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(Symbol.MethodSymbol.class, Symbol.ClassSymbol.class,Name.class,List.class);
        //Method method = JavacTrees.class.getDeclaredMethod("findMethod", Symbol.ClassSymbol.class, Name.class, List.class);
        //method.setAccessible(true);
        //MethodHandle mh = lookup.unreflect(method);
        try{
            MethodHandle handle = lookup.findVirtual(JavacTrees.class,"findMethod",mt);
            return (Symbol.MethodSymbol) handle.invokeExact(trees,var1,var2,var3);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        return null;
    }

}



//    @Override
//    public void visitVarDef(JCTree.JCVariableDecl var1){
//        super.visitVarDef(var1);
//        //null -> BOT
//        //not primitive -> CLASS
//        //JCTree.JCLiteral jcLiteral = (JCTree.JCLiteral) var1.init;
//        //System.out.println(jcLiteral.typetag);
//        //System.out.println(jcLiteral.value);
//    }