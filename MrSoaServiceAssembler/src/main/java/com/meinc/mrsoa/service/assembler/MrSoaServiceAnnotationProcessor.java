package com.meinc.mrsoa.service.assembler;

import static com.meinc.mrsoa.service.assembler.MrSoaCompilerMojo.fileSeparatorReplaceRegex;
import static java.lang.String.format;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor6;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.meinc.mrsoa.service.IMrSoaProxy;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.annotation.ExposeReturnObjectMethods;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.mrsoa.service.exception.AdaptorException;

@SupportedAnnotationTypes("com.meinc.mrsoa.service.annotation.Service")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class MrSoaServiceAnnotationProcessor extends AbstractProcessor {
    public static final String OSGI_ACTIVATOR_CLASSNAME = "com.meinc.mrsoa.service.stubs.OsgiActivator";
    private static final String STUB_TEMPLATE_PATH = "com/meinc/mrsoa/service/assembler/osgi-mrsoa-service.java.vm";
    private static final String CLIENTPROXY_TEMPLATE_PATH = "com/meinc/mrsoa/service/assembler/client-mrsoa-proxy-stub.java.vm";
    private static final String OSGI_ADAPTOR_TEMPLATE_PATH = "com/meinc/mrsoa/service/assembler/osgi-activator.java.vm";

    private ProcessingEnvironment env;

    public MrSoaServiceAnnotationProcessor() {
    }
    
    @Override
    public void init(ProcessingEnvironment env) {
        this.env = env;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotationElements, RoundEnvironment roundEnvironment) {
        try ( MrSoaMojoSharedState state = MrSoaMojoSharedState.readState() ) {
            for (TypeElement annotationElement : annotationElements) {
                Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(annotationElement);
                for (Element annotatedElement : annotatedElements) {
                    System.out.println("       * Processing " + annotatedElement.asType().toString());
                    processService(annotatedElement, state);
                }
            }
            if (!roundEnvironment.errorRaised() && roundEnvironment.processingOver())
                finalizeServices(state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true; // tell compiler the annotation has be processed, don't pass it to other processors
    }

    private void processService(Element serviceClassElement, MrSoaMojoSharedState state) {
        try {
            Velocity.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        VelocityContext context = new VelocityContext();

        context.put("test_mode", new Boolean(false));

        String targetQualifiedName = serviceClassElement.asType().toString();
        String targetSimpleName = serviceClassElement.getSimpleName().toString();
        String targetPackage = targetQualifiedName.substring(0, targetQualifiedName.length()-targetSimpleName.length()-1);
        String clientProxyPackage = "clientproxy."+targetSimpleName.toLowerCase();
        
        context.put("target_package", targetPackage);
        context.put("target_qualified_classname", targetQualifiedName);
        context.put("target_classname", targetSimpleName);
        context.put("clientproxy_package", clientProxyPackage);
 
        state.serviceFqNames.add(targetQualifiedName);

        String serviceName = null;
        String namespace = null;
        ArrayList<String> interfaces = new ArrayList<String>();
        String version = null;
        String exposeAsTypeName = null;
        Boolean exposeAsTypeIsInterface = false;
        for (AnnotationMirror annotation : serviceClassElement.getAnnotationMirrors()) {
            String annotationName = annotation.getAnnotationType().toString();
            if (annotationName.equals(Service.class.getName())) {
                for (ExecutableElement annotationParmKey : annotation.getElementValues().keySet()) {
                    if ("name".equals(annotationParmKey.getSimpleName().toString())) {
                        serviceName = (String) annotation.getElementValues().get(annotationParmKey).getValue();
                        serviceName = ServiceEndpoint.delimitServiceString(serviceName);
                    } else if ("namespace".equals(annotationParmKey.getSimpleName().toString())) {
                        namespace = (String) annotation.getElementValues().get(annotationParmKey).getValue();
                        namespace = ServiceEndpoint.delimitServiceString(namespace);
                    } else if ("interfaces".equals(annotationParmKey.getSimpleName().toString())) {
                        String interfaceString = (String) annotation.getElementValues().get(annotationParmKey).getValue();
                        String[] interfaceArray = interfaceString.split(",");
                        for (String interfaceName : interfaceArray)
                            interfaces.add(ServiceEndpoint.delimitServiceString(interfaceName.trim()));
                    } else if ("version".equals(annotationParmKey.getSimpleName().toString())) {
                        String versionString = (String) annotation.getElementValues().get(annotationParmKey).getValue();
                        version = versionString.trim();
                    } else if ("exposeAs".equals(annotationParmKey.getSimpleName().toString())) {
                        Object exposeAs = annotation.getElementValues().get(annotationParmKey).getValue();
                        if (DeclaredType.class.isInstance(exposeAs))
                            exposeAsTypeIsInterface = true;
                        exposeAsTypeName = exposeAs.toString();
                        // Object is what is returned when the user did not
                        // specify an 'exposeAs' annotation variable
                        if ("java.lang.Object".equals(exposeAsTypeName))
                            exposeAsTypeName = null;
                    }
                }
                break;
            }
        }

        if (serviceName == null || serviceName.length() == 0)
            throw new AdaptorException("Service Class " + targetQualifiedName + " has invalid @Service(name)");

        if (namespace == null || namespace.length() == 0)
            namespace = ServiceEndpoint.DEFAULT_NAMESPACE;

        if (interfaces.isEmpty())
            throw new AdaptorException("Service Class " + targetQualifiedName + " must provide at least one interface in @Service(interfaces)");

        context.put("service_name", serviceName);
        context.put("service_namespace", namespace);
        context.put("interfaces", interfaces);
        context.put("version", version);
        context.put("expose_as", exposeAsTypeName);
        context.put("expose_as_is_interface", exposeAsTypeIsInterface);

        if (state.pluginFactoryClassName != null && state.pluginFactoryClassName.length() > 0) {
            Map<String, String> pluginParmsMap = new HashMap<String, String>();
            for (String parmKey : state.pluginParms.keySet()) {
                String pluginMatch = null;
                int pluginMatchTailLen = 0;
                if (version != null) {
                    String tail = "." + serviceName + "." + version;
                    if (parmKey.endsWith(tail)) {
                        pluginMatch = parmKey;
                        pluginMatchTailLen = tail.length();
                    }
                }
                String tail = "." + serviceName;
                if (pluginMatch == null && parmKey.endsWith(tail)) {
                    pluginMatch = parmKey;
                    pluginMatchTailLen = tail.length();
                }
                if (pluginMatch != null) {
                    String parmVal = state.pluginParms.get(pluginMatch);
                    String parmRealKey = pluginMatch.substring(0, pluginMatch.length() - pluginMatchTailLen);
                    pluginParmsMap.put(parmRealKey, parmVal);
                }
            }
            // Do not use a plugin if there were no parameters sent to it
            if (!pluginParmsMap.isEmpty()) {
                context.put("plugin_factory_qualified_classname", state.pluginFactoryClassName);
                context.put("plugin_parms", pluginParmsMap);
            }
        }

        List<Map<String, Object>> serviceMethodObjects = new ArrayList<Map<String, Object>>();
        List<String> onStartMethods = new ArrayList<String>();
        List<String> onStopMethods = new ArrayList<String>();
        List<String> onStopProxyDependencies = new ArrayList<String>();

        /* Assemble methods for a Service from its annotations */

        // Scan for ServiceMethod and OnStart method annotations
        for (Element element : serviceClassElement.getEnclosedElements()) {
            List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
            for (AnnotationMirror annotation : annotations) {
                String annotationName = annotation.getAnnotationType().toString();
                if (annotationName.equals(ServiceMethod.class.getName())) {
                    ExecutableElement method = (ExecutableElement) element;
                    Map<String, Object> serviceMethodObject = new HashMap<String, Object>();
                    serviceMethodObjects.add(serviceMethodObject);

                    serviceMethodObject.put("name", method.getSimpleName().toString());
                    serviceMethodObject.put("serviceMethodName", method.getSimpleName().toString());

                    //REVIEW
                    String genericTypes = "";
                    List<? extends TypeParameterElement> tpes = method.getTypeParameters();
                    for (TypeParameterElement tpe : tpes) {
                        if (!genericTypes.isEmpty())
                            genericTypes += ",";
                        genericTypes += tpe.getSimpleName().toString();
                    }
                    if (!genericTypes.isEmpty())
                        genericTypes = "<" + genericTypes + ">";
                    serviceMethodObject.put("generic_types", genericTypes);
                    //ME

                    if ("void".equals(method.getReturnType().toString()))
                        serviceMethodObject.put("returnsVoid", true);

                    String typeName = null;
                    TypeMirror returnType = method.getReturnType();
                    typeName = returnType.toString();

                    serviceMethodObject.put("return_type", typeName);
                    serviceMethodObject.put("proper_return_type", getPrimitiveTypeObject(typeName));

                    serviceMethodObject.put("args", getArgsList(method));

                    List<? extends TypeMirror> throwsTypes = method.getThrownTypes();
                    List<String> throwsList = new ArrayList<String>();
                    SimpleTypeVisitor6<Void,Void> throwsVisitor = new SimpleTypeVisitor6<Void,Void>() {
                        public Void visitDeclared(DeclaredType exceptionType, Void v) {
                            String throwsTypeName = exceptionType.toString();
                            throwsList.add(throwsTypeName);
                            return null;
                        }
                    };
                    for (TypeMirror thrownType : throwsTypes)
                        thrownType.accept(throwsVisitor, null);
                    serviceMethodObject.put("throws", throwsList);

                } else if (annotationName.equals(OnStart.class.getName())) {
                    onStartMethods.add(element.getSimpleName().toString());

                } else if (annotationName.equals(OnStop.class.getName())) {
                    onStopMethods.add(element.getSimpleName().toString());

                    for (Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationEntry : annotation.getElementValues().entrySet()) {
                        if ("depends".equals(annotationEntry.getKey().getSimpleName().toString())) {
                            AnnotationValue annotationParmValue = annotationEntry.getValue();
                            @SuppressWarnings("unchecked")
                            List<? extends AnnotationValue> serviceDependencyAnnotations = (List<? extends AnnotationValue>) annotationParmValue.getValue();
                            for (AnnotationValue serviceDependencyAnnotation : serviceDependencyAnnotations) {
                                AnnotationMirror onServiceAnnotationMirror = (AnnotationMirror) serviceDependencyAnnotation.getValue();
                                Map<? extends ExecutableElement, ? extends AnnotationValue> onServiceParms = onServiceAnnotationMirror.getElementValues();
                                String requiresServiceName = null;
                                String requiresServiceNamespace = null;
                                String requiresVersion = null;
                                String requiresInterface = null;
                                DeclaredType requiresProxy = null;

                                for (ExecutableElement onServiceParmKey : onServiceParms.keySet()) {
                                    if ("name".equals(onServiceParmKey.getSimpleName().toString())) {
                                        requiresServiceName = (String) onServiceParms.get(onServiceParmKey).getValue();
                                    } else if ("namespace".equals(onServiceParmKey.getSimpleName().toString())) {
                                        requiresServiceNamespace = (String) onServiceParms.get(onServiceParmKey).getValue();
                                    } else if ("version".equals(onServiceParmKey.getSimpleName().toString())) {
                                        requiresVersion = (String) onServiceParms.get(onServiceParmKey).getValue();
                                    } else if ("interface".equals(onServiceParmKey.getSimpleName().toString())) {
                                        requiresInterface = (String) onServiceParms.get(onServiceParmKey).getValue();
                                    } else if ("proxy".equals(onServiceParmKey.getSimpleName().toString())) {
                                        requiresProxy = (DeclaredType) onServiceParms.get(onServiceParmKey).getValue();
                                    }
                                }

                                if (requiresServiceName != null && !requiresServiceName.isEmpty() ||
                                        requiresServiceNamespace != null && !requiresServiceNamespace.isEmpty() ||
                                        requiresVersion != null && !requiresVersion.isEmpty() ||
                                        requiresInterface != null && !requiresInterface.isEmpty())
                                    throw new AdaptorException(format("*** Only @OnServce(proxy) is currently supported (%s) ***", serviceDependencyAnnotation.getValue().toString()));
                                if (Object.class.equals(requiresProxy))
                                    throw new AdaptorException(format("*** @OnServce(proxy) must be specified (%s) ***", serviceDependencyAnnotation.getValue().toString()));

                                Collection<? extends TypeMirror> proxyInterfaces = ((TypeElement) requiresProxy.asElement()).getInterfaces();
                                boolean isProxy = false;
                                for (TypeMirror proxyInterface : proxyInterfaces) {
                                    if (IMrSoaProxy.class.getCanonicalName().equals(proxyInterface.toString())) {
                                        isProxy = true;
                                        break;
                                    }
                                }
                                if (!isProxy)
                                    throw new AdaptorException(format("*** Class %s is not a MrSOA client proxy (%s) ***", requiresProxy.toString(), serviceDependencyAnnotation.getValue().toString()));

                                onStopProxyDependencies.add(requiresProxy.toString());
                            }
                        }
                    }
                }
            }
        }

        /* Scan for ExposeReturnObjectMethods method annotations */

        for (Element element : serviceClassElement.getEnclosedElements()) {
            if (ExecutableElement.class.isInstance(element)) {
                ExecutableElement method = (ExecutableElement) element;
                Collection<? extends AnnotationMirror> annotations = method.getAnnotationMirrors();
                for (AnnotationMirror annotation : annotations) {
                    if (ExposeReturnObjectMethods.class.getName().equals(annotation.getAnnotationType().toString())) {
                        if (!((ExecutableElement) method).getParameters().isEmpty())
                            throw new AdaptorException("*** Methods annotated with ExposeReturnObjectMethods must have no parameters ***");
                        Map<String, Object> excludeMethods = new HashMap<String, Object>();
                        StringBuilder serviceMethodPrefix = new StringBuilder();
                        for (ExecutableElement annotationParmKey : annotation.getElementValues().keySet()) {
                            if ("exclude".equals(annotationParmKey.getSimpleName().toString())) {
                                String excludMethodsString = (String) annotation.getElementValues().get(annotationParmKey).getValue();
                                String[] excludeMethodNames = excludMethodsString.split(",");
                                for (String excludeMethodName : excludeMethodNames) {
                                    excludeMethods.put(excludeMethodName.trim(), null);
                                }
                            } else if ("serviceMethodPrefix".equals(annotationParmKey.getSimpleName().toString())) {
                                String prefix = (String) annotation.getElementValues().get(annotationParmKey).getValue();
                                serviceMethodPrefix.append(prefix);
                            }
                        }
                        TypeMirror returnType = method.getReturnType();
                        if (!(returnType instanceof DeclaredType))
                            throw new AdaptorException("*** Methods annotated with ExposeReturnObjectMethods must return an object of a simple type ***");
                        returnType.accept(new SimpleTypeVisitor6<Void,Void>() {
                            public Void visitDeclared(DeclaredType type, Void v) {
                                Collection<? extends Element> elements = type.asElement().getEnclosedElements();
                                for (Element element2 : elements) {
                                    ExecutableElement typeMethod = (ExecutableElement) element2;
                                    if (ExecutableElement.class.isInstance(element)) {
                                        boolean typeMethodIsPublic = false;
                                        for (Modifier modifier : typeMethod.getModifiers()) {
                                            if (Modifier.PUBLIC.equals(modifier)) {
                                                typeMethodIsPublic = true;
                                                break;
                                            }
                                        }
                                        String typeMethodName = typeMethod.getSimpleName().toString();
                                        if (typeMethodIsPublic && !excludeMethods.containsKey(typeMethodName)) {
                                            Map<String, Object> serviceMethodObject = new HashMap<String, Object>();
                                            serviceMethodObjects.add(serviceMethodObject);
                                            serviceMethodObject.put("isExposedObjectMethod", true);
                                            serviceMethodObject.put("exposeObjectMethod", method.getSimpleName().toString());
                                            serviceMethodObject.put("name", typeMethodName);
                                            if (serviceMethodPrefix != null)
                                                serviceMethodObject.put("serviceMethodName", serviceMethodPrefix + typeMethodName);
                                            else
                                                serviceMethodObject.put("serviceMethodName", typeMethodName);
                                            if ("void".equals(typeMethod.getReturnType().toString()))
                                                serviceMethodObject.put("returnsVoid", true);
                                            serviceMethodObject.put("return_type", typeMethod.getReturnType().toString());
                                            serviceMethodObject.put("proper_return_type", getPrimitiveTypeObject(typeMethod.getReturnType().toString()));
                                            serviceMethodObject.put("args", getArgsList(typeMethod));
                                        }
                                    }
                                }
                                return null;
                            }
                        }, null);
                    }
                }
            }
        }

        context.put("service_methods", serviceMethodObjects);
        context.put("onstart_methods", onStartMethods);
        context.put("onstop_methods", onStopMethods);
        context.put("onstopproxy_classes", onStopProxyDependencies);
        context.put("services_fq_names", state.serviceFqNames);

        String stubClassName = "com.meinc.mrsoa.service.stubs." + targetQualifiedName + "Stub";
        state.serviceStubFqNames.add(stubClassName);
        state.generatedServiceFiles.add(stubClassName.replaceAll("\\.", fileSeparatorReplaceRegex));

        String clientProxyName = clientProxyPackage + "." + targetSimpleName + "ClientProxy";
        state.generatedClientFiles.add(clientProxyName.replaceAll("\\.", fileSeparatorReplaceRegex));
        
        String fastClientProxyName = clientProxyPackage + "." + targetSimpleName + "FastClientProxy";
        state.generatedClientFiles.add(fastClientProxyName.replaceAll("\\.", fileSeparatorReplaceRegex));

        Writer writer1 = null;
        Writer writer2 = null;
        Writer writer3 = null;
        try {
            writer1 = env.getFiler().createSourceFile(stubClassName, serviceClassElement).openWriter();
            writer2 = env.getFiler().createSourceFile(clientProxyName, serviceClassElement).openWriter();
            writer3 = env.getFiler().createSourceFile(fastClientProxyName, serviceClassElement).openWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Template template1;
        Template template2;
        try {
            template1 = Velocity.getTemplate(STUB_TEMPLATE_PATH);
            template2 = Velocity.getTemplate(CLIENTPROXY_TEMPLATE_PATH);
            template1.merge(context, writer1);
            context.put("fast_copy", false);
            template2.merge(context, writer2);
            context.put("fast_copy", true);
            context.put("target_classname", targetSimpleName+"Fast");
            template2.merge(context, writer3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // If you don't close the writer, the compiler will miss the new source file
        try {
            writer1.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            writer2.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            writer3.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void finalizeServices(MrSoaMojoSharedState state) {
        try {
            Velocity.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        VelocityContext context = new VelocityContext();

        context.put("service_stub_fq_names", state.serviceStubFqNames);

        state.generatedServiceFiles.add(OSGI_ACTIVATOR_CLASSNAME.replaceAll("\\.", fileSeparatorReplaceRegex));

        Writer writer4 = null;
        try {
            writer4 = env.getFiler().createSourceFile(OSGI_ACTIVATOR_CLASSNAME).openWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Template template3;
        try {
            template3 = Velocity.getTemplate(OSGI_ADAPTOR_TEMPLATE_PATH);
            template3.merge(context, writer4);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            writer4.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Used in Velocity templates
    public static class Arg {
        public String type;
        public String realType;
        public String name;

        public Arg(String type, String realType, String name) {
            this.type = type;
            this.realType = realType;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public String getRealType() {
            return realType;
        }

        public String getName() {
            return name;
        }
    }

    private ArrayList<Arg> getArgsList(ExecutableElement method) {
        ArrayList<Arg> argsList = new ArrayList<Arg>();
        for (VariableElement parameter : method.getParameters()) {
            parameter.asType().accept(new SimpleTypeVisitor6<Void,Void>() {
                public Void visitArray(ArrayType arg0, Void v) {
                    argsList.add(new Arg(arg0.toString(), arg0.toString(), parameter.getSimpleName().toString()));
                    return null;
                }

                public Void visitDeclared(DeclaredType arg0, Void v) {
                    argsList.add(new Arg(arg0.toString(), arg0.toString(), parameter.getSimpleName().toString()));
                    return null;
                }

                public Void visitPrimitive(PrimitiveType arg0, Void v) {
                    String typeName = arg0.toString();
                    String newTypeName = getPrimitiveTypeObject(typeName);
                    argsList.add(new Arg(newTypeName, typeName, parameter.getSimpleName().toString()));
                    return null;
                }
            }, null);
        }
        return argsList;
    }

    private String getPrimitiveTypeObject(String typeName) {
        if ("boolean".equals(typeName))
            return "Boolean";
        else if ("int".equals(typeName))
            return "Integer";
        else if ("long".equals(typeName))
            return "Long";
        else if ("short".equals(typeName))
            return "Short";
        else if ("float".equals(typeName))
            return "Float";
        else if ("double".equals(typeName))
            return "Double";
        else if ("byte".equals(typeName))
            return "Byte";
        else if ("char".equals(typeName))
            return "Character";
        else
            return typeName;
    }
}
