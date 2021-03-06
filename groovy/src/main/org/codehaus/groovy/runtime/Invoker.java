/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime;

import groovy.lang.*;
import org.codehaus.groovy.runtime.wrappers.PojoWrapper;

/**
 * A helper class to invoke methods or extract properties on arbitrary Java objects dynamically.
 * All methodes in this calss are deprecated!
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision: 9487 $
 * @deprecated
 */
public class Invoker {

    protected static final Object[] EMPTY_ARGUMENTS = {
    };
    protected static final Class[] EMPTY_TYPES = {
    };

    /**
     * @deprecated
     */
    public MetaClassRegistry getMetaRegistry() {
        return metaRegistry;
    }

    private final MetaClassRegistry metaRegistry = GroovySystem.getMetaClassRegistry();

    /**
     * @deprecated
     */
    public MetaClass getMetaClass(Object object) {
        return metaRegistry.getMetaClass(object.getClass());
    }

    /**
     * Invokes the given method on the object.
     * @deprecated
     */
    public Object invokeMethod(Object object, String methodName, Object arguments) {
        /*
        System
            .out
            .println(
                "Invoker - Invoking method on object: "
                    + object
                    + " method: "
                    + methodName
                    + " arguments: "
                    + InvokerHelper.toString(arguments));
                    */

        if (object == null) {
            object = NullObject.getNullObject();
            //throw new NullPointerException("Cannot invoke method " + methodName + "() on null object");
        }

        // if the object is a Class, call a static method from that class
        if (object instanceof Class) {
            Class theClass = (Class) object;
            MetaClass metaClass = metaRegistry.getMetaClass(theClass);
            return metaClass.invokeStaticMethod(object, methodName, asArray(arguments));
        }
        else // it's an instance
        {
            // if it's not an object implementing GroovyObject (thus not builder, nor a closure)
            if (!(object instanceof GroovyObject)) {
                return invokePojoMethod(object, methodName, arguments);
            }
            // it's an object implementing GroovyObject
            else {
                return invokePogoMethod(object, methodName, arguments);
            }
        }
    }

    private Object invokePojoMethod(Object object, String methodName, Object arguments) {
        Class theClass = object.getClass();
        MetaClass metaClass = metaRegistry.getMetaClass(theClass);
        return metaClass.invokeMethod(object, methodName, asArray(arguments));
    }

    private Object invokePogoMethod(Object object, String methodName, Object arguments) {
        GroovyObject groovy = (GroovyObject) object;
        boolean intercepting = groovy instanceof GroovyInterceptable;
        try {
            // if it's a pure interceptable object (even intercepting toString(), clone(), ...)
            if (intercepting) {
                return groovy.invokeMethod(methodName, asUnwrappedArray(arguments));
            }
            //else try a statically typed method or a GDK method
            return groovy.getMetaClass().invokeMethod(object, methodName, asArray(arguments));
        } catch (MissingMethodException e) {
            if (!intercepting && e.getMethod().equals(methodName) && object.getClass() == e.getType()) {
                // in case there's nothing else, invoke the object's own invokeMethod()
                return groovy.invokeMethod(methodName, asUnwrappedArray(arguments));
            }
            throw e;
        }
    }

    /**
     * @deprecated
     */
    public Object invokeSuperMethod(Object object, String methodName, Object arguments) {
        if (object == null) {
            throw new NullPointerException("Cannot invoke method " + methodName + "() on null object");
        }

        Class theClass = object.getClass();

        MetaClass metaClass = metaRegistry.getMetaClass(theClass.getSuperclass());
        return metaClass.invokeMethod(object, methodName, asArray(arguments));
    }

    /**
     * @deprecated
     */
    public Object invokeStaticMethod(Class type, String method, Object arguments) {
        MetaClass metaClass = metaRegistry.getMetaClass(type);
        return metaClass.invokeStaticMethod(type, method, asArray(arguments));
    }

    /**
     * @deprecated
     */
    public Object invokeConstructorOf(Class type, Object arguments) {
        MetaClass metaClass = metaRegistry.getMetaClass(type);
        return metaClass.invokeConstructor(asArray(arguments));
    }

    /**
     * Converts the given object into an array; if its an array then just
     * cast otherwise wrap it in an array
     * @deprecated 
     */
    public Object[] asArray(Object arguments) {

    	if (arguments == null) {
    		return EMPTY_ARGUMENTS;
    	}
    	if (arguments instanceof Object[]) {
    		return  (Object[]) arguments;
    	}
    	return new Object[]{arguments};
    }

    /**
     * @deprecated
     */
    public Object[] asUnwrappedArray(Object arguments) {

        Object[] args = asArray(arguments);

        for (int i=0; i<args.length; i++) {
            if (args[i] instanceof PojoWrapper) {
                args[i] = ((PojoWrapper)args[i]).unwrap();
            }
        }

        return args;
    }

    /**
     * Looks up the given property of the given object
     * @deprecated
     */
    public Object getProperty(Object object, String property) {
        if (object == null) {
            throw new NullPointerException("Cannot get property: " + property + " on null object");
        }
        if (object instanceof GroovyObject) {
            GroovyObject pogo = (GroovyObject) object;
            return pogo.getProperty(property);
        }
        if (object instanceof Class) {
            Class c = (Class) object;
            return metaRegistry.getMetaClass(c).getProperty(object, property);
        }
        return metaRegistry.getMetaClass(object.getClass()).getProperty(object, property);
    }

    /**
     * Sets the property on the given object
     * @deprecated
     */
    public void setProperty(Object object, String property, Object newValue) {
        if (object == null) {
            throw new GroovyRuntimeException("Cannot set property on null object");
        }
        if (object instanceof GroovyObject) {
            GroovyObject pogo = (GroovyObject) object;
            pogo.setProperty(property, newValue);
        }
        else {
            if (object instanceof Class)
                metaRegistry.getMetaClass((Class) object).setProperty((Class) object, property, newValue);
            else
                metaRegistry.getMetaClass(object.getClass()).setProperty(object, property, newValue);
        }
    }

    /**
     * Looks up the given attribute (field) on the given object
     * @deprecated
     */
    public Object getAttribute(Object object, String attribute) {
        if (object == null) {
            throw new NullPointerException("Cannot get attribute: " + attribute + " on null object");

            /**
             } else if (object instanceof GroovyObject) {
             GroovyObject pogo = (GroovyObject) object;
             return pogo.getAttribute(attribute);
             } else if (object instanceof Map) {
             Map map = (Map) object;
             return map.get(attribute);
             */
        }
        else {
            if (object instanceof Class) {
                return metaRegistry.getMetaClass((Class) object).getAttribute(object, attribute);
            } else if (object instanceof GroovyObject) {
                return ((GroovyObject)object).getMetaClass().getAttribute(object, attribute);
            } else {
                return metaRegistry.getMetaClass(object.getClass()).getAttribute(object, attribute);
            }
	}
    }

    /**
     * Sets the given attribute (field) on the given object
     * @deprecated
     */
    public void setAttribute(Object object, String attribute, Object newValue) {
        if (object == null) {
            throw new GroovyRuntimeException("Cannot set attribute on null object");
            /*
        } else if (object instanceof GroovyObject) {
            GroovyObject pogo = (GroovyObject) object;
            pogo.setProperty(attribute, newValue);
        } else if (object instanceof Map) {
            Map map = (Map) object;
            map.put(attribute, newValue);
            */
        }
        else {
            if (object instanceof Class) {
                metaRegistry.getMetaClass((Class) object).setAttribute(object, attribute, newValue);
            } else if (object instanceof GroovyObject) {
                ((GroovyObject)object).getMetaClass().setAttribute(object, attribute, newValue);
            } else {
                metaRegistry.getMetaClass(object.getClass()).setAttribute(object, attribute, newValue);
            }
	}
    }

    /**
     * Returns the method pointer for the given object name
     * @deprecated
     */
    public Closure getMethodPointer(Object object, String methodName) {
        if (object == null) {
            throw new NullPointerException("Cannot access method pointer for '" + methodName + "' on null object");
        }
        return MetaClassHelper.getMethodPointer(object, methodName);
    }

    /**
     * @deprecated
     */
    public void removeMetaClass(Class clazz) {
        metaRegistry.removeMetaClass(clazz);
    }
}
