package gls.generics.vm5;


import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyClassLoader.InnerLoader;
import groovy.util.GroovyTestCase;


public class GenericsTestBase extends GroovyTestCase {
    MyLoader loader;
    HashMap signatures = new HashMap();
    
    private class MyLoader extends GroovyClassLoader{
        public MyLoader(ClassLoader classLoader) {
            super(classLoader);
        }

        protected ClassCollector createCollector(CompilationUnit unit,SourceUnit su) {
            return new MyCollector(new InnerLoader(this), unit, su);
        }
    }
    private class MyCollector extends GroovyClassLoader.ClassCollector{

        public MyCollector(InnerLoader myLoader, CompilationUnit unit, SourceUnit su) {
           super(myLoader,unit,su);
        }
        protected Class createClass(byte[] code, ClassNode classNode) {
            ClassReader cr = new ClassReader(code);
            GenericsTester classVisitor = new GenericsTester(new org.objectweb.asm.tree.ClassNode());
            cr.accept(classVisitor, true);
            return super.createClass(code,classNode);
        }        
    }
    private class GenericsTester  extends ClassAdapter {
        public GenericsTester(ClassVisitor cv) {
            super(cv);
        }
        public void visit(int version, int access, String name,
                String signature, String superName, String[] interfaces) {
            if (signature!=null) signatures.put("class", signature);
        }
        public FieldVisitor visitField(int access, String name, String desc,
                String signature, Object value) {
            if (signature!=null) signatures.put(name,signature);
            return super.visitField(access, name, desc, signature, value);
        }
        public MethodVisitor visitMethod(int access, String name, String desc,
                String signature, String[] exceptions) {
            if (signature!=null) signatures.put(name+desc,signature);
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }
    
    public void setUp(){
        loader = new MyLoader(this.getClass().getClassLoader());
    }
    
    public void createClassInfo(String script) {
        loader.parseClass(script);
    }
    
    public Map getSignatures() {
        return signatures;
    }
    
    void shouldNotCompile(String script) {
        try {
            loader.parseClass(script);
        } catch (CompilationFailedException cfe) {
            return;
        }
        throw new AssertionError("compilation of script '"+script+"' should have failed, but did not.");
    }
}

